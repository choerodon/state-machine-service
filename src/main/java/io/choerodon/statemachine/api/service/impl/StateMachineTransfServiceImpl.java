package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.ConfigEnumDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineTransfService;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.StateMachineConfigType;
import io.choerodon.statemachine.infra.enums.StateMachineNodeStatus;
import io.choerodon.statemachine.infra.enums.StateMachineStatus;
import io.choerodon.statemachine.infra.enums.StateMachineTransfStatus;
import io.choerodon.statemachine.infra.mapper.*;
import io.choerodon.statemachine.infra.utils.ConvertUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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
    private StateMachineMapper stateMachineMapper;
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

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public StateMachineTransfDTO create(Long organizationId, StateMachineTransfDTO transfDTO) {
        StateMachineTransf transf = modelMapper.map(transfDTO, StateMachineTransf.class);
        transf.setStatus(StateMachineNodeStatus.STATUS_CUSTOM);
        int isInsert = transfMapper.insert(transf);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransf.create");
        }
        transf = transfMapper.selectByPrimaryKey(transf.getId());
        updateStateMachineStatus(transf.getStateMachineId());
        return modelMapper.map(transf, StateMachineTransfDTO.class);
    }

    @Override
    public StateMachineTransfDTO update(Long organizationId, Long transfId, StateMachineTransfDTO transfDTO) {
        StateMachineTransf transf = modelMapper.map(transfDTO, StateMachineTransf.class);
        transf.setId(transfId);
        int isUpdate = transfMapper.updateByPrimaryKeySelective(transf);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineTransf.update");
        }
        transf = transfMapper.selectByPrimaryKey(transf.getId());
        updateStateMachineStatus(transf.getStateMachineId());
        return modelMapper.map(transf, StateMachineTransfDTO.class);
    }

    @Override
    public Boolean delete(Long organizationId, Long transfId) {
        StateMachineTransf transf = transfMapper.selectByPrimaryKey(transfId);
        int isDelete = transfMapper.deleteByPrimaryKey(transfId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachineTransf.delete");
        }
        updateStateMachineStatus(transf.getStateMachineId());
        return true;
    }

    @Override
    public Boolean checkName(Long stateMachineId, Long transfId, String name) {
        StateMachineTransf transf = new StateMachineTransf();
        transf.setStateMachineId(stateMachineId);
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
        StateMachineTransf transf = transfMapper.selectByPrimaryKey(transfId);
        StateMachineTransfDTO dto = modelMapper.map(transf, StateMachineTransfDTO.class);
        List<StateMachineConfigDTO> conditions = new ArrayList<>();
        List<StateMachineConfigDTO> validators = new ArrayList<>();
        List<StateMachineConfigDTO> triggers = new ArrayList<>();
        List<StateMachineConfigDTO> postpositions = new ArrayList<>();
        StateMachineConfig config = new StateMachineConfig();

        List<ConfigEnumDTO> conditionConfigEnums = configService.buildConfigEnum(StateMachineConfigType.STATUS_CONDITION.value());
        List<ConfigEnumDTO> validatorConfigEnums = configService.buildConfigEnum(StateMachineConfigType.STATUS_VALIDATOR.value());
        List<ConfigEnumDTO> triggerConfigEnums = configService.buildConfigEnum(StateMachineConfigType.STATUS_TRIGGER.value());
        List<ConfigEnumDTO> postpositionConfigEnums = configService.buildConfigEnum(StateMachineConfigType.STATUS_POSTPOSITION.value());

        config.setTransfId(transfId);
        List<StateMachineConfig> list = configMapper.select(config);
        if (list != null && !list.isEmpty()) {
            List<StateMachineConfigDTO> dtoList = modelMapper.map(list, new TypeToken<List<StateMachineConfigDTO>>() {
            }.getType());
            for (StateMachineConfigDTO configDto : dtoList) {
                if (StateMachineConfigType.STATUS_CONDITION.value().equals(configDto.getType())) {
                    for (ConfigEnumDTO configEnum : conditionConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    conditions.add(configDto);
                } else if (StateMachineConfigType.STATUS_VALIDATOR.value().equals(configDto.getType())) {
                    for (ConfigEnumDTO configEnum : validatorConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    validators.add(configDto);
                } else if (StateMachineConfigType.STATUS_TRIGGER.value().equals(configDto.getType())) {
                    for (ConfigEnumDTO configEnum : triggerConfigEnums) {
                        if (configEnum.getCode().equals(configDto.getCode())) {
                            configDto.setDescription(configEnum.getDescription());
                            break;
                        }
                    }
                    triggers.add(configDto);
                } else {
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
        dto.setStartNodeDTO(ConvertUtils.convertNodeToNodeDTO(startNode));
        dto.setEndNodeDTO(ConvertUtils.convertNodeToNodeDTO(endNode));
        return dto;
    }

    /**
     * 修改状态机状态
     * 发布 -> 修改
     *
     * @param stateMachineId
     */
    private void updateStateMachineStatus(Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.selectByPrimaryKey(stateMachineId);
        if (stateMachine != null && stateMachine.getStatus().equals(StateMachineStatus.STATUS_ACTIVE)) {
            stateMachine.setStatus(StateMachineStatus.STATUS_DRAFT);
            int stateMachineUpdate = stateMachineMapper.updateByPrimaryKey(stateMachine);
            if (stateMachineUpdate != 1) {
                throw new CommonException("error.stateMachine.update");
            }
        }
    }

    @Override
    public Long getInitTransf(Long stateMachineId) {
        StateMachineTransf transf = new StateMachineTransf();
        transf.setStateMachineId(stateMachineId);
        transf.setStartNodeId(nodeService.getInitNode(stateMachineId));
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
        List<StateMachineTransfDeploy> transfs = transfDeployMapper.select(transf);
        List<StateMachineTransfDTO> dtos = modelMapper.map(transfs, new TypeToken<List<StateMachineTransfDTO>>() {
        }.getType());
        return dtos;
    }

    @Override
    public StateMachineTransfDTO createAllStateTransf(Long organizationId, StateMachineTransfDTO transfDTO) {
        Long endNodeId = transfDTO.getEndNodeId();
        if (endNodeId != null) {
            throw new CommonException("error.endNodeId.null");
        }
        StateMachineNode node = nodeMapper.getNodeById(endNodeId);
        if (node != null) {
            throw new CommonException("error.stateMachineNode.null");
        }

        //创建【全部转换到当前】的transf
        State state = stateMapper.selectByPrimaryKey(node.getStateId());
        transfDTO.setName(state.getName());
        transfDTO.setDescription("全部转换");
        transfDTO.setEndNodeId(endNodeId);
        transfDTO.setStartNodeId(null);
        transfDTO.setStatus(StateMachineNodeStatus.STATUS_CUSTOM);
        transfDTO.setConditionStrategy(StateMachineTransfStatus.CONDITION_STRATEGY_ONE);
        StateMachineTransf transf = modelMapper.map(transfDTO, StateMachineTransf.class);
        int isInsert = transfMapper.insert(transf);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineTransf.create");
        }

        //更新node的【全部转换到当前】转换id
        node.setAllStateTransfId(transf.getId());
        nodeService.updateOptional(node, "allStateTransfId");

        transf = transfMapper.selectByPrimaryKey(transf.getId());
        updateStateMachineStatus(transf.getStateMachineId());
        return modelMapper.map(transf, StateMachineTransfDTO.class);
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
