package io.choerodon.statemachine.api.service;

import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.domain.StateMachineConfigDraft;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StateMachineConfigService extends BaseService<StateMachineConfigDraft> {

    /**
     * 创建配置（草稿）
     *
     * @param stateMachineId 状态机Id
     * @param configDTO      配置对象
     * @return
     */
    StateMachineConfigDTO create(Long organizationId, Long stateMachineId, Long transformId, StateMachineConfigDTO configDTO);

    /**
     * 删除配置
     *
     * @param configId
     * @return
     */
    Boolean delete(Long organizationId, Long configId);

    /**
     * 查询配置列表（草稿、活跃）
     *
     * @param transformId 转换id
     * @param type        配置类型
     * @param isDraft     是否草稿
     * @return
     */
    List<StateMachineConfigDTO> queryByTransformId(Long organizationId, Long transformId, String type, Boolean isDraft);

    /**
     * 批量获取转换的配置列表
     *
     * @param organizationId
     * @param transformIds
     * @param type
     * @return
     */
    List<StateMachineConfigDTO> queryDeployByTransformIds(Long organizationId, String type, List<Long> transformIds);
}
