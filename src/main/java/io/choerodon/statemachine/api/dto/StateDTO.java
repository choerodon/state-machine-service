package io.choerodon.statemachine.api.dto;

/**
 * @author peng.jiang@hand-china.com
 */
public class StateDTO {

    private Long id;
    private String name;
    private String description;
    private String type;
    private Long organizationId;
    private Long objectVersionNumber;
    private Long associationMachine;

    private Boolean canDelete;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getAssociationMachine() {
        return associationMachine;
    }

    public void setAssociationMachine(Long associationMachine) {
        this.associationMachine = associationMachine;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }
}
