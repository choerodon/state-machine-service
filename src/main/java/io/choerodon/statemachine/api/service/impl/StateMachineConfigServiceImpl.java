package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.ConfigEnumDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.app.assembler.StateMachineConfigAssembler;
import io.choerodon.statemachine.domain.StateMachineConfig;
import io.choerodon.statemachine.domain.StateMachineConfigDraft;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.mapper.StateMachineConfigDraftMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineConfigMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class StateMachineConfigServiceImpl extends BaseServiceImpl<StateMachineConfigDraft> implements StateMachineConfigService {

    @Autowired
    private StateMachineConfigDraftMapper configDraftMapper;
    @Autowired
    private StateMachineConfigMapper configDeployMapper;
    @Autowired
    private StateMachineConfigAssembler stateMachineConfigAssembler;

    @Autowired
    private StateMachineConfigService configService;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public StateMachineConfigDTO create(Long organizationId, Long stateMachineId, StateMachineConfigDTO configDTO) {
        configDTO.setOrganizationId(organizationId);
        StateMachineConfigDraft config = modelMapper.map(configDTO, StateMachineConfigDraft.class);
        config.setStateMachineId(stateMachineId);
        int isInsert = configDraftMapper.insert(config);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineConfig.create");
        }
        config = configDraftMapper.queryById(organizationId,config.getId());
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
    public List<StateMachineConfigDTO> queryByTransformId(Long organizationId,Long transformId, String type,Boolean isDraft) {
        List<StateMachineConfigDTO> configDTOS = null;
        if (isDraft) {
            StateMachineConfigDraft config = new StateMachineConfigDraft();
            config.setTransformId(transformId);
            config.setOrganizationId(organizationId);
            config.setType(type);
            List<StateMachineConfigDraft> configs = configDraftMapper.select(config);
            configDTOS = stateMachineConfigAssembler.toTargetList(configs, StateMachineConfigDTO.class);
        } else {
            StateMachineConfig config = new StateMachineConfig();
            config.setTransformId(transformId);
            config.setOrganizationId(organizationId);
            config.setType(type);
            List<StateMachineConfig> configs = configDeployMapper.select(config);
            configDTOS = stateMachineConfigAssembler.toTargetList(configs, StateMachineConfigDTO.class);
        }
        //todo 这里是代码中构建的数据，后面可以考虑放在数据库中维护
        List<ConfigEnumDTO> enumDTOS = configService.buildConfigEnum(type);
        for (StateMachineConfigDTO configDTO : configDTOS) {
            for (ConfigEnumDTO enumDTO : enumDTOS) {
                if (configDTO.getCode().equals(enumDTO.getCode())) {
                    configDTO.setDescription(enumDTO.getDescription());
                }
            }
        }
        return configDTOS;
    }

    @Override
    public List<ConfigEnumDTO> queryConfig(Long organizationId,Long transformId, String type) {
        List<ConfigEnumDTO> configEnumDTOS = buildConfigEnum(type);
        StateMachineConfigDraft config = new StateMachineConfigDraft();
        config.setOrganizationId(organizationId);
        config.setTransformId(transformId);
        config.setType(type);
        List<StateMachineConfigDraft> configs = configDraftMapper.select(config);
        if (configs != null && !configs.isEmpty()) {
            List<ConfigEnumDTO> removeEnums = new ArrayList<>();
            for (ConfigEnumDTO enumDTO : configEnumDTOS) {
                for (StateMachineConfigDraft temp : configs) {
                    if (enumDTO.getCode().equals(temp.getCode())) {
                        removeEnums.add(enumDTO);
                    }
                }
            }
            configEnumDTOS.removeAll(removeEnums);
        }
        return configEnumDTOS;
    }

    /**
     * 条件，验证，后置条件数据
     * 写死构建
     *
     * @param type
     * @return
     */
    @Override
    public List<ConfigEnumDTO> buildConfigEnum(String type) {
        //todo 条件验证动作code，用数据库实现
        List<ConfigEnumDTO> configEnumDTOS = new ArrayList<>();
        if (type.equals(ConfigType.CONDITION)) {
            ConfigEnumDTO reporter = new ConfigEnumDTO();
            reporter.setCode("reporter");
            reporter.setName("仅允许报告人");
            reporter.setDescription("只有该报告人才能执行转换");
            reporter.setType(type);

            ConfigEnumDTO handler = new ConfigEnumDTO();
            handler.setCode("handler");
            handler.setName("仅允许经办人");
            handler.setDescription("只有经办人才能执行转换");
            handler.setType(type);

            ConfigEnumDTO inGroup = new ConfigEnumDTO();
            inGroup.setCode("inGroup");
            inGroup.setName("在任何组内的用户");
            inGroup.setDescription("只有隶属于某个组的用户才能执行转换");
            inGroup.setType(type);

            ConfigEnumDTO inProjectRole = new ConfigEnumDTO();
            inProjectRole.setCode("inProjectRole");
            inProjectRole.setName("在任何项目角色内的用户");
            inProjectRole.setDescription("只允许给定项目角色的用户才能执行转换");
            inProjectRole.setType(type);

            ConfigEnumDTO authority = new ConfigEnumDTO();
            authority.setCode("authority");
            authority.setName("权限条件");
            authority.setDescription("只有具有某项权限的用户才能执行转换");
            authority.setType(type);

            configEnumDTOS.add(reporter);
            configEnumDTOS.add(handler);
            configEnumDTOS.add(inGroup);
            configEnumDTOS.add(inProjectRole);
            configEnumDTOS.add(authority);
        } else if (type.equals(ConfigType.VALIDATOR)) {
            ConfigEnumDTO validator = new ConfigEnumDTO();
            validator.setCode("validator");
            validator.setName("权限校验");
            validator.setDescription("校验用户的权限。");
            validator.setType(type);
            configEnumDTOS.add(validator);
        } else if (type.equals(ConfigType.POSTPOSITION)) {
            ConfigEnumDTO assignCurrentUser = new ConfigEnumDTO();
            assignCurrentUser.setCode("assignCurrentUser");
            assignCurrentUser.setName("分配给当前用户");
            assignCurrentUser.setDescription("如果当前用户具有 “可分配”权限，那么将这个问题分配给当前用户");
            assignCurrentUser.setType(type);

            ConfigEnumDTO assignReporter = new ConfigEnumDTO();
            assignReporter.setCode("assignReporter");
            assignReporter.setName("分配给报告人");
            assignReporter.setDescription("将问题分配给报告人");
            assignReporter.setType(type);

            ConfigEnumDTO assignDeveloper = new ConfigEnumDTO();
            assignDeveloper.setCode("assignDeveloper");
            assignDeveloper.setName("分配给负责开发人");
            assignDeveloper.setDescription("将问题分配给项目/组件负责开发人");
            assignDeveloper.setType(type);

            configEnumDTOS.add(assignCurrentUser);
            configEnumDTOS.add(assignReporter);
            configEnumDTOS.add(assignDeveloper);
        }

        return configEnumDTOS;
    }
}
