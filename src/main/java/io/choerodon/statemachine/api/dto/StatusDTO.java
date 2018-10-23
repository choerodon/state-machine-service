package io.choerodon.statemachine.api.dto;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public class StatusDTO {

    private Long id;
    private String name;
    /**
     * code是用来识别是否是初始化状态
     */
    private String code;
    private String description;
    private String type;
    private Long organizationId;
    private Long objectVersionNumber;

    public StatusDTO(){}

    public StatusDTO(String name, String description, String type, Long organizationId) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.organizationId = organizationId;
    }

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

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
