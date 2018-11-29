package io.choerodon.statemachine.infra.feign.dto;

import java.util.List;

/**
 * @author shinan.chen
 * @since 2018/11/28
 */
public class ChangeStatus {
    private List<Long> addStatusIds;
    private List<Long> deleteStatusIds;

    public ChangeStatus(List<Long> addStatusIds, List<Long> deleteStatusIds) {
        this.addStatusIds = addStatusIds;
        this.deleteStatusIds = deleteStatusIds;
    }

    public ChangeStatus() {
    }

    public List<Long> getDeleteStatusIds() {
        return deleteStatusIds;
    }

    public void setDeleteStatusIds(List<Long> deleteStatusIds) {
        this.deleteStatusIds = deleteStatusIds;
    }

    public List<Long> getAddStatusIds() {
        return addStatusIds;
    }

    public void setAddStatusIds(List<Long> addStatusIds) {
        this.addStatusIds = addStatusIds;
    }
}
