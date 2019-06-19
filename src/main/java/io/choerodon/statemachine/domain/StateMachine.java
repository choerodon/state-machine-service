package io.choerodon.statemachine.domain;

import javax.persistence.*;
import java.util.List;

import io.choerodon.mybatis.entity.BaseDTO;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
@Table(name = "state_machine")
public class StateMachine extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String status;
    private Long organizationId;
    private Boolean isDefault;

    @Transient
    private List<StateMachineNode> nodes;
    @Transient
    private List<StateMachineTransform> transforms;

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

    public List<StateMachineNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<StateMachineNode> nodes) {
        this.nodes = nodes;
    }

    public List<StateMachineTransform> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<StateMachineTransform> transforms) {
        this.transforms = transforms;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }
}
