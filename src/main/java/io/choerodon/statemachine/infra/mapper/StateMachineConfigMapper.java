package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineConfig;
import org.apache.ibatis.annotations.Param;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineConfigMapper extends BaseMapper<StateMachineConfig> {

    int insertWithId(@Param("stateMachineConfig") StateMachineConfig stateMachineConfig);

    StateMachineConfig queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
