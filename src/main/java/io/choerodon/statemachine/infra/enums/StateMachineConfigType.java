package io.choerodon.statemachine.infra.enums;

/**
 * @author peng.jiang@hand-china.com
 */
public enum StateMachineConfigType {

    STATUS_CONDITION("condition"),  //条件
    STATUS_VALIDATOR("validator"),  //验证器
    STATUS_TRIGGER("trigger"),  //触发器
    STATUS_POSTPOSITION("postposition"); //后置功能

    private String value;

    StateMachineConfigType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
