package io.choerodon.statemachine.domain;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
@ModifyAudit
@VersionAudit
@Table(name = "state_machine_node")
public class StateMachineNode extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    private Long stateMachineId;
    private Long statusId;
    private Long positionX;
    private Long positionY;
    private Long width;
    private Long height;
    private String type;
    private Long organizationId;
    /**
     * 所有状态都转换此状态的转换id
     */
    private Long allStatusTransformId;

    @Transient
    private Status state;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Status getState() {
        return state;
    }

    public void setState(Status state) {
        this.state = state;
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

    public Long getAllStatusTransformId() {
        return allStatusTransformId;
    }

    public void setAllStatusTransformId(Long allStatusTransformId) {
        this.allStatusTransformId = allStatusTransformId;
    }
}
