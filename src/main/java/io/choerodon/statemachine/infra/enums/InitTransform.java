package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/10/15
 */
public enum InitTransform {
    TRANSTFORM0("初始化", null, "初始节点", "待处理", TransformType.INIT, TransformConditionStrategy.ALL),
    TRANSTFORM1("处理", null, "待处理", "处理中", TransformType.CUSTOM, TransformConditionStrategy.ALL),
    TRANSTFORM2("测试", null, "处理中", "测试中", TransformType.CUSTOM, TransformConditionStrategy.ALL),
    TRANSTFORM3("验证", null, "测试中", "验证中", TransformType.CUSTOM, TransformConditionStrategy.ALL),
    TRANSTFORM4("完成", null, "验证中", "已完成", TransformType.CUSTOM, TransformConditionStrategy.ALL),
    TRANSTFORMALL1("全部转换到待处理", null, null, "待处理", TransformType.ALL, TransformConditionStrategy.ALL),
    TRANSTFORMALL2("全部转换到处理中", null, null, "处理中", TransformType.ALL, TransformConditionStrategy.ALL),
    TRANSTFORMALL3("全部转换到测试中", null, null, "测试中", TransformType.ALL, TransformConditionStrategy.ALL),
    TRANSTFORMALL4("全部转换到验证中", null, null, "验证中", TransformType.ALL, TransformConditionStrategy.ALL),
    TRANSTFORMALL5("全部转换到已完成", null, null, "已完成", TransformType.ALL, TransformConditionStrategy.ALL),
    ;
    private String name;
    private String description;
    private String startNodeName;
    private String endNodeName;
    private String type;
    private String conditionStrategy;

    InitTransform(String name, String description, String startNodeName, String endNodeName, String type, String conditionStrategy) {
        this.name = name;
        this.description = description;
        this.startNodeName = startNodeName;
        this.endNodeName = endNodeName;
        this.type = type;
        this.conditionStrategy = conditionStrategy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartNodeName() {
        return startNodeName;
    }

    public void setStartNodeName(String startNodeName) {
        this.startNodeName = startNodeName;
    }

    public String getEndNodeName() {
        return endNodeName;
    }

    public void setEndNodeName(String endNodeName) {
        this.endNodeName = endNodeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConditionStrategy() {
        return conditionStrategy;
    }

    public void setConditionStrategy(String conditionStrategy) {
        this.conditionStrategy = conditionStrategy;
    }
}
