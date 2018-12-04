package io.choerodon.statemachine.infra.cache;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shinan.chen
 * @since 2018/12/3
 */
@Component
public class InstanceCache {
    /**
     * 状态机id -> 状态机构建器
     */
    private static Map<Long, StateMachineBuilder.Builder<String, String>> builderMap = new ConcurrentHashMap<>();

    /**
     * 服务名 -> 状态机实例key的list
     */
    private static Map<String, Set<String>> serviceMap = new ConcurrentHashMap<>();

    /**
     * 状态机id -> 状态机实例key的list
     */
    private static Map<Long, Set<String>> stateMachineMap = new ConcurrentHashMap<>();

    /**
     * key【服务名:状态机id:实例id】 -> 状态机实例
     */
    private static Map<String, StateMachine<String, String>> instanceMap = new ConcurrentHashMap<>();

    /**
     * 清除单个实例
     */
    public void cleanInstance(String serviceCode, Long stateMachineId, Long instanceId) {
        String key = serviceCode + ":" + stateMachineId + ":" + instanceId;
        instanceMap.remove(key);
    }

    /**
     * 清除某个状态机的所有实例
     */
    public void cleanStateMachine(Long stateMachineId) {
        builderMap.remove(stateMachineId);
        Set<String> instanceKeys = stateMachineMap.get(stateMachineId);
        stateMachineMap.remove(stateMachineId);
        instanceKeys.forEach(key -> instanceMap.remove(key));
    }

    /**
     * 缓存状态机构建器
     */
    public void putBuilder(Long stateMachineId, StateMachineBuilder.Builder<String, String> builder) {
        builderMap.put(stateMachineId, builder);
    }

    /**
     * 缓存状态机实例
     */
    public void putInstance(String serviceCode, Long stateMachineId, Long instanceId, StateMachine<String, String> stateMachineInstance) {
        String key = serviceCode + ":" + stateMachineId + ":" + instanceId;
        instanceMap.put(key, stateMachineInstance);
        Set<String> instanceKeys = stateMachineMap.get(stateMachineId);
        instanceKeys.add(key);
        Set<String> serviceInstanceKeys = serviceMap.get(stateMachineId);
        serviceInstanceKeys.add(key);
    }

    /**
     * 获取状态机构建器
     */
    public StateMachineBuilder.Builder<String, String> getBuilder(Long stateMachineId) {
        return builderMap.get(stateMachineId);
    }

    /**
     * 获取单个实例
     */
    public StateMachine<String, String> getInstance(String serviceCode, Long stateMachineId, Long instanceId) {
        String key = serviceCode + ":" + stateMachineId + ":" + instanceId;
        return instanceMap.get(key);
    }
}
