package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateDTO;
import io.choerodon.statemachine.api.service.StateService;
import io.choerodon.statemachine.domain.State;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeDeployMapper;
import io.choerodon.statemachine.infra.mapper.StateMachineNodeMapper;
import io.choerodon.statemachine.infra.mapper.StateMapper;
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
public class StateServiceImpl extends BaseServiceImpl<State> implements StateService {

    @Autowired
    private StateMapper stateMapper;

    @Autowired
    private StateMachineNodeMapper nodeMapper;

    @Autowired
    private StateMachineNodeDeployMapper nodeDeployMapper;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public Page<StateDTO> pageQuery(PageRequest pageRequest, StateDTO stateDTO, String param) {
        State state = modelMapper.map(stateDTO, State.class);
        Page<State> page = PageHelper.doPageAndSort(pageRequest, () -> stateMapper.fulltextSearch(state, param));
        List<State> states = page.getContent();
        List<StateDTO> stateDTOS = modelMapper.map(states, new TypeToken<List<StateDTO>>() {
        }.getType());
        for (StateDTO dto : stateDTOS) {
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
        Page<StateDTO> returnPage = new Page<>();
        returnPage.setContent(stateDTOS);
        returnPage.setNumber(page.getNumber());
        returnPage.setNumberOfElements(page.getNumberOfElements());
        returnPage.setSize(page.getSize());
        returnPage.setTotalElements(page.getTotalElements());
        returnPage.setTotalPages(page.getTotalPages());
        return returnPage;
    }

    @Override
    public StateDTO create(Long organizationId, StateDTO stateDTO) {
        stateDTO.setOrganizationId(organizationId);
        State state = modelMapper.map(stateDTO, State.class);
        int isInsert = stateMapper.insert(state);
        if (isInsert != 1) {
            throw new CommonException("error.state.create");
        }
        state = stateMapper.queryById(organizationId, state.getId());
        return modelMapper.map(state, StateDTO.class);
    }

    @Override
    public StateDTO update(StateDTO stateDTO) {
        State state = modelMapper.map(stateDTO, State.class);
        int isUpdate = stateMapper.updateByPrimaryKeySelective(state);
        if (isUpdate != 1) {
            throw new CommonException("error.state.update");
        }
        state = stateMapper.queryById(state.getOrganizationId(), state.getId());
        return modelMapper.map(state, StateDTO.class);
    }

    @Override
    public Boolean delete(Long organizationId, Long stateId) {
        State state = stateMapper.queryById(organizationId, stateId);
        if (state == null) {
            throw new CommonException("error.state.delete.nofound");
        }
        Long draftUsed = nodeMapper.checkStateDelete(organizationId, stateId);
        Long deployUsed = nodeDeployMapper.checkStateDelete(organizationId, stateId);
        if (draftUsed != 0 || deployUsed != 0) {
            throw new CommonException("error.state.delete");
        }
        int isDelete = stateMapper.deleteByPrimaryKey(stateId);
        if (isDelete != 1) {
            throw new CommonException("error.state.delete");
        }
        return true;
    }

    @Override
    public StateDTO queryStateById(Long organizationId, Long stateId) {
        State state = stateMapper.queryById(organizationId, stateId);
        return state != null ? modelMapper.map(state, StateDTO.class) : null;
    }

    @Override
    public List<StateDTO> queryAllState(Long organizationId) {
        State state = new State();
        state.setOrganizationId(organizationId);
        List<State> states = stateMapper.select(state);
        return modelMapper.map(states, new TypeToken<List<StateDTO>>() {
        }.getType());
    }

    @Override
    public Boolean checkName(Long organizationId, Long stateId, String name) {
        State state = new State();
        state.setOrganizationId(organizationId);
        state.setName(name);
        state = stateMapper.selectOne(state);
        if (state != null) {
            //若传了id，则为更新校验（更新校验不校验本身），不传为创建校验
            return state.getId().equals(stateId);
        }
        return true;
    }

}
