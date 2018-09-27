package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.domain.State;
import io.choerodon.statemachine.domain.StateMachine;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineTransf;
import io.choerodon.statemachine.infra.enums.StateMachineNodeStatus;
import io.choerodon.statemachine.infra.enums.StateMachineStatus;
import io.choerodon.statemachine.infra.mapper.StateMachineMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineTransfMapper;
import io.choerodon.statemachine.infra.mapper.StateMapper;
import io.choerodon.statemachine.infra.utils.ConvertUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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
    private StateMachineMapper stateMachineMapper;

    @Autowired
    private StateMachineTransfMapper transfMapper;

    @Autowired
    private StateMapper stateMapper;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<StateMachineNodeDTO> create(Long organizationId, StateMachineNodeDTO nodeDTO) {
        createState(organizationId, nodeDTO);
        StateMachineNode node = modelMapper.map(nodeDTO, StateMachineNode.class);
        node.setStatus(StateMachineNodeStatus.STATUS_CUSTOM);
        int isInsert = nodeMapper.insert(node);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }
        node = nodeMapper.getNodeById(node.getId());
        updateStateMachineStatus(node.getStateMachineId());
        List<StateMachineNode> nodes = nodeMapper.selectByStateMachineId(node.getStateMachineId());
        return ConvertUtils.convertNodesToNodeDTOs(nodes);
    }

    @Override
    public List<StateMachineNodeDTO> update(Long organizationId, Long nodeId, StateMachineNodeDTO nodeDTO) {
        createState(organizationId, nodeDTO);
        StateMachineNode node = modelMapper.map(nodeDTO, StateMachineNode.class);
        node.setId(nodeId);
        int isUpdate = nodeMapper.updateByPrimaryKeySelective(node);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineNode.update");
        }
        node = nodeMapper.getNodeById(node.getId());
        updateStateMachineStatus(node.getStateMachineId());
        List<StateMachineNode> nodes = nodeMapper.selectByStateMachineId(node.getStateMachineId());
        return ConvertUtils.convertNodesToNodeDTOs(nodes);
    }

    @Override
    public List<StateMachineNodeDTO> delete(Long organizationId, Long nodeId) {
        StateMachineNode node = nodeMapper.selectByPrimaryKey(nodeId);
        int isDelete = nodeMapper.deleteByPrimaryKey(nodeId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachineNode.delete");
        }
        transfMapper.deleteByNodeId(nodeId);
        updateStateMachineStatus(node.getStateMachineId());
        List<StateMachineNode> nodes = nodeMapper.selectByStateMachineId(node.getStateMachineId());
        return ConvertUtils.convertNodesToNodeDTOs(nodes);
    }

    @Override
    public StateMachineNodeDTO queryById(Long organizationId, Long nodeId) {
        StateMachineNode node = nodeMapper.getNodeById(nodeId);
        if (node == null) {
            throw new CommonException("error.stateMachineNode.noFound");
        }
        StateMachineNodeDTO nodeDTO = ConvertUtils.convertNodeToNodeDTO(node);
        StateMachineTransf intoTransfSerach = new StateMachineTransf();
        intoTransfSerach.setEndNodeId(nodeId);
        List<StateMachineTransf> intoTransfs = transfMapper.select(intoTransfSerach);
        if (intoTransfs != null && !intoTransfs.isEmpty()){
            List<StateMachineTransfDTO> transfDTOS = modelMapper.map(intoTransfs, new TypeToken<List<StateMachineTransfDTO>>(){}.getType());
            nodeDTO.setIntoTransf(transfDTOS);
        }
        StateMachineTransf outTransfSerach = new StateMachineTransf();
        outTransfSerach.setStartNodeId(nodeId);
        List<StateMachineTransf> outTransfs = transfMapper.select(outTransfSerach);
        if (outTransfs != null && !outTransfs.isEmpty()){
            List<StateMachineTransfDTO> transfDTOS = modelMapper.map(outTransfs, new TypeToken<List<StateMachineTransfDTO>>(){}.getType());
            nodeDTO.setOutTransf(transfDTOS);
        }
        return nodeDTO;
    }

    /**
     * 修改状态机状态
     * 发布 -> 修改
     *
     * @param stateMachineId
     */
    private void updateStateMachineStatus(Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.selectByPrimaryKey(stateMachineId);
        if (stateMachine != null && stateMachine.getStatus().equals(StateMachineStatus.STATUS_ACTIVE)) {
            stateMachine.setStatus(StateMachineStatus.STATUS_DRAFT);
            int stateMachineUpdate = stateMachineMapper.updateByPrimaryKey(stateMachine);
            if (stateMachineUpdate != 1) {
                throw new CommonException("error.stateMachine.update");
            }
        }
    }

    /**
     * 新增状态
     *
     * @param organizationId 组织id
     * @param nodeDTO        节点
     */
    private void createState(Long organizationId, StateMachineNodeDTO nodeDTO) {
        if (nodeDTO.getStateId() == null && nodeDTO.getStateDTO() != null && nodeDTO.getStateDTO().getName() != null) {
            State state = modelMapper.map(nodeDTO.getStateDTO(), State.class);
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
     * @param stateMachineId
     * @return
     */
    @Override
    public Long getInitNode(Long stateMachineId) {
        StateMachineNode node = new StateMachineNode();
        node.setStatus(StateMachineNodeStatus.STATUS_START);
        node.setStateMachineId(stateMachineId);
        List<StateMachineNode> nodes = nodeMapper.select(node);
        if (nodes.isEmpty()) {
            throw new CommonException("error.initNode.null");
        }
        return nodes.get(0).getId();
    }
}
