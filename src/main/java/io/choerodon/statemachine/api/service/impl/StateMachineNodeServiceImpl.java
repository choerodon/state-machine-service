package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineTransform;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineTransformMapper;
import io.choerodon.statemachine.infra.mapper.StatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class StateMachineNodeServiceImpl extends BaseServiceImpl<StateMachineNode> implements StateMachineNodeService {

    @Autowired
    private StateMachineNodeMapper nodeMapper;

    @Autowired
    private StateMachineTransformMapper transformMapper;
    @Autowired
    private StateMachineService stateMachineService;
    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;
    @Autowired
    private StateMachineTransformAssembler stateMachineTransformAssembler;

    @Autowired
    private StatusMapper stateMapper;

    @Override
    public List<StateMachineNodeDTO> create(Long organizationId, StateMachineNodeDTO nodeDTO) {
        nodeDTO.setOrganizationId(organizationId);
        createState(organizationId, nodeDTO);
        StateMachineNode node = stateMachineNodeAssembler.toTarget(nodeDTO, StateMachineNode.class);
        node.setType(NodeType.CUSTOM);
        int isInsert = nodeMapper.insert(node);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }
        node = nodeMapper.getNodeById(node.getId());
        stateMachineService.updateStateMachineStatus(organizationId, node.getStateMachineId());
        return stateMachineNodeAssembler.toTargetList(nodeMapper.selectByStateMachineId(node.getStateMachineId()), StateMachineNodeDTO.class);
    }

    @Override
    public List<StateMachineNodeDTO> update(Long organizationId, Long nodeId, StateMachineNodeDTO nodeDTO) {
        nodeDTO.setOrganizationId(organizationId);
        createState(organizationId, nodeDTO);
        StateMachineNode node = stateMachineNodeAssembler.toTarget(nodeDTO, StateMachineNode.class);
        node.setId(nodeId);
        int isUpdate = nodeMapper.updateByPrimaryKeySelective(node);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineNode.update");
        }
        node = nodeMapper.getNodeById(node.getId());
        stateMachineService.updateStateMachineStatus(node.getOrganizationId(), node.getStateMachineId());
        return stateMachineNodeAssembler.toTargetList(nodeMapper.selectByStateMachineId(node.getStateMachineId()), StateMachineNodeDTO.class);
    }

    @Override
    public List<StateMachineNodeDTO> delete(Long organizationId, Long nodeId) {
        StateMachineNode node = nodeMapper.queryById(organizationId, nodeId);
        int isDelete = nodeMapper.deleteByPrimaryKey(nodeId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachineNode.delete");
        }
        transformMapper.deleteByNodeId(nodeId);
        stateMachineService.updateStateMachineStatus(organizationId, node.getStateMachineId());
        return stateMachineNodeAssembler.toTargetList(nodeMapper.selectByStateMachineId(node.getStateMachineId()), StateMachineNodeDTO.class);
    }

    @Override
    public StateMachineNodeDTO queryById(Long organizationId, Long nodeId) {
        StateMachineNode node = nodeMapper.getNodeById(nodeId);
        if (node == null) {
            throw new CommonException("error.stateMachineNode.noFound");
        }
        StateMachineNodeDTO nodeDTO = stateMachineNodeAssembler.toTarget(node, StateMachineNodeDTO.class);
        StateMachineTransform intoTransformSerach = new StateMachineTransform();
        intoTransformSerach.setEndNodeId(nodeId);
        List<StateMachineTransform> intoTransforms = transformMapper.select(intoTransformSerach);
        nodeDTO.setIntoTransform(stateMachineTransformAssembler.toTargetList(intoTransforms, StateMachineTransformDTO.class));
        StateMachineTransform outTransformSerach = new StateMachineTransform();
        outTransformSerach.setStartNodeId(nodeId);
        List<StateMachineTransform> outTransforms = transformMapper.select(outTransformSerach);
        nodeDTO.setOutTransform(stateMachineTransformAssembler.toTargetList(outTransforms, StateMachineTransformDTO.class));
        return nodeDTO;
    }

    /**
     * 新增状态
     *
     * @param organizationId 组织id
     * @param nodeDTO        节点
     */
    private void createState(Long organizationId, StateMachineNodeDTO nodeDTO) {
        if (nodeDTO.getStatusId() == null && nodeDTO.getStateDTO() != null && nodeDTO.getStateDTO().getName() != null) {
            Status status = stateMachineNodeAssembler.toTarget(nodeDTO.getStateDTO(), Status.class);
            status.setOrganizationId(organizationId);
            int isStateInsert = stateMapper.insert(status);
            if (isStateInsert != 1) {
                throw new CommonException("error.state.create");
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
        StateMachineNode node = new StateMachineNode();
        node.setType(NodeType.START);
        node.setStateMachineId(stateMachineId);
        node.setOrganizationId(organizationId);
        List<StateMachineNode> nodes = nodeMapper.select(node);
        if (nodes.isEmpty()) {
            throw new CommonException("error.initNode.null");
        }
        return nodes.get(0).getId();
    }
}
