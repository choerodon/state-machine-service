package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.statemachine.api.dto.*;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.service.StatusService;
import io.choerodon.statemachine.domain.StateMachineInfo;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.StatusWithInfo;
import io.choerodon.statemachine.infra.enums.StatusType;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDraftMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import io.choerodon.statemachine.infra.mapper.StatusMapper;
import io.choerodon.statemachine.infra.utils.EnumUtil;
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
    private StateMachineNodeDraftMapper nodeMapper;
    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;
    @Autowired
    private StateMachineNodeService nodeService;
    @Autowired
    private StateMachineService stateMachineService;

    private ModelMapper modelMapper = new ModelMapper();

//    @Override
//    public Page<StatusDTO> pageQuery(PageRequest pageRequest, StatusDTO statusDTO, String param) {
//        Status status = modelMapper.map(statusDTO, Status.class);
//        Page<Status> page = PageHelper.doPageAndSort(pageRequest,
//                () -> statusMapper.fulltextSearch(status, param));
//        List<Status> statuses = page.getContent();
//        List<StatusDTO> statusDTOs = modelMapper.map(statuses, new TypeToken<List<StatusDTO>>() {
//        }.getType());
//        for (StatusDTO dto : statusDTOs) {
//            //该状态已被草稿状态机使用个数
//            Long draftUsed = nodeMapper.checkStateDelete(dto.getOrganizationId(), dto.getId());
//            //该状态已被发布状态机使用个数
//            Long deployUsed = nodeDeployMapper.checkStateDelete(dto.getOrganizationId(), dto.getId());
//            if (draftUsed == 0 && deployUsed == 0) {
//                dto.setCanDelete(true);
//            } else {
//                dto.setCanDelete(false);
//            }
//        }
//        Page<StatusDTO> returnPage = new Page<>();
//        returnPage.setContent(statusDTOs);
//        returnPage.setNumber(page.getNumber());
//        returnPage.setNumberOfElements(page.getNumberOfElements());
//        returnPage.setSize(page.getSize());
//        returnPage.setTotalElements(page.getTotalElements());
//        returnPage.setTotalPages(page.getTotalPages());
//        return returnPage;
//    }

    private void removeDuplicate(List<StatusWithInfo> statuses) {
        for (StatusWithInfo statusWithInfo : statuses) {
            List<StateMachineInfo> stateMachineInfoList = statusWithInfo.getStateMachineInfoList();
            List<StateMachineInfo> res = new ArrayList<>();
            List<Long> ids = new ArrayList<>();
            for (StateMachineInfo stateMachineInfo : stateMachineInfoList) {
                if (!ids.contains(stateMachineInfo.getStateMachineId())) {
                    res.add(stateMachineInfo);
                    ids.add(stateMachineInfo.getStateMachineId());
                }
            }
            statusWithInfo.setStateMachineInfoList(res);
        }
    }

    @Override
    public Page<StatusWithInfoDTO> queryStatusList(PageRequest pageRequest, Long organizationId, StatusSearchDTO statusSearchDTO) {
        Page<Long> statusIdsPage = PageHelper.doPageAndSort(pageRequest, () -> statusMapper.selectStatusIds(organizationId, statusSearchDTO));
        List<StatusWithInfo> statuses = statusMapper.queryStatusList(organizationId, statusIdsPage.getContent());
//        removeDuplicate(statuses);
        List<StatusWithInfoDTO> statusWithInfoDTOList = modelMapper.map(statuses, new TypeToken<List<StatusWithInfoDTO>>() {
        }.getType());
        Page<StatusWithInfoDTO> returnPage = new Page<>();
        returnPage.setContent(statusWithInfoDTOList);
        returnPage.setNumber(statusIdsPage.getNumber());
        returnPage.setNumberOfElements(statusIdsPage.getNumberOfElements());
        returnPage.setSize(statusIdsPage.getSize());
        returnPage.setTotalElements(statusIdsPage.getTotalElements());
        returnPage.setTotalPages(statusIdsPage.getTotalPages());
        return returnPage;
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
        int isInsert = statusMapper.insert(status);
        if (isInsert != 1) {
            throw new CommonException("error.status.create");
        }
        status = statusMapper.queryById(organizationId, status.getId());
        return modelMapper.map(status, StatusDTO.class);
    }

    @Override
    public StatusDTO update(StatusDTO statusDTO) {
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
        Long draftUsed = nodeMapper.checkStateDelete(organizationId, statusId);
        Long deployUsed = nodeDeployMapper.checkStateDelete(organizationId, statusId);
        if (draftUsed != 0 || deployUsed != 0) {
            throw new CommonException("error.status.delete");
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
        List<Status> statuses = statusMapper.batchStatusGet(ids);
        Map<Long, Status> map = new HashMap();
        for (Status status : statuses) {
            map.put(status.getId(), status);
        }
        return map;
    }

    @Override
    public StatusDTO createStatusForAgile(Long organizationId, Long stateMachineId, StatusDTO statusDTO) {
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
        //将状态加入状态机中
        nodeService.createNodeForAgile(organizationId, stateMachineId, statusDTO.getId());
        //发布状态机
        stateMachineService.deploy(organizationId, stateMachineId, false);
        return statusDTO;
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
