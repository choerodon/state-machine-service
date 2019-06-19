package io.choerodon.statemachine.infra.mapper;

import io.choerodon.mybatis.common.Mapper;
import io.choerodon.statemachine.domain.StateMachineNodeDraft;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public interface StateMachineNodeDraftMapper extends Mapper<StateMachineNodeDraft> {

    StateMachineNodeDraft getNodeById(@Param("nodeId") Long nodeId);

    List<StateMachineNodeDraft> selectByStateMachineId(@Param("stateMachineId") Long stateMachineId);

    Long checkStateDelete(@Param("organizationId") Long organizationId, @Param("statusId") Long statusId);

    StateMachineNodeDraft queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    /**
     * 获取最大的postionY
     * @param stateMachineId
     * @return
     */
    StateMachineNodeDraft selectMaxPositionY(@Param("stateMachineId") Long stateMachineId);

    /**
     * 单独写更新，版本号不变，否则前端处理复杂
     */
    int updateAllStatusTransformId(@Param("organizationId") Long organizationId, @Param("id") Long id, @Param("allStatusTransformId") Long allStatusTransformId);
}
