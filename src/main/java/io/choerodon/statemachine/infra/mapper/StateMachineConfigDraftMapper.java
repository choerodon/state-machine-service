package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineConfigDraft;
import org.apache.ibatis.annotations.Param;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineConfigDraftMapper extends BaseMapper<StateMachineConfigDraft> {

    StateMachineConfigDraft queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
