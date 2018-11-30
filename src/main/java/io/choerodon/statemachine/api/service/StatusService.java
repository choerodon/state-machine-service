package io.choerodon.statemachine.api.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.statemachine.api.dto.*;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.StatusWithInfo;
import io.choerodon.statemachine.fixdata.dto.StatusForMoveDataDO;

import java.util.List;
import java.util.Map;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StatusService {

//    /**
//     * 分页查询状态列表
//     *
//     * @param pageRequest 分页对象
//     * @param statusDTO   参数对象
//     * @param param       模糊查询参数
//     * @return 状态列表
//     */
//    Page<StatusDTO> pageQuery(PageRequest pageRequest, StatusDTO statusDTO, String param);

    Page<StatusWithInfoDTO> queryStatusList(PageRequest pageRequest, Long organizationId, StatusSearchDTO statusSearchDTO);

    /**
     * 创建状态
     *
     * @param organizationId 组织id
     * @param statusDTO      状态对象
     * @return 状态对象
     */
    StatusDTO create(Long organizationId, StatusDTO statusDTO);

    /**
     * 更新状态
     *
     * @param statusDTO 更新对象
     * @return 更新对象
     */
    StatusDTO update(StatusDTO statusDTO);

    /**
     * 删除状态
     *
     * @param organizationId 组织id
     * @param statusId       状态机id
     * @return
     */
    Boolean delete(Long organizationId, Long statusId);

    /**
     * 根据id获取状态对象
     *
     * @param organizationId 组织id
     * @param statusId       状态id
     * @return
     */
    StatusInfoDTO queryStatusById(Long organizationId, Long statusId);

    /**
     * 获取所有
     *
     * @param organizationId 组织id
     * @return
     */
    List<StatusDTO> queryAllStatus(Long organizationId);

    Map<Long, StatusMapDTO> queryAllStatusMap(Long organizationId);

    /**
     * 校验状态名字是否未被使用
     *
     * @param organizationId 组织id
     * @param name           名称
     * @return
     */
    StatusCheckDTO checkName(Long organizationId, String name);

    Map<Long, Status> batchStatusGet(List<Long> ids);

    /**
     * 敏捷添加状态
     * @param organizationId
     * @param statusDTO
     * @return
     */
    StatusDTO createStatusForAgile(Long organizationId, Long stateMachineId, StatusDTO statusDTO);

    /**
     * 查询状态机下的所有状态
     *
     * @param organizationId
     * @param stateMachineIds
     * @return
     */
    List<StatusDTO> queryByStateMachineIds(Long organizationId,List<Long> stateMachineIds);

}
