package io.choerodon.statemachine.infra.enums;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineStatus {
    /**
     * 草稿状态
     */
    String STATUS_DRAFT = "2";
    /**
     * 活跃状态
     */
    String STATUS_ACTIVE = "1";
    /**
     * 不活跃状态  新建
     */
    String STATUS_INACTIVE = "0";
}
