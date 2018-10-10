package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineConfigDraft;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StateMachineConfigDraftMapper extends BaseMapper<StateMachineConfigDraft> {

    StateMachineConfigDraft queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    List<StateMachineConfigDraft> queryWithCodeInfo(@Param("organizationId") Long organizationId, @Param("transformId") Long transformId, @Param("type") String type);
}
