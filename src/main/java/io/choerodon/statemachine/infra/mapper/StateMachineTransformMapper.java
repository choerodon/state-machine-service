package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineTransform;
import org.apache.ibatis.annotations.Param;

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
     * 修复0.12.0发布后，敏捷移除节点，但没移除转换
     *
     * @param organizationId
     * @return
     */
    int fixDeleteIllegalTransforms(@Param("organizationId") Long organizationId);
}
