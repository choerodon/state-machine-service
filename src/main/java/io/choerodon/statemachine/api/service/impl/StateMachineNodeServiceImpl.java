package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.app.assembler.StatusAssembler;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.enums.StateMachineStatus;
import io.choerodon.statemachine.infra.feign.IssueFeignClient;
import io.choerodon.statemachine.infra.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class StateMachineNodeServiceImpl extends BaseServiceImpl<StateMachineNodeDraft> implements StateMachineNodeService {

    @Autowired
    private StateMachineNodeDraftMapper nodeDraftMapper;
    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;
    @Autowired
    private StateMachineTransformDraftMapper transformMapper;
    @Autowired
    private StateMachineService stateMachineService;
    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;
    @Autowired
    private StateMachineTransformAssembler stateMachineTransformAssembler;
    @Autowired
    private StatusAssembler statusAssembler;
    @Autowired
    private StatusMapper statusMapper;
    @Autowired
    private StateMachineTransformService transformService;
    @Autowired
    private StateMachineMapper stateMachineMapper;
    @Autowired
    private IssueFeignClient issueFeignClient;

    @Override
    public List<StateMachineNodeDTO> create(Long organizationId, StateMachineNodeDTO nodeDTO) {
        nodeDTO.setOrganizationId(organizationId);
        createStatus(organizationId, nodeDTO);
        StateMachineNodeDraft node = stateMachineNodeAssembler.toTarget(nodeDTO, StateMachineNodeDraft.class);
        node.setType(NodeType.CUSTOM);
        int isInsert = nodeDraftMapper.insert(node);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }
        stateMachineService.updateStateMachineStatus(organizationId, node.getStateMachineId());
        return queryByStateMachineId(organizationId, node.getStateMachineId(), true);
    }

    @Override
    public List<StateMachineNodeDTO> update(Long organizationId, Long nodeId, StateMachineNodeDTO nodeDTO) {
        nodeDTO.setOrganizationId(organizationId);
        createStatus(organizationId, nodeDTO);
        StateMachineNodeDraft node = stateMachineNodeAssembler.toTarget(nodeDTO, StateMachineNodeDraft.class);
        node.setId(nodeId);
        int isUpdate = nodeDraftMapper.updateByPrimaryKeySelective(node);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineNode.update");
        }
        stateMachineService.updateStateMachineStatus(node.getOrganizationId(), node.getStateMachineId());
        return queryByStateMachineId(organizationId, node.getStateMachineId(), true);
    }

    @Override
    public List<StateMachineNodeDTO> delete(Long organizationId, Long nodeId) {
        StateMachineNodeDraft node = nodeDraftMapper.queryById(organizationId, nodeId);
        int isDelete = nodeDraftMapper.deleteByPrimaryKey(nodeId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachineNode.delete");
        }
        transformMapper.deleteByNodeId(nodeId);
        stateMachineService.updateStateMachineStatus(organizationId, node.getStateMachineId());
        return queryByStateMachineId(organizationId, node.getStateMachineId(), true);
    }

    public Map<String, Object> checkDelete(Long organizationId, Long stateMachineId, Long statusId){
        Map<String, Object> result = new HashMap<>(2);
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        //只有草稿状态才进行删除校验
        if(stateMachine.getStatus().equals(StateMachineStatus.DRAFT)){
            result = issueFeignClient.checkDeleteNode(organizationId, stateMachineId, statusId).getBody();
        }else{
            result.put("canDelete", false);
        }
        return result;
    }

    @Override
    public StateMachineNodeDTO queryById(Long organizationId, Long nodeId) {
        StateMachineNodeDraft node = nodeDraftMapper.getNodeById(nodeId);
        if (node == null) {
            throw new CommonException("error.stateMachineNode.noFound");
        }
        StateMachineNodeDTO nodeDTO = stateMachineNodeAssembler.draftToNodeDTO(node);

        //获取进入的转换
        StateMachineTransformDraft intoTransformSerach = new StateMachineTransformDraft();
        intoTransformSerach.setEndNodeId(nodeId);
        List<StateMachineTransformDraft> intoTransforms = transformMapper.select(intoTransformSerach);
        nodeDTO.setIntoTransform(stateMachineTransformAssembler.toTargetList(intoTransforms, StateMachineTransformDTO.class));
        //获取出去的转换
        StateMachineTransformDraft outTransformSerach = new StateMachineTransformDraft();
        outTransformSerach.setStartNodeId(nodeId);
        List<StateMachineTransformDraft> outTransforms = transformMapper.select(outTransformSerach);
        nodeDTO.setOutTransform(stateMachineTransformAssembler.toTargetList(outTransforms, StateMachineTransformDTO.class));
        return nodeDTO;
    }

    /**
     * 状态机下新增状态
     *
     * @param organizationId 组织id
     * @param nodeDTO        节点
     */
    private void createStatus(Long organizationId, StateMachineNodeDTO nodeDTO) {
        if (nodeDTO.getStatusId() == null && nodeDTO.getStatusDTO() != null && nodeDTO.getStatusDTO().getName() != null) {
            Status status = statusAssembler.toTarget(nodeDTO.getStatusDTO(), Status.class);
            status.setOrganizationId(organizationId);
            int isStateInsert = statusMapper.insert(status);
            if (isStateInsert != 1) {
                throw new CommonException("error.status.create");
            }
            nodeDTO.setStatusId(status.getId());
        }
    }

    /**
     * 初始节点
     *
     * @param stateMachineId
     * @return
     */
    @Override
    public Long getInitNode(Long organizationId, Long stateMachineId) {
        StateMachineNodeDraft node = new StateMachineNodeDraft();
        node.setType(NodeType.START);
        node.setStateMachineId(stateMachineId);
        node.setOrganizationId(organizationId);
        List<StateMachineNodeDraft> nodes = nodeDraftMapper.select(node);
        if (nodes.isEmpty()) {
            throw new CommonException("error.initNode.null");
        }
        return nodes.get(0).getId();
    }

    @Override
    public List<StateMachineNodeDTO> queryByStateMachineId(Long organizationId, Long stateMachineId, Boolean isDraft) {
        List<StateMachineNodeDTO> nodeDTOS;
        if (isDraft) {
            //获取节点
            List<StateMachineNodeDraft> nodes = nodeDraftMapper.selectByStateMachineId(stateMachineId);
            nodeDTOS = stateMachineNodeAssembler.draftToList(nodes);
        } else {
            List<StateMachineNode> nodes = nodeDeployMapper.selectByStateMachineId(stateMachineId);
            nodeDTOS = stateMachineNodeAssembler.toList(nodes);
        }
        return nodeDTOS;
    }

    @Override
    public void createNodeForAgile(Long organizationId, Long stateMachineId, Long statusId) {
        //校验是否已经存在在状态机中
        StateMachineNodeDraft select = new StateMachineNodeDraft();
        select.setStatusId(statusId);
        select.setStateMachineId(stateMachineId);
        if(nodeDraftMapper.select(select).isEmpty()){
            //获取状态机中positionY最大的节点
            StateMachineNodeDraft maxNode = nodeDraftMapper.selectMaxPositionY(stateMachineId);
            //创建节点
            StateMachineNodeDraft nodeDraft = new StateMachineNodeDraft();
            nodeDraft.setStatusId(statusId);
            nodeDraft.setOrganizationId(organizationId);
            nodeDraft.setStateMachineId(stateMachineId);
            nodeDraft.setType(NodeType.CUSTOM);
            nodeDraft.setPositionX(maxNode.getPositionX());
            nodeDraft.setPositionY(maxNode.getPositionY()+100);
            nodeDraft.setHeight(maxNode.getHeight());
            nodeDraft.setWidth(maxNode.getWidth());
            int isInsert = nodeDraftMapper.insert(nodeDraft);
            if (isInsert != 1) {
                throw new CommonException("error.stateMachineNode.create");
            }
            stateMachineService.updateStateMachineStatus(organizationId, stateMachineId);
            //创建转换
            transformService.createAllStatusTransform(organizationId, stateMachineId, nodeDraft.getId());
        }
    }
}
