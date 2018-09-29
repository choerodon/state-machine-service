package io.choerodon.statemachine.api.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.StatusDTO;
import io.choerodon.statemachine.domain.Status;

import java.util.List;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public interface StatusService {

    /**
     * 分页查询状态列表
     * @param pageRequest 分页对象
     * @param statusDTO 参数对象
     * @param param 模糊查询参数
     * @return 状态列表
     */
    public Page<StatusDTO> pageQuery(PageRequest pageRequest, StatusDTO statusDTO, String param);

    /**
     * 创建状态
     * @param organizationId 组织id
     * @param statusDTO 状态对象
     * @return 状态对象
     */
    StatusDTO create(Long organizationId, StatusDTO statusDTO);

    /**
     * 更新状态
     * @param statusDTO 更新对象
     * @return 更新对象
     */
    StatusDTO update(StatusDTO statusDTO);

    /**
     * 删除状态
     * @param organizationId 组织id
     * @param statusId 状态机id
     * @return
     */
    Boolean delete(Long organizationId, Long statusId);

    /**
     * 根据id获取状态对象
     * @param organizationId 组织id
     * @param statusId 状态id
     * @return
     */
    StatusDTO queryStateById(Long organizationId, Long statusId);

    /**
     * 获取所有
     * @param organizationId 组织id
     * @return
     */
    List<StatusDTO> queryAllState(Long organizationId);

    /**
     * 校验状态名字是否未被使用
     * @param organizationId 组织id
     * @param statusId 状态
     * @param name 名称
     * @return
     */
    Boolean checkName(Long organizationId, Long statusId, String name);

    void initSystemStateMachineDetail(Long organizationId, Long stateMachineId, Long startNodeId);

}
