package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.service.InstanceService;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineTransform;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.factory.MachineFactory;
import io.choerodon.statemachine.infra.feign.CustomFeignClientAdaptor;
import io.choerodon.statemachine.infra.feign.dto.TransformInfo;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineTransformMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author shinan.chen
 * @date 2018/9/18
 */
@Component("instanceService")
@Transactional(rollbackFor = Exception.class)
public class InstanceServiceImpl implements InstanceService {

    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;
    @Autowired
    private StateMachineConfigService configService;
    @Autowired
    private StateMachineTransformService transformService;
    @Autowired
    private StateMachineTransformMapper transformMapper;
    @Autowired
    private MachineFactory machineFactory;
    @Autowired
    private CustomFeignClientAdaptor customFeignClientAdaptor;

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceServiceImpl.class);
    private static final String EXCEPTION = "Exception:{}";

    @Override
    public ExecuteResult startInstance(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId) {
        ExecuteResult executeResult;
        try {
            executeResult = machineFactory.startInstance(organizationId, serviceCode, stateMachineId, instanceId);
        } catch (Exception e) {
            e.printStackTrace();
            executeResult = new ExecuteResult(false, null, "创建状态机实例失败");
        }
        return executeResult;
    }

    @Override
    public Long queryInitStatusId(Long organizationId, Long stateMachineId) {
        StateMachineNode select = new StateMachineNode();
        select.setOrganizationId(organizationId);
        select.setStateMachineId(stateMachineId);
        select.setType(NodeType.INIT);
        List<StateMachineNode> nodes = nodeDeployMapper.select(select);
        if (nodes.isEmpty()) {
            throw new CommonException("error.queryInitStatusId.notFound");
        }
        return nodes.get(0).getStatusId();
    }

    @Override
    public ExecuteResult executeTransform(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId, Long currentStatusId, Long transformId) {
        ExecuteResult executeResult;
        try {
            executeResult = machineFactory.executeTransform(organizationId, serviceCode, stateMachineId, instanceId, currentStatusId, transformId);
        } catch (Exception e) {
            e.printStackTrace();
            executeResult = new ExecuteResult(false, null, "执行转换失败");
        }
        return executeResult;
    }

    @Override
    public List<TransformInfo> queryListTransform(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId, Long statusId) {
        List<TransformInfo> transformInfos = transformService.queryListByStatusIdByDeploy(organizationId, stateMachineId, statusId);
        //获取转换的条件配置
        transformInfos.forEach(transformInfo -> transformInfo.setConditions(condition(transformInfo.getOrganizationId(), transformInfo.getId())));
        //调用对应服务，根据条件校验转换，过滤掉可用的转换
        try {
            ResponseEntity<List<TransformInfo>> listEntity = customFeignClientAdaptor.filterTransformsByConfig(getFilterTransformURI(serviceCode, instanceId), transformInfos);
            transformInfos = listEntity.getBody();
        } catch (Exception e) {
            LOGGER.error(EXCEPTION, e);
            transformInfos = Collections.emptyList();
        }
        return transformInfos;
    }

    @Override
    public Boolean validatorGuard(Long organizationId, String serviceCode, Long transformId, Long instanceId, StateContext<String, String> context) {
        StateMachineTransform transform = transformMapper.queryById(organizationId, transformId);
        List<StateMachineConfigDTO> conditionConfigs = condition(organizationId, transformId);
        List<StateMachineConfigDTO> validatorConfigs = validator(organizationId, transformId);
        ExecuteResult executeResult;
        //调用对应服务，执行条件和验证，返回是否成功
        try {
            executeResult = customFeignClientAdaptor.executeConfig(getExecuteConfigConditionURI(serviceCode, instanceId, null, transform.getConditionStrategy()), conditionConfigs).getBody();
            if (executeResult.getSuccess()) {
                executeResult = customFeignClientAdaptor.executeConfig(getExecuteConfigValidatorURI(serviceCode, instanceId, null), validatorConfigs).getBody();
            }
        } catch (Exception e) {
            LOGGER.error(EXCEPTION, e);
            executeResult = new ExecuteResult(false, null, "验证调用失败");
        }

        Map<Object, Object> variables = context.getExtendedState().getVariables();
        variables.put("executeResult", executeResult);
        return executeResult.getSuccess();
    }

    @Override
    public Boolean postAction(Long organizationId, String serviceCode, Long transformId, Long instanceId, StateContext<String, String> context) {
        List<StateMachineConfigDTO> configs = action(organizationId, transformId);
        StateMachineTransform transform = transformMapper.queryById(organizationId, transformId);
        //节点转状态
        Long targetStatusId = nodeDeployMapper.getNodeDeployById(Long.parseLong(context.getTarget().getId())).getStatusId();
        if (targetStatusId == null) {
            throw new CommonException("error.postAction.targetStatusId.notNull");
        }
        ExecuteResult executeResult;
        //调用对应服务，执行动作，返回是否成功
        try {
            ResponseEntity<ExecuteResult> executeResultEntity = customFeignClientAdaptor.executeConfig(getExecuteConfigPostActionURI(serviceCode, instanceId, targetStatusId, transform.getType()), configs);
            //返回为空则调用对应服务，对应服务方法报错
            if (executeResultEntity.getBody().getSuccess() != null) {
                executeResult = executeResultEntity.getBody();
            } else {
                executeResult = new ExecuteResult(false, null, "后置动作调用失败");
            }
        } catch (Exception e) {
            LOGGER.error(EXCEPTION, e);
            executeResult = new ExecuteResult(false, null, "后置动作调用失败");
        }
        Map<Object, Object> variables = context.getExtendedState().getVariables();
        variables.put("executeResult", executeResult);
        return executeResult.getSuccess();
    }

    @Override
    public List<StateMachineConfigDTO> condition(Long organizationId, Long transformId) {
        List<StateMachineConfigDTO> configs = configService.queryByTransformId(organizationId, transformId, ConfigType.CONDITION, false);
        return configs == null ? Collections.emptyList() : configs;
    }

    @Override
    public List<StateMachineConfigDTO> validator(Long organizationId, Long transformId) {
        List<StateMachineConfigDTO> configs = configService.queryByTransformId(organizationId, transformId, ConfigType.VALIDATOR, false);
        return configs == null ? Collections.emptyList() : configs;
    }

    @Override
    public List<StateMachineConfigDTO> trigger(Long organizationId, Long transformId) {
        List<StateMachineConfigDTO> configs = configService.queryByTransformId(organizationId, transformId, ConfigType.TRIGGER, false);
        return configs == null ? Collections.emptyList() : configs;
    }

    @Override
    public List<StateMachineConfigDTO> action(Long organizationId, Long transformId) {
        List<StateMachineConfigDTO> configs = configService.queryByTransformId(organizationId, transformId, ConfigType.ACTION, false);
        return configs == null ? Collections.emptyList() : configs;
    }

    /**
     * 获取过滤转换的URI
     *
     * @param serviceCode
     * @param instanceId
     * @return
     */
    private URI getFilterTransformURI(String serviceCode, Long instanceId) {
        URI uri = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceCode).append("/v1").append("/statemachine/filter_transform").append("?1=1");
        if (instanceId != null) {
            stringBuilder.append("&instance_id=").append(instanceId);
        }
        LOGGER.info("uri:{}", Optional.of(stringBuilder).map(result -> stringBuilder.toString()));
        try {
            uri = new URI(stringBuilder.toString());
        } catch (URISyntaxException e) {
            LOGGER.error(EXCEPTION, e);
        }
        return uri;
    }

    /**
     * 获取执行条件的URI
     *
     * @param serviceCode
     * @param instanceId
     * @param conditionStrategy
     * @return
     */
    private URI getExecuteConfigConditionURI(String serviceCode, Long instanceId, Long targetStatusId, String conditionStrategy) {
        URI uri = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceCode).append("/v1").append("/statemachine/execute_config_condition").append("?1=1");
        if (instanceId != null) {
            stringBuilder.append("&instance_id=").append(instanceId);
        }
        if (targetStatusId != null) {
            stringBuilder.append("&target_status_id=").append(targetStatusId);
        }
        if (conditionStrategy != null) {
            stringBuilder.append("&condition_strategy=").append(conditionStrategy);
        }
        LOGGER.info("uri:{}", Optional.of(stringBuilder).map(result -> stringBuilder.toString()));
        try {
            uri = new URI(stringBuilder.toString());
        } catch (URISyntaxException e) {
            LOGGER.error(EXCEPTION, e);
        }
        return uri;
    }

    /**
     * 获取执行验证的URI
     *
     * @param serviceCode
     * @param instanceId
     * @return
     */
    private URI getExecuteConfigValidatorURI(String serviceCode, Long instanceId, Long targetStatusId) {
        URI uri = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceCode).append("/v1").append("/statemachine/execute_config_validator").append("?1=1");
        if (instanceId != null) {
            stringBuilder.append("&instance_id=").append(instanceId);
        }
        if (targetStatusId != null) {
            stringBuilder.append("&target_status_id=").append(targetStatusId);
        }
        LOGGER.info("uri:{}", Optional.of(stringBuilder).map(result -> stringBuilder.toString()));
        try {
            uri = new URI(stringBuilder.toString());
        } catch (URISyntaxException e) {
            LOGGER.error(EXCEPTION, e);
        }
        return uri;
    }

    /**
     * 获取执行后置动作的URI
     *
     * @param serviceCode
     * @param instanceId
     * @param targetStatusId
     * @return
     */
    private URI getExecuteConfigPostActionURI(String serviceCode, Long instanceId, Long targetStatusId, String transformType) {
        URI uri = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceCode).append("/v1").append("/statemachine/execute_config_action").append("?1=1");
        if (instanceId != null) {
            stringBuilder.append("&instance_id=").append(instanceId);
        }
        if (targetStatusId != null) {
            stringBuilder.append("&target_status_id=").append(targetStatusId);
        }
        if (transformType != null) {
            stringBuilder.append("&transform_type=").append(transformType);
        }
        LOGGER.info("uri:{}", Optional.of(stringBuilder).map(result -> stringBuilder.toString()));
        try {
            uri = new URI(stringBuilder.toString());
        } catch (URISyntaxException e) {
            LOGGER.error(EXCEPTION, e);
        }
        return uri;
    }
}
