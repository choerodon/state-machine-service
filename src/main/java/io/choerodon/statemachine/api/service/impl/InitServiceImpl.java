package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.service.InitService;
import io.choerodon.statemachine.domain.StateMachine;
import io.choerodon.statemachine.domain.StateMachineNodeDraft;
import io.choerodon.statemachine.domain.StateMachineTransformDraft;
import io.choerodon.statemachine.domain.Status;
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
    private StateMachineMapper stateMachineMapper;
    @Autowired
    private StateMachineTransformDraftMapper transformDraftMapper;

    @Override
    public List<Status> initStatus(Long organizationId) {
        List<Status> initStatuses = new ArrayList<>();
        for (InitStatus initStatus : InitStatus.values()) {
            Status status = new Status();
            status.setOrganizationId(organizationId);
            status.setName(initStatus.getName());
            status.setDescription(initStatus.getName());
            status.setType(initStatus.getType());
            if (statusMapper.insert(status) != 1) {
                throw new CommonException("error.initStatus.create");
            }
            initStatuses.add(status);
        }
        return initStatuses;
    }

    @Override
    public void initAGStateMachine(Long organizationId, List<Status> initStatuses) {

        //初始化状态机
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName("默认状态机");
        stateMachine.setDescription("默认状态机");
        stateMachine.setStatus(StateMachineStatus.CREATE);
        if (stateMachineMapper.insert(stateMachine) != 1) {
            throw new CommonException("error.stateMachine.create");
        }

        //初始化节点
        Map<String, StateMachineNodeDraft> nodeMap = new HashMap<>();
        Map<String, Status> statusMap = initStatuses.stream().collect(Collectors.toMap(Status::getName, x -> x));
        for (InitNode initNode : InitNode.values()) {
            StateMachineNodeDraft node = new StateMachineNodeDraft();
            node.setStateMachineId(stateMachine.getId());
            if (initNode.getType().equals(NodeType.START)) {
                node.setStatusId(0L);
            } else {
                node.setStatusId(statusMap.get(initNode.getStatusName()).getId());
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
            nodeMap.put(initNode.getStatusName(), node);
        }
        //初始化转换
        for (InitTransform initTransform : InitTransform.values()) {
            StateMachineTransformDraft transform = new StateMachineTransformDraft();
            transform.setStateMachineId(stateMachine.getId());
            transform.setName(initTransform.getName());
            transform.setDescription("'全部'转换");
            if (initTransform.getType().equals(TransformType.ALL)) {
                transform.setStartNodeId(0L);
            } else {
                transform.setStartNodeId(nodeMap.get(initTransform.getStartNodeName()).getId());
            }
            transform.setEndNodeId(nodeMap.get(initTransform.getEndNodeName()).getId());
            transform.setType(initTransform.getType());
            transform.setConditionStrategy(initTransform.getConditionStrategy());
            transform.setOrganizationId(organizationId);
            int isTransformInsert = transformDraftMapper.insert(transform);
            if (isTransformInsert != 1) {
                throw new CommonException("error.stateMachineTransform.create");
            }
            //如果是ALL类型的转换，要更新节点的allStatusTransformId
            if (initTransform.getType().equals(TransformType.ALL)) {
                StateMachineNodeDraft nodeDraft = nodeMap.get(initTransform.getEndNodeName());
                int update = nodeDraftMapper.updateAllStatusTransformId(organizationId, nodeDraft.getId(), transform.getId());
                if (update != 1) {
                    throw new CommonException("error.stateMachineNode.allStatusTransformId.update");
                }
            }
        }
    }
}
