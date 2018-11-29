package io.choerodon.statemachine.infra.feign.dto;

import io.choerodon.statemachine.domain.Status;

import java.util.List;

/**
 * @author shinan.chen
 * @since 2018/11/28
 */
public class AddStatusWithProject {
    private Long projectId;
    private List<Long> addStatusIds;
    private List<Status> addStatuses;

    public List<Long> getAddStatusIds() {
        return addStatusIds;
    }

    public void setAddStatusIds(List<Long> addStatusIds) {
        this.addStatusIds = addStatusIds;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<Status> getAddStatuses() {
        return addStatuses;
    }

    public void setAddStatuses(List<Status> addStatuses) {
        this.addStatuses = addStatuses;
    }
}
