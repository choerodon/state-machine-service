package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.ConfigCodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.service.ConfigCodeService;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.app.assembler.StateMachineConfigAssembler;
import io.choerodon.statemachine.domain.StateMachineConfig;
import io.choerodon.statemachine.domain.StateMachineConfigDraft;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.mapper.StateMachineConfigDraftMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineConfigMapper;
import io.choerodon.statemachine.infra.utils.EnumUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class StateMachineConfigServiceImpl extends BaseServiceImpl<StateMachineConfigDraft> implements StateMachineConfigService {

    @Autowired
    private StateMachineConfigDraftMapper configDraftMapper;
    @Autowired
    private StateMachineConfigMapper configDeployMapper;
    @Autowired
    private ConfigCodeService configCodeService;
    @Autowired
    private StateMachineConfigAssembler stateMachineConfigAssembler;

    private ModelMapper modelMapper = new ModelMapper();
    private static final String ERROR_STATUS_TYPE_ILLEGAL = "error.status.type.illegal";

    @Override
    public StateMachineConfigDTO create(Long organizationId, Long stateMachineId, Long transformId, StateMachineConfigDTO configDTO) {
        if (!EnumUtil.contain(ConfigType.class, configDTO.getType())) {
            throw new CommonException(ERROR_STATUS_TYPE_ILLEGAL);
        }
        //验证configCode
        checkCode(transformId, configDTO.getType(), configDTO.getCode());

        configDTO.setTransformId(transformId);
        configDTO.setOrganizationId(organizationId);
        StateMachineConfigDraft config = modelMapper.map(configDTO, StateMachineConfigDraft.class);
        config.setStateMachineId(stateMachineId);
        int isInsert = configDraftMapper.insert(config);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineConfig.create");
        }
        config = configDraftMapper.queryById(organizationId, config.getId());
        return modelMapper.map(config, StateMachineConfigDTO.class);
    }

    @Override
    public Boolean delete(Long organizationId, Long configId) {
        StateMachineConfigDraft config = new StateMachineConfigDraft();
        config.setId(configId);
        config.setOrganizationId(organizationId);
        int isDelete = configDraftMapper.delete(config);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachineConfig.delete");
        }
        return true;
    }

    @Override
    public List<StateMachineConfigDTO> queryByTransformId(Long organizationId, Long transformId, String type, Boolean isDraft) {
        if (type != null && !EnumUtil.contain(ConfigType.class, type)) {
            throw new CommonException(ERROR_STATUS_TYPE_ILLEGAL);
        }

        List<StateMachineConfigDTO> configDTOS;
        if (isDraft) {
            List<StateMachineConfigDraft> configs = configDraftMapper.queryWithCodeInfo(organizationId, transformId, type);
            configDTOS = stateMachineConfigAssembler.toTargetList(configs, StateMachineConfigDTO.class);
        } else {
            List<StateMachineConfig> configs = configDeployMapper.queryWithCodeInfo(organizationId, transformId, type);
            configDTOS = stateMachineConfigAssembler.toTargetList(configs, StateMachineConfigDTO.class);
        }
        return configDTOS;
    }

    @Override
    public List<StateMachineConfigDTO> queryDeployByTransformIds(Long organizationId, String type, List<Long> transformIds) {
        if (!EnumUtil.contain(ConfigType.class, type)) {
            throw new CommonException(ERROR_STATUS_TYPE_ILLEGAL);
        }
        if (transformIds != null && !transformIds.isEmpty()) {
            List<StateMachineConfig> configs = configDeployMapper.queryWithCodeInfoByTransformIds(organizationId, type, transformIds);
            return stateMachineConfigAssembler.toTargetList(configs, StateMachineConfigDTO.class);
        } else {
            return Collections.emptyList();
        }

    }

    public void checkCode(Long transformId, String type, String code) {
        List<ConfigCodeDTO> configCodeDTOs = configCodeService.queryByType(type);
        if (configCodeDTOs.stream().noneMatch(configCodeDTO -> configCodeDTO.getCode().equals(code))) {
            throw new CommonException("error.configCode.illegal");
        }
        StateMachineConfigDraft configDraft = new StateMachineConfigDraft();
        configDraft.setTransformId(transformId);
        configDraft.setCode(code);
        if (!configDraftMapper.select(configDraft).isEmpty()) {
            throw new CommonException("error.configCode.exist");
        }

    }
}
