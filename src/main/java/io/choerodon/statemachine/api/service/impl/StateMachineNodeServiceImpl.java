package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.api.dto.StatusDTO;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.app.assembler.StatusAssembler;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.annotation.ChangeStateMachineStatus;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.enums.StateMachineStatus;
import io.choerodon.statemachine.infra.enums.TransformConditionStrategy;
import io.choerodon.statemachine.infra.enums.TransformType;
import io.choerodon.statemachine.infra.feign.IssueFeignClient;
import io.choerodon.statemachine.infra.mapper.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
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
    private StateMachineTransformDraftMapper transformDraftMapper;
    @Autowired
    private StateMachineTransformMapper transformDeployMapper;
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

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    @ChangeStateMachineStatus
    public List<StateMachineNodeDTO> create(Long organizationId, Long stateMachineId, StateMachineNodeDTO nodeDTO) {
        nodeDTO.setStateMachineId(stateMachineId);
        nodeDTO.setOrganizationId(organizationId);
        createStatus(organizationId, nodeDTO);
        StateMachineNodeDraft node = stateMachineNodeAssembler.toTarget(nodeDTO, StateMachineNodeDraft.class);
        node.setType(NodeType.CUSTOM);
        if (nodeDraftMapper.select(node).isEmpty()) {
            int isInsert = nodeDraftMapper.insert(node);
            if (isInsert != 1) {
                throw new CommonException("error.stateMachineNode.create");
            }
        }
        return queryByStateMachineId(organizationId, node.getStateMachineId(), true);
    }

    @Override
    @ChangeStateMachineStatus
    public List<StateMachineNodeDTO> update(Long organizationId, Long stateMachineId, Long nodeId, StateMachineNodeDTO nodeDTO) {
        nodeDTO.setStateMachineId(stateMachineId);
        nodeDTO.setOrganizationId(organizationId);
        createStatus(organizationId, nodeDTO);
        StateMachineNodeDraft node = stateMachineNodeAssembler.toTarget(nodeDTO, StateMachineNodeDraft.class);
        node.setId(nodeId);
        int isUpdate = nodeDraftMapper.updateByPrimaryKeySelective(node);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachineNode.update");
        }
        return queryByStateMachineId(organizationId, node.getStateMachineId(), true);
    }

    @Override
    @ChangeStateMachineStatus
    public List<StateMachineNodeDTO> delete(Long organizationId, Long stateMachineId, Long nodeId) {
        StateMachineNodeDraft node = nodeDraftMapper.queryById(organizationId, nodeId);
        if (node == null) {
            throw new CommonException("error.node.notFound");
        }
        if (!node.getType().equals(NodeType.CUSTOM)) {
            throw new CommonException("error.node.delete.illegal");
        }
        //校验节点的状态是否关联状态机
        if ((Boolean) checkDelete(organizationId, stateMachineId, node.getStatusId()).get("canDelete")) {
            int isDelete = nodeDraftMapper.deleteByPrimaryKey(nodeId);
            if (isDelete != 1) {
                throw new CommonException("error.stateMachineNode.delete");
            }
            //删除关联的转换
            transformDraftMapper.deleteByNodeId(nodeId);
        } else {
            throw new CommonException("error.stateMachineNode.statusHasIssues");
        }
        return queryByStateMachineId(organizationId, stateMachineId, true);
    }

    @Override
    public Map<String, Object> checkDelete(Long organizationId, Long stateMachineId, Long statusId) {
        Map<String, Object> result = new HashMap<>(2);
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.notFound");
        }
        Status status = statusMapper.queryById(organizationId, statusId);
        if (status == null) {
            throw new CommonException("error.status.notFound");
        }
        //只有草稿状态才进行删除校验
        if (stateMachine.getStatus().equals(StateMachineStatus.CREATE)) {
            result.put("canDelete", true);
        } else {
            result = issueFeignClient.checkDeleteNode(organizationId, stateMachineId, statusId).getBody();
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
        List<StateMachineTransformDraft> intoTransforms = transformDraftMapper.select(intoTransformSerach);
        nodeDTO.setIntoTransform(stateMachineTransformAssembler.toTargetList(intoTransforms, StateMachineTransformDTO.class));
        //获取出去的转换
        StateMachineTransformDraft outTransformSerach = new StateMachineTransformDraft();
        outTransformSerach.setStartNodeId(nodeId);
        List<StateMachineTransformDraft> outTransforms = transformDraftMapper.select(outTransformSerach);
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

    /**
     * 敏捷添加状态，需要先在草稿中判断有没有存在该节点，并添加草稿全部转换，然后把该节点和该转换带到发布中
     *
     * @param organizationId
     * @param stateMachineId
     * @param statusDTO
     */
    @Override
    public void createNodeAndTransformForAgile(Long organizationId, Long stateMachineId, StatusDTO statusDTO) {
        Long statusId = statusDTO.getId();
        //校验是否已经存在在发布的状态机中
        StateMachineNode deploy = new StateMachineNode();
        deploy.setStatusId(statusId);
        deploy.setStateMachineId(stateMachineId);
        if (nodeDeployMapper.select(deploy).isEmpty()) {
            //判断草稿中是否存在节点、转换
            StateMachineNodeDraft nodeDraft = new StateMachineNodeDraft();
            nodeDraft.setStatusId(statusId);
            nodeDraft.setStateMachineId(stateMachineId);
            nodeDraft = nodeDraftMapper.selectOne(nodeDraft);
            if (nodeDraft == null) {
                //获取状态机中positionY最大的节点
                StateMachineNodeDraft maxNode = nodeDraftMapper.selectMaxPositionY(stateMachineId);
                //创建节点
                nodeDraft = new StateMachineNodeDraft();
                nodeDraft.setStatusId(statusId);
                nodeDraft.setOrganizationId(organizationId);
                nodeDraft.setStateMachineId(stateMachineId);
                nodeDraft.setType(NodeType.CUSTOM);
                nodeDraft.setPositionX(maxNode.getPositionX());
                nodeDraft.setPositionY(maxNode.getPositionY() + 100);
                nodeDraft.setHeight(maxNode.getHeight());
                nodeDraft.setWidth(maxNode.getWidth());
                int isInsert = nodeDraftMapper.insert(nodeDraft);
                if (isInsert != 1) {
                    throw new CommonException("error.stateMachineNode.create");
                }
            }
            //判断当前节点是否已存在【全部】的转换id
            StateMachineTransformDraft transformDraft = new StateMachineTransformDraft();
            transformDraft.setStateMachineId(stateMachineId);
            transformDraft.setEndNodeId(nodeDraft.getId());
            transformDraft.setType(TransformType.ALL);
            transformDraft = transformDraftMapper.selectOne(transformDraft);
            if (transformDraft == null) {
                //创建全部转换
                transformDraft = new StateMachineTransformDraft();
                transformDraft.setStateMachineId(stateMachineId);
                transformDraft.setName(statusDTO.getName());
                transformDraft.setDescription("全部转换");
                transformDraft.setStartNodeId(0L);
                transformDraft.setEndNodeId(nodeDraft.getId());
                transformDraft.setOrganizationId(organizationId);
                transformDraft.setType(TransformType.ALL);
                transformDraft.setConditionStrategy(TransformConditionStrategy.ALL);
                if (transformDraftMapper.insert(transformDraft) != 1) {
                    throw new CommonException("error.stateMachineTransform.create");
                }
            }

            //更新node的【全部转换到当前】转换id
            int update = nodeDraftMapper.updateAllStatusTransformId(organizationId, nodeDraft.getId(), transformDraft.getId());
            if (update != 1) {
                throw new CommonException("error.createAllStatusTransform.updateAllStatusTransformId");
            }
            //将节点和转换添加到发布中
            insertNodeAndTransformForDeploy(nodeDraft.getId(), transformDraft.getId());
        }
    }

    private void insertNodeAndTransformForDeploy(Long nodeDraftId, Long transformDraftId) {
        StateMachineNodeDraft nodeDraft = nodeDraftMapper.selectByPrimaryKey(nodeDraftId);
        StateMachineTransformDraft transformDraft = transformDraftMapper.selectByPrimaryKey(transformDraftId);
        //插入节点
        StateMachineNode nodeDeploy = new StateMachineNode();
        BeanUtils.copyProperties(nodeDraft, nodeDeploy);
        nodeDeployMapper.insert(nodeDeploy);
        //插入转换
        StateMachineTransform transformDeploy = new StateMachineTransform();
        BeanUtils.copyProperties(transformDraft, transformDeploy);
        transformDeployMapper.insert(transformDeploy);
    }
}
