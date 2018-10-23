package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/9/27
 */
public class StatusType {
    private StatusType() {
    }

    /**
     * 待处理
     */
    public static final String TODO = "todo";
    /**
     * 处理中
     */
    public static final String DOING = "doing";
    /**
     * 已完成
     */
    public static final String DONE = "done";
    /**
     * 无类型
     */
    public static final String NONE = "none";
}