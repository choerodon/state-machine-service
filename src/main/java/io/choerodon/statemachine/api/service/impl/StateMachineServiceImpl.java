package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.dto.StateMachineDTO;
import io.choerodon.statemachine.api.dto.StateMachineNodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.service.StatusService;
import io.choerodon.statemachine.app.assembler.StateMachineAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineConfigAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.ConfigType;
import io.choerodon.statemachine.infra.enums.NodeType;
import io.choerodon.statemachine.infra.enums.StateMachineStatus;
import io.choerodon.statemachine.infra.enums.TransformType;
import io.choerodon.statemachine.infra.factory.MachineFactory;
import io.choerodon.statemachine.infra.mapper.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author peng.jiang,dinghuang123@gmail.com
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class StateMachineServiceImpl extends BaseServiceImpl<StateMachine> implements StateMachineService {

    private final String STATE_MACHINE_CREATE = "state_machine_create";

    @Autowired
    private StateMachineMapper stateMachineMapper;

    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;

    @Autowired
    private StateMachineNodeDraftMapper nodeDraftMapper;

    @Autowired
    private StateMachineTransformMapper transformDeployMapper;

    @Autowired
    private StateMachineTransformDraftMapper transformDraftMapper;

    @Autowired
    private StateMachineConfigMapper configDeployMapper;
    @Autowired
    private StateMachineConfigDraftMapper configDraftMapper;
    @Autowired
    private StateMachineNodeAssembler stateMachineNodeAssembler;
    @Autowired
    private StateMachineTransformAssembler stateMachineTransformAssembler;
    @Autowired
    private StateMachineConfigAssembler stateMachineConfigAssembler;
    @Autowired
    private StateMachineAssembler stateMachineAssembler;

    @Autowired
    private StatusService statusService;

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
        StateMachineNodeDraft startNode = new StateMachineNodeDraft();
        startNode.setStateMachineId(stateMachine.getId());
        startNode.setOrganizationId(organizationId);
        startNode.setPositionX(25L);
        startNode.setPositionY(0L);
        startNode.setWidth(50L);
        startNode.setHeight(50L);
        startNode.setStatusId(0L);
        startNode.setType(NodeType.START);
        int isStartNodeInsert = nodeDraftMapper.insert(startNode);
        if (isStartNodeInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }

        //创建默认的节点和转换
        StateMachineNodeDraft node = new StateMachineNodeDraft();
        node.setStateMachineId(stateMachine.getId());
        //TODO 初始化默认状态
        node.setStatusId(1L);
        node.setPositionX(0L);
        node.setPositionY(120L);
        node.setWidth(100L);
        node.setHeight(50L);
        node.setType(NodeType.INIT);
        node.setOrganizationId(organizationId);
        int isNodeInsert = nodeDraftMapper.insert(node);
        if (isNodeInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }

        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setStateMachineId(stateMachine.getId());
        transform.setStartNodeId(startNode.getId());
        transform.setEndNodeId(node.getId());
        transform.setType(TransformType.INIT);
        transform.setOrganizationId(organizationId);
        int isTransformInsert = transformDraftMapper.insert(transform);
        if (isTransformInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }
        return queryStateMachineWithConfigById(organizationId, stateMachine.getId(), true);
    }

    @Override
    public StateMachineDTO update(Long organizationId, Long stateMachineId, StateMachineDTO stateMachineDTO) {
        StateMachine stateMachine = modelMapper.map(stateMachineDTO, StateMachine.class);
        stateMachine.setId(stateMachineId);
        stateMachine.setOrganizationId(organizationId);
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
        StateMachineNodeDraft node = new StateMachineNodeDraft();
        node.setStateMachineId(stateMachineId);
        node.setOrganizationId(organizationId);
        nodeDraftMapper.delete(node);
        //删除转换
        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setStateMachineId(stateMachineId);
        transform.setOrganizationId(organizationId);
        transformDraftMapper.delete(transform);
        //删除配置
        StateMachineConfigDraft config = new StateMachineConfigDraft();
        config.setStateMachineId(stateMachineId);
        config.setOrganizationId(organizationId);
        configDraftMapper.delete(config);
        //删除发布节点
        StateMachineNode nodeDeploy = new StateMachineNode();
        nodeDeploy.setStateMachineId(stateMachineId);
        nodeDeploy.setOrganizationId(organizationId);
        nodeDeployMapper.delete(nodeDeploy);
        //删除发布转换
        StateMachineTransform transformDeploy = new StateMachineTransform();
        transformDeploy.setStateMachineId(stateMachineId);
        transformDeploy.setOrganizationId(organizationId);
        transformDeployMapper.delete(transformDeploy);
        //删除发布配置
        StateMachineConfig configDeploy = new StateMachineConfig();
        configDeploy.setStateMachineId(stateMachineId);
        configDeploy.setOrganizationId(organizationId);
        configDeployMapper.delete(configDeploy);

        return true;
    }

    @Override
    public StateMachineDTO deploy(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (null == stateMachine) {
            throw new CommonException("error.stateMachine.null");
        }
        if (StateMachineStatus.ACTIVE.equals(stateMachine.getStatus())) {
            throw new CommonException("error.stateMachine.status.deployed");
        }
        stateMachine.setStatus(StateMachineStatus.ACTIVE);
        int stateMachineDeploy = updateOptional(stateMachine, "status");
        if (stateMachineDeploy != 1) {
            throw new CommonException("error.stateMachine.deploy");
        }
        //删除上一版本的节点
        StateMachineNode nodeDeploy = new StateMachineNode();
        nodeDeploy.setStateMachineId(stateMachineId);
        nodeDeploy.setOrganizationId(organizationId);
        nodeDeployMapper.delete(nodeDeploy);
        //删除上一版本的转换
        StateMachineTransform transformDeploy = new StateMachineTransform();
        transformDeploy.setStateMachineId(stateMachineId);
        transformDeploy.setOrganizationId(organizationId);
        transformDeployMapper.delete(transformDeploy);
        //删除上一版本的配置
        StateMachineConfig configDeploy = new StateMachineConfig();
        configDeploy.setStateMachineId(stateMachineId);
        configDeploy.setOrganizationId(organizationId);
        configDeployMapper.delete(configDeploy);
        //写入发布的节点
        StateMachineNodeDraft node = new StateMachineNodeDraft();
        node.setStateMachineId(stateMachineId);
        node.setOrganizationId(organizationId);
        List<StateMachineNodeDraft> nodes = nodeDraftMapper.select(node);
        if (nodes != null && !nodes.isEmpty()) {
            List<StateMachineNode> nodeDeploys = stateMachineNodeAssembler.toTargetList(nodes, StateMachineNode.class);
            int nodeDeployInsert = nodeDeployMapper.insertList(nodeDeploys);
            if (nodeDeployInsert == 0) {
                throw new CommonException("error.stateMachineNodeDeploy.create");
            }
        }
        //写入发布的转换
        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setStateMachineId(stateMachineId);
        transform.setOrganizationId(organizationId);
        List<StateMachineTransformDraft> transforms = transformDraftMapper.select(transform);
        if (transforms != null && !transforms.isEmpty()) {
            List<StateMachineTransform> transformDeploys = modelMapper.map(transforms, new TypeToken<List<StateMachineTransform>>() {
            }.getType());
            int transformDeployInsert = transformDeployMapper.insertList(transformDeploys);
            if (transformDeployInsert == 0) {
                throw new CommonException("error.stateMachineTransformDeploy.create");
            }
        }
        //写入发布的配置
        StateMachineConfigDraft config = new StateMachineConfigDraft();
        config.setStateMachineId(stateMachineId);
        config.setOrganizationId(organizationId);
        List<StateMachineConfigDraft> configs = configDraftMapper.select(config);
        if (configs != null && !configs.isEmpty()) {
            List<StateMachineConfig> configDeploys = modelMapper.map(configs, new TypeToken<List<StateMachineConfig>>() {
            }.getType());
            int configDeployInsert = configDeployMapper.insertList(configDeploys);
            if (configDeployInsert == 0) {
                throw new CommonException("error.stateMachineConfigDeploy.create");
            }
        }
        //清理内存中的旧状态机构建器与实例
        machineFactory.deployStateMachine(stateMachineId);

        return queryStateMachineWithConfigById(organizationId, stateMachine.getId(), false);
    }

    @Override
    public StateMachineDTO queryStateMachineWithConfigById(Long organizationId, Long stateMachineId, Boolean isDraft) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.notFound");
        }
        List<StateMachineNodeDTO> nodeDTOS;
        List<StateMachineTransformDTO> transformDTOS;
        if (isDraft) {
            //获取节点
            List<StateMachineNodeDraft> nodes = nodeDraftMapper.selectByStateMachineId(stateMachineId);
            nodeDTOS = stateMachineNodeAssembler.toTargetList(nodes, StateMachineNodeDTO.class);
            //获取转换
            StateMachineTransformDraft select = new StateMachineTransformDraft();
            select.setStateMachineId(stateMachineId);
            select.setOrganizationId(organizationId);
            List<StateMachineTransformDraft> transforms = transformDraftMapper.select(select);
            transformDTOS = stateMachineTransformAssembler.toTargetList(transforms, StateMachineTransformDTO.class);
        } else {
            List<StateMachineNode> nodes = nodeDeployMapper.selectByStateMachineId(stateMachineId);
            nodeDTOS = stateMachineNodeAssembler.toTargetList(nodes, StateMachineNodeDTO.class);
            StateMachineTransform select = new StateMachineTransform();
            select.setStateMachineId(stateMachineId);
            select.setOrganizationId(organizationId);
            List<StateMachineTransform> transforms = transformDeployMapper.select(select);
            transformDTOS = stateMachineTransformAssembler.toTargetList(transforms, StateMachineTransformDTO.class);
        }


        StateMachineDTO stateMachineDTO = stateMachineAssembler.toTarget(stateMachine, StateMachineDTO.class);
        stateMachineDTO.setNodeDTOs(nodeDTOS);
        stateMachineDTO.setTransformDTOs(transformDTOS);

        //获取转换中的配置
        for (StateMachineTransformDTO transformDTO : transformDTOS) {
            List<StateMachineConfigDTO> configDTOS = null;
            if (isDraft) {
                StateMachineConfigDraft config = new StateMachineConfigDraft();
                config.setTransformId(transformDTO.getId());
                config.setOrganizationId(organizationId);
                List<StateMachineConfigDraft> configs = configDraftMapper.select(config);
                configDTOS = stateMachineConfigAssembler.toTargetList(configs, StateMachineConfigDTO.class);
            } else {
                StateMachineConfig config = new StateMachineConfig();
                config.setTransformId(transformDTO.getId());
                config.setOrganizationId(organizationId);
                List<StateMachineConfig> configs = configDeployMapper.select(config);
                configDTOS = stateMachineConfigAssembler.toTargetList(configs, StateMachineConfigDTO.class);
            }
            if (configDTOS != null && !configDTOS.isEmpty()) {
                Map<String, List<StateMachineConfigDTO>> map = configDTOS.stream().collect(Collectors.groupingBy(StateMachineConfigDTO::getType));
                transformDTO.setConditions(Optional.ofNullable(map.get(ConfigType.CONDITION)).orElse(Collections.emptyList()));
                transformDTO.setValidators(Optional.ofNullable(map.get(ConfigType.VALIDATOR)).orElse(Collections.emptyList()));
                transformDTO.setTriggers(Optional.ofNullable(map.get(ConfigType.TRIGGER)).orElse(Collections.emptyList()));
                transformDTO.setPostpositions(Optional.ofNullable(map.get(ConfigType.POSTPOSITION)).orElse(Collections.emptyList()));
            }
        }
        return stateMachineDTO;
    }

    @Override
    public StateMachine queryDeployForInstance(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.notExist");
        }
        if (stateMachine.getStatus().equals(StateMachineStatus.CREATE)) {
            throw new CommonException("error.buildInstance.stateMachine.inActive");
        }
        //获取原件节点
        List<StateMachineNode> nodeDeploys = nodeDeployMapper.selectByStateMachineId(stateMachineId);
        if (nodeDeploys != null && !nodeDeploys.isEmpty()) {
            List<StateMachineNode> nodes = stateMachineNodeAssembler.toTargetList(nodeDeploys, StateMachineNode.class);
            stateMachine.setNodes(nodes);
        }
        //获取原件转换
        StateMachineTransform transformDeploy = new StateMachineTransform();
        transformDeploy.setStateMachineId(stateMachineId);
        List<StateMachineTransform> transformDeploys = transformDeployMapper.select(transformDeploy);
        if (transformDeploys != null && !transformDeploys.isEmpty()) {
            List<StateMachineTransform> transforms = modelMapper.map(transformDeploys, new TypeToken<List<StateMachineTransform>>() {
            }.getType());
            stateMachine.setTransforms(transforms);
        }
        return stateMachine;
    }

    @Override
    public StateMachineDTO deleteDraft(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.deleteDraft.noFound");
        }
        int isDelete = stateMachineMapper.deleteByPrimaryKey(stateMachineId);
        if (isDelete != 1) {
            throw new CommonException("error.stateMachine.deleteDraft");
        }
        stateMachine.setStatus(StateMachineStatus.DRAFT);
        int stateMachineDeploy = updateOptional(stateMachine, "status");
        if (stateMachineDeploy != 1) {
            throw new CommonException("error.stateMachine.deleteDraft");
        }
        //删除草稿节点
        StateMachineNodeDraft node = new StateMachineNodeDraft();
        node.setStateMachineId(stateMachineId);
        node.setOrganizationId(organizationId);
        nodeDraftMapper.delete(node);
        //删除草稿转换
        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setStateMachineId(stateMachineId);
        transform.setOrganizationId(organizationId);
        transformDraftMapper.delete(transform);
        //删除草稿配置
        StateMachineConfigDraft config = new StateMachineConfigDraft();
        config.setStateMachineId(stateMachineId);
        config.setOrganizationId(organizationId);
        configDraftMapper.delete(config);
        //写入活跃的节点到草稿中，id一致
        StateMachineNode nodeDeploy = new StateMachineNode();
        nodeDeploy.setStateMachineId(stateMachineId);
        nodeDeploy.setOrganizationId(organizationId);
        List<StateMachineNode> nodeDeploys = nodeDeployMapper.select(nodeDeploy);
        if (nodeDeploys != null && !nodeDeploys.isEmpty()) {
            List<StateMachineNodeDraft> nodes = stateMachineNodeAssembler.toTargetList(nodeDeploys, StateMachineNodeDraft.class);
            for (StateMachineNodeDraft insertNode : nodes) {
                int nodeInsert = nodeDraftMapper.insert(insertNode);
                if (nodeInsert != 1) {
                    throw new CommonException("error.stateMachineNode.create");
                }
            }
        }
        //写入活跃的转换到草稿中，id一致
        StateMachineTransform transformDeploy = new StateMachineTransform();
        transformDeploy.setStateMachineId(stateMachineId);
        transformDeploy.setOrganizationId(organizationId);
        List<StateMachineTransform> transformDeploys = transformDeployMapper.select(transformDeploy);
        if (transformDeploys != null && !transformDeploys.isEmpty()) {
            List<StateMachineTransformDraft> transformInserts = modelMapper.map(transformDeploys, new TypeToken<List<StateMachineTransformDraft>>() {
            }.getType());
            for (StateMachineTransformDraft insertTransform : transformInserts) {
                int transformInsert = transformDraftMapper.insert(insertTransform);
                if (transformInsert != 1) {
                    throw new CommonException("error.stateMachineTransform.create");
                }
            }
        }
        //写入活跃的配置到草稿中，id一致
        StateMachineConfig configDeploy = new StateMachineConfig();
        configDeploy.setStateMachineId(stateMachineId);
        List<StateMachineConfig> configDeploys = configDeployMapper.select(configDeploy);
        if (configDeploys != null && !configDeploys.isEmpty()) {
            List<StateMachineConfigDraft> configs = modelMapper.map(configDeploys, new TypeToken<List<StateMachineConfigDraft>>() {
            }.getType());
            for (StateMachineConfigDraft insertConfig : configs) {
                int configInsert = configDraftMapper.insert(insertConfig);
                if (configInsert != 1) {
                    throw new CommonException("error.stateMachineCreate.create");
                }
            }
        }
        return queryStateMachineWithConfigById(organizationId, stateMachine.getId(), false);
    }

    @Override
    public StateMachineDTO queryStateMachineById(Long organizationId, Long stateMachineId) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        return stateMachine != null ? modelMapper.map(stateMachine, StateMachineDTO.class) : null;
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
            int stateMachineUpdate = updateOptional(stateMachine, "status");
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

    @Override
    public void initSystemStateMachine(Long organizationId) {
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName("默认状态机");
        stateMachine.setDescription("默认状态机");
        stateMachine.setStatus(STATE_MACHINE_CREATE);
        if(stateMachineMapper.insert(stateMachine) != 1) {
            throw new CommonException("error.stateMachine.insert");
        }

        //创建默认开始节点
        StateMachineNodeDraft startNode = new StateMachineNodeDraft();
        startNode.setStateMachineId(stateMachine.getId());
        startNode.setOrganizationId(organizationId);
        startNode.setPositionX(25L);
        startNode.setPositionY(0L);
        startNode.setWidth(50L);
        startNode.setHeight(50L);
        startNode.setStatusId(0L);
        startNode.setType(NodeType.START);
        int isStartNodeInsert = nodeDraftMapper.insert(startNode);
        if (isStartNodeInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }
        statusService.initSystemStateMachineDetail(organizationId, stateMachine.getId(), startNode.getId());

    }
}
