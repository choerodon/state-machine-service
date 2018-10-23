package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/10/15
 */
public enum InitTransform {
    TRANSTFORM0("初始化", null, "start", "create", TransformType.INIT, TransformConditionStrategy.ALL),
    TRANSTFORMALL1("全部转换到待处理", null, null, "create", TransformType.ALL, TransformConditionStrategy.ALL),
    TRANSTFORMALL2("全部转换到处理中", null, null, "processing", TransformType.ALL, TransformConditionStrategy.ALL),
    TRANSTFORMALL5("全部转换到已完成", null, null, "complete", TransformType.ALL, TransformConditionStrategy.ALL),
    ;
    private String name;
    private String description;
    private String startNodeCode;
    private String endNodeCode;
    private String type;
    private String conditionStrategy;

    InitTransform(String name, String description, String startNodeCode, String endNodeCode, String type, String conditionStrategy) {
        this.name = name;
        this.description = description;
        this.startNodeCode = startNodeCode;
        this.endNodeCode = endNodeCode;
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

    public String getStartNodeCode() {
        return startNodeCode;
    }

    public void setStartNodeCode(String startNodeCode) {
        this.startNodeCode = startNodeCode;
    }

    public String getEndNodeCode() {
        return endNodeCode;
    }

    public void setEndNodeCode(String endNodeCode) {
        this.endNodeCode = endNodeCode;
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
