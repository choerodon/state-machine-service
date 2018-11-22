package io.choerodon.statemachine.api.dto;

import java.util.List;

/**
 * @author shinan.chen
 * @since 2018/11/20
 */
public class StateMachineWithStatusDTO {

    private Long id;
    private String name;
    private String description;
    private String status;
    private Long organizationId;

    private List<StatusDTO> statusDTOS;

    public StateMachineWithStatusDTO() {
    }

    public StateMachineWithStatusDTO(Long id, String name, String description, String status, Long organizationId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
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

    public List<StatusDTO> getStatusDTOS() {
        return statusDTOS;
    }

    public void setStatusDTOS(List<StatusDTO> statusDTOS) {
        this.statusDTOS = statusDTOS;
    }
}
