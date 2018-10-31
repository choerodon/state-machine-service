package io.choerodon.statemachine.domain.event;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
public class DeployStatusPayload {
    private List<Long> projectIds;
    private List<StatusPayload> statusPayloads;

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }

    public List<StatusPayload> getStatusPayloads() {
        return statusPayloads;
    }

    public void setStatusPayloads(List<StatusPayload> statusPayloads) {
        this.statusPayloads = statusPayloads;
    }
}
