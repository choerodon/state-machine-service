package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StatusDTO;
import io.choerodon.statemachine.api.service.StatusService;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.enums.TransformType;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineTransformMapper;
import io.choerodon.statemachine.infra.mapper.StatusMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
@Service
public class StatusServiceImpl implements StatusService {

    private static final String STATUS_CATEGORY_TODO = "status_todo";
    private static final String STATUS_CATEGORY_DOING = "status_doing";
    private static final String STATUS_CATEGORY_DONE = "status_done";

    @Autowired
    private StatusMapper stateMapper;

    @Autowired
    private StateMachineNodeDraftMapper nodeMapper;

    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;

    @Autowired
    private StateMachineTransformMapper stateMachineTransformMapper;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public Page<StatusDTO> pageQuery(PageRequest pageRequest, StatusDTO statusDTO, String param) {
        Status status = modelMapper.map(statusDTO, Status.class);
        Page<Status> page = PageHelper.doPageAndSort(pageRequest,
                () -> stateMapper.fulltextSearch(status, param));
        List<Status> statuses = page.getContent();
        List<StatusDTO> statusDTOs = modelMapper.map(statuses, new TypeToken<List<StatusDTO>>() {
        }.getType());
        for (StatusDTO dto : statusDTOs) {
            //该状态已被草稿状态机使用个数
            Long draftUsed = nodeMapper.checkStateDelete(dto.getOrganizationId(), dto.getId());
            //该状态已被发布状态机使用个数
            Long deployUsed = nodeDeployMapper.checkStateDelete(dto.getOrganizationId(), dto.getId());
            if (draftUsed == 0 && deployUsed == 0) {
                dto.setCanDelete(true);
            } else {
                dto.setCanDelete(false);
            }
        }
        Page<StatusDTO> returnPage = new Page<>();
        returnPage.setContent(statusDTOs);
        returnPage.setNumber(page.getNumber());
        returnPage.setNumberOfElements(page.getNumberOfElements());
        returnPage.setSize(page.getSize());
        returnPage.setTotalElements(page.getTotalElements());
        returnPage.setTotalPages(page.getTotalPages());
        return returnPage;
    }

    @Override
    public StatusDTO create(Long organizationId, StatusDTO statusDTO) {
        statusDTO.setOrganizationId(organizationId);
        Status status = modelMapper.map(statusDTO, Status.class);
        int isInsert = stateMapper.insert(status);
        if (isInsert != 1) {
            throw new CommonException("error.status.create");
        }
        status = stateMapper.queryById(organizationId, status.getId());
        return modelMapper.map(status, StatusDTO.class);
    }

    @Override
    public StatusDTO update(StatusDTO statusDTO) {
        Status status = modelMapper.map(statusDTO, Status.class);
        int isUpdate = stateMapper.updateByPrimaryKeySelective(status);
        if (isUpdate != 1) {
            throw new CommonException("error.status.update");
        }
        status = stateMapper.queryById(status.getOrganizationId(), status.getId());
        return modelMapper.map(status, StatusDTO.class);
    }

    @Override
    public Boolean delete(Long organizationId, Long statusId) {
        Status status = stateMapper.queryById(organizationId, statusId);
        if (status == null) {
            throw new CommonException("error.status.delete.nofound");
        }
        Long draftUsed = nodeMapper.checkStateDelete(organizationId, statusId);
        Long deployUsed = nodeDeployMapper.checkStateDelete(organizationId, statusId);
        if (draftUsed != 0 || deployUsed != 0) {
            throw new CommonException("error.status.delete");
        }
        int isDelete = stateMapper.deleteByPrimaryKey(statusId);
        if (isDelete != 1) {
            throw new CommonException("error.status.delete");
        }
        return true;
    }

    @Override
    public StatusDTO queryStateById(Long organizationId, Long stateId) {
        Status status = stateMapper.queryById(organizationId, stateId);
        if(status==null){
            throw new CommonException("error.queryStateById.notExist");
        }
        return modelMapper.map(status, StatusDTO.class);
    }

    @Override
    public List<StatusDTO> queryAllState(Long organizationId) {
        Status status = new Status();
        status.setOrganizationId(organizationId);
        List<Status> statuses = stateMapper.select(status);
        return modelMapper.map(statuses, new TypeToken<List<StatusDTO>>() {
        }.getType());
    }

    @Override
    public Boolean checkName(Long organizationId, Long statusId, String name) {
        Status status = new Status();
        status.setOrganizationId(organizationId);
        status.setName(name);
        status = stateMapper.selectOne(status);
        if (status != null) {
            //若传了id，则为更新校验（更新校验不校验本身），不传为创建校验
            return status.getId().equals(statusId);
        }
        return true;
    }

    private Long createStateMachineByDetail(Long organizationId,
                                      String name,
                                      String description,
                                      String type,
                                      Long stateMachineId,
                                      Long startNodeId,
                                      String nodeType,
                                      String transformType) {
        Status status = new Status();
        status.setOrganizationId(organizationId);
        status.setName(name);
        status.setDescription(description);
        status.setType(type);
        if (stateMapper.insert(status) != 1) {
            throw new CommonException("error.status.insrt");
        }

        StateMachineNode node = new StateMachineNode();
        node.setStateMachineId(stateMachineId);
        node.setStatusId(status.getId());
        node.setPositionX(0L);
        node.setPositionY(120L);
        node.setWidth(100L);
        node.setHeight(50L);
        node.setType(nodeType);
        node.setOrganizationId(organizationId);
        int isNodeInsert = nodeDeployMapper.insert(node);
        if (isNodeInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }

        StateMachineTransform transform = new StateMachineTransform();
        transform.setStateMachineId(stateMachineId);
        transform.setStartNodeId(startNodeId);
        transform.setEndNodeId(node.getId());
        transform.setType(transformType);
        transform.setOrganizationId(organizationId);
        int isTransformInsert = stateMachineTransformMapper.insert(transform);
        if (isTransformInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }

        return node.getId();
    }

    @Override
    public void initSystemStateMachineDetail(Long organizationId, Long stateMachineId, Long startNodeId) {
        Long startId;
        startId = createStateMachineByDetail(organizationId, "待处理", "待处理", STATUS_CATEGORY_TODO, stateMachineId, startNodeId, NodeType.INIT, TransformType.INIT);
        startId = createStateMachineByDetail(organizationId, "处理中", "处理中", STATUS_CATEGORY_DOING, stateMachineId, startId, NodeType.CUSTOM, TransformType.CUSTOM);
        startId = createStateMachineByDetail(organizationId, "已完成", "已完成", STATUS_CATEGORY_DONE, stateMachineId, startId, NodeType.CUSTOM, TransformType.CUSTOM);
        startId = createStateMachineByDetail(organizationId, "测试中", "测试中", STATUS_CATEGORY_DOING, stateMachineId, startId, NodeType.CUSTOM, TransformType.CUSTOM);
        createStateMachineByDetail(organizationId, "待部署", "待部署", STATUS_CATEGORY_DOING, stateMachineId, startId, NodeType.CUSTOM, TransformType.CUSTOM);
    }
}
