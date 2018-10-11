package io.choerodon.statemachine.infra.factory;

import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.service.InstanceService;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineTransform;
import io.choerodon.statemachine.infra.enums.StateMachineStatus;
import io.choerodon.statemachine.infra.enums.TransformType;
import io.choerodon.statemachine.infra.mapper.StateMachineMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @date 2018/9/14
 */
@Component
public class MachineFactory {
    private static Logger logger = LoggerFactory.getLogger(MachineFactory.class);

    private static final String INSTANCE_ID = "instanceId";
    private static final String EXECUTE_RESULT = "executeResult";
    @Autowired
    private StateMachineService stateMachineService;
    @Autowired
    private StateMachineTransformService transformService;
    @Autowired
    private StateMachineNodeService nodeService;
    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;
    @Autowired
    private InstanceService instanceService;
    /**
     * 状态机id -> 状态机构建器
     */
    private static Map<Long, StateMachineBuilder.Builder<String, String>> builderMaps = new ConcurrentHashMap<>();

    /**
     * code【服务名:状态机id:实例id】 -> 状态机实例
     */
    private static Map<String, StateMachine<String, String>> stateMachineMap = new ConcurrentHashMap<>();

    private StateMachineBuilder.Builder<String, String> getBuilder(Long organizationId, String serviceCode, Long stateMachineId) {
        io.choerodon.statemachine.domain.StateMachine stateMachine = stateMachineService.queryDeployForInstance(organizationId, stateMachineId);
        List<StateMachineNode> nodes = stateMachine.getNodes();
        List<StateMachineTransform> transforms = stateMachine.getTransforms();
        Long initNodeId = nodeService.getInitNode(organizationId, stateMachineId);

        StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();
        try {
            builder.configureConfiguration()
                    .withConfiguration()
                    .machineId(stateMachineId.toString());
            builder.configureStates()
                    .withStates()
                    .initial(initNodeId.toString(), initialAction(organizationId, serviceCode))
                    .states(nodes.stream().map(x -> x.getId().toString()).collect(Collectors.toSet()));
            for (StateMachineTransform transform : transforms) {
                if (transform.getType().equals(TransformType.ALL)) {
                    //若配置了全部转换
                    for (StateMachineNode node : nodes) {
                        String event = transform.getId().toString();
                        String source = node.getId().toString();
                        String target = transform.getEndNodeId().toString();
                        builder.configureTransitions()
                                .withExternal()
                                .source(source).target(target)
                                .event(event)
                                .action(action(organizationId, serviceCode), errorAction(organizationId, serviceCode))
                                .guard(guard(organizationId, serviceCode));
                    }
                } else {
                    //转换都是通过id配置
                    String event = transform.getId().toString();
                    String source = transform.getStartNodeId().toString();
                    String target = transform.getEndNodeId().toString();
                    builder.configureTransitions()
                            .withExternal()
                            .source(source).target(target)
                            .event(event)
                            .action(action(organizationId, serviceCode), errorAction(organizationId, serviceCode))
                            .guard(guard(organizationId, serviceCode));
                }

            }
        } catch (Exception e) {
            logger.error("build StateMachineBuilder error,exception:{},stateMachineId:{}", e, stateMachineId);
        }
        return builder;
    }

    private StateMachine<String, String> buildInstance(Long organizationId, String serviceCode, Long stateMachineId) {
        StateMachineBuilder.Builder<String, String> builder = builderMaps.get(stateMachineId);
        if (builder == null) {
            builder = getBuilder(organizationId, serviceCode, stateMachineId);
            logger.info("build StateMachineBuilder successful,stateMachineId:{}", stateMachineId);
            builderMaps.put(stateMachineId, builder);
        }
        StateMachine<String, String> smInstance = builder.build();
        smInstance.start();
        return smInstance;
    }

    /**
     * 开始实例
     *
     * @param serviceCode
     * @param stateMachineId
     * @param instanceId
     * @return
     */
    public ExecuteResult startInstance(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId) {
        StateMachine<String, String> instance = buildInstance(organizationId, serviceCode, stateMachineId);
        //存入instanceId，以便执行guard和action
        instance.getExtendedState().getVariables().put(INSTANCE_ID, instanceId);
        //执行初始转换
        Long initTransformId = transformService.getInitTransform(organizationId, stateMachineId);
        instance.sendEvent(initTransformId.toString());

        ExecuteResult result = instance.getExtendedState().getVariables().get(EXECUTE_RESULT) == null ? new ExecuteResult(false, null, null) : (ExecuteResult) instance.getExtendedState().getVariables().get(EXECUTE_RESULT);
        return result;
    }

