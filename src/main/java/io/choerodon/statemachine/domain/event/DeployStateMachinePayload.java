package io.choerodon.statemachine.domain.event;

import io.choerodon.statemachine.infra.feign.dto.AddStatusWithProject;
import io.choerodon.statemachine.infra.feign.dto.RemoveStatusWithProject;

import java.util.List;
import java.util.Map;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
public class DeployStateMachinePayload {
    private List<RemoveStatusWithProject> removeStatusWithProjects;
    private List<AddStatusWithProject> addStatusWithProjects;

    public List<RemoveStatusWithProject> getRemoveStatusWithProjects() {
        return removeStatusWithProjects;
    }

    public void setRemoveStatusWithProjects(List<RemoveStatusWithProject> removeStatusWithProjects) {
        this.removeStatusWithProjects = removeStatusWithProjects;
    }

    public List<AddStatusWithProject> getAddStatusWithProjects() {
        return addStatusWithProjects;
    }

    public void setAddStatusWithProjects(List<AddStatusWithProject> addStatusWithProjects) {
        this.addStatusWithProjects = addStatusWithProjects;
    }
}
