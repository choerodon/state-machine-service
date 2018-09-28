package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/9/27
 */
public class TransformType {
    /**
     * 默认初始化转换
     */
    public static final String INIT = "transformorm_init";
    /**
     * 全部节点都转换到某个节点的转换
     */
    public static final String ALL = "transformorm_all";
    /**
     * 自定义转换
     */
    public static final String CUSTOM = "transformorm_custom";
}
