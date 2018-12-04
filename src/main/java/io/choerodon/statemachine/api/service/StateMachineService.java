package io.choerodon.statemachine.api.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.StateMachineDTO;
import io.choerodon.statemachine.api.dto.StateMachineWithStatusDTO;
import io.choerodon.statemachine.domain.StateMachine;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
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
    Boolean deploy(Long organizationId, Long stateMachineId, Boolean isStartSaga);

    /**
     * 获取状态机及配置（草稿、活跃）
     *
     * @param organizationId
     * @param stateMachineId
     * @param isDraft        是否为草稿
     * @return
     */
    StateMachineDTO queryStateMachineWithConfigById(Long organizationId, Long stateMachineId, Boolean isDraft);

    /**
     * 获取状态机及配置，用于内部状态机实例构建
     *
     * @param stateMachineId 状态机id
     * @return
     */
    StateMachine queryDeployForInstance(Long organizationId, Long stateMachineId);

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
    StateMachineDTO queryStateMachineById(Long organizationId, Long stateMachineId);

    /**
     * 获取组织默认状态机
     *
     * @param organizationId
     * @return
     */
    StateMachineDTO queryDefaultStateMachine(Long organizationId);

    /**
     * 校验问题状态机名字是否未被使用
     *
     * @param organizationId 组织id
     * @param name           名称
     * @return
     */
    Boolean checkName(Long organizationId, String name);

    /**
     * 获取所有状态机
     *
     * @param organizationId 组织id
     * @return 状态机列表
     */
    List<StateMachineDTO> queryAll(Long organizationId);

    /**
     * 修改状态机状态
     * 活跃 -> 草稿
     *
     * @param organizationId organizationId
     * @param stateMachineId stateMachineId
     */
    void updateStateMachineStatus(Long organizationId, Long stateMachineId);

    /**
     * 批量活跃状态机
     *
     * @param organizationId
     * @param stateMachineIds
     * @return
     */
    Boolean activeStateMachines(Long organizationId, List<Long> stateMachineIds);

    /**
     * 批量使活跃状态机变成未活跃
     *
     * @param organizationId
     * @param stateMachineIds
     * @return
     */
    Boolean notActiveStateMachines(Long organizationId, List<Long> stateMachineIds);

    /**
     * 获取组织下所有状态机，附带状态
     *
     * @param organizationId 组织id
     * @return 状态机列表
     */
    List<StateMachineWithStatusDTO> queryAllWithStatus(Long organizationId);

    /**
     * 获取组织下所有状态机
     *
     * @param organizationId
     * @return
     */
    List<StateMachineDTO> queryByOrgId(Long organizationId);
}
