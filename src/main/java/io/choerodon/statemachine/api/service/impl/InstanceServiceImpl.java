package io.choerodon.statemachine.api.service.impl;

import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.api.service.InstanceService;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.statemachine.api.service.StateMachineTransfService;
import io.choerodon.statemachine.infra.enums.StateMachineConfigType;
import io.choerodon.statemachine.infra.factory.MachineFactory;
import io.choerodon.statemachine.infra.feign.CustomFeignClientAdaptor;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineTransfMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author shinan.chen
 * @date 2018/9/18
 */
@Component
@Import(FeignClientsConfiguration.class)
public class InstanceServiceImpl implements InstanceService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceServiceImpl.class);
    @Autowired
    private StateMachineNodeMapper nodeMapper;
    @Autowired
    private StateMachineTransfMapper transfMapper;
    @Autowired
    private StateMachineConfigService configService;
    @Autowired
    private StateMachineTransfService transfService;
    @Autowired
    private MachineFactory machineFactory;

    private static final String METHOD_FILTER_TRANSF = "config_filter";
    private static final String METHOD_EXECUTE_CONFIG = "execute_config";
    /**
     * 自定义FeignClient客户端
     */
    CustomFeignClientAdaptor customFeignClientAdaptor;

    @Autowired
    public InstanceServiceImpl(Client client, Decoder decoder, Encoder encoder, RequestInterceptor interceptor) {
        customFeignClientAdaptor = Feign.builder().encoder(encoder).decoder(decoder)
                .client(client)
                .contract(new Contract.Default())
                .requestInterceptor(interceptor)
                .target(Target.EmptyTarget.create(CustomFeignClientAdaptor.class));
    }

    @Override
    public ExecuteResult startInstance(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId) {
        ExecuteResult executeResult;
        try {
            executeResult = machineFactory.startInstance(organizationId, serviceCode, stateMachineId, instanceId);
        } catch (CommonException e) {
            e.printStackTrace();
            executeResult = new ExecuteResult(false, null, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            executeResult = new ExecuteResult(false, null, e.getMessage());
        }
        return executeResult;
    }

    @Override
    public ExecuteResult executeTransf(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId, Long currentStateId, Long transfId) {
        ExecuteResult executeResult;
        try {
            executeResult = machineFactory.executeTransf(organizationId, serviceCode, stateMachineId, instanceId, currentStateId, transfId);
        } catch (CommonException e) {
            executeResult = new ExecuteResult(false, null, e.getMessage());
        } catch (Exception e) {
            executeResult = new ExecuteResult(false, null, e.getMessage());
        }
        return executeResult;
    }

    @Override
    public List<StateMachineTransfDTO> transfList(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId, Long stateId) {
        List<StateMachineTransfDTO> list = transfService.queryListByStateId(organizationId, stateMachineId, stateId);
        //获取转换的条件配置
        for (StateMachineTransfDTO transfDTO : list) {
            transfDTO.setConditions(condition(organizationId,transfDTO.getId()));
        }
        //调用对应服务，根据条件校验转换，过滤掉可用的转换
        list = list == null ? Collections.emptyList() : list;
        try {
            ResponseEntity<List<StateMachineTransfDTO>> listEntity = customFeignClientAdaptor.filterTransfsByConfig(getURI(serviceCode, organizationId, METHOD_FILTER_TRANSF, instanceId, null, null, null), list);
            list = listEntity.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            list = Collections.emptyList();
        }
        return list;
    }

    @Override
    public Boolean validatorGuard(Long organizationId, String serviceCode, Long transfId, Long instanceId, StateContext<String, String> context) {
        List<StateMachineConfigDTO> configs = validator(organizationId,transfId);
        ExecuteResult executeResult = new ExecuteResult(false, null, "error.customFeignClientAdaptor.executeConfig.validatorGuard");
        //调用对应服务，执行验证，返回是否成功
        try {
            ResponseEntity<ExecuteResult> executeResultEntity = customFeignClientAdaptor.executeConfig(getURI(serviceCode, organizationId, METHOD_EXECUTE_CONFIG, instanceId, null, null, StateMachineConfigType.STATUS_VALIDATOR.value()), configs);
            //返回为空则调用对应服务，对应服务方法报错
            if (executeResultEntity.getBody().getSuccess() != null) {
                executeResult = executeResultEntity.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<Object, Object> variables = context.getExtendedState().getVariables();
        variables.put("executeResult", executeResult);
        return executeResult.getSuccess();
    }

    @Override
    public Boolean postpositionAction(Long organizationId, String serviceCode, Long transfId, Long instanceId, StateContext<String, String> context) {
        List<StateMachineConfigDTO> configs = postposition(organizationId,transfId);
        //节点转状态
        Long targetStateId = nodeMapper.getNodeById(Long.parseLong(context.getTarget().getId())).getStateId();

        ExecuteResult executeResult = new ExecuteResult(false, null, "error.customFeignClientAdaptor.executeConfig.postpositionAction");
        //调用对应服务，执行动作，返回是否成功
        try {
            ResponseEntity<ExecuteResult> executeResultEntity = customFeignClientAdaptor.executeConfig(getURI(serviceCode, organizationId, METHOD_EXECUTE_CONFIG, instanceId, targetStateId, null, StateMachineConfigType.STATUS_POSTPOSITION.value()), configs);
            //返回为空则调用对应服务，对应服务方法报错
            if (executeResultEntity.getBody().getSuccess() != null) {
                executeResult = executeResultEntity.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<Object, Object> variables = context.getExtendedState().getVariables();
        variables.put("executeResult", executeResult);
        return executeResult.getSuccess();
    }

    @Override
    public List<StateMachineConfigDTO> condition(Long organizationId, Long transfId) {
        List<StateMachineConfigDTO> configs = configService.queryByTransfId(organizationId, transfId, StateMachineConfigType.STATUS_CONDITION.value());
        if (configs == null) {
            return Collections.emptyList();
        }
        return configs;
    }

    @Override
    public List<StateMachineConfigDTO> validator(Long organizationId, Long transfId) {
        List<StateMachineConfigDTO> configs = configService.queryByTransfId(organizationId, transfId, StateMachineConfigType.STATUS_VALIDATOR.value());
        if (configs == null) {
            return Collections.emptyList();
        }
        return configs;
    }

    @Override
    public List<StateMachineConfigDTO> trigger(Long organizationId, Long transfId) {
        List<StateMachineConfigDTO> configs = configService.queryByTransfId(organizationId, transfId, StateMachineConfigType.STATUS_TRIGGER.value());
        if (configs == null) {
            return Collections.emptyList();
        }
        return configs;
    }

    @Override
    public List<StateMachineConfigDTO> postposition(Long organizationId, Long transfId) {
        List<StateMachineConfigDTO> configs = configService.queryByTransfId(organizationId, transfId, StateMachineConfigType.STATUS_POSTPOSITION.value());
        if (configs == null) {
            return Collections.emptyList();
        }
        return configs;
    }

    /**
     * 测试专用
     *
     * @throws URISyntaxException
     */
    @Override
    public void test() throws URISyntaxException {
//        customFeignClientAdaptor.action(new URI("http://cloopm-service/v1/projects/26/issue/9"));
        customFeignClientAdaptor.executeConfig(getURI("cloopm-service", 1L, METHOD_EXECUTE_CONFIG, 1L, 1L, null, StateMachineConfigType.STATUS_VALIDATOR.value()), new ArrayList<>());

    }

    private URI getURI(String serviceCode, Long organizationId, String method, Long instanceId, Long targetStateId, String conditionStrategy, String type) {
        //接口模版：http://cloopm-service/v1/organizations/1/state_machine/config_filter?instance_id=1&type=sd
        try {
            StringBuffer uri = new StringBuffer("http://" + serviceCode + "/v1/organizations/" + organizationId + "/state_machine/" + method + "?1=1");
            if (instanceId != null) {
                uri.append("&instance_id=" + instanceId);
            }
            if (targetStateId != null) {
                uri.append("&target_state_id=" + targetStateId);
            }
            if (type != null) {
                uri.append("&type=" + type);
            }
            if (conditionStrategy != null) {
                uri.append("&condition_strategy=" + conditionStrategy);
            }
            logger.info("uri:{}", uri.toString());
            return new URI(uri.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
