package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/9/27
 */
public final class StatusType {
    /**
     * 待处理
     */
    public static final String TODO = "status_todo";
    /**
     * 处理中
     */
    public static final String DOING = "status_doing";
    /**
     * 已完成
     */
    public static final String DONE = "status_done";
    /**
     * 无类型
     */
    public static final String NONE = "status_none";
}