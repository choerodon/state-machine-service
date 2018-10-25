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
     * 创建项目的默认状态机
     * @param organizationId
     * @param projectCode
     * @param statuses
     * @return
     */
    Long createStateMachine(Long organizationId, String projectCode, List<String> statuses);

    /**
     * 修复创建组织的状态
     * @param statusForMoveDataDOList
     * @return
     */
    Map<Long, List<Status>> createStatus(List<StatusForMoveDataDO> statusForMoveDataDOList);
}
