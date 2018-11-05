package io.choerodon.statemachine.fixdata.service;

import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.fixdata.dto.StatusForMoveDataDO;

import java.util.List;
import java.util.Map;

/**
 * @author shinan.chen
 * @date 2018/10/25
 */
public interface FixDataService {
    /**
     * 创建项目的默认敏捷状态机和测试状态机
     * @param organizationId
     * @param projectCode
     * @param statuses
     * @return
     */
    Map<String, Long> createAGStateMachineAndTEStateMachine(Long organizationId, String projectCode, List<String> statuses);

    /**
     * 修复创建组织的状态
     * @param statusForMoveDataDOList
     * @return
     */
    Boolean createStatus(List<StatusForMoveDataDO> statusForMoveDataDOList);

    /**
     * 获取所有组织的所有状态
     *
     * @return
     */
    Map<Long, List<Status>> queryAllStatus();
}
