package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.api.dto.StatusDTO;
import io.choerodon.statemachine.api.dto.StatusSearchDTO;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.StatusWithInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StatusMapper extends BaseMapper<Status> {

    /**
     * 分页查询状态
     *
     * @param status 状态对象
     * @param param  模糊查询参数
     * @return 状态列表
     */
    List<Status> fulltextSearch(@Param("status") Status status, @Param("param") String param);

    List<StatusWithInfo> queryStatusList(@Param("organizationId") Long organizationId, @Param("statusSearchDTO") StatusSearchDTO statusSearchDTO);

    Status queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    List<Status> batchStatusGet(@Param("ids") List<Long> ids);

    void batchInsert(@Param("statusList") List<Status> statusList);
    /**
     * 查询状态机下的所有状态
     *
     * @param organizationId
     * @param stateMachineIds
     * @return
     */
    List<Status> queryByStateMachineIds(@Param("organizationId") Long organizationId, @Param("stateMachineIds") List<Long> stateMachineIds);
}
