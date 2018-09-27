package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.app.assembler.StateMachineAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.enums.StateMachineStatus;
import io.choerodon.statemachine.infra.enums.TransfType;
import io.choerodon.statemachine.infra.factory.MachineFactory;
import io.choerodon.statemachine.infra.mapper.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class StateMachineServiceImpl extends BaseServiceImpl<StateMachine> implements StateMachineService {

    @Autowired
    private StateMachineMapper stateMachineMapper;

    @Autowired
    private StateMachineNodeMapper nodeMapper;

    @Autowired
    private StateMachineTransfMapper transfMapper;

    @Autowired
    private StateMachineNodeDeployMapper nodeDeployMapper;

    @Autowired
    private StateMachineTransfDeployMapper transfDeployMapper;

    @Autowired
    private StateMachineConfigMapper configMapper;

    @Autowired
    private StateMachineConfigDeployMapper configDeployMapper;
    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;
    @Autowired
    private StateMachineAssembler stateMachineAssembler;

    @Autowired
    private MachineFactory machineFactory;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public Page<StateMachineDTO> pageQuery(PageRequest pageRequest, StateMachineDTO stateMachineDTO, String param) {
        StateMachine stateMachine = modelMapper.map(stateMachineDTO, StateMachine.class);
        Page<StateMachine> page = PageHelper.doPageAndSort(pageRequest,
                () -> stateMachineMapper.fulltextSearch(stateMachine, param));
        List<StateMachine> schemes = page.getContent();
        List<StateMachineDTO> stateMachineDTOS = modelMapper.map(schemes, new TypeToken<List<StateMachineDTO>>() {
        }.getType());
        Page<StateMachineDTO> returnPage = new Page<>();
        returnPage.setContent(stateMachineDTOS);
        returnPage.setNumber(page.getNumber());
        returnPage.setNumberOfElements(page.getNumberOfElements());
        returnPage.setSize(page.getSize());
        returnPage.setTotalElements(page.getTotalElements());
        returnPage.setTotalPages(page.getTotalPages());
        return returnPage;
    }

    @Override
    public StateMachineDTO create(Long organizationId, StateMachineDTO stateMachineDTO) {
        stateMachineDTO.setId(null);
        stateMachineDTO.setStatus(StateMachineStatus.CREATE);
        stateMachineDTO.setOrganizationId(organizationId);
        StateMachine stateMachine = modelMapper.map(stateMachineDTO, StateMachine.class);
        int isInsert = stateMachineMapper.insertSelective(stateMachine);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachine.create");
        }

        //创建默认开始节点
        StateMachineNode startNode = new StateMachineNode();
        startNode.setStateMachineId(stateMachine.getId());
        startNode.setPositionX(25L);
        startNode.setPositionY(0L);
        startNode.setWidth(50L);
        startNode.setHeight(50L);
        startNode.setStatusId(0L);
        startNode.setType(NodeType.START);
        int isStartNodeInsert = nodeMapper.insert(startNode);
        if (isStartNodeInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }

        //创建默认的节点和转换
        StateMachineNode node = new StateMachineNode();
        node.setStateMachineId(stateMachine.getId());
        //TODO 初始化默认状态
        node.setStatusId(1L);
        node.setPositionX(0L);
        node.setPositionY(120L);
        node.setWidth(100L);
        node.setHeight(50L);
        node.setType(NodeType.INIT);
        int isNodeInsert = nodeMapper.insert(node);
        if (isNodeInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }

        StateMachineTransf transf = new StateMachineTransf();
        transf.setStateMachineId(stateMachine.getId());
        transf.setStartNodeId(startNode.getId());
        transf.setEndNodeId(node.getId());
        transf.setType(TransfType.INIT);
        int isTransfInsert = transfMapper.insert(transf);
        if (isTransfInsert != 1) {
            throw new CommonException("error.stateMachineTransf.create");
        }
        return queryStateMachineWithConfigById(organizationId, stateMachine.getId());
    }

    @Override
    public StateMachineDTO update(Long organizationId, Long stateMachineId, StateMachineDTO stateMachineDTO) {
        StateMachine stateMachine = modelMapper.map(stateMachineDTO, StateMachine.class);
        stateMachine.setId(stateMachineId);
        int isUpdate = stateMachineMapper.updateByPrimaryKeySelective(stateMachine);
        if (isUpdate != 1) {
            throw new CommonException("error.stateMachine.update");
        }
        stateMachine = stateMachineMapper.queryById(organizationId, stateMachine.getId());
        return modelMapper.map(stateMachine, StateMachineDTO.class);
    }

    @Override
    public Boolean delete(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.delete.noFound");
        }
        int isDelete = stateMachineMapper.deleteByPrimaryKey(stateMachineId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachine.delete");
        }
        //删除节点
        StateMachineNode node = new StateMachineNode();
        node.setStateMachineId(stateMachineId);
        nodeMapper.delete(node);
        //删除转换
        StateMachineTransf transf = new StateMachineTransf();
        transf.setStateMachineId(stateMachineId);
        transfMapper.delete(transf);
        //删除发布节点
        StateMachineNodeDeploy nodeDeploy = new StateMachineNodeDeploy();
        nodeDeploy.setStateMachineId(stateMachineId);
        nodeDeployMapper.delete(nodeDeploy);
        //删除发布转换
        StateMachineTransfDeploy transfDeploy = new StateMachineTransfDeploy();
        transfDeploy.setStateMachineId(stateMachineId);
        transfDeployMapper.delete(transfDeploy);
        //删除配置
        StateMachineConfig config = new StateMachineConfig();
        config.setStateMachineId(stateMachineId);
        configMapper.delete(config);
        //删除发布配置
        StateMachineConfigDeploy configDeploy = new StateMachineConfigDeploy();
        configDeploy.setStateMachineId(stateMachineId);
        configDeployMapper.delete(configDeploy);
        return true;
    }

    @Override
    public StateMachineDTO deploy(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (null == stateMachine) {
            throw new CommonException("stateMachine.deploy.no.found");
        }
        if (StateMachineStatus.ACTIVE.equals(stateMachine.getStatus())) {
            throw new CommonException("stateMachine.status.deployed");
        }
        stateMachine.setStatus(StateMachineStatus.ACTIVE);
        int stateMachineDeploy = stateMachineMapper.updateByPrimaryKeySelective(stateMachine);
        if (stateMachineDeploy != 1) {
            throw new CommonException("error.stateMachine.deploy");
        }
        //删除上一版本的节点
        StateMachineNodeDeploy nodeDeploy = new StateMachineNodeDeploy();
        nodeDeploy.setStateMachineId(stateMachineId);
        nodeDeployMapper.delete(nodeDeploy);
        //删除上一版本的转换
        StateMachineTransfDeploy transfDeploy = new StateMachineTransfDeploy();
        transfDeploy.setStateMachineId(stateMachineId);
        transfDeployMapper.delete(transfDeploy);
        //删除上一版本的配置
        StateMachineConfigDeploy configDeploy = new StateMachineConfigDeploy();
        configDeploy.setStateMachineId(stateMachineId);
        configDeployMapper.delete(configDeploy);
        //写入发布的节点
        StateMachineNode node = new StateMachineNode();
        node.setStateMachineId(stateMachineId);
        List<StateMachineNode> nodes = nodeMapper.select(node);
        if (nodes != null && !nodes.isEmpty()) {
            List<StateMachineNodeDeploy> nodeDeploys = stateMachineNodeAssembler.toTargetList(nodes, StateMachineNodeDeploy.class);
            int nodeDeployInsert = nodeDeployMapper.insertList(nodeDeploys);
            if (nodeDeployInsert < 1) {
                throw new CommonException("error.stateMachineNodeDeploy.create");
            }
        }
        //写入发布的转换
        StateMachineTransf transf = new StateMachineTransf();
        transf.setStateMachineId(stateMachineId);
        List<StateMachineTransf> transfs = transfMapper.select(transf);
        if (transfs != null && !transfs.isEmpty()) {
            List<StateMachineTransfDeploy> transfDeploys = modelMapper.map(transfs, new TypeToken<List<StateMachineTransfDeploy>>() {
            }.getType());
            int transfDeployInsert = transfDeployMapper.insertList(transfDeploys);
            if (transfDeployInsert < 1) {
                throw new CommonException("error.stateMachineTransfDeploy.create");
            }
        }
        //写入发布的配置
        StateMachineConfig config = new StateMachineConfig();
        config.setStateMachineId(stateMachineId);
        List<StateMachineConfig> configs = configMapper.select(config);
        if (configs != null && !configs.isEmpty()) {
            List<StateMachineConfigDeploy> configDeploys = modelMapper.map(configs, new TypeToken<List<StateMachineConfigDeploy>>() {
            }.getType());
            int configDeployInsert = configDeployMapper.insertList(configDeploys);
            if (configDeployInsert < 1) {
                throw new CommonException("error.stateMachineConfigDeploy.create");
            }
        }
        //清理内存中的旧状态机构建器与实例
        machineFactory.deployStateMachine(stateMachineId);
        return queryStateMachineWithConfigById(organizationId, stateMachine.getId());
    }

    @Override
    public StateMachineDTO queryStateMachineWithConfigById(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.nofound");
        }
        //获取节点
        List<StateMachineNode> nodes = nodeMapper.selectByStateMachineId(stateMachineId);
        stateMachine.setStateMachineNodes(nodes);
        //获取转换
        StateMachineTransf stateMachineTransf = new StateMachineTransf();
        stateMachineTransf.setStateMachineId(stateMachineId);
        List<StateMachineTransf> transfs = transfMapper.select(stateMachineTransf);
        stateMachine.setStateMachineTransfs(transfs);
        StateMachineDTO dto = stateMachineAssembler.covertStateMachine(stateMachine);
        List<StateMachineTransfDTO> transfDTOS = dto.getTransfDTOs();
        for (StateMachineTransfDTO transfDTO : transfDTOS) {
            List<StateMachineConfigDTO> conditions = new ArrayList<>();
            List<StateMachineConfigDTO> validators = new ArrayList<>();
            List<StateMachineConfigDTO> triggers = new ArrayList<>();
            List<StateMachineConfigDTO> postpositions = new ArrayList<>();
            List<StateMachineConfigDTO> dtoList = new ArrayList<>();
            StateMachineConfig config = new StateMachineConfig();
            config.setTransfId(transfDTO.getId());
            List<StateMachineConfig> list = configMapper.select(config);
            if (list != null && !list.isEmpty()) {
                dtoList = modelMapper.map(list, new TypeToken<List<StateMachineConfigDTO>>() {
                }.getType());
                for (StateMachineConfigDTO configDto : dtoList) {
                    if (ConfigType.CONDITION.equals(configDto.getType())) {
                        conditions.add(configDto);
                    } else if (ConfigType.VALIDATOR.equals(configDto.getType())) {
                        validators.add(configDto);
                    } else if (ConfigType.TRIGGER.equals(configDto.getType())) {
                        triggers.add(configDto);
                    } else {
                        postpositions.add(configDto);
                    }
                }
                transfDTO.setConditions(conditions);
                transfDTO.setValidators(validators);
                transfDTO.setTriggers(triggers);
                transfDTO.setPostpositions(postpositions);
            }
        }
        dto.setTransfDTOs(transfDTOS);
        return dto;
    }

    @Override
    public StateMachine getOriginalById(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.notExist");
        }
        //获取原件节点
        List<StateMachineNodeDeploy> nodeDeploys = nodeDeployMapper.selectByStateMachineId(stateMachineId);
        if (nodeDeploys != null && !nodeDeploys.isEmpty()) {
            List<StateMachineNode> nodes = stateMachineNodeAssembler.toTargetList(nodeDeploys, StateMachineNode.class);
            stateMachine.setStateMachineNodes(nodes);
        }
        //获取原件转换
        StateMachineTransfDeploy transfDeploy = new StateMachineTransfDeploy();
        transfDeploy.setStateMachineId(stateMachineId);
        List<StateMachineTransfDeploy> transfDeploys = transfDeployMapper.select(transfDeploy);
        if (transfDeploys != null && !transfDeploys.isEmpty()) {
            List<StateMachineTransf> transfs = modelMapper.map(transfDeploys, new TypeToken<List<StateMachineTransf>>() {
            }.getType());
            stateMachine.setStateMachineTransfs(transfs);
        }
        return stateMachine;
    }

    @Override
    public StateMachineDTO queryOriginalById(Long organizationId, Long stateMachineId) {
        StateMachineDTO dto = stateMachineAssembler.covertStateMachine(getOriginalById(organizationId, stateMachineId));
        List<StateMachineTransfDTO> transfDTOS = dto.getTransfDTOs();
        for (StateMachineTransfDTO transfDTO : transfDTOS) {
            List<StateMachineConfigDTO> conditions = new ArrayList<>();
            List<StateMachineConfigDTO> validators = new ArrayList<>();
            List<StateMachineConfigDTO> triggers = new ArrayList<>();
            List<StateMachineConfigDTO> postpositions = new ArrayList<>();
            List<StateMachineConfigDTO> dtoList = new ArrayList<>();
            StateMachineConfigDeploy configDeploy = new StateMachineConfigDeploy();
            configDeploy.setTransfId(transfDTO.getId());
            List<StateMachineConfigDeploy> list = configDeployMapper.select(configDeploy);
            if (list != null && !list.isEmpty()) {
                dtoList = modelMapper.map(list, new TypeToken<List<StateMachineConfigDTO>>() {
                }.getType());
                for (StateMachineConfigDTO configDto : dtoList) {
                    if (ConfigType.CONDITION.equals(configDto.getType())) {
                        conditions.add(configDto);
                    } else if (ConfigType.VALIDATOR.equals(configDto.getType())) {
                        validators.add(configDto);
                    } else if (ConfigType.TRIGGER.equals(configDto.getType())) {
                        triggers.add(configDto);
                    } else {
                        postpositions.add(configDto);
                    }
                }
                transfDTO.setConditions(conditions);
                transfDTO.setValidators(validators);
                transfDTO.setTriggers(triggers);
                transfDTO.setPostpositions(postpositions);
            }
        }
        dto.setTransfDTOs(transfDTOS);
        return dto;
    }

    @Override
    public StateMachineDTO deleteDraft(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (null == stateMachine) {
            throw new CommonException("stateMachine.deleteDraft.no.found");
        }
        if (!StateMachineStatus.DRAFT.equals(stateMachine.getStatus())) {
            throw new CommonException("stateMachine.status.is.not.draft");
        }
        stateMachine.setStatus(StateMachineStatus.DRAFT);
        int stateMachineDeploy = stateMachineMapper.updateByPrimaryKeySelective(stateMachine);
        if (stateMachineDeploy != 1) {
            throw new CommonException("error.stateMachine.deleteDraft");
        }
        //删除草稿节点
        StateMachineNode node = new StateMachineNode();
        node.setStateMachineId(stateMachineId);
        nodeMapper.delete(node);
        //删除草稿转换
        StateMachineTransf transf = new StateMachineTransf();
        transf.setStateMachineId(stateMachineId);
        transfMapper.delete(transf);
        //删除草稿配置
        StateMachineConfig config = new StateMachineConfig();
        config.setStateMachineId(stateMachineId);
        configMapper.delete(config);
        //写入活跃的节点
        StateMachineNodeDeploy nodeDeploy = new StateMachineNodeDeploy();
        nodeDeploy.setStateMachineId(stateMachineId);
        List<StateMachineNodeDeploy> nodeDeploys = nodeDeployMapper.select(nodeDeploy);
        if (nodeDeploys != null && !nodeDeploys.isEmpty()) {
            List<StateMachineNode> nodes = stateMachineNodeAssembler.toTargetList(nodeDeploys, StateMachineNode.class);
            for (StateMachineNode insertNode : nodes) {
                int nodeInsert = nodeMapper.insertWithId(insertNode);
                if (nodeInsert < 1) {
                    throw new CommonException("error.stateMachineNode.create");
                }
            }
        }
        //写入活跃的转换
        StateMachineTransfDeploy transfDeploy = new StateMachineTransfDeploy();
        transfDeploy.setStateMachineId(stateMachineId);
        List<StateMachineTransfDeploy> transfDeploys = transfDeployMapper.select(transfDeploy);
        if (transfDeploys != null && !transfDeploys.isEmpty()) {
            List<StateMachineTransf> transfInserts = modelMapper.map(transfDeploys, new TypeToken<List<StateMachineTransf>>() {
            }.getType());
            for (StateMachineTransf insertTransf : transfInserts) {
                int transfInsert = transfMapper.insertWithId(insertTransf);
                if (transfInsert < 1) {
                    throw new CommonException("error.stateMachineTransf.create");
                }
            }
        }
        //写入活跃的配置
        StateMachineConfigDeploy configDeploy = new StateMachineConfigDeploy();
        configDeploy.setStateMachineId(stateMachineId);
        List<StateMachineConfigDeploy> configDeploys = configDeployMapper.select(configDeploy);
        if (configDeploys != null && !configDeploys.isEmpty()) {
            List<StateMachineConfig> configs = modelMapper.map(configDeploys, new TypeToken<List<StateMachineConfig>>() {
            }.getType());
            for (StateMachineConfig insertConfig : configs) {
                int configInsert = configMapper.insertWithId(insertConfig);
                if (configInsert < 1) {
                    throw new CommonException("error.stateMachineCreate.create");
                }
            }
        }
        return queryStateMachineWithConfigById(organizationId, stateMachine.getId());
    }

    @Override
    public StateMachineDTO queryStateMachineById(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine != null) {
            return modelMapper.map(stateMachine, StateMachineDTO.class);
        }
        return null;
    }

    @Override
    public List<StateMachineDTO> queryAll(Long organizationId) {
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        List<StateMachine> list = stateMachineMapper.select(stateMachine);
        return modelMapper.map(list, new TypeToken<List<StateMachineDTO>>() {
        }.getType());
    }

    @Override
    public void updateStateMachineStatus(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine != null && stateMachine.getStatus().equals(StateMachineStatus.ACTIVE)) {
            stateMachine.setStatus(StateMachineStatus.DRAFT);
            int stateMachineUpdate = stateMachineMapper.updateByPrimaryKey(stateMachine);
            if (stateMachineUpdate != 1) {
                throw new CommonException("error.stateMachine.update");
            }
        }
    }

    @Override
    public Boolean checkName(Long organizationId, Long stateMachineId, String name) {
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName(name);
        stateMachine = stateMachineMapper.selectOne(stateMachine);
        if (stateMachine != null) {
            //若传了id，则为更新校验（更新校验不校验本身），不传为创建校验
            return stateMachine.getId().equals(stateMachineId);
        }
        return true;
    }
}
