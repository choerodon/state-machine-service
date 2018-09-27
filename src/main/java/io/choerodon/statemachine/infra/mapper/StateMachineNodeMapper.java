package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineNodeMapper extends BaseMapper<StateMachineNode> {

    StateMachineNode getNodeById(@Param("nodeId") Long nodeId);

    StateMachineNode getNodeByStateId(@Param("stateMachineId") Long stateMachineId, @Param("stateId") Long stateId);

    List<StateMachineNode> selectByStateMachineId(@Param("stateMachineId") Long stateMachineId);

    Long checkStateDelete(@Param("organizationId") Long organizationId, @Param("stateId") Long stateId);

    int insertWithId(@Param("stateMachineNode") StateMachineNode stateMachineNode);

    StateMachineNode queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
