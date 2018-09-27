package io.choerodon.statemachine.api.dto;

import javax.persistence.Column;
import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public class StateMachineTransfDTO {
    private Long id;
    private String name;
    private String description;
    private Long stateMachineId;
    private Long startNodeId;
    private Long endNodeId;
    private String url; //页面方案id
    private Long objectVersionNumber;
    private String status;
    private String style;
    private String conditionStrategy;
    private Long organizationId;

    //是否所有状态都转换此状态
    private Boolean allStateTransf;

    private StateMachineNodeDTO startNodeDTO;
    private StateMachineNodeDTO endNodeDTO;
    private List<StateMachineConfigDTO> conditions;       //配置的条件
    private List<StateMachineConfigDTO> validators;       //配置的验证器
    private List<StateMachineConfigDTO> triggers;         //配置的触发器
    private List<StateMachineConfigDTO> postpositions;    //配置的后置处理

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

    public Long getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(Long startNodeId) {
        this.startNodeId = startNodeId;
    }

    public Long getEndNodeId() {
        return endNodeId;
    }

    public void setEndNodeId(Long endNodeId) {
        this.endNodeId = endNodeId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getStateMachineId() {
        return stateMachineId;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public void setStateMachineId(Long stateMachineId) {
        this.stateMachineId = stateMachineId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public List<StateMachineConfigDTO> getConditions() {
        return conditions;
    }

    public void setConditions(List<StateMachineConfigDTO> conditions) {
        this.conditions = conditions;
    }

    public List<StateMachineConfigDTO> getValidators() {
        return validators;
    }

    public void setValidators(List<StateMachineConfigDTO> validators) {
        this.validators = validators;
    }

    public List<StateMachineConfigDTO> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<StateMachineConfigDTO> triggers) {
        this.triggers = triggers;
    }

    public List<StateMachineConfigDTO> getPostpositions() {
        return postpositions;
    }

    public void setPostpositions(List<StateMachineConfigDTO> postpositions) {
        this.postpositions = postpositions;
    }

    public StateMachineNodeDTO getStartNodeDTO() {
        return startNodeDTO;
    }

    public void setStartNodeDTO(StateMachineNodeDTO startNodeDTO) {
        this.startNodeDTO = startNodeDTO;
    }

    public StateMachineNodeDTO getEndNodeDTO() {
        return endNodeDTO;
    }

    public void setEndNodeDTO(StateMachineNodeDTO endNodeDTO) {
        this.endNodeDTO = endNodeDTO;
    }

    public String getConditionStrategy() {
        return conditionStrategy;
    }

    public void setConditionStrategy(String conditionStrategy) {
        this.conditionStrategy = conditionStrategy;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Boolean getAllStateTransf() {
        return allStateTransf;
    }

    public void setAllStateTransf(Boolean allStateTransf) {
        this.allStateTransf = allStateTransf;
    }
}
