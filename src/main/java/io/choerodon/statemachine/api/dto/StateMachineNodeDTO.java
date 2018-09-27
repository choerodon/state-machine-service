package io.choerodon.statemachine.api.dto;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public class StateMachineNodeDTO {
    private Long id;
    private Long stateMachineId;
    private Long statusId;
    private Long positionX;
    private Long positionY;
    private Long width;
    private Long height;
    private Long objectVersionNumber;
    private String status;
    private Boolean allStateTransf;
    private Long organizationId;

    private StatusDTO stateDTO;

    private List<StateMachineTransfDTO> intoTransf;
    private List<StateMachineTransfDTO> outTransf;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStateMachineId() {
        return stateMachineId;
    }

    public void setStateMachineId(Long stateMachineId) {
        this.stateMachineId = stateMachineId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Long getPositionX() {
        return positionX;
    }

    public void setPositionX(Long positionX) {
        this.positionX = positionX;
    }

    public Long getPositionY() {
        return positionY;
    }

    public void setPositionY(Long positionY) {
        this.positionY = positionY;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public StatusDTO getStateDTO() {
        return stateDTO;
    }

    public void setStateDTO(StatusDTO stateDTO) {
        this.stateDTO = stateDTO;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<StateMachineTransfDTO> getIntoTransf() {
        return intoTransf;
    }

    public void setIntoTransf(List<StateMachineTransfDTO> intoTransf) {
        this.intoTransf = intoTransf;
    }

    public List<StateMachineTransfDTO> getOutTransf() {
        return outTransf;
    }

    public void setOutTransf(List<StateMachineTransfDTO> outTransf) {
        this.outTransf = outTransf;
    }

    public Boolean getAllStateTransf() {
        return allStateTransf;
    }

    public void setAllStateTransf(Boolean allStateTransf) {
        this.allStateTransf = allStateTransf;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
