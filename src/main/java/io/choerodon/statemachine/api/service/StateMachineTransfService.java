package io.choerodon.statemachine.api.service;

import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.domain.StateMachineTransf;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public interface StateMachineTransfService extends BaseService<StateMachineTransf> {

    /**
     * 创建转换
     *
     * @param organizationId
     * @param transfDTO
     * @return
     */
    StateMachineTransfDTO create(Long organizationId, StateMachineTransfDTO transfDTO);

    /**
     * 更新转换
     *
     * @param organizationId 组织id
     * @param transfId       转换id
     * @param transfDTO      转换对象
     * @return 更新转换
     */
    StateMachineTransfDTO update(Long organizationId, Long transfId, StateMachineTransfDTO transfDTO);

    /**
     * 删除转换
     *
     * @param organizationId 组织id
     * @param transfId       节点id
     * @return
     */
    Boolean delete(Long organizationId, Long transfId);

    /**
     * 获取初始转换
     * @param stateMachineId
     * @return
     */
    Long getInitTransf(Long stateMachineId);

    /**
     * 校验转换名字是否未被使用
     *
     * @param stateMachineId 状态机id
     * @param transfId       转换id
     * @param name           名称
     * @return
     */
    Boolean checkName(Long stateMachineId, Long transfId, String name);

    /**
     * 根据id获取转换
     *
     * @param organizationId
     * @param transfId
     * @return
     */
    StateMachineTransfDTO getById(Long organizationId, Long transfId);

    /**
     * 获取当前状态拥有的转换列表
     *
     * @param organizationId
     * @param stateId
     * @return
     */
    List<StateMachineTransfDTO> queryListByStateId(Long organizationId, Long stateMachineId, Long stateId);

}
