package io.choerodon.statemachine.infra.utils;

import io.choerodon.statemachine.api.dto.StatusDTO;
import io.choerodon.statemachine.api.dto.StateMachineDTO;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.domain.StateMachine;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.StateMachineNodeDeploy;
import io.choerodon.statemachine.domain.StateMachineTransf;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
public class ConvertUtils {
    private ConvertUtils() {
    }

    /**
     * 节点 转换
     * @param node
     * @return
     */
    public static StateMachineNodeDTO convertNodeToNodeDTO(final StateMachineNode node) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(new PropertyMap<StateMachineNode, StateMachineNodeDTO>() {
            @Override
            protected void configure() {
                skip(destination.getStatusId());
            }
        });
        StateMachineNodeDTO nodeDTO = modelMapper.map(node, StateMachineNodeDTO.class);
        nodeDTO.setStatusId(node.getStatusId());
        if (node.getState() != null){
            StatusDTO stateDTO = modelMapper.map(node.getState(), StatusDTO.class);
            nodeDTO.setStateDTO(stateDTO);
        }
        return nodeDTO;
    }

    /**
     * 状态机转换
     * @param stateMachine
     * @return
     */
    public static StateMachineDTO covertStateMachine(final StateMachine stateMachine) {
        ModelMapper modelMapper = new ModelMapper();
        StateMachineDTO stateMachineDTO = modelMapper.map(stateMachine, StateMachineDTO.class);
        List<StateMachineNode> nodes = stateMachine.getStateMachineNodes();
        if (null != nodes && !nodes.isEmpty()) {
            List<StateMachineNodeDTO> nodeDTOs = new ArrayList<>(nodes.size());
            for (StateMachineNode node: nodes) {
                StateMachineNodeDTO nodeDTO = convertNodeToNodeDTO(node);
                nodeDTOs.add(nodeDTO);
            }
            stateMachineDTO.setNodeDTOs(nodeDTOs);
        }
        List<StateMachineTransf> transfs = stateMachine.getStateMachineTransfs();
        if (null != transfs && !transfs.isEmpty()) {
            List<StateMachineTransfDTO> transfDTOs = modelMapper.map(transfs, new TypeToken<List<StateMachineTransfDTO>>() {}.getType());
            stateMachineDTO.setTransfDTOs(transfDTOs);
        }
        return stateMachineDTO;
    }

    /**
     * 节点转换
     * @param node
     * @return
     */
    public static StateMachineNodeDeploy convertNodeToNodeDeploy(final StateMachineNode node) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(new PropertyMap<StateMachineNode, StateMachineNodeDeploy>() {
            @Override
            protected void configure() {
                skip(destination.getStatusId());
            }
        });
        StateMachineNodeDeploy nodeDeploy = modelMapper.map(node, StateMachineNodeDeploy.class);
        nodeDeploy.setStatusId(node.getStatusId());
        return nodeDeploy;
    }

    /**
     * 节点列表转换
     * @param nodes
     * @return
     */
    public static List<StateMachineNodeDTO> convertNodesToNodeDTOs(final List<StateMachineNode> nodes) {
        List<StateMachineNodeDTO> list = new ArrayList<>(nodes.size());
        for (StateMachineNode node:nodes) {
            StateMachineNodeDTO nodeDTO = convertNodeToNodeDTO(node);
            list.add(nodeDTO);
        }
        return list;
    }

    /**
     * 节点列表转换
     * @param nodes
     * @return
     */
    public static List<StateMachineNodeDeploy> convertNodesToNodeDeploys(final List<StateMachineNode> nodes) {
        List<StateMachineNodeDeploy> list = new ArrayList<>(nodes.size());
        for (StateMachineNode node:nodes) {
            StateMachineNodeDeploy nodeDeploy = convertNodeToNodeDeploy(node);
            list.add(nodeDeploy);
        }
        return list;
    }

    /**
     * 节点转换
     * @param nodeDeploy
     * @return
     */
    public static StateMachineNode convertNodeDeployToNode(final StateMachineNodeDeploy nodeDeploy) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.addMappings(new PropertyMap<StateMachineNodeDeploy, StateMachineNode>() {
            @Override
            protected void configure() {
                skip(destination.getStatusId());
            }
        });
        StateMachineNode node = modelMapper.map(nodeDeploy, StateMachineNode.class);
        node.setStatusId(nodeDeploy.getStatusId());
        node.setState(nodeDeploy.getState());
        return node;
    }

    /**
     * 节点列表转换
     * @param nodeDeploys
     * @return
     */
    public static List<StateMachineNode> convertNodeDeploysToNodes(final List<StateMachineNodeDeploy> nodeDeploys) {
        List<StateMachineNode> list = new ArrayList<>(nodeDeploys.size());
        for (StateMachineNodeDeploy nodeDeploy:nodeDeploys) {
            StateMachineNode node = convertNodeDeployToNode(nodeDeploy);
            list.add(node);
        }
        return list;
    }
}
