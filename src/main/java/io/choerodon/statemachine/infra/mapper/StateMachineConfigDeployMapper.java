package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineConfigDeploy;
import org.apache.ibatis.annotations.Param;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineConfigDeployMapper extends BaseMapper<StateMachineConfigDeploy> {
    StateMachineConfigDeploy queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
