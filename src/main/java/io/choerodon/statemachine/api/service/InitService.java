package io.choerodon.statemachine.api.service;

import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.event.ProjectEvent;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/10/15
 */
public interface InitService {
    /**
     * 初始化状态
     *
     * @param organizationId
     */
    List<Status> initStatus(Long organizationId);

    /**
     * 初始化敏捷状态机
     *
     * @param organizationId
     * @param projectEvent
     * @return
     */
    Long initAGStateMachine(Long organizationId, ProjectEvent projectEvent);

    /**
     * 初始化测试状态机
     *
     * @param organizationId
     * @param projectEvent
     * @return
     */
    Long initTEStateMachine(Long organizationId, ProjectEvent projectEvent);

    void sendSagaToAgile(ProjectEvent projectEvent, Long stateMachineId);
}

