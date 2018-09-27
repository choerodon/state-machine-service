package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineNodeDeploy;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineNodeDeployMapper extends BaseMapper<StateMachineNodeDeploy> {

    StateMachineNodeDeploy getNodeDeployById(@Param("nodeId") Long nodeId);

    List<StateMachineNodeDeploy> selectByStateMachineId(@Param("stateMachineId") Long stateMachineId);

    Long checkStateDelete(@Param("organizationId") Long organizationId, @Param("stateId") Long stateId);

    StateMachineNodeDeploy queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
