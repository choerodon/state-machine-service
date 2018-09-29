package io.choerodon.statemachine.api.dto;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public class StateMachineConfigDTO {

    private Long id;
    private Long transformId;
    private Long stateMachineId;
    private String code;
    private String type;
    private Long organizationId;

    /**
     * 条件描述
     */
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransformId() {
        return transformId;
    }

    public void setTransformId(Long transformId) {
        this.transformId = transformId;
    }

    public Long getStateMachineId() {
        return stateMachineId;
    }

    public void setStateMachineId(Long stateMachineId) {
        this.stateMachineId = stateMachineId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
