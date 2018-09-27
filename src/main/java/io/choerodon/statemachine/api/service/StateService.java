package io.choerodon.statemachine.api.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.StateDTO;
import io.choerodon.statemachine.domain.State;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateService extends BaseService<State> {

    /**
     * 分页查询状态列表
     * @param pageRequest 分页对象
     * @param stateDTO 参数对象
     * @param param 模糊查询参数
     * @return 状态列表
     */
    public Page<StateDTO> pageQuery(PageRequest pageRequest, StateDTO stateDTO, String param);

    /**
     * 创建状态
     * @param organizationId 组织id
     * @param stateDTO 状态对象
     * @return 状态对象
     */
    StateDTO create(Long organizationId, StateDTO stateDTO);

    /**
     * 更新状态
     * @param stateDTO 更新对象
     * @return 更新对象
     */
    StateDTO update(StateDTO stateDTO);

    /**
     * 删除状态
     * @param organizationId 组织id
     * @param stateId 状态机id
     * @return
     */
    Boolean delete(Long organizationId, Long stateId);

    /**
     * 根据id获取状态对象
     * @param organizationId 组织id
     * @param stateId 状态id
     * @return
     */
    StateDTO queryStateById(Long organizationId, Long stateId);

    /**
     * 获取所有
     * @param organizationId 组织id
     * @return
     */
    List<StateDTO> queryAllState(Long organizationId);

    /**
     * 校验状态名字是否未被使用
     * @param organizationId 组织id
     * @param stateId 状态
     * @param name 名称
     * @return
     */
    Boolean checkName(Long organizationId, Long stateId, String name);

}
