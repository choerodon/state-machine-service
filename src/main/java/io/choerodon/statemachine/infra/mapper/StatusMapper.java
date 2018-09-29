package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.Status;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public interface StatusMapper extends BaseMapper<Status> {

    /**
     * 分页查询状态
     *
     * @param status 状态对象
     * @param param 模糊查询参数
     * @return 状态列表
     */
    List<Status> fulltextSearch(@Param("status") Status status, @Param("param") String param);

    Status queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);
}
