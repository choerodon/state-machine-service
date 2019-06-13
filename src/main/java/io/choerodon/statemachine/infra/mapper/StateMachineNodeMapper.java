package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.Mapper;
import io.choerodon.statemachine.domain.StateMachineNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StateMachineNodeMapper extends Mapper<StateMachineNode> {

    StateMachineNode getNodeDeployById(@Param("nodeId") Long nodeId);

    StateMachineNode getNodeDeployByStatusId(@Param("stateMachineId") Long stateMachineId, @Param("statusId") Long statusId);


    List<StateMachineNode> selectByStateMachineId(@Param("stateMachineId") Long stateMachineId);

    Long checkStateDelete(@Param("organizationId") Long organizationId, @Param("statusId") Long statusId);

    StateMachineNode queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    List<StateMachineNode> queryInitByStateMachineIds(@Param("stateMachineIds") List<Long> stateMachineIds, @Param("organizationId") Long organizationId);

    /**
     * 获取最大的postionY
     *
     * @param stateMachineId
     * @return
     */
    StateMachineNode selectMaxPositionY(@Param("stateMachineId") Long stateMachineId);

    /**
     * 单独写更新，版本号不变，否则前端处理复杂
     */
    int updateAllStatusTransformId(@Param("organizationId") Long organizationId, @Param("id") Long id, @Param("allStatusTransformId") Long allStatusTransformId);

    List<StateMachineNode> queryByStateMachineIds(@Param("organizationId") Long organizationId, @Param("stateMachineIds") List<Long> stateMachineIds);

}
