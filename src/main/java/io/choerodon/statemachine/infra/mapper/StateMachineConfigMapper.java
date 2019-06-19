package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.Mapper;
import io.choerodon.statemachine.domain.StateMachineConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StateMachineConfigMapper extends Mapper<StateMachineConfig> {
    StateMachineConfig queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    List<StateMachineConfig> queryWithCodeInfo(@Param("organizationId") Long organizationId, @Param("transformId") Long transformId, @Param("type") String type);

    List<StateMachineConfig> queryWithCodeInfoByTransformIds(@Param("organizationId") Long organizationId, @Param("type") String type, @Param("transformIds") List<Long> transformIds);
}
