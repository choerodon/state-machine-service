package io.choerodon.statemachine.api.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public class StateMachineNodeDTO {
    @ApiModelProperty(value = "状态机节点id")
    private Long id;
    @ApiModelProperty(value = "状态机id")
    private Long stateMachineId;
    @ApiModelProperty(value = "节点对应的状态id")
    private Long statusId;
    @ApiModelProperty(value = "X轴位置")
    private Long positionX;
    @ApiModelProperty(value = "Y轴位置")
    private Long positionY;
    @ApiModelProperty(value = "宽度")
    private Long width;
    @ApiModelProperty(value = "高度")
    private Long height;
    @ApiModelProperty(value = "乐观锁")
    private Long objectVersionNumber;
    @ApiModelProperty(value = "节点类型（node_init/node_start/node_custom）")
    private String type;
    @ApiModelProperty(value = "全部转换到当前节点的转换id")
    private Long allStatusTransformId;
    @ApiModelProperty(value = "组织id")
    private Long organizationId;
    @ApiModelProperty(value = "状态对象")
    private StatusDTO statusDTO;

    /**
     * 前端要用到
     */
    @ApiModelProperty(value = "转入该节点的转换列表")
    private List<StateMachineTransformDTO> intoTransform;
    @ApiModelProperty(value = "转出该节点的转换列表")
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

    public StatusDTO getStatusDTO() {
        return statusDTO;
    }

    public void setStatusDTO(StatusDTO statusDTO) {
        this.statusDTO = statusDTO;
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
