package io.choerodon.statemachine.api.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.exception.CommonException;
import io.choerodon.statemachine.api.dto.*;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StatusService;
import io.choerodon.statemachine.domain.StateMachineNode;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.StatusWithInfo;
import io.choerodon.statemachine.infra.cache.InstanceCache;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.enums.StatusType;
import io.choerodon.statemachine.infra.exception.RemoveStatusException;
import io.choerodon.statemachine.infra.mapper.*;
import io.choerodon.statemachine.infra.utils.EnumUtil;
import io.choerodon.statemachine.infra.utils.PageUtil;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@Service
public class StatusServiceImpl implements StatusService {

    @Autowired
    private StatusMapper statusMapper;
    @Autowired
    private StateMachineNodeDraftMapper nodeDraftMapper;
    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;
    @Autowired
    private StateMachineNodeService nodeService;
    @Autowired
    private StateMachineTransformDraftMapper transformDraftMapper;
    @Autowired
    private StateMachineTransformMapper transformDeployMapper;
    @Autowired
    private InstanceCache instanceCache;
    @Autowired
    private StateMachineMapper stateMachineMapper;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public PageInfo<StatusWithInfoDTO> queryStatusList(PageRequest pageRequest, Long organizationId, StatusSearchDTO statusSearchDTO) {
        PageInfo<Long> statusIdsPage = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), pageRequest.getSort().toSql())
                .doSelectPageInfo(() -> statusMapper.selectStatusIds(organizationId, statusSearchDTO));
        List<StatusWithInfoDTO> statusWithInfoDTOList = new ArrayList<>();
        if (!statusIdsPage.getList().isEmpty()) {
            List<StatusWithInfo> statuses = statusMapper.queryStatusList(organizationId, statusIdsPage.getList());
            statusWithInfoDTOList = modelMapper.map(statuses, new TypeToken<List<StatusWithInfoDTO>>() {
            }.getType());
        }
        return PageUtil.buildPageInfoWithPageInfoList(statusIdsPage, statusWithInfoDTOList);
    }

    @Override
    public StatusDTO create(Long organizationId, StatusDTO statusDTO) {
        if (checkName(organizationId, statusDTO.getName()).getStatusExist()) {
            throw new CommonException("error.statusName.exist");
        }
        if (!EnumUtil.contain(StatusType.class, statusDTO.getType())) {
            throw new CommonException("error.status.type.illegal");
        }
        statusDTO.setOrganizationId(organizationId);
        Status status = modelMapper.map(statusDTO, Status.class);
        List<Status> select = statusMapper.select(status);
        if (select.isEmpty()) {
            int isInsert = statusMapper.insert(status);
            if (isInsert != 1) {
                throw new CommonException("error.status.create");
            }
        } else {
            status = select.get(0);
        }
        status = statusMapper.queryById(organizationId, status.getId());
        return modelMapper.map(status, StatusDTO.class);
    }

    private Boolean checkNameUpdate(Long organizationId, Long statusId, String name) {
        Status status = new Status();
        status.setOrganizationId(organizationId);
        status.setName(name);
        Status res = statusMapper.selectOne(status);
        return res != null && !statusId.equals(res.getId());
    }

    @Override
    public StatusDTO update(StatusDTO statusDTO) {
        if (checkNameUpdate(statusDTO.getOrganizationId(), statusDTO.getId(), statusDTO.getName())) {
            throw new CommonException("error.statusName.exist");
        }
        if (!EnumUtil.contain(StatusType.class, statusDTO.getType())) {
            throw new CommonException("error.status.type.illegal");
        }
        Status status = modelMapper.map(statusDTO, Status.class);
        int isUpdate = statusMapper.updateByPrimaryKeySelective(status);
        if (isUpdate != 1) {
            throw new CommonException("error.status.update");
        }
        status = statusMapper.queryById(status.getOrganizationId(), status.getId());
        return modelMapper.map(status, StatusDTO.class);
    }

    @Override
    public Boolean delete(Long organizationId, Long statusId) {
        Status status = statusMapper.queryById(organizationId, statusId);
        if (status == null) {
            throw new CommonException("error.status.delete.nofound");
        }
        Long draftUsed = nodeDraftMapper.checkStateDelete(organizationId, statusId);
        Long deployUsed = nodeDeployMapper.checkStateDelete(organizationId, statusId);
        if (draftUsed != 0 || deployUsed != 0) {
            throw new CommonException("error.status.delete");
        }
        if (status.getCode() != null) {
            throw new CommonException("error.status.illegal");
        }
        int isDelete = statusMapper.deleteByPrimaryKey(statusId);
        if (isDelete != 1) {
            throw new CommonException("error.status.delete");
        }
        return true;
    }

    @Override
    public StatusInfoDTO queryStatusById(Long organizationId, Long stateId) {
        Status status = statusMapper.queryById(organizationId, stateId);
        if (status == null) {
            throw new CommonException("error.queryStatusById.notExist");
        }
        return modelMapper.map(status, StatusInfoDTO.class);
    }

    @Override
    public List<StatusDTO> queryAllStatus(Long organizationId) {
        Status status = new Status();
        status.setOrganizationId(organizationId);
        List<Status> statuses = statusMapper.select(status);
        return modelMapper.map(statuses, new TypeToken<List<StatusDTO>>() {
        }.getType());
    }

    @Override
    public Map<Long, StatusMapDTO> queryAllStatusMap(Long organizationId) {
        Status status = new Status();
        status.setOrganizationId(organizationId);
        List<Status> statuses = statusMapper.select(status);
        Map<Long, StatusMapDTO> statusMap = new HashMap<>();
        for (Status sta : statuses) {
            StatusMapDTO statusMapDTO = modelMapper.map(sta, new TypeToken<StatusMapDTO>() {
            }.getType());
            statusMap.put(statusMapDTO.getId(), statusMapDTO);
        }
        return statusMap;
    }

    @Override
    public StatusCheckDTO checkName(Long organizationId, String name) {
        Status status = new Status();
        status.setOrganizationId(organizationId);
        status.setName(name);
        Status res = statusMapper.selectOne(status);
        StatusCheckDTO statusCheckDTO = new StatusCheckDTO();
        if (res != null) {
            statusCheckDTO.setStatusExist(true);
            statusCheckDTO.setId(res.getId());
            statusCheckDTO.setName(res.getName());
            statusCheckDTO.setType(res.getType());
        } else {
            statusCheckDTO.setStatusExist(false);
        }
        return statusCheckDTO;
    }

    @Override
    public Map<Long, Status> batchStatusGet(List<Long> ids) {
        if (!ids.isEmpty()) {
            List<Status> statuses = statusMapper.batchStatusGet(ids);
            Map<Long, Status> map = new HashMap();
            for (Status status : statuses) {
                map.put(status.getId(), status);
            }
            return map;
        } else {
            return new HashMap<>();
        }

    }

    @Override
    public StatusDTO createStatusForAgile(Long organizationId, Long stateMachineId, StatusDTO statusDTO) {
        if (stateMachineId == null) {
            throw new CommonException("error.stateMachineId.notNull");
        }
        if (stateMachineMapper.queryById(organizationId, stateMachineId) == null) {
            throw new CommonException("error.stateMachine.notFound");
        }

        String statusName = statusDTO.getName();
        Status select = new Status();
        select.setName(statusName);
        select.setOrganizationId(organizationId);
        Status status = statusMapper.selectOne(select);
        if (status == null) {
            statusDTO = create(organizationId, statusDTO);
        } else {
            statusDTO = modelMapper.map(status, StatusDTO.class);
        }
        //将状态加入状态机中，直接加到发布表中
        nodeService.createNodeAndTransformForAgile(organizationId, stateMachineId, statusDTO);
        //清理状态机实例
        instanceCache.cleanStateMachine(stateMachineId);
        return statusDTO;
    }

    @Override
    public void removeStatusForAgile(Long organizationId, Long stateMachineId, Long statusId) {
        if (statusId == null) {
            throw new CommonException("error.statusId.notNull");
        }
        StateMachineNode stateNode = new StateMachineNode();
        stateNode.setOrganizationId(organizationId);
        stateNode.setStateMachineId(stateMachineId);
        stateNode.setStatusId(statusId);
        StateMachineNode res = nodeDeployMapper.selectOne(stateNode);
        if (res == null) {
            throw new RemoveStatusException("error.status.exist");
        }
        if (res.getType().equals(NodeType.INIT)) {
            throw new RemoveStatusException("error.status.illegal");
        }
        if (res.getId() != null) {
            //删除节点
            nodeDeployMapper.deleteByPrimaryKey(res.getId());
            //删除节点关联的转换
            transformDeployMapper.deleteByNodeId(res.getId());
            //删除节点
            nodeDraftMapper.deleteByPrimaryKey(res.getId());
            //删除节点关联的转换
            transformDraftMapper.deleteByNodeId(res.getId());
        }
        //清理状态机实例
        instanceCache.cleanStateMachine(stateMachineId);
    }

    @Override
    public List<StatusDTO> queryByStateMachineIds(Long organizationId, List<Long> stateMachineIds) {
        if (!stateMachineIds.isEmpty()) {
            List<Status> statuses = statusMapper.queryByStateMachineIds(organizationId, stateMachineIds);
            return modelMapper.map(statuses, new TypeToken<List<StatusDTO>>() {
            }.getType());
        }
        return Collections.emptyList();
    }
}
