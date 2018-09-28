package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.ConfigEnumDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.app.assembler.StateMachineConfigAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.enums.StateMachineTransformStatus;
import io.choerodon.statemachine.infra.enums.TransformType;
import io.choerodon.statemachine.infra.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peng.jiang@hand-china.com
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
    private StateMachineConfigAssembler stateMachineConfigAssembler;
    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;
    @Autowired
    private StateMachineService stateMachineService;

    @Override
    public StateMachineTransformDTO create(Long organizationId, StateMachineTransformDTO transformDTO) {
        StateMachineTransformDraft transform = stateMachineTransformAssembler.toTarget(transformDTO, StateMachineTransformDraft.class);
        transform.setType(TransformType.CUSTOM);
        int isInsert = transformDraftMapper.insert(transform);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }
        transform = transformDraftMapper.queryById(organizationId, transform.getId());
        stateMachineService.updateStateMachineStatus(organizationId, transform.getStateMachineId());
        return stateMachineTransformAssembler.toTarget(transform, StateMachineTransformDTO.class);

    }

    @Override
    public StateMachineTransformDTO update(Long organizationId, Long transformId, StateMachineTransformDTO transformDTO) {
        StateMachineTransformDraft transform = stateMachineTransformAssembler.toTarget(transformDTO, StateMachineTransformDraft.class);
        transform.setId(transformId);
        int isUpdate = transformDraftMapper.updateByPrimaryKeySelective(transform);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineTransform.update");
        }

        transform = transformDraftMapper.queryById(organizationId, transform.getId());
        stateMachineService.updateStateMachineStatus(organizationId, transform.getStateMachineId());
        return stateMachineTransformAssembler.toTarget(transform, StateMachineTransformDTO.class);

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
        StateMachineTransformDTO dto = stateMachineTransformAssembler.toTarget(transform, StateMachineTransformDTO.class);
        List<StateMachineConfigDTO> conditions = new ArrayList<>();
        List<StateMachineConfigDTO> validators = new ArrayList<>();
        List<StateMachineConfigDTO> triggers = new ArrayList<>();
        List<StateMachineConfigDTO> postpositions = new ArrayList<>();
        StateMachineConfigDraft config = new StateMachineConfigDraft();
        config.setTransformId(transformId);
        List<StateMachineConfigDraft> stateMachineConfigList = configMapper.select(config);
        if (stateMachineConfigList != null && !stateMachineConfigList.isEmpty()) {
            List<StateMachineConfigDTO> dtoList = stateMachineConfigAssembler.toTargetList(stateMachineConfigList, StateMachineConfigDTO.class);
            for (StateMachineConfigDTO configDto : dtoList) {
                if (ConfigType.CONDITION.equals(configDto.getType())) {
                    List<ConfigEnumDTO> conditionConfigEnums = configService.buildConfigEnum(ConfigType.CONDITION);
                    for (ConfigEnumDTO configEnum : conditionConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    conditions.add(configDto);
                } else if (ConfigType.VALIDATOR.equals(configDto.getType())) {
                    List<ConfigEnumDTO> validatorConfigEnums = configService.buildConfigEnum(ConfigType.VALIDATOR);
                    for (ConfigEnumDTO configEnum : validatorConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    validators.add(configDto);
                } else if (ConfigType.TRIGGER.equals(configDto.getType())) {
                    List<ConfigEnumDTO> triggerConfigEnums = configService.buildConfigEnum(ConfigType.TRIGGER);
                    for (ConfigEnumDTO configEnum : triggerConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    triggers.add(configDto);
                } else {
                    List<ConfigEnumDTO> postpositionConfigEnums = configService.buildConfigEnum(ConfigType.POSTPOSITION);
                    for (ConfigEnumDTO configEnum : postpositionConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    postpositions.add(configDto);
                }
            }
        }
        dto.setConditions(conditions);
        dto.setValidators(validators);
        dto.setTriggers(triggers);
        dto.setPostpositions(postpositions);
        StateMachineNodeDraft startNode = nodeDraftMapper.getNodeById(dto.getStartNodeId());
        StateMachineNodeDraft endNode = nodeDraftMapper.getNodeById(dto.getEndNodeId());
        dto.setStartNodeDTO(stateMachineNodeAssembler.toTarget(startNode, StateMachineNodeDTO.class));
        dto.setEndNodeDTO(stateMachineNodeAssembler.toTarget(endNode, StateMachineNodeDTO.class));
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
    public List<StateMachineTransformDTO> queryListByStatusIdByDeloy(Long organizationId, Long stateMachineId, Long statusId) {
        Long startNodeId = nodeDeployMapper.getNodeDeployByStatusId(stateMachineId, statusId).getId();
        StateMachineTransform transform = new StateMachineTransform();
        transform.setStateMachineId(stateMachineId);
        transform.setStartNodeId(startNodeId);
        return stateMachineTransformAssembler.toTargetList(transformDeployMapper.select(transform), StateMachineTransformDTO.class);
    }

    @Override
    public StateMachineTransformDTO createAllStatusTransform(Long organizationId, StateMachineTransformDTO transformDTO) {
        Long endNodeId = transformDTO.getEndNodeId();
        if (endNodeId == null) {
            throw new CommonException("error.endNodeId.null");
        }
        StateMachineNodeDraft node = nodeDraftMapper.getNodeById(endNodeId);
        if (node == null) {
            throw new CommonException("error.stateMachineNode.null");
        }
        //创建【全部转换到当前】的transform
        Status state = stateMapper.queryById(organizationId, node.getStatusId());
        transformDTO.setName(state.getName());
        transformDTO.setDescription("全部转换");
        transformDTO.setEndNodeId(endNodeId);
        transformDTO.setStartNodeId(null);
        transformDTO.setOrganizationId(organizationId);
        transformDTO.setType(TransformType.ALL);
        transformDTO.setConditionStrategy(StateMachineTransformStatus.CONDITION_STRATEGY_ONE);
        StateMachineTransformDraft transform = stateMachineTransformAssembler.toTarget(transformDTO, StateMachineTransformDraft.class);
        int isInsert = transformDraftMapper.insert(transform);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }
        //更新node的【全部转换到当前】转换id
        node.setAllStatusTransformId(transform.getId());
        nodeService.updateOptional(node, "allStateTransformId");
        transform = transformDraftMapper.queryById(organizationId, transform.getId());
        stateMachineService.updateStateMachineStatus(organizationId, transform.getStateMachineId());
        return stateMachineTransformAssembler.toTarget(transform, StateMachineTransformDTO.class);

    }

    @Override
    public Boolean deleteAllStatusTransform(Long organizationId, Long nodeId) {
        StateMachineNodeDraft node = nodeDraftMapper.getNodeById(nodeId);
        if (node == null) {
            throw new CommonException("error.stateMachineNode.null");
        }
        //删除【全部转换到当前】的转换
        Boolean result = delete(organizationId, node.getAllStatusTransformId());
        //更新node的【全部转换到当前】转换id
        node.setAllStatusTransformId(null);
        int updateResult = nodeService.updateOptional(node, "allStateTransformId");
        if (updateResult != 1) {
            throw new CommonException("error.StateMachineTransformServiceImpl.updateOptional");
        }
        return result;
    }
}
