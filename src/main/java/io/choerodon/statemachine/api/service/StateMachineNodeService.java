package io.choerodon.statemachine.api.service;

import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.domain.StateMachineNodeDraft;

import java.util.List;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
public interface StateMachineNodeService extends BaseService<StateMachineNodeDraft> {
    /**
     * 创建状态节点
     * @param organizationId
     * @param nodeDTO
     * @return
     */
    List<StateMachineNodeDTO> create(Long organizationId, StateMachineNodeDTO nodeDTO);

    /**
     * 更新节点
     * @param organizationId 组织id
     * @param nodeId 节点id
     * @param nodeDTO 节点对象
     * @return 更新节点
     */
    List<StateMachineNodeDTO> update(Long organizationId, Long nodeId, StateMachineNodeDTO nodeDTO);

    /**
     * 删除状态节点
     * @param organizationId 组织id
     * @param nodeId 节点id
     * @return
     */
    List<StateMachineNodeDTO> delete(Long organizationId, Long nodeId);

    /**
     * 获取状态机初始节点id
     * @param stateMachineId
     * @return
     */
    Long getInitNode(Long organizationId,Long stateMachineId);

    /**
     * 根据id获取节点
     *
     * @param organizationId
     * @param nodeId
     * @return
     */
    StateMachineNodeDTO queryById(Long organizationId, Long nodeId);
    /**
     * 根据状态机id获取所有节点
     *
     * @param organizationId
     * @param stateMachineId
     * @return
     */
    List<StateMachineNodeDTO> queryByStateMachineId(Long organizationId, Long stateMachineId, Boolean isDraft);

}
