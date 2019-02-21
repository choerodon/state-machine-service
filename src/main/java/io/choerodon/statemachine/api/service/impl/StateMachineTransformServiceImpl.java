package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.annotation.ChangeStateMachineStatus;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.enums.TransformConditionStrategy;
import io.choerodon.statemachine.infra.enums.TransformType;
import io.choerodon.statemachine.infra.mapper.*;
import io.choerodon.statemachine.infra.utils.EnumUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@Component
public class StateMachineTransformServiceImpl extends BaseServiceImpl<StateMachineTransformDraft> implements StateMachineTransformService {
    @Autowired
    private StateMachineTransformDraftMapper transformDraftMapper;
    @Autowired
    private StateMachineTransformMapper transformDeployMapper;
    @Autowired
    private StateMachineNodeService nodeService;
    @Autowired
    private StateMachineNodeDraftMapper nodeDraftMapper;
    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;
    @Autowired
    private StateMachineConfigDraftMapper configMapper;
    @Autowired
    private StateMachineConfigService configService;
    @Autowired
    private StatusMapper stateMapper;
    @Autowired
    private StateMachineTransformAssembler stateMachineTransformAssembler;
    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;

    @Override
    @ChangeStateMachineStatus
    @Transactional(rollbackFor = Exception.class)
    public StateMachineTransformDTO create(Long organizationId, Long stateMachineId, StateMachineTransformDTO transformDTO) {
        transformDTO.setStateMachineId(stateMachineId);
        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setStartNodeId(transformDTO.getStartNodeId());
        transform.setEndNodeId(transformDTO.getEndNodeId());
        transform.setName(transformDTO.getName());
        if (!transformDraftMapper.select(transform).isEmpty()) {
            throw new CommonException("error.stateMachineTransform.exist");
        }
        transform = stateMachineTransformAssembler.toTarget(transformDTO, StateMachineTransformDraft.class);
        transform.setType(TransformType.CUSTOM);
        transform.setOrganizationId(organizationId);
        transform.setConditionStrategy(TransformConditionStrategy.ALL);

        int isInsert = transformDraftMapper.insert(transform);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }
        return queryById(organizationId, transform.getId());
    }

    @Override
    @ChangeStateMachineStatus
    @Transactional(rollbackFor = Exception.class)
    public StateMachineTransformDTO update(Long organizationId, Long stateMachineId, Long transformId, StateMachineTransformDTO transformDTO) {
        transformDTO.setStateMachineId(stateMachineId);
        StateMachineTransformDraft origin = transformDraftMapper.queryById(organizationId, transformId);
        if (origin == null) {
            throw new CommonException("error.stateMachineTransform.queryById.notFound");
        }
        StateMachineTransformDraft transform = stateMachineTransformAssembler.toTarget(transformDTO, StateMachineTransformDraft.class);
        transform.setId(transformId);
        transform.setOrganizationId(organizationId);
        transform.setType(origin.getType());
        int isUpdate = transformDraftMapper.updateByPrimaryKeySelective(transform);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineTransform.update");
        }
        return queryById(organizationId, transformId);

    }

    @Override
    @ChangeStateMachineStatus
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long organizationId, Long stateMachineId, Long transformId) {
        StateMachineTransformDraft transform = transformDraftMapper.queryById(organizationId, transformId);
        if (!stateMachineId.equals(transform.getStateMachineId())) {
            throw new CommonException("error.stateMachineTransform.deleteIllegal");
        }
        int isDelete = transformDraftMapper.deleteByPrimaryKey(transformId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachineTransform.delete");
        }
        return true;
    }

    @Override
    public StateMachineTransformDTO queryById(Long organizationId, Long transformId) {
        StateMachineTransformDraft transform = transformDraftMapper.queryById(organizationId, transformId);
        if (transform == null) {
            throw new CommonException("error.stateMachineTransform.queryById.notFound");
        }
        StateMachineTransformDTO dto = stateMachineTransformAssembler.toTarget(transform, StateMachineTransformDTO.class);
        dto.setConditions(configService.queryByTransformId(organizationId, transformId, ConfigType.CONDITION, true));
        dto.setValidators(configService.queryByTransformId(organizationId, transformId, ConfigType.VALIDATOR, true));
        dto.setTriggers(configService.queryByTransformId(organizationId, transformId, ConfigType.TRIGGER, true));
        dto.setPostpositions(configService.queryByTransformId(organizationId, transformId, ConfigType.ACTION, true));
        //获取开始节点，若为初始转换，则没有开始节点
        if (TransformType.CUSTOM.equals(transform.getType())) {
            StateMachineNodeDraft startNode = nodeDraftMapper.getNodeById(dto.getStartNodeId());
            dto.setStartNodeDTO(stateMachineNodeAssembler.draftToNodeDTO(startNode));
        }
        //获取结束节点
        StateMachineNodeDraft endNode = nodeDraftMapper.getNodeById(dto.getEndNodeId());
        dto.setEndNodeDTO(stateMachineNodeAssembler.draftToNodeDTO(endNode));
        return dto;
    }

    @Override
    public Long getInitTransform(Long organizationId, Long stateMachineId) {
        StateMachineTransform transform = new StateMachineTransform();
        transform.setStateMachineId(stateMachineId);
        transform.setOrganizationId(organizationId);
        transform.setType(TransformType.INIT);
        List<StateMachineTransform> transforms = transformDeployMapper.select(transform);
        if (transforms.isEmpty()) {
            throw new CommonException("error.initTransform.null");
        }
        return transforms.get(0).getId();
    }

    @Override
    public List<StateMachineTransform> queryListByStatusIdByDeploy(Long organizationId, Long stateMachineId, Long statusId) {
        StateMachineNode startNode = nodeDeployMapper.getNodeDeployByStatusId(stateMachineId, statusId);
        if (startNode == null) {
            throw new CommonException("error.statusId.notFound");
        }
        return transformDeployMapper.queryByStartNodeIdOrType(organizationId, stateMachineId, startNode.getId(), TransformType.ALL);
    }

    @Override
    @ChangeStateMachineStatus
    @Transactional(rollbackFor = Exception.class)
    public StateMachineTransformDTO createAllStatusTransform(Long organizationId, Long stateMachineId, Long endNodeId) {
        if (endNodeId == null) {
            throw new CommonException("error.endNodeId.null");
        }
        StateMachineNodeDraft stateMachineNodeDraft = nodeDraftMapper.getNodeById(endNodeId);
        if (stateMachineNodeDraft == null) {
            throw new CommonException("error.stateMachineNode.null");
        }
        //创建【全部转换到当前】的transform
        Status state = stateMapper.queryById(organizationId, stateMachineNodeDraft.getStatusId());
        if (state == null) {
            throw new CommonException("error.createAllStatusTransform.state.null");
        }
        //判断当前节点是否已存在【全部】的转换id
        StateMachineTransformDraft select = new StateMachineTransformDraft();
        select.setStateMachineId(stateMachineId);
        select.setEndNodeId(endNodeId);
        select.setType(TransformType.ALL);
        if (!transformDraftMapper.select(select).isEmpty()) {
            throw new CommonException("error.stateMachineTransform.exist");
        }

        //创建
        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setStateMachineId(stateMachineId);
        transform.setName(state.getName());
        transform.setDescription("全部转换");
        transform.setStartNodeId(0L);
        transform.setEndNodeId(endNodeId);
        transform.setOrganizationId(organizationId);
        transform.setType(TransformType.ALL);
        transform.setConditionStrategy(TransformConditionStrategy.ALL);
        int isInsert = transformDraftMapper.insert(transform);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }
        //更新node的【全部转换到当前】转换id
        int update = nodeDraftMapper.updateAllStatusTransformId(organizationId, endNodeId, transform.getId());
        if (update != 1) {
            throw new CommonException("error.createAllStatusTransform.updateAllStatusTransformId");
        }
        transform = transformDraftMapper.queryById(organizationId, transform.getId());
        return stateMachineTransformAssembler.toTarget(transform, StateMachineTransformDTO.class);
    }

    public StateMachineTransformServiceImpl() {
        super();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAllStatusTransform(Long organizationId, Long transformId) {
        StateMachineTransformDraft transformDraft = transformDraftMapper.queryById(organizationId, transformId);
        if (transformDraft == null) {
            throw new CommonException("error.stateMachineTransform.null");
        }
        if (!TransformType.ALL.equals(transformDraft.getType())) {
            throw new CommonException("error.stateMachineTransform.type.illegal");
        }
        //目标节点
        StateMachineNodeDraft node = nodeDraftMapper.getNodeById(transformDraft.getEndNodeId());
        if (node == null) {
            throw new CommonException("error.stateMachineNode.null");
        }
        //删除【全部转换到当前】的转换
        Boolean result = delete(organizationId, transformDraft.getStateMachineId(), node.getAllStatusTransformId());
        //更新node的【全部转换到当前】转换id
        int update = nodeDraftMapper.updateAllStatusTransformId(organizationId, transformDraft.getEndNodeId(), null);
        if (update != 1) {
            throw new CommonException("error.deleteAllStatusTransform.updateAllStatusTransformId");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateConditionStrategy(Long organizationId, Long transformId, String conditionStrategy) {
        if (!EnumUtil.contain(TransformConditionStrategy.class, conditionStrategy)) {
            throw new CommonException("error.updateConditionStrategy.conditionStrategy.illegal");
        }
        StateMachineTransformDraft transform = transformDraftMapper.queryById(organizationId, transformId);
        if (transform == null) {
            throw new CommonException("error.updateConditionStrategy.queryById.notFound");
        }
        transform.setConditionStrategy(conditionStrategy);
        int update = updateOptional(transform, "conditionStrategy");
        if (update != 1) {
            throw new CommonException("error.updateConditionStrategy.updateOptional");
        }
        return true;
    }

    @Override
    public Boolean checkName(Long organizationId, Long stateMachineId, Long startNodeId, Long endNodeId, String name) {
        StateMachineTransformDraft transformDraft = new StateMachineTransformDraft();
        transformDraft.setOrganizationId(organizationId);
        transformDraft.setName(name);
        transformDraft.setStartNodeId(startNodeId);
        transformDraft.setEndNodeId(endNodeId);
        transformDraft.setStateMachineId(stateMachineId);
        if(transformDraftMapper.select(transformDraft).isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public Boolean fixDeleteIllegalTransforms(Long organizationId) {
        transformDeployMapper.fixDeleteIllegalTransforms(organizationId);
        return true;
    }

    @Override
    public Map<Long, Map<Long, List<StateMachineTransform>>> queryStatusTransformsMap(Long organizationId, List<Long> stateMachineIds) {
        if (stateMachineIds == null || stateMachineIds.isEmpty()) {
            return null;
        }
        Map<Long, Map<Long, List<StateMachineTransform>>> resultMap = new HashMap<>(stateMachineIds.size());
        List<StateMachineTransform> allTransforms = transformDeployMapper.queryByStateMachineIds(organizationId, stateMachineIds);
        Map<Long, List<StateMachineTransform>> transformStateMachineIdMap = allTransforms.stream().collect(Collectors.groupingBy(StateMachineTransform::getStateMachineId));
        List<StateMachineNode> allNodes = nodeDeployMapper.queryByStateMachineIds(organizationId, stateMachineIds);
        Map<Long, List<StateMachineNode>> nodeStateMachineIdMap = allNodes.stream().collect(Collectors.groupingBy(StateMachineNode::getStateMachineId));
        for (Long stateMachineId : stateMachineIds) {
            List<StateMachineTransform> transforms = transformStateMachineIdMap.get(stateMachineId) != null ? transformStateMachineIdMap.get(stateMachineId) : new ArrayList<>();
            List<StateMachineTransform> typeAll = transforms.stream().filter(x -> x.getType().equals(TransformType.ALL)).collect(Collectors.toList());
            Map<Long, List<StateMachineTransform>> startListMap = transforms.stream().collect(Collectors.groupingBy(StateMachineTransform::getStartNodeId));
            List<StateMachineNode> nodes = nodeStateMachineIdMap.get(stateMachineId) != null ? nodeStateMachineIdMap.get(stateMachineId) : new ArrayList<>();
            Map<Long, List<StateMachineTransform>> statusMap = new HashMap<>(nodes.size());
            for (StateMachineNode node : nodes) {
                List<StateMachineTransform> nodeTransforms = startListMap.get(node.getId()) != null ? startListMap.get(node.getId()) : new ArrayList<>();
                nodeTransforms.addAll(typeAll);
                statusMap.put(node.getStatusId(), nodeTransforms);
            }
            resultMap.put(stateMachineId, statusMap);
        }

        return resultMap;
    }
}
