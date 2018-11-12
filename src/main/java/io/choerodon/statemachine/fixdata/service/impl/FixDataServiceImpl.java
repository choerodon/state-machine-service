package io.choerodon.statemachine.fixdata.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.service.InitService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @date 2018/10/25
 */
@Component
public class FixDataServiceImpl implements FixDataService {

    private static final Logger logger = LoggerFactory.getLogger(FixDataServiceImpl.class);
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
    @Autowired
    private InitService initService;

    @Override
    public Map<String, Long> createAGStateMachineAndTEStateMachine(Long organizationId, String projectCode, List<String> statuses) {

        Map<String, Long> stateMachineIdMap = new HashMap<>(2);

        Status select = new Status();
        select.setOrganizationId(organizationId);
        List<Status> initStatuses = statusMapper.select(select);

        Map<String, Status> statusMap = initStatuses.stream().collect(Collectors.toMap(Status::getName, x -> x));

        //准备修复节点和转换的数据
        List<FixNode> fixNodes = new ArrayList<>();
        List<FixTransform> fixTransforms = new ArrayList<>();
        prepareFixNodeAndTransform(fixNodes, fixTransforms, statuses);
        //创建敏捷状态机
        stateMachineIdMap.put(SchemeApplyType.AGILE, createStateMachine(organizationId, projectCode + "默认状态机【敏捷】", fixNodes, fixTransforms, statusMap));
        //创建测试状态机
        stateMachineIdMap.put(SchemeApplyType.TEST, createStateMachine(organizationId, projectCode + "默认状态机【测试】", fixNodes, fixTransforms, statusMap));
        logger.info("成功修复组织{}，项目code:{}, 敏捷状态机id:{},测试状态机id:{}", organizationId, projectCode, stateMachineIdMap.get(SchemeApplyType.AGILE), stateMachineIdMap.get(SchemeApplyType.TEST));
        return stateMachineIdMap;
    }

    /**
     * 准备修复节点和转换的数据
     *
     * @param fixNodes
     * @param fixTransforms
     * @param statuses
     */
    private void prepareFixNodeAndTransform(List<FixNode> fixNodes, List<FixTransform> fixTransforms, List<String> statuses) {
        //创建start节点
        FixNode start = new FixNode("开始圆点", InitNode.START.getPositionX(), InitNode.START.getPositionY(), InitNode.START.getWidth(), InitNode.START.getHeight(), NodeType.START);
        //创建init节点
        String initStatus;
        if (statuses.contains("待处理")) {
            initStatus = "待处理";
        } else {
            initStatus = statuses.get(0);
        }
        FixNode init = new FixNode(initStatus, 0L, 120L, 100L, 50L, NodeType.INIT);

        fixNodes.add(start);
        fixNodes.add(init);
        Long positionY = 120L;
        for (String statusName : statuses) {
            //跳过init节点
            if (initStatus.equals(statusName)) {
                continue;
            }
            positionY += 100L;
            FixNode custom = new FixNode(statusName, 0L, positionY, 100L, 50L, NodeType.CUSTOM);
            fixNodes.add(custom);
        }

        //创建初始转换
        FixTransform initTransform = new FixTransform("初始化", "开始圆点", "初始化", initStatus, TransformType.INIT);
        fixTransforms.add(initTransform);
        for (String statusName : statuses) {
            FixTransform custom = new FixTransform("全部转换到" + statusName, null, "【全部】转换", statusName, TransformType.ALL);
            fixTransforms.add(custom);
        }
    }

    /**
     * 创建状态机
     *
     * @param organizationId
     * @param name
     * @param fixNodes
     * @param fixTransforms
     * @param statusMap
     * @return
     */
    private Long createStateMachine(Long organizationId, String name, List<FixNode> fixNodes, List<FixTransform> fixTransforms, Map<String, Status> statusMap) {
        //初始化状态机
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName(name);
        stateMachine.setDescription(name);
        stateMachine.setStatus(StateMachineStatus.CREATE);
        stateMachine.setDefault(false);
        if (stateMachineMapper.insert(stateMachine) != 1) {
            throw new CommonException("error.stateMachine.create");
        }
        Long stateMachineId = stateMachine.getId();

        //创建状态机节点和状态机转换
        createStateMachineDetail(organizationId, stateMachineId, fixNodes, fixTransforms, statusMap);

        //异步发布状态机
        Observable.just(stateMachineId)
                .map(t -> {
                    stateMachineService.deploy(organizationId, stateMachine.getId(), false);
                    return t;
                })
                .retryWhen(x -> x.zipWith(Observable.range(1, 10),
                        (t, retryCount) -> {
                            if (retryCount >= 10) {
                                logger.warn("error.stateMachine.deploy.error,stateMachineId:{}", stateMachineId);
                            }
                            return retryCount;
                        }).flatMap(y -> Observable.timer(2, TimeUnit.SECONDS)))
                .subscribeOn(Schedulers.io())
                .subscribe((Long id) -> {
                });

        return stateMachineId;
    }

    /**
     * 创建状态机节点和状态机转换
     *
     * @param organizationId
     * @param stateMachineId
     * @param fixNodes
     * @param fixTransforms
     * @param statusMap
     */
    private void createStateMachineDetail(Long organizationId, Long stateMachineId, List<FixNode> fixNodes, List<FixTransform> fixTransforms, Map<String, Status> statusMap) {
        Map<String, StateMachineNodeDraft> nodeMap = new HashMap<>(fixNodes.size());
        //开始创建节点
        for (FixNode fixNode : fixNodes) {
            StateMachineNodeDraft node = new StateMachineNodeDraft();
            node.setStateMachineId(stateMachineId);
            if (fixNode.getType().equals(NodeType.START)) {
                node.setStatusId(0L);
            } else {
                Status status = statusMap.get(fixNode.getName());
                if (status != null) {
                    node.setStatusId(status.getId());
                } else {
                    throw new CommonException("error.status.name.notFound");
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
            transform.setStateMachineId(stateMachineId);
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
        //补充，每个项目都要有初始的三个状态
        Map<Long, List<Status>> orgMap = statusList.stream().collect(Collectors.groupingBy(Status::getOrganizationId));
        for(Map.Entry<Long, List<Status>> listEntry:orgMap.entrySet()){
            Long organizationId = listEntry.getKey();
            List<Status> statuses = listEntry.getValue();
            List<String> names = statuses.stream().map(Status::getName).collect(Collectors.toList());
            for(InitStatus initStatus:InitStatus.values()){
                if(!names.contains(initStatus.getName())){
                    String name = initStatus.getName();
                    Status status = new Status();
                    status.setOrganizationId(organizationId);
                    status.setName(name);
                    status.setDescription(name);
                    status.setCode(initStatus.getCode());
                    status.setType(initStatus.getType());
                    statusList.add(status);
                }
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
