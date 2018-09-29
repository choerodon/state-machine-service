package io.choerodon.statemachine.api.dto;

import java.util.List;

/**
 * @author peng.jiang,dinghuang123@gmail.com
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
    private String type;
    private Long allStatusTransformId;
    private Long organizationId;

    private StatusDTO stateDTO;

    /**
     * 前端要用到
     */
    private List<StateMachineTransformDTO> intoTransform;
    private List<StateMachineTransformDTO> outTransform;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<StateMachineTransformDTO> getIntoTransform() {
        return intoTransform;
    }

    public void setIntoTransform(List<StateMachineTransformDTO> intoTransform) {
        this.intoTransform = intoTransform;
    }

    public List<StateMachineTransformDTO> getOutTransform() {
        return outTransform;
    }

    public void setOutTransform(List<StateMachineTransformDTO> outTransform) {
        this.outTransform = outTransform;
    }

    public Long getAllStatusTransformId() {
        return allStatusTransformId;
    }

    public void setAllStatusTransformId(Long allStatusTransformId) {
        this.allStatusTransformId = allStatusTransformId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
