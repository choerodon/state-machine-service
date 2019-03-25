package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineTransform;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StateMachineTransformMapper extends BaseMapper<StateMachineTransform> {
    /**
     * 删除节点时，删除关联的转换
     *
     * @param nodeId 节点id
     * @return
     */
    int deleteByNodeId(Long nodeId);

    StateMachineTransform queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    /**
     * 获取某个节点拥有的转换（包括全部转换）
     * @param organizationId
     * @param stateMachineId
     * @param startNodeId
     * @param transformType
     * @return
     */
    List<StateMachineTransform> queryByStartNodeIdOrType(@Param("organizationId") Long organizationId, @Param("stateMachineId") Long stateMachineId, @Param("startNodeId") Long startNodeId,@Param("transformType") String transformType);
    
    List<StateMachineTransform> queryByStateMachineIds(@Param("organizationId") Long organizationId, @Param("stateMachineIds") List<Long> stateMachineIds);
}
