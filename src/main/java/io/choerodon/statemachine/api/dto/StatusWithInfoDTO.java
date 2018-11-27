package io.choerodon.statemachine.api.dto;

import io.choerodon.statemachine.domain.StateMachineInfo;

import java.util.List;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/11/27.
 * Email: fuqianghuang01@gmail.com
 */
public class StatusWithInfoDTO {

    private Long id;

    private String name;

    private String description;

    private String type;

    private Long organizationId;

    private Long objectVersionNumber;

    private List<StateMachineInfo> stateMachineInfoList;

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

    public void setStateMachineInfoList(List<StateMachineInfo> stateMachineInfoList) {
        this.stateMachineInfoList = stateMachineInfoList;
    }

    public List<StateMachineInfo> getStateMachineInfoList() {
        return stateMachineInfoList;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }
}
