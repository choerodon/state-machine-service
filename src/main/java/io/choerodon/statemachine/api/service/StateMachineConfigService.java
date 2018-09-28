package io.choerodon.statemachine.api.service;

import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.ConfigEnumDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.domain.StateMachineConfigDraft;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineConfigService extends BaseService<StateMachineConfigDraft> {

    /**
     * 创建配置（草稿）
     *
     * @param stateMachineId 状态机Id
     * @param configDTO      配置对象
     * @return
     */
    StateMachineConfigDTO create(Long organizationId, Long stateMachineId,Long transformId, StateMachineConfigDTO configDTO);

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
     * @param type     配置类型
     * @param isDraft  是否草稿
     * @return
     */
    List<StateMachineConfigDTO> queryByTransformId(Long organizationId, Long transformId, String type, Boolean isDraft);

    /**
     * 查询可配置对象的列表
     *
     * @param transformId
     * @param type
     * @return
     */
    List<ConfigEnumDTO> queryConfig(Long organizationId, Long transformId, String type);

    /**
     * 构建条件，验证等数据
     *
     * @param type
     * @return
     */
    List<ConfigEnumDTO> buildConfigEnum(String type);

}
