package io.choerodon.statemachine.api.service;

import io.choerodon.statemachine.api.dto.ExecuteResult;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import org.springframework.statemachine.StateContext;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/9/18
 */
public interface InstanceService {

    /**
     * 创建状态机实例，并返回初始状态
     *
     * @param serviceCode
     * @param stateMachineId
     * @param instanceId
     * @return
     */
    ExecuteResult startInstance(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId);

    /**
     * 执行状态转换，并返回转换后的状态
     *
     * @param stateMachineId 状态机Id
     * @param transformId       转换Id
     * @param currentStatusId 当前状态Id
     * @param instanceId     操作对象Id(cloopm-service: issueId)
     * @param serviceCode    请求服务code
     * @return
     */
    ExecuteResult executeTransform(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId, Long currentStatusId, Long transformId);

    /**
     * 获取当前状态拥有的转换列表，feign调用对应服务的条件验证
     *
     * @param organizationId
     * @param statusId
     * @return
     */
    List<StateMachineTransformDTO> queryListTransform(Long organizationId, String serviceCode, Long stateMachineId, Long instanceId, Long statusId);

    /**
     * 调用相应服务，验证转换
     *
     * @param organizationId
     * @param serviceCode
     * @param transformId
     * @param instanceId
     * @param context        状态机上下文，传递参数
     * @return
     */
    Boolean validatorGuard(Long organizationId, String serviceCode, Long transformId, Long instanceId, StateContext<String, String> context);

    /**
     * 调用相应服务，执行后置动作
     *
     * @param organizationId
     * @param serviceCode
     * @param transformId
     * @param instanceId
     * @param context        状态机上下文，传递参数
     * @return
     */
    Boolean postpositionAction(Long organizationId, String serviceCode, Long transformId, Long instanceId, StateContext<String, String> context);

    /**
     * 条件
     *
     * @param transformId 转换id
     * @return
     */
    List<StateMachineConfigDTO> condition(Long organizationId, Long transformId);

    /**
     * 验证器
     *
     * @param transformId 转换id
     * @return
     */
    List<StateMachineConfigDTO> validator(Long organizationId, Long transformId);

    /**
     * 触发器
     *
     * @param transformId 转换id
     * @return
     */
    List<StateMachineConfigDTO> trigger(Long organizationId, Long transformId);

    /**
     * 后置功能
     *
     * @param transformId 转换id
     * @return
     */
    List<StateMachineConfigDTO> postposition(Long organizationId, Long transformId);
}
