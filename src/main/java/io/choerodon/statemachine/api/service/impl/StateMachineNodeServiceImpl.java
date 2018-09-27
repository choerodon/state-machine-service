package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransfAssembler;
import io.choerodon.statemachine.domain.State;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineTransf;
import io.choerodon.statemachine.infra.enums.StateMachineNodeStatus;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineTransfMapper;
import io.choerodon.statemachine.infra.mapper.StateMapper;
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
    private StateMachineTransfMapper transfMapper;
    @Autowired
    private StateMachineService stateMachineService;
    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;
    @Autowired
    private StateMachineTransfAssembler stateMachineTransfAssembler;

    @Autowired
    private StateMapper stateMapper;

    @Override
    public List<StateMachineNodeDTO> create(Long organizationId, StateMachineNodeDTO nodeDTO) {
        nodeDTO.setOrganizationId(organizationId);
        createState(organizationId, nodeDTO);
        StateMachineNode node = stateMachineNodeAssembler.toTarget(nodeDTO, StateMachineNode.class);
        node.setStatus(StateMachineNodeStatus.STATUS_CUSTOM);
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
        transfMapper.deleteByNodeId(nodeId);
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
        StateMachineTransf intoTransfSerach = new StateMachineTransf();
        intoTransfSerach.setEndNodeId(nodeId);
        List<StateMachineTransf> intoTransfs = transfMapper.select(intoTransfSerach);
        nodeDTO.setIntoTransf(stateMachineTransfAssembler.toTargetList(intoTransfs, StateMachineTransfDTO.class));
        StateMachineTransf outTransfSerach = new StateMachineTransf();
        outTransfSerach.setStartNodeId(nodeId);
        List<StateMachineTransf> outTransfs = transfMapper.select(outTransfSerach);
        nodeDTO.setOutTransf(stateMachineTransfAssembler.toTargetList(outTransfs, StateMachineTransfDTO.class));
        return nodeDTO;
    }

    /**
     * 新增状态
     *
     * @param organizationId 组织id
     * @param nodeDTO        节点
     */
    private void createState(Long organizationId, StateMachineNodeDTO nodeDTO) {
        if (nodeDTO.getStateId() == null && nodeDTO.getStateDTO() != null && nodeDTO.getStateDTO().getName() != null) {
            State state = stateMachineNodeAssembler.toTarget(nodeDTO.getStateDTO(), State.class);
            state.setOrganizationId(organizationId);
            int isStateInsert = stateMapper.insert(state);
            if (isStateInsert != 1) {
                throw new CommonException("error.state.create");
            }
            nodeDTO.setStateId(state.getId());
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
        node.setStatus(StateMachineNodeStatus.STATUS_START);
        node.setStateMachineId(stateMachineId);
        node.setOrganizationId(organizationId);
        List<StateMachineNode> nodes = nodeMapper.select(node);
        if (nodes.isEmpty()) {
            throw new CommonException("error.initNode.null");
        }
        return nodes.get(0).getId();
    }
}
