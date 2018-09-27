package io.choerodon.statemachine.api.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.StateMachineDTO;
import io.choerodon.statemachine.domain.StateMachine;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineService extends BaseService<StateMachine> {

    /**
     * 分页查询状态机
     *
     * @param pageRequest     分页信息
     * @param stateMachineDTO 参数对象
     * @param params          模糊查询参数
     * @return 状态机列表
     */
    Page<StateMachineDTO> pageQuery(PageRequest pageRequest, StateMachineDTO stateMachineDTO, String params);

    /**
     * 创建状态机及配置
     *
     * @param organizationId  组织id
     * @param stateMachineDTO 状态机及配置对象
     * @return 状态机
     */
    StateMachineDTO create(Long organizationId, StateMachineDTO stateMachineDTO);

    /**
     * 更新状态机
     *
     * @param organizationId  组织id
     * @param stateMachineId  状态机id
     * @param stateMachineDTO 状态机对象
     * @return 更新状态机
     */
    StateMachineDTO update(Long organizationId, Long stateMachineId, StateMachineDTO stateMachineDTO);

    /**
     * 删除状态机
     *
     * @param organizationId 组织id
     * @param stateMachineId 状态机id
     * @return
     */
    Boolean delete(Long organizationId, Long stateMachineId);

    /**
     * 发布状态机
     *
     * @param organizationId 组织id
     * @param stateMachineId 状态机id
     * @return 发布状态机对象
     */
    StateMachineDTO deploy(Long organizationId, Long stateMachineId);

    /**
     * 获取状态机及配置
     *
     * @param stateMachineId 状态机id
     * @return
     */
    StateMachineDTO getStateMachineWithConfigById(Long organizationId, Long stateMachineId);

    /**
     * 获取状态机原件DTO
     *
     * @param stateMachineId 状态机id
     * @return
     */
    StateMachineDTO getOriginalDTOById(Long organizationId, Long stateMachineId);

    /**
     * 获取状态机原件
     *
     * @param stateMachineId 状态机id
     * @return
     */
    StateMachine getOriginalById(Long organizationId, Long stateMachineId);

    /**
     * 删除草稿
     *
     * @param stateMachineId 状态机Id
     * @return 状态机对象
     */
    StateMachineDTO deleteDraft(Long organizationId, Long stateMachineId);

    /**
     * 获取状态机
     *
     * @param stateMachineId 状态机id
     * @return
     */
    StateMachineDTO getStateMachineById(Long organizationId, Long stateMachineId);

    /**
     * 校验问题状态机名字是否未被使用
     *
     * @param organizationId 组织id
     * @param name           名称
     * @return
     */
    Boolean checkName(Long organizationId, Long stateMachineId, String name);

    /**
     * 获取所有状态机
     *
     * @param organizationId 组织id
     * @return 状态机列表
     */
    List<StateMachineDTO> queryAll(Long organizationId);

}
