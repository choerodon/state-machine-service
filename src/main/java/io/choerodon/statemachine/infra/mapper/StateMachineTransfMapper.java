package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineTransf;
import org.apache.ibatis.annotations.Param;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineTransfMapper extends BaseMapper<StateMachineTransf> {

    /**
     * 删除节点时，删除关联的转换
     *
     * @param nodeId 节点id
     * @return
     */
    int deleteByNodeId(Long nodeId);

    /**
     * 删除草稿，原件写回草稿时,带上id写入数据
     *
     * @param stateMachineTransf
     * @return
     */
    int insertWithId(@Param("stateMachineTransf") StateMachineTransf stateMachineTransf);

    StateMachineTransf queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
