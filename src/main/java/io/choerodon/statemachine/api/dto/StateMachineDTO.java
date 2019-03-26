package io.choerodon.statemachine.api.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public class StateMachineDTO {
    @ApiModelProperty(value = "状态机id")
    private Long id;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "没事")
    private String description;
    @ApiModelProperty(value = "状态机状态（state_machine_draft/state_machine_active/state_machine_create）")
    private String status;
    @ApiModelProperty(value = "组织id")
    private Long organizationId;
    @ApiModelProperty(value = "是否默认状态机")
    private Boolean isDefault;
    @ApiModelProperty(value = "乐观锁")
    private Long objectVersionNumber;
    @ApiModelProperty(value = "状态机节点列表")
    private List<StateMachineNodeDTO> nodeDTOs;
    @ApiModelProperty(value = "状态机转换列表")
    private List<StateMachineTransformDTO> transformDTOs;

    public StateMachineDTO() {
    }

    public StateMachineDTO(String name, String description, Long organizationId) {
        this.name = name;
        this.description = description;
        this.organizationId = organizationId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public List<StateMachineNodeDTO> getNodeDTOs() {
        return nodeDTOs;
    }

    public void setNodeDTOs(List<StateMachineNodeDTO> nodeDTOs) {
        this.nodeDTOs = nodeDTOs;
    }

    public List<StateMachineTransformDTO> getTransformDTOs() {
        return transformDTOs;
    }

    public void setTransformDTOs(List<StateMachineTransformDTO> transformDTOs) {
        this.transformDTOs = transformDTOs;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }
}
