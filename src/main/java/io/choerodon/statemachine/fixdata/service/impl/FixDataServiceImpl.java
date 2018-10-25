package io.choerodon.statemachine.fixdata.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.domain.StateMachine;
import io.choerodon.statemachine.domain.StateMachineNodeDraft;
import io.choerodon.statemachine.domain.StateMachineTransformDraft;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.fixdata.dto.FixNode;
import io.choerodon.statemachine.fixdata.dto.FixTransform;
import io.choerodon.statemachine.fixdata.dto.StatusForMoveDataDO;
import io.choerodon.statemachine.fixdata.service.FixDataService;
import io.choerodon.statemachine.infra.enums.*;
import io.choerodon.statemachine.infra.mapper.StateMachineMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineTransformDraftMapper;
import io.choerodon.statemachine.infra.mapper.StatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @date 2018/10/25
 */
@Component
public class FixDataServiceImpl implements FixDataService {
    @Autowired
    private StatusMapper statusMapper;
    @Autowired
    private StateMachineMapper stateMachineMapper;
    @Autowired
    private StateMachineNodeDraftMapper nodeDraftMapper;
    @Autowired
    private StateMachineTransformDraftMapper transformDraftMapper;
    @Autowired
    private StateMachineService stateMachineService;

    @Override
    public Long createStateMachine(Long organizationId, String projectCode, List<String> statuses) {
        Status select = new Status();
        select.setOrganizationId(organizationId);
        List<Status> initStatuses = statusMapper.select(select);

        //初始化状态机
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName(projectCode + "默认状态机");
        stateMachine.setDescription(projectCode + "默认状态机");
        stateMachine.setStatus(StateMachineStatus.CREATE);
        if (stateMachineMapper.insert(stateMachine) != 1) {
            throw new CommonException("error.stateMachine.create");
        }

        //初始化节点
        Map<String, StateMachineNodeDraft> nodeMap = new HashMap<>();
        Map<String, Status> statusMap = initStatuses.stream().collect(Collectors.toMap(Status::getName, x -> x));
        List<FixNode> fixNodes = new ArrayList<>();
        //创建start节点
        FixNode start = new FixNode("开始圆点", InitNode.START.getPositionX(), InitNode.START.getPositionY(), InitNode.START.getWidth(), InitNode.START.getHeight(), NodeType.START);
        //创建init节点
        String initStatus;
        if (statuses.contains("待处理")) {
            initStatus = "待处理";
        } else {
            initStatus = statuses.get(0);
        }
        statuses.remove(initStatus);
        FixNode init = new FixNode(initStatus, 0L, 120L, 100L, 50L, NodeType.INIT);

        fixNodes.add(start);
        fixNodes.add(init);
        Long positionY = 120L;
        for (String statusName : statuses) {
            positionY += 100L;
            FixNode custom = new FixNode(statusName, 0L, positionY, 100L, 50L, NodeType.CUSTOM);
            fixNodes.add(custom);
        }
        for (FixNode fixNode : fixNodes) {
            StateMachineNodeDraft node = new StateMachineNodeDraft();
            node.setStateMachineId(stateMachine.getId());
            if (fixNode.getType().equals(NodeType.START)) {
                node.setStatusId(0L);
            } else {
                node.setStatusId(statusMap.get(fixNode.getName()).getId());
            }
            node.setPositionX(fixNode.getPositionX());
            node.setPositionY(fixNode.getPositionY());
            node.setWidth(fixNode.getWidth());
            node.setHeight(fixNode.getHeight());
            node.setType(fixNode.getType());
            node.setOrganizationId(organizationId);
            int isNodeInsert = nodeDraftMapper.insert(node);
            if (isNodeInsert != 1) {
                throw new CommonException("error.stateMachineNode.create");
            }
            nodeMap.put(fixNode.getName(), node);
        }
        //创建初始转换
        List<FixTransform> fixTransforms = new ArrayList<>();
        FixTransform initTransform = new FixTransform("初始化", "开始圆点", initStatus, TransformType.INIT);
        fixTransforms.add(initTransform);
        for (String statusName : statuses) {
            FixTransform custom = new FixTransform("全部转换到" + statusName, null, statusName, TransformType.ALL);
            fixTransforms.add(custom);
        }

        //初始化转换
        for (FixTransform fixTransform : fixTransforms) {
            StateMachineTransformDraft transform = new StateMachineTransformDraft();
            transform.setStateMachineId(stateMachine.getId());
            transform.setName(fixTransform.getName());
            transform.setDescription("'全部'转换");
            if (fixTransform.getType().equals(TransformType.ALL)) {
                transform.setStartNodeId(0L);
            } else {
                transform.setStartNodeId(nodeMap.get(fixTransform.getStartNodeName()).getId());
            }
            transform.setEndNodeId(nodeMap.get(fixTransform.getEndNodeName()).getId());
            transform.setType(fixTransform.getType());
            transform.setConditionStrategy(TransformConditionStrategy.ALL);
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
        //发布状态机
        stateMachineService.deploy(organizationId, stateMachine.getId());

        return stateMachine.getId();
    }

    @Override
    public Map<Long, List<Status>> createStatus(List<StatusForMoveDataDO> statusForMoveDataDOList) {
        Map<Long, List<Status>> result = new HashMap<>();
        for (StatusForMoveDataDO statusForMoveDataDO : statusForMoveDataDOList) {
            Status status = new Status();
            status.setOrganizationId(statusForMoveDataDO.getOrganizationId());
            status.setName(statusForMoveDataDO.getName());
            List<Status> temp = statusMapper.select(status);
            if (temp == null || temp.isEmpty()) {
                status.setDescription(statusForMoveDataDO.getName());
                status.setType(statusForMoveDataDO.getCategoryCode());
                if (statusMapper.insert(status) != 1) {
                    throw new CommonException("error.status.insert");
                }
                if (result.get(status.getOrganizationId()) == null) {
                    List<Status> statusList = new ArrayList<>();
                    statusList.add(status);
                    result.put(status.getOrganizationId(), statusList);
                } else {
                    List<Status> statusList = result.get(status.getOrganizationId());
                    statusList.add(status);
                    result.put(status.getOrganizationId(), statusList);
                }
            }
        }
        return result;
    }
}
