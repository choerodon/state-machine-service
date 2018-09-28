package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachineTransform;
import org.apache.ibatis.annotations.Param;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineTransformMapper extends BaseMapper<StateMachineTransform> {
    StateMachineTransform queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
