package io.choerodon.statemachine.infra.enums;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public interface StateMachineTransformStatus {
    /**
     * 满足下列所有条件
     */
    String CONDITION_STRATEGY_ALL = "all";
    /**
     * 满足下列条件之一
     */
    String CONDITION_STRATEGY_ONE = "one";

}
