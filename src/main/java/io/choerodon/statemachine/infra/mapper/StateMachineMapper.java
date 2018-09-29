package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.StateMachine;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public interface StateMachineMapper extends BaseMapper<StateMachine> {

    /**
     * 分页查询状态机
     *
     * @param stateMachine 状态机
     * @param param 模糊查询参数
     * @return 状态机列表
     */
    List<StateMachine> fulltextSearch(@Param("stateMachine") StateMachine stateMachine, @Param("param") String param);

    StateMachine queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
