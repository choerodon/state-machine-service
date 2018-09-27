package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.ConfigEnumDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.service.StateMachineTransfService;
import io.choerodon.statemachine.app.assembler.StateMachineConfigAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransfAssembler;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.StateMachineConfigType;
import io.choerodon.statemachine.infra.enums.StateMachineNodeStatus;
import io.choerodon.statemachine.infra.enums.StateMachineTransfStatus;
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
public class StateMachineTransfServiceImpl extends BaseServiceImpl<StateMachineTransf> implements StateMachineTransfService {
    @Autowired
    private StateMachineTransfMapper transfMapper;
    @Autowired
    private StateMachineTransfDeployMapper transfDeployMapper;
    @Autowired
    private StateMachineNodeService nodeService;
    @Autowired
    private StateMachineNodeMapper nodeMapper;
    @Autowired
    private StateMachineConfigMapper configMapper;
    @Autowired
    private StateMachineConfigService configService;
    @Autowired
    private StateMapper stateMapper;
    @Autowired
    private StateMachineTransfAssembler stateMachineTransfAssembler;
    @Autowired
    private StateMachineConfigAssembler stateMachineConfigAssembler;
    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;
    @Autowired
    private StateMachineService stateMachineService;

    @Override
    public StateMachineTransfDTO create(Long organizationId, StateMachineTransfDTO transfDTO) {
        StateMachineTransf transf = stateMachineTransfAssembler.toTarget(transfDTO, StateMachineTransf.class);
        transf.setStatus(StateMachineNodeStatus.STATUS_CUSTOM);
        int isInsert = transfMapper.insert(transf);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransf.create");
        }
        transf = transfMapper.queryById(organizationId, transf.getId());
        stateMachineService.updateStateMachineStatus(organizationId, transf.getStateMachineId());
        return stateMachineTransfAssembler.toTarget(transf, StateMachineTransfDTO.class);

    }

    @Override
    public StateMachineTransfDTO update(Long organizationId, Long transfId, StateMachineTransfDTO transfDTO) {
        StateMachineTransf transf = stateMachineTransfAssembler.toTarget(transfDTO, StateMachineTransf.class);
        transf.setId(transfId);
        int isUpdate = transfMapper.updateByPrimaryKeySelective(transf);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineTransf.update");
        }

        transf = transfMapper.queryById(organizationId, transf.getId());
        stateMachineService.updateStateMachineStatus(organizationId, transf.getStateMachineId());
        return stateMachineTransfAssembler.toTarget(transf, StateMachineTransfDTO.class);

    }

    @Override
    public Boolean delete(Long organizationId, Long transfId) {
        StateMachineTransf transf = transfMapper.queryById(organizationId, transfId);
        int isDelete = transfMapper.deleteByPrimaryKey(transfId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachineTransf.delete");
        }
        stateMachineService.updateStateMachineStatus(organizationId, transf.getStateMachineId());
        return true;
    }

    @Override
    public Boolean checkName(Long organizationId, Long stateMachineId, Long transfId, String name) {
        StateMachineTransf transf = new StateMachineTransf();
        transf.setStateMachineId(stateMachineId);
        transf.setOrganizationId(organizationId);
        transf.setName(name);
        transf = transfMapper.selectOne(transf);
        if (transf != null) {
            //若传了id，则为更新校验（更新校验不校验本身），不传为创建校验
            return transf.getId().equals(transfId);
        }
        return true;
    }

    @Override
    public StateMachineTransfDTO queryById(Long organizationId, Long transfId) {
        StateMachineTransf transf = transfMapper.queryById(organizationId, transfId);
        StateMachineTransfDTO dto = stateMachineTransfAssembler.toTarget(transf, StateMachineTransfDTO.class);
        List<StateMachineConfigDTO> conditions = new ArrayList<>();
        List<StateMachineConfigDTO> validators = new ArrayList<>();
        List<StateMachineConfigDTO> triggers = new ArrayList<>();
        List<StateMachineConfigDTO> postpositions = new ArrayList<>();
        StateMachineConfig config = new StateMachineConfig();
        config.setTransfId(transfId);
        List<StateMachineConfig> stateMachineConfigList = configMapper.select(config);
        if (stateMachineConfigList != null && !stateMachineConfigList.isEmpty()) {
            List<StateMachineConfigDTO> dtoList = stateMachineConfigAssembler.toTargetList(stateMachineConfigList, StateMachineConfigDTO.class);
            for (StateMachineConfigDTO configDto : dtoList) {
                if (StateMachineConfigType.STATUS_CONDITION.value().equals(configDto.getType())) {
                    List<ConfigEnumDTO> conditionConfigEnums = configService.buildConfigEnum(StateMachineConfigType.STATUS_CONDITION.value());
                    for (ConfigEnumDTO configEnum : conditionConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    conditions.add(configDto);
                } else if (StateMachineConfigType.STATUS_VALIDATOR.value().equals(configDto.getType())) {
                    List<ConfigEnumDTO> validatorConfigEnums = configService.buildConfigEnum(StateMachineConfigType.STATUS_VALIDATOR.value());
                    for (ConfigEnumDTO configEnum : validatorConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    validators.add(configDto);
                } else if (StateMachineConfigType.STATUS_TRIGGER.value().equals(configDto.getType())) {
                    List<ConfigEnumDTO> triggerConfigEnums = configService.buildConfigEnum(StateMachineConfigType.STATUS_TRIGGER.value());
                    for (ConfigEnumDTO configEnum : triggerConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    triggers.add(configDto);
                } else {
                    List<ConfigEnumDTO> postpositionConfigEnums = configService.buildConfigEnum(StateMachineConfigType.STATUS_POSTPOSITION.value());
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
        StateMachineNode startNode = nodeMapper.getNodeById(dto.getStartNodeId());
        StateMachineNode endNode = nodeMapper.getNodeById(dto.getEndNodeId());
        dto.setStartNodeDTO(stateMachineNodeAssembler.toTarget(startNode, StateMachineNodeDTO.class));
        dto.setEndNodeDTO(stateMachineNodeAssembler.toTarget(endNode, StateMachineNodeDTO.class));
        return dto;
    }

    @Override
    public Long getInitTransf(Long organizationId, Long stateMachineId) {
        StateMachineTransf transf = new StateMachineTransf();
        transf.setStateMachineId(stateMachineId);
        transf.setOrganizationId(organizationId);
        transf.setStartNodeId(nodeService.getInitNode(organizationId, stateMachineId));
        List<StateMachineTransf> transfs = transfMapper.select(transf);
        if (transfs.isEmpty()) {
            throw new CommonException("error.initTransf.null");
        }
        return transfs.get(0).getId();
    }

    @Override
    public List<StateMachineTransfDTO> queryListByStateId(Long organizationId, Long stateMachineId, Long stateId) {
        Long startNodeId = nodeMapper.getNodeByStateId(stateMachineId, stateId).getId();
        StateMachineTransfDeploy transf = new StateMachineTransfDeploy();
        transf.setStateMachineId(stateMachineId);
        transf.setStartNodeId(startNodeId);
        return stateMachineTransfAssembler.toTargetList(transfDeployMapper.select(transf), StateMachineTransfDTO.class);
    }

    @Override
    public StateMachineTransfDTO createAllStateTransf(Long organizationId, StateMachineTransfDTO transfDTO) {
        Long endNodeId = transfDTO.getEndNodeId();
        if (endNodeId == null) {
            throw new CommonException("error.endNodeId.null");
        }
        StateMachineNode node = nodeMapper.getNodeById(endNodeId);
        if (node == null) {
            throw new CommonException("error.stateMachineNode.null");
        }
        //创建【全部转换到当前】的transf
        State state = stateMapper.queryById(organizationId, node.getStateId());
        transfDTO.setName(state.getName());
        transfDTO.setDescription("全部转换");
        transfDTO.setEndNodeId(endNodeId);
        transfDTO.setStartNodeId(null);
        transfDTO.setOrganizationId(organizationId);
        transfDTO.setStatus(StateMachineNodeStatus.STATUS_CUSTOM);
        transfDTO.setConditionStrategy(StateMachineTransfStatus.CONDITION_STRATEGY_ONE);
        StateMachineTransf transf = stateMachineTransfAssembler.toTarget(transfDTO, StateMachineTransf.class);
        int isInsert = transfMapper.insert(transf);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransf.create");
        }
        //更新node的【全部转换到当前】转换id
        node.setAllStateTransfId(transf.getId());
        nodeService.updateOptional(node, "allStateTransfId");
        transf = transfMapper.queryById(organizationId, transf.getId());
        stateMachineService.updateStateMachineStatus(organizationId, transf.getStateMachineId());
        return stateMachineTransfAssembler.toTarget(transf, StateMachineTransfDTO.class);

    }

    @Override
    public Boolean deleteAllStateTransf(Long organizationId, Long nodeId) {
        StateMachineNode node = nodeMapper.getNodeById(nodeId);
        if (node == null) {
            throw new CommonException("error.stateMachineNode.null");
        }
        //删除【全部转换到当前】的转换
        Boolean result = delete(organizationId, node.getAllStateTransfId());
        //更新node的【全部转换到当前】转换id
        node.setAllStateTransfId(null);
        int updateResult = nodeService.updateOptional(node, "allStateTransfId");
        if (updateResult != 1) {
            throw new CommonException("error.StateMachineTransfServiceImpl.updateOptional");
        }
        return result;
    }
}
