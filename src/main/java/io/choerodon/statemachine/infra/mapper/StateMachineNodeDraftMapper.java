package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineNodeDraft;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public interface StateMachineNodeDraftMapper extends BaseMapper<StateMachineNodeDraft> {

    StateMachineNodeDraft getNodeById(@Param("nodeId") Long nodeId);

    List<StateMachineNodeDraft> selectByStateMachineId(@Param("stateMachineId") Long stateMachineId);

    Long checkStateDelete(@Param("organizationId") Long organizationId, @Param("statusId") Long statusId);

    StateMachineNodeDraft queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