    /**
     * 状态转换
     *
     * @param serviceCode
     * @param stateMachineId
     * @param instanceId
     * @param currentStatusId
     * @param transformId
     * @return
     */
    public ExecuteResult executeTransform(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId, Long currentStatusId, Long transformId) {
        //状态转节点
        Long currentNodeId = nodeDeployMapper.getNodeDeployByStatusId(stateMachineId, currentStatusId).getId();

        String instanceCode = serviceCode + ":" + stateMachineId + ":" + instanceId;
        StateMachine<String, String> instance = stateMachineMap.get(instanceCode);
        if (instance == null) {
            instance = buildInstance(organizationId, serviceCode, stateMachineId);
            //恢复节点
            String id = instance.getId();
            instance.getStateMachineAccessor()
                    .doWithAllRegions(access ->
                            access.resetStateMachine(new DefaultStateMachineContext<>(currentNodeId.toString(), null, null, null, null, id)));
            //存入instanceId，以便执行guard和action
            instance.getExtendedState().getVariables().put(INSTANCE_ID, instanceId);
            logger.info("restore stateMachine instance successful,stateMachineId:{}", stateMachineId);
            stateMachineMap.put(instanceCode, instance);
        }
        //触发事件
        instance.sendEvent(transformId.toString());

        //节点转状态
        Long statusId = nodeDeployMapper.getNodeDeployById(Long.parseLong(instance.getState().getId())).getStatusId();
        Object executeResult = instance.getExtendedState().getVariables().get(EXECUTE_RESULT);
        if (executeResult == null) {
            executeResult = new ExecuteResult(true, statusId, null);
        }

        return (ExecuteResult) executeResult;
    }

    /**
     * 初始化动作
     *
     * @param serviceCode
     * @return
     */
    private Action<String, String> initialAction(Long organizationId, String serviceCode) {
        return new Action<String, String>() {
            @Override
            public void execute(StateContext<String, String> context) {
                logger.info("stateMachine instance execute initialAction:{}", context.getEvent());
//                instanceService.postposition()
            }
        };
    }

    /**
     * 转换动作
     *
     * @param serviceCode
     * @return
     */
    private Action<String, String> action(Long organizationId, String serviceCode) {
        return new Action<String, String>() {
            @Override
            public void execute(StateContext<String, String> context) {
                Long transformId = Long.parseLong(context.getEvent());
                Long instanceId = (Long) context.getExtendedState().getVariables().get(INSTANCE_ID);
                logger.info("stateMachine instance execute transformorm action,instanceId:{},transformId:{}", instanceId, transformId);
                Boolean result = instanceService.postpositionAction(organizationId, serviceCode, transformId, instanceId, context);
                if (!result) {
                    throw new CommonException("error.stateMachine.action");
                }
            }
        };
    }

    /**
     * 转换出错动作
     *
     * @param serviceCode
     * @return
     */
    private Action<String, String> errorAction(Long organizationId, String serviceCode) {
        return new Action<String, String>() {
            @Override
            public void execute(StateContext<String, String> context) {
                Long transformId = Long.parseLong(context.getEvent());
                Long instanceId = (Long) context.getExtendedState().getVariables().get(INSTANCE_ID);
                logger.error("stateMachine instance execute transformorm error,instanceId:{},transformId:{}", instanceId, transformId);
                // do something
            }
        };
    }

    /**
     * 条件验证是否转换
     *
     * @param serviceCode
     * @return
     */
    private Guard<String, String> guard(Long organizationId, String serviceCode) {
        return new Guard<String, String>() {
            @Override
            public boolean evaluate(StateContext<String, String> context) {
                Long transformId = Long.parseLong(context.getEvent());
                Long instanceId = (Long) context.getExtendedState().getVariables().get(INSTANCE_ID);
                logger.info("stateMachine instance execute transformorm guard,instanceId:{},transformId:{}", instanceId, transformId);
                return instanceService.validatorGuard(organizationId, serviceCode, transformId, instanceId, context);
            }
        };
    }

    /**
     * 清理内存中旧状态机构建器与实例
     */
    public void deployStateMachine(Long stateMachineId) {
        //清理旧状态机构建器
        builderMaps.remove(stateMachineId);
        //清理旧状态机实例
        for (Map.Entry<String, StateMachine<String, String>> entry : stateMachineMap.entrySet()) {
            Long entryStateMachineId = Long.parseLong(entry.getKey().split(":")[1]);
            if (entryStateMachineId.equals(stateMachineId)) {
                stateMachineMap.remove(entry.getKey());
            }
        }
    }
}
