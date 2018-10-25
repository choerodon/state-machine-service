package io.choerodon.statemachine.api.service;

import io.choerodon.statemachine.domain.Status;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/10/15
 */
public interface InitService {
    /**
     * 初始化状态
     * @param organizationId
     */
    List<Status> initStatus(Long organizationId);

    /**
     * 创建敏捷项目时初始化项目的状态机，并发布
     * @param organizationId
     * @param projectCode
     * @return
     */
    Long initAGStateMachine(Long organizationId, String projectCode);
}

