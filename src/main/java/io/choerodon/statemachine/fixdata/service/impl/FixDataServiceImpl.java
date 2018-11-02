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
        List<String> statusStrs = new ArrayList<>(statuses);

        //初始化状态机
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName(projectCode + "默认状态机");
        stateMachine.setDescription(projectCode + "默认状态机");
        //保证幂等性
        List<StateMachine> stateMachines = stateMachineMapper.select(stateMachine);
        if (!stateMachines.isEmpty()) {
            return stateMachines.get(0).getId();
        }
        stateMachine.setStatus(StateMachineStatus.CREATE);
        if (stateMachineMapper.insert(stateMachine) != 1) {
            throw new CommonException("error.stateMachine.create");
        }

        //初始化节点
        Map<String, StateMachineNodeDraft> nodeMap = new HashMap<>();
        System.out.println(organizationId + ":" + projectCode);
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
        //创建初始转换
        List<FixTransform> fixTransforms = new ArrayList<>();
        FixTransform initTransform = new FixTransform("初始化", "开始圆点", "初始化", initStatus, TransformType.INIT);
        fixTransforms.add(initTransform);
        for (String statusName : statusStrs) {
            FixTransform custom = new FixTransform("全部转换到" + statusName, null, "【全部】转换", statusName, TransformType.ALL);
            fixTransforms.add(custom);
        }

        //开始创建
        for (FixNode fixNode : fixNodes) {
            StateMachineNodeDraft node = new StateMachineNodeDraft();
            node.setStateMachineId(stateMachine.getId());
            if (fixNode.getType().equals(NodeType.START)) {
                node.setStatusId(0L);
            } else {
                System.out.println(organizationId + ":" + fixNode.getName());
                Status status = statusMap.get(fixNode.getName());
                if (status != null) {
                    node.setStatusId(status.getId());
                } else {
                    Status status1 = new Status();
                    status1.setOrganizationId(organizationId);
                    status1.setName(fixNode.getName());
                    List<Status> statusList = statusMapper.select(status1);
                    if (!statusList.isEmpty()) {
                        node.setStatusId(statusList.get(0).getId());
                    } else {
                        throw new CommonException("error.status.name.notFound");
                    }
                }
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

        //初始化转换
        for (FixTransform fixTransform : fixTransforms) {
            StateMachineTransformDraft transform = new StateMachineTransformDraft();
            transform.setStateMachineId(stateMachine.getId());
            transform.setName(fixTransform.getName());
            transform.setDescription(fixTransform.getDescription());
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
            if (fixTransform.getType().equals(TransformType.ALL)) {
                StateMachineNodeDraft nodeDraft = nodeMap.get(fixTransform.getEndNodeName());
                int update = nodeDraftMapper.updateAllStatusTransformId(organizationId, nodeDraft.getId(), transform.getId());
                if (update != 1) {
                    throw new CommonException("error.stateMachineNode.allStatusTransformId.update");
                }
            }
        }
        //发布状态机
        stateMachineService.deploy(organizationId, stateMachine.getId(), false);

        return stateMachine.getId();
    }

    @Override
    public Boolean createStatus(List<StatusForMoveDataDO> statusForMoveDataDOList) {
        Map<String, String> codeMap = new HashMap<>(3);
        codeMap.put(InitStatus.STATUS1.getName(), InitStatus.STATUS1.getCode());
        codeMap.put(InitStatus.STATUS2.getName(), InitStatus.STATUS2.getCode());
        codeMap.put(InitStatus.STATUS3.getName(), InitStatus.STATUS3.getCode());
        Map<Long, List<String>> statusCheckMap = new HashMap<>();
        List<Status> statusList = new ArrayList<>();
        for (StatusForMoveDataDO statusForMoveDataDO : statusForMoveDataDOList) {
            if (statusCheckMap.get(statusForMoveDataDO.getOrganizationId()) != null) {
                List<String> strings = statusCheckMap.get(statusForMoveDataDO.getOrganizationId());
                if (strings.contains(statusForMoveDataDO.getName())) {
                    continue;
                }
                Status status = new Status();
                status.setOrganizationId(statusForMoveDataDO.getOrganizationId());
                status.setName(statusForMoveDataDO.getName());
                status.setDescription(statusForMoveDataDO.getName());
                status.setCode(codeMap.get(status.getName()));
                status.setType(statusForMoveDataDO.getCategoryCode());
                statusList.add(status);
                strings.add(statusForMoveDataDO.getName());
                statusCheckMap.put(statusForMoveDataDO.getOrganizationId(), strings);
            } else {
                Status status = new Status();
                status.setOrganizationId(statusForMoveDataDO.getOrganizationId());
                status.setName(statusForMoveDataDO.getName());
                status.setDescription(statusForMoveDataDO.getName());
                status.setCode(codeMap.get(status.getName()));
                status.setType(statusForMoveDataDO.getCategoryCode());
                statusList.add(status);
                List<String> strings = new ArrayList<>();
                strings.add(statusForMoveDataDO.getName());
                statusCheckMap.put(statusForMoveDataDO.getOrganizationId(), strings);
            }
        }
        statusMapper.batchInsert(statusList);
        return true;
    }

    @Override
    public Map<Long, List<Status>> queryAllStatus() {
        List<Status> issueTypes = statusMapper.selectAll();
        Map<Long, List<Status>> orgMaps = issueTypes.stream().collect(Collectors.groupingBy(Status::getOrganizationId));
        return orgMaps;
    }
}
