package io.choerodon.statemachine.api.service;

import io.choerodon.mybatis.service.BaseService;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.domain.StateMachineTransform;
import io.choerodon.statemachine.domain.StateMachineTransformDraft;

import java.util.List;
import java.util.Map;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StateMachineTransformService extends BaseService<StateMachineTransformDraft> {

    /**
     * 创建转换
     *
     * @param organizationId
     * @param transformDTO
     * @return
     */
    StateMachineTransformDTO create(Long organizationId, Long stateMachineId, StateMachineTransformDTO transformDTO);

    /**
     * 更新转换
     *
     * @param organizationId 组织id
     * @param transformId    转换id
     * @param transformDTO   转换对象
     * @return 更新转换
     */
    StateMachineTransformDTO update(Long organizationId, Long stateMachineId, Long transformId, StateMachineTransformDTO transformDTO);

    /**
     * 删除转换
     *
     * @param organizationId 组织id
     * @param transformId    节点id
     * @return
     */
    Boolean delete(Long organizationId, Long stateMachineId, Long transformId);

    /**
     * 获取初始转换
     *
     * @param stateMachineId
     * @return
     */
    StateMachineTransform getInitTransform(Long organizationId, Long stateMachineId);

    /**
     * 根据id获取转换
     *
     * @param organizationId
     * @param transformId
     * @return
     */
    StateMachineTransformDTO queryById(Long organizationId, Long transformId);

    /**
     * 获取当前状态拥有的转换列表，包括【全部】类型的转换
     *
     * @param organizationId
     * @param stateMachineId
     * @param statusId
     * @return
     */
    List<StateMachineTransform> queryListByStatusIdByDeploy(Long organizationId, Long stateMachineId, Long statusId);

    /**
     * 创建【全部转换到此状态】转换，所有节点均可转换到当前节点
     *
     * @param organizationId 组织id
     * @param endNodeId
     * @return
     */
    StateMachineTransformDTO createAllStatusTransform(Long organizationId, Long stateMachineId, Long endNodeId);

    /**
     * 删除【全部转换到此状态】转换
     *
     * @param organizationId
     * @param transformId
     * @return
     */
    Boolean deleteAllStatusTransform(Long organizationId, Long transformId);

    /**
     * 更新转换的条件策略
     *
     * @param organizationId
     * @param transformId
     * @param conditionStrategy
     * @return
     */
    Boolean updateConditionStrategy(Long organizationId, Long transformId, String conditionStrategy);

    /**
     * 校验名字是否重复
     *
     * @param organizationId
     * @param stateMachineId
     * @param name
     * @return
     */
    Boolean checkName(Long organizationId, Long stateMachineId, Long startNodeId, Long endNodeId, String name);

    /**
     * 修复0.12.0发布后，敏捷移除节点，但没移除转换
     *
     * @param organizationId
     * @return
     */
    Boolean fixDeleteIllegalTransforms(Long organizationId);

    /**
     * 根据状态机id列表查询出这些状态机每个状态对应的转换列表
     *
     * @param organizationId
     * @param stateMachineIds
     * @return
     */
    Map<Long, Map<Long, List<StateMachineTransform>>> queryStatusTransformsMap(Long organizationId, List<Long> stateMachineIds);
}
