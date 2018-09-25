package io.choerodon.statemachine.infra.enums;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineNodeStatus {
    /**
     * 默认初始化节点
     */
    String STATUS_DEFAULT = "2";
    /**
     * 默认初始化节点 开始节点 圆圈
     */
    String STATUS_START = "1";
    /**
     * 自定义  创建
     */
    String STATUS_CUSTOM = "0";
}
