package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineTransfDeploy;
import org.apache.ibatis.annotations.Param;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineTransfDeployMapper extends BaseMapper<StateMachineTransfDeploy> {
    StateMachineTransfDeploy queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
