package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.dto.InputDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.service.InstanceService;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.domain.StateMachine;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineTransform;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.factory.MachineFactory;
import io.choerodon.statemachine.infra.feign.CustomFeignClientAdaptor;
import io.choerodon.statemachine.infra.feign.dto.TransformInfo;
import io.choerodon.statemachine.infra.mapper.StateMachineMapper;
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
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private StateMachineMapper stateMachineMapper;
    @Autowired
    private StateMachineTransformAssembler stateMachineTransformAssembler;

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceServiceImpl.class);
    private static final String EXCEPTION = "Exception:{}";

    @Override
    public ExecuteResult startInstance(Long organizationId, String serviceCode, Long stateMachineId, InputDTO inputDTO) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.notFound");
        }
        ExecuteResult executeResult;
        try {
            executeResult = machineFactory.startInstance(organizationId, serviceCode, stateMachineId, inputDTO);
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
    public ExecuteResult executeTransform(Long organizationId, String serviceCode, Long stateMachineId, Long currentStatusId, Long transformId, InputDTO inputDTO) {
        return machineFactory.executeTransform(organizationId, serviceCode, stateMachineId, currentStatusId, transformId, inputDTO);
    }

    @Override
    public List<TransformInfo> queryListTransform(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId, Long statusId) {
        Boolean isNeedFilter = false;
        List<StateMachineTransform> stateMachineTransforms = transformService.queryListByStatusIdByDeploy(organizationId, stateMachineId, statusId);
        //获取节点信息
        List<StateMachineNode> nodes = nodeDeployMapper.selectByStateMachineId(stateMachineId);
        List<StateMachineConfigDTO> configs = configService.queryDeployByTransformIds(organizationId, ConfigType.CONDITION, stateMachineTransforms.stream().map(StateMachineTransform::getId).collect(Collectors.toList()));
        Map<Long, Long> nodeMap = nodes.stream().collect(Collectors.toMap(StateMachineNode::getId, StateMachineNode::getStatusId));
        Map<Long, List<StateMachineConfigDTO>> configMaps = configs.stream().collect(Collectors.groupingBy(StateMachineConfigDTO::getTransformId));
        List<TransformInfo> transformInfos = new ArrayList<>(stateMachineTransforms.size());
        for (StateMachineTransform transform : stateMachineTransforms) {
            TransformInfo transformInfo = stateMachineTransformAssembler.toTarget(transform, TransformInfo.class);
            transformInfo.setStartStatusId(nodeMap.get(transform.getStartNodeId()));
            transformInfo.setEndStatusId(nodeMap.get(transform.getEndNodeId()));
            //获取转换的条件配置
            List<StateMachineConfigDTO> conditionConfigs = configMaps.get(transform.getId());
            if (conditionConfigs == null) {
                transformInfo.setConditions(Collections.emptyList());
            } else {
                transformInfo.setConditions(conditionConfigs);
                isNeedFilter = true;
            }
            transformInfos.add(transformInfo);
        }
        //调用对应服务，根据条件校验转换，过滤掉可用的转换
        if (isNeedFilter) {
            try {
                ResponseEntity<List<TransformInfo>> listEntity = customFeignClientAdaptor.filterTransformsByConfig(getFilterTransformURI(serviceCode, instanceId), transformInfos);
                transformInfos = listEntity.getBody();
            } catch (Exception e) {
                LOGGER.error(EXCEPTION, e);
                transformInfos = Collections.emptyList();
            }
        }
        return transformInfos;
    }

    @Override
    public Boolean validatorGuard(Long organizationId, String serviceCode, Long transformId, InputDTO inputDTO, StateContext<String, String> context) {
        StateMachineTransform transform = transformMapper.queryById(organizationId, transformId);
        List<StateMachineConfigDTO> conditionConfigs = condition(organizationId, transformId);
        List<StateMachineConfigDTO> validatorConfigs = validator(organizationId, transformId);
        ExecuteResult executeResult = new ExecuteResult(true, null, null);
        //调用对应服务，执行条件和验证，返回是否成功
        try {
            if (!conditionConfigs.isEmpty()) {
                inputDTO.setConfigs(conditionConfigs);
                executeResult = customFeignClientAdaptor.executeConfig(getExecuteConfigConditionURI(serviceCode, null, transform.getConditionStrategy()), inputDTO).getBody();
            }
            if (executeResult.getSuccess() && !validatorConfigs.isEmpty()) {
                inputDTO.setConfigs(validatorConfigs);
                executeResult = customFeignClientAdaptor.executeConfig(getExecuteConfigValidatorURI(serviceCode, null), inputDTO).getBody();
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
    public Boolean postAction(Long organizationId, String serviceCode, Long transformId, InputDTO inputDTO, StateContext<String, String> context) {
        List<StateMachineConfigDTO> configs = action(organizationId, transformId);
        inputDTO.setConfigs(configs);
        StateMachineTransform transform = transformMapper.queryById(organizationId, transformId);
        //节点转状态
        Long targetStatusId = nodeDeployMapper.getNodeDeployById(Long.parseLong(context.getTarget().getId())).getStatusId();
        if (targetStatusId == null) {
            throw new CommonException("error.postAction.targetStatusId.notNull");
        }
        ExecuteResult executeResult;
        //调用对应服务，执行动作，返回是否成功
        try {
            ResponseEntity<ExecuteResult> executeResultEntity = customFeignClientAdaptor.executeConfig(getExecuteConfigPostActionURI(serviceCode, targetStatusId, transform.getType()), inputDTO);
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

    @Override
    public Map<Long, Long> queryInitStatusIds(Long organizationId, List<Long> stateMachineIds) {
        if (!stateMachineIds.isEmpty()) {
            return nodeDeployMapper.queryByStateMachineIds(stateMachineIds, organizationId).stream()
                    .collect(Collectors.toMap(StateMachineNode::getStateMachineId, StateMachineNode::getStatusId));
        } else {
            return new HashMap<>();
        }
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
     * @param targetStatusId
     * @param conditionStrategy
     * @return
     */
    private URI getExecuteConfigConditionURI(String serviceCode, Long targetStatusId, String conditionStrategy) {
        URI uri = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceCode).append("/v1").append("/statemachine/execute_config_condition").append("?1=1");
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
     * @return
     */
    private URI getExecuteConfigValidatorURI(String serviceCode, Long targetStatusId) {
        URI uri = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceCode).append("/v1").append("/statemachine/execute_config_validator").append("?1=1");
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
     * @param targetStatusId
     * @return
     */
    private URI getExecuteConfigPostActionURI(String serviceCode, Long targetStatusId, String transformType) {
        URI uri = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(serviceCode).append("/v1").append("/statemachine/execute_config_action").append("?1=1");
        if (targetStatusId != null) {
            stringBuilder.append("&target_status_id=").append(targetStatusId);
        }
        if (transformType != null) {
            stringBuilder.append("&transform_type=").append(transformType);
        }
        String uriStr = stringBuilder.toString();
        LOGGER.debug("uri:{}", uriStr);
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            LOGGER.error(EXCEPTION, e);
        }
        return uri;
    }
}
