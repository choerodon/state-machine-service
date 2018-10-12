package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.domain.StateMachineNodeDraft;
import io.choerodon.statemachine.domain.StateMachineTransform;
import io.choerodon.statemachine.domain.StateMachineTransformDraft;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.enums.TransformConditionStrategy;
import io.choerodon.statemachine.infra.enums.TransformType;
import io.choerodon.statemachine.infra.feign.dto.TransformInfo;
import io.choerodon.statemachine.infra.mapper.*;
import io.choerodon.statemachine.infra.utils.EnumUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@Component
@Transactional(rollbackFor = Exception.class)
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
    @Autowired
    private StateMachineService stateMachineService;

    @Override
    public StateMachineTransformDTO create(Long organizationId, StateMachineTransformDTO transformDTO) {
        StateMachineTransformDraft transform = stateMachineTransformAssembler.toTarget(transformDTO, StateMachineTransformDraft.class);
        transform.setType(TransformType.CUSTOM);
        transform.setOrganizationId(organizationId);
        transform.setConditionStrategy(TransformConditionStrategy.ALL);

        int isInsert = transformDraftMapper.insert(transform);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }
        //更新状态机状态
        stateMachineService.updateStateMachineStatus(organizationId, transform.getStateMachineId());
        return queryById(organizationId, transform.getId());
    }

    @Override
    public StateMachineTransformDTO update(Long organizationId, Long transformId, StateMachineTransformDTO transformDTO) {
        StateMachineTransformDraft transform = stateMachineTransformAssembler.toTarget(transformDTO, StateMachineTransformDraft.class);
        transform.setId(transformId);
        transform.setType(TransformType.CUSTOM);
        transform.setOrganizationId(organizationId);
        int isUpdate = transformDraftMapper.updateByPrimaryKeySelective(transform);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineTransform.update");
        }

        transform = transformDraftMapper.queryById(organizationId, transform.getId());
        stateMachineService.updateStateMachineStatus(organizationId, transform.getStateMachineId());
        return queryById(organizationId, transform.getId());

    }

    @Override
    public Boolean delete(Long organizationId, Long transformId) {
        StateMachineTransformDraft transform = transformDraftMapper.queryById(organizationId, transformId);
        int isDelete = transformDraftMapper.deleteByPrimaryKey(transformId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachineTransform.delete");
        }
        stateMachineService.updateStateMachineStatus(organizationId, transform.getStateMachineId());
        return true;
    }

    @Override
    public Boolean checkName(Long organizationId, Long stateMachineId, Long transformId, String name) {
        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setStateMachineId(stateMachineId);
        transform.setOrganizationId(organizationId);
        transform.setName(name);
        transform = transformDraftMapper.selectOne(transform);
        if (transform != null) {
            //若传了id，则为更新校验（更新校验不校验本身），不传为创建校验
            return transform.getId().equals(transformId);
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
        dto.setPostpositions(configService.queryByTransformId(organizationId, transformId, ConfigType.POSTPOSITION, true));
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
    public List<TransformInfo> queryListByStatusIdByDeploy(Long organizationId, Long stateMachineId, Long statusId) {
        Long startNodeId = nodeDeployMapper.getNodeDeployByStatusId(stateMachineId, statusId).getId();
        StateMachineTransform select1 = new StateMachineTransform();
        select1.setStateMachineId(stateMachineId);
        select1.setStartNodeId(startNodeId);
        List<StateMachineTransform> stateMachineTransforms = transformDeployMapper.select(select1);
        //增加【全部】类型的转换
        StateMachineTransform select2 = new StateMachineTransform();
        select2.setStateMachineId(stateMachineId);
        select2.setType(TransformType.ALL);
        List<StateMachineTransform> typeAllTransforms = transformDeployMapper.select(select2);
        stateMachineTransforms.addAll(typeAllTransforms);
        return stateMachineTransformAssembler.toTransformInfo(stateMachineId, stateMachineTransforms);
    }

    @Override
    public StateMachineTransformDTO createAllStatusTransform(Long organizationId, StateMachineTransformDTO transformDTO) {
        Long endNodeId = transformDTO.getEndNodeId();
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
        //todo 增加当前节点判断是否已存在【全部】的转换id

        //创建
        transformDTO.setName(state.getName());
        transformDTO.setDescription("全部转换");
        transformDTO.setEndNodeId(endNodeId);
        transformDTO.setStartNodeId(0L);
        transformDTO.setOrganizationId(organizationId);
        transformDTO.setType(TransformType.ALL);
        transformDTO.setConditionStrategy(TransformConditionStrategy.ALL);
        StateMachineTransformDraft transform = stateMachineTransformAssembler.toTarget(transformDTO, StateMachineTransformDraft.class);
        int isInsert = transformDraftMapper.insert(transform);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }
        //更新node的【全部转换到当前】转换id
        stateMachineNodeDraft.setAllStatusTransformId(transform.getId());
        nodeService.updateOptional(stateMachineNodeDraft, "allStatusTransformId");
        transform = transformDraftMapper.queryById(organizationId, transform.getId());
        stateMachineService.updateStateMachineStatus(organizationId, transform.getStateMachineId());
        return stateMachineTransformAssembler.toTarget(transform, StateMachineTransformDTO.class);
    }

    @Override
    public Boolean deleteAllStatusTransform(Long organizationId, Long transformId) {
        StateMachineTransformDraft transformDraft = transformDraftMapper.queryById(organizationId, transformId);
        if (transformDraft == null) {
            throw new CommonException("error.stateMachineTransform.null");
        }
        if (TransformType.ALL.equals(transformDraft.getType())) {
            throw new CommonException("error.stateMachineTransform.type.illegal");
        }
        //目标节点
        StateMachineNodeDraft node = nodeDraftMapper.getNodeById(transformDraft.getEndNodeId());
        if (node == null) {
            throw new CommonException("error.stateMachineNode.null");
        }
        //删除【全部转换到当前】的转换
        Boolean result = delete(organizationId, node.getAllStatusTransformId());
        //更新node的【全部转换到当前】转换id
        node.setAllStatusTransformId(null);
        int updateResult = nodeService.updateOptional(node, "allStatusTransformId");
        if (updateResult != 1) {
            throw new CommonException("error.deleteAllStatusTransform.updateOptional");
        }
        return result;
    }

    @Override
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
}
