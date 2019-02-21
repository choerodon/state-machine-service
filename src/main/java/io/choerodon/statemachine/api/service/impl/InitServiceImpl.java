package io.choerodon.statemachine.api.service.impl;

import com.alibaba.fastjson.JSON;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.service.InitService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.domain.StateMachine;
import io.choerodon.statemachine.domain.StateMachineNodeDraft;
import io.choerodon.statemachine.domain.StateMachineTransformDraft;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.event.ProjectCreateAgilePayload;
import io.choerodon.statemachine.domain.event.ProjectEvent;
import io.choerodon.statemachine.domain.event.StatusPayload;
import io.choerodon.statemachine.infra.enums.*;
import io.choerodon.statemachine.infra.mapper.StateMachineMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineTransformDraftMapper;
import io.choerodon.statemachine.infra.mapper.StatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @date 2018/10/15
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class InitServiceImpl implements InitService {

    @Autowired
    private StatusMapper statusMapper;
    @Autowired
    private StateMachineNodeDraftMapper nodeDraftMapper;
    @Autowired
    private StateMachineService stateMachineService;
    @Autowired
    private StateMachineMapper stateMachineMapper;
    @Autowired
    private StateMachineTransformDraftMapper transformDraftMapper;
    @Autowired
    private SagaClient sagaClient;

    private final static String ERROR_STATEMACHINE_CREATE = "error.stateMachine.create";

    @Override
    public synchronized List<Status> initStatus(Long organizationId) {
        List<Status> initStatuses = new ArrayList<>();
        for (InitStatus initStatus : InitStatus.values()) {
            Status status = new Status();
            status.setOrganizationId(organizationId);
            status.setCode(initStatus.getCode());
            List<Status> statuses = statusMapper.select(status);
            if (statuses.isEmpty()) {
                status.setName(initStatus.getName());
                status.setDescription(initStatus.getName());
                status.setType(initStatus.getType());
                if (statusMapper.insert(status) != 1) {
                    throw new CommonException("error.initStatus.create");
                }
                initStatuses.add(status);
            } else {
                initStatuses.add(statuses.get(0));
            }
        }
        return initStatuses;
    }

    @Override
    public Long initDefaultStateMachine(Long organizationId) {
        //初始化默认状态机
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName("默认状态机");
        stateMachine.setDescription("默认状态机");
        stateMachine.setStatus(StateMachineStatus.CREATE);
        stateMachine.setDefault(true);
        List<StateMachine> selects = stateMachineMapper.select(stateMachine);
        Long stateMachineId;
        if (selects.isEmpty()) {
            if (stateMachineMapper.insert(stateMachine) != 1) {
                throw new CommonException(ERROR_STATEMACHINE_CREATE);
            }
            //创建状态机节点和转换
            createStateMachineDetail(organizationId, stateMachine.getId());
            stateMachineId = stateMachine.getId();
        } else {
            stateMachineId = selects.get(0).getId();
        }
        return stateMachineId;
    }

    @Override
    public Long initAGStateMachine(Long organizationId, ProjectEvent projectEvent) {
        String projectCode = projectEvent.getProjectCode();
        //初始化状态机
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName(projectCode + "默认状态机【敏捷】");
        stateMachine.setDescription(projectCode + "默认状态机【敏捷】");
        stateMachine.setStatus(StateMachineStatus.CREATE);
        stateMachine.setDefault(false);
        if (stateMachineMapper.insert(stateMachine) != 1) {
            throw new CommonException(ERROR_STATEMACHINE_CREATE);
        }
        //创建状态机节点和转换
        createStateMachineDetail(organizationId, stateMachine.getId());
        //发布状态机
        Long stateMachineId = stateMachine.getId();
        stateMachineService.deploy(organizationId, stateMachineId, false);
        sendSagaToAgile(projectEvent, stateMachineId);
        return stateMachineId;
    }

    @Override
    public Long initTEStateMachine(Long organizationId, ProjectEvent projectEvent) {
        String projectCode = projectEvent.getProjectCode();
        //初始化状态机
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName(projectCode + "默认状态机【测试】");
        stateMachine.setDescription(projectCode + "默认状态机【测试】");
        stateMachine.setStatus(StateMachineStatus.CREATE);
        stateMachine.setDefault(false);
        if (stateMachineMapper.insert(stateMachine) != 1) {
            throw new CommonException(ERROR_STATEMACHINE_CREATE);
        }
        //创建状态机节点和转换
        createStateMachineDetail(organizationId, stateMachine.getId());
        //发布状态机
        Long stateMachineId = stateMachine.getId();
        stateMachineService.deploy(organizationId, stateMachineId, false);
        return stateMachineId;
    }

    /**
     * 创建状态机节点和转换
     *
     * @param organizationId
     * @param stateMachineId
     */
    @Override
    public void createStateMachineDetail(Long organizationId, Long stateMachineId) {
        Status select = new Status();
        select.setOrganizationId(organizationId);
        List<Status> initStatuses = statusMapper.select(select);
        //老的组织没有相关数据要重新创建
        initStatuses = initOrganization(organizationId, initStatuses);
        //初始化节点
        Map<String, StateMachineNodeDraft> nodeMap = new HashMap<>();
        Map<String, Status> statusMap = initStatuses.stream().filter(x -> x.getCode() != null).collect(Collectors.toMap(Status::getCode, x -> x));
        for (InitNode initNode : InitNode.values()) {
            StateMachineNodeDraft node = new StateMachineNodeDraft();
            node.setStateMachineId(stateMachineId);
            if (initNode.getType().equals(NodeType.START)) {
                node.setStatusId(0L);
            } else {
                node.setStatusId(statusMap.get(initNode.getCode()).getId());
            }
            node.setPositionX(initNode.getPositionX());
            node.setPositionY(initNode.getPositionY());
            node.setWidth(initNode.getWidth());
            node.setHeight(initNode.getHeight());
            node.setType(initNode.getType());
            node.setOrganizationId(organizationId);
            int isNodeInsert = nodeDraftMapper.insert(node);
            if (isNodeInsert != 1) {
                throw new CommonException("error.stateMachineNode.create");
            }
            nodeMap.put(initNode.getCode(), node);
        }
        //初始化转换
        for (InitTransform initTransform : InitTransform.values()) {
            StateMachineTransformDraft transform = new StateMachineTransformDraft();
            transform.setStateMachineId(stateMachineId);
            transform.setName(initTransform.getName());
            if (initTransform.getType().equals(TransformType.ALL)) {
                transform.setStartNodeId(0L);
                transform.setDescription("【全部】转换");
            } else {
                transform.setStartNodeId(nodeMap.get(initTransform.getStartNodeCode()).getId());
                transform.setDescription("初始化");
            }
            transform.setEndNodeId(nodeMap.get(initTransform.getEndNodeCode()).getId());
            transform.setType(initTransform.getType());
            transform.setConditionStrategy(initTransform.getConditionStrategy());
            transform.setOrganizationId(organizationId);
            int isTransformInsert = transformDraftMapper.insert(transform);
            if (isTransformInsert != 1) {
                throw new CommonException("error.stateMachineTransform.create");
            }
            //如果是ALL类型的转换，要更新节点的allStatusTransformId
            if (initTransform.getType().equals(TransformType.ALL)) {
                StateMachineNodeDraft nodeDraft = nodeMap.get(initTransform.getEndNodeCode());
                int update = nodeDraftMapper.updateAllStatusTransformId(organizationId, nodeDraft.getId(), transform.getId());
                if (update != 1) {
                    throw new CommonException("error.stateMachineNode.allStatusTransformId.update");
                }
            }
        }
    }

    private List<Status> initOrganization(Long organizationId, List<Status> initStatuses) {
        if (initStatuses == null || initStatuses.isEmpty()) {
            //初始化状态
            initStatus(organizationId);
            //初始化默认状态机
            initDefaultStateMachine(organizationId);
            Status select = new Status();
            select.setOrganizationId(organizationId);
            return statusMapper.select(select);
        } else {
            return initStatuses;
        }
    }

    @Override
    @Saga(code = "project-create-state-machine", description = "创建项目发送消息至agile", inputSchemaClass = ProjectCreateAgilePayload.class)
    public void sendSagaToAgile(ProjectEvent projectEvent, Long stateMachineId) {
        List<StatusPayload> statusPayloads = stateMachineMapper.getStatusBySmId(projectEvent.getProjectId(), stateMachineId);
        Long projectId = projectEvent.getProjectId();
        ProjectCreateAgilePayload projectCreateAgilePayload = new ProjectCreateAgilePayload();
        projectCreateAgilePayload.setProjectEvent(projectEvent);
        projectCreateAgilePayload.setStatusPayloads(statusPayloads);
        sagaClient.startSaga("project-create-state-machine", new StartInstanceDTO(JSON.toJSONString(projectCreateAgilePayload), "", "", ResourceLevel.PROJECT.value(), projectId));
    }


}
