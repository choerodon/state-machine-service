package io.choerodon.statemachine.fixdata.dto;

/**
 * @author shinan.chen
 * @date 2018/10/25
 */
public class FixTransform {
    private String name;
    private String startNodeName;
    private String description;
    private String endNodeName;
    private String type;

    public FixTransform(String name, String startNodeName, String description, String endNodeName, String type) {
        this.name = name;
        this.startNodeName = startNodeName;
        this.description = description;
        this.endNodeName = endNodeName;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartNodeName() {
        return startNodeName;
    }

    public void setStartNodeName(String startNodeName) {
        this.startNodeName = startNodeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
