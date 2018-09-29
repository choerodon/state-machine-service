package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineTransformDraft;
import org.apache.ibatis.annotations.Param;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public interface StateMachineTransformDraftMapper extends BaseMapper<StateMachineTransformDraft> {

    /**
     * 删除节点时，删除关联的转换
     *
     * @param nodeId 节点id
     * @return
     */
    int deleteByNodeId(Long nodeId);

    StateMachineTransformDraft queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
