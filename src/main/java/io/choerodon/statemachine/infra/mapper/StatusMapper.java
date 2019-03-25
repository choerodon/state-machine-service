package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.api.dto.StatusSearchDTO;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.StatusWithInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StatusMapper extends BaseMapper<Status> {

    List<Long> selectStatusIds(@Param("organizationId") Long organizationId, @Param("statusSearchDTO") StatusSearchDTO statusSearchDTO);

    List<StatusWithInfo> queryStatusList(@Param("organizationId") Long organizationId, @Param("statusIds") List<Long> statusIds);

    Status queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    List<Status> batchStatusGet(@Param("ids") List<Long> ids);

    /**
     * 查询状态机下的所有状态
     *
     * @param organizationId
     * @param stateMachineIds
     * @return
     */
    List<Status> queryByStateMachineIds(@Param("organizationId") Long organizationId, @Param("stateMachineIds") List<Long> stateMachineIds);
}
