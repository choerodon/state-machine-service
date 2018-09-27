package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.State;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMapper extends BaseMapper<State> {

    /**
     * 分页查询状态
     *
     * @param state 状态对象
     * @param param 模糊查询参数
     * @return 状态列表
     */
    List<State> fulltextSearch(@Param("state") State state, @Param("param") String param);

    State queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
