package io.choerodon.statemachine.infra.enums;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineTransfStatus {
    /**
     * 满足下列所有条件
     */
    String CONDITION_STRATEGY_ALL = "all";
    /**
     * 满足下列条件之一
     */
    String CONDITION_STRATEGY_ONE = "one";

}
