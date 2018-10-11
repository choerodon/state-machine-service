package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.ConfigCodeDTO;
import io.choerodon.statemachine.api.dto.PropertyData;
import io.choerodon.statemachine.api.service.ConfigCodeService;
import io.choerodon.statemachine.app.assembler.ConfigCodeAssembler;
import io.choerodon.statemachine.domain.ConfigCode;
import io.choerodon.statemachine.domain.StateMachineConfigDraft;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.mapper.ConfigCodeMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineConfigDraftMapper;
import io.choerodon.statemachine.infra.utils.EnumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @date 2018/10/10
 */
@Service
public class ConfigCodeServiceImpl extends BaseServiceImpl<ConfigCode> implements ConfigCodeService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigCodeServiceImpl.class);
    @Autowired
    private ConfigCodeMapper configCodeMapper;
    @Autowired
    private StateMachineConfigDraftMapper configDraftMapper;

    @Autowired
    private ConfigCodeAssembler configCodeAssembler;

    @Override
    public List<ConfigCodeDTO> queryByType(String type) {
        if (!EnumUtil.contain(ConfigType.class, type)) {
            throw new CommonException("error.status.type.illegal");
        }
        ConfigCode configCode = new ConfigCode();
        configCode.setType(type);
        List<ConfigCode> configCodes = configCodeMapper.select(configCode);
        return configCodeAssembler.toTargetList(configCodes, ConfigCodeDTO.class);
    }

    @Override
    public List<ConfigCodeDTO> queryByTransformId(Long organizationId, Long transformId, String type) {
        StateMachineConfigDraft config = new StateMachineConfigDraft();
        config.setOrganizationId(organizationId);
        config.setTransformId(transformId);
        config.setType(type);
        List<StateMachineConfigDraft> configs = configDraftMapper.select(config);
        List<String> configCodes = configs.stream().map(StateMachineConfigDraft::getCode).collect(Collectors.toList());
        //过滤掉已经配置的，返回未配置的code
        List<ConfigCodeDTO> configCodeDTOS = queryByType(type).stream().filter(configCodeDTO -> !configCodes.contains(configCodeDTO.getCode())).collect(Collectors.toList());
        return configCodeDTOS;
    }

    @Override
    public void handlePropertyData(PropertyData propertyData) {
        String service = propertyData.getServiceName();
        if (service == null) {
            throw new CommonException("error.handlePropertyData.service.notNull");
        }
        //先删除该服务的ConfigCode
        ConfigCode delete = new ConfigCode();
        delete.setService(propertyData.getServiceName());
        configCodeMapper.delete(delete);
        //再插入扫描到的ConfigCode
        List<ConfigCodeDTO> configCodeDTOS = propertyData.getList();
        configCodeDTOS.forEach(configCodeDTO -> {
            configCodeDTO.setService(service);
            configCodeMapper.insert(configCodeAssembler.toTarget(configCodeDTO, ConfigCode.class));
            logger.info("handlePropertyData service:{} insert code:{} successful", service, configCodeDTO.getCode());
        });
        logger.info("handlePropertyData load service:{} successful", service);
    }
}
