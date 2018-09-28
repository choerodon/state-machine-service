package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineTransformDeploy;
import org.apache.ibatis.annotations.Param;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineTransformDeployMapper extends BaseMapper<StateMachineTransformDeploy> {
    StateMachineTransformDeploy queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
