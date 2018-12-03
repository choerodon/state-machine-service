package io.choerodon.statemachine.api.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.statemachine.api.dto.*;
import io.choerodon.statemachine.api.service.StateMachineNodeService;
import io.choerodon.statemachine.api.service.StateMachineService;
import io.choerodon.statemachine.api.service.StatusService;
import io.choerodon.statemachine.app.assembler.StateMachineAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineConfigAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineNodeAssembler;
import io.choerodon.statemachine.app.assembler.StateMachineTransformAssembler;
import io.choerodon.statemachine.domain.*;
import io.choerodon.statemachine.infra.enums.*;
import io.choerodon.statemachine.infra.exception.RemoveStatusException;
import io.choerodon.statemachine.infra.factory.MachineFactory;
import io.choerodon.statemachine.infra.mapper.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class StateMachineServiceImpl extends BaseServiceImpl<StateMachine> implements StateMachineService {

    private static final Logger logger = LoggerFactory.getLogger(StateMachineServiceImpl.class);
    private final String STATE_MACHINE_CREATE = "state_machine_create";
    @Autowired
    private StateMachineMapper stateMachineMapper;
    @Autowired
    private StateMachineNodeMapper nodeDeployMapper;
    @Autowired
    private StateMachineNodeDraftMapper nodeDraftMapper;
    @Autowired
    private StateMachineNodeService nodeService;
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
    @Autowired
    private SagaServiceImpl sagaService;

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
        if (checkName(organizationId, stateMachineDTO.getName())) {
            throw new CommonException("error.stateMachineName.exist");
        }
        stateMachineDTO.setId(null);
        stateMachineDTO.setStatus(StateMachineStatus.CREATE);
        stateMachineDTO.setOrganizationId(organizationId);
        stateMachineDTO.setDefault(false);
        StateMachine stateMachine = modelMapper.map(stateMachineDTO, StateMachine.class);
        int isInsert = stateMachineMapper.insertSelective(stateMachine);
        if (isInsert != 1) {
            throw new CommonException("error.stateMachine.create");
        }

        //创建默认开始节点
        StateMachineNodeDraft startNode = new StateMachineNodeDraft();
        startNode.setStateMachineId(stateMachine.getId());
        startNode.setOrganizationId(organizationId);
        startNode.setStatusId(0L);
        startNode.setPositionX(InitNode.START.getPositionX());
        startNode.setPositionY(InitNode.START.getPositionY());
        startNode.setWidth(InitNode.START.getWidth());
        startNode.setHeight(InitNode.START.getHeight());
        startNode.setType(NodeType.START);
        int isStartNodeInsert = nodeDraftMapper.insert(startNode);
        if (isStartNodeInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }

        //创建默认的初始节点
        StateMachineNodeDraft initNode = new StateMachineNodeDraft();
        initNode.setStateMachineId(stateMachine.getId());
        //获取第一个状态
        List<StatusDTO> statusDTOS = statusService.queryAllStatus(organizationId);
        initNode.setStatusId(statusDTOS.isEmpty() ? 0L : statusDTOS.get(0).getId());
        initNode.setPositionX(InitNode.INIT.getPositionX());
        initNode.setPositionY(InitNode.INIT.getPositionY());
        initNode.setWidth(InitNode.INIT.getWidth());
        initNode.setHeight(InitNode.INIT.getHeight());
        initNode.setType(NodeType.INIT);
        initNode.setOrganizationId(organizationId);
        int isNodeInsert = nodeDraftMapper.insert(initNode);
        if (isNodeInsert != 1) {
            throw new CommonException("error.stateMachineNode.create");
        }

        //创建默认的转换
        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setName("初始化");
        transform.setStateMachineId(stateMachine.getId());
        transform.setStartNodeId(startNode.getId());
        transform.setEndNodeId(initNode.getId());
        transform.setType(TransformType.INIT);
        transform.setConditionStrategy(TransformConditionStrategy.ALL);
        transform.setOrganizationId(organizationId);
        int isTransformInsert = transformDraftMapper.insert(transform);
        if (isTransformInsert != 1) {
            throw new CommonException("error.stateMachineTransform.create");
        }
        return queryStateMachineWithConfigById(organizationId, stateMachine.getId(), true);
    }

    private Boolean checkNameUpdate(Long organizationId, Long stateMachineId, String name) {
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName(name);
        StateMachine res = stateMachineMapper.selectOne(stateMachine);
        if (res != null && !stateMachineId.equals(res.getId())) {
            return true;
        }
        return false;
    }

    @Override
    public StateMachineDTO update(Long organizationId, Long stateMachineId, StateMachineDTO stateMachineDTO) {
        if (checkNameUpdate(organizationId, stateMachineId, stateMachineDTO.getName())) {
            throw new CommonException("error.stateMachineName.exist");
        }
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
        if (stateMachine.getDefault()) {
            throw new CommonException("error.stateMachine.defaultForbiddenDelete");
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
    public Boolean deploy(Long organizationId, Long stateMachineId, Boolean isStartSaga) {
        if (stateMachineId == null) {
            throw new CommonException("error.stateMachineId.null");
        }
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        String oldStatus = stateMachine.getStatus();
        //是否同步状态到其他服务:前置处理
        Map<String, List<Status>> changeMap = null;
        if (isStartSaga && !oldStatus.equals(StateMachineStatus.CREATE)) {
            changeMap = new HashMap<>(3);
            deployHandleChange(changeMap, stateMachineId);
            //校验删除的节点状态是否有使用的issue
            Boolean result = deployCheckDelete(organizationId, changeMap, stateMachineId);
            if (!result) {
                return false;
            }
        }
        if (stateMachine == null) {
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
            nodeDeploys.forEach(n -> nodeDeployMapper.insert(n));
        }
        //写入发布的转换
        StateMachineTransformDraft transform = new StateMachineTransformDraft();
        transform.setStateMachineId(stateMachineId);
        transform.setOrganizationId(organizationId);
        List<StateMachineTransformDraft> transforms = transformDraftMapper.select(transform);
        if (transforms != null && !transforms.isEmpty()) {
            List<StateMachineTransform> transformDeploys = modelMapper.map(transforms, new TypeToken<List<StateMachineTransform>>() {
            }.getType());
            transformDeploys.forEach(t -> transformDeployMapper.insert(t));
        }
        //写入发布的配置
        StateMachineConfigDraft config = new StateMachineConfigDraft();
        config.setStateMachineId(stateMachineId);
        config.setOrganizationId(organizationId);
        List<StateMachineConfigDraft> configs = configDraftMapper.select(config);
        if (configs != null && !configs.isEmpty()) {
            List<StateMachineConfig> configDeploys = modelMapper.map(configs, new TypeToken<List<StateMachineConfig>>() {
            }.getType());
            configDeploys.forEach(c -> configDeployMapper.insert(c));

        }
        //清理内存中的旧状态机构建器与实例
        machineFactory.deployStateMachine(stateMachineId);

        //是否同步状态到其他服务:发saga
        if (isStartSaga && !oldStatus.equals(StateMachineStatus.CREATE)) {
            sagaService.deployStateMachine(organizationId, stateMachineId, changeMap);
        }
        return true;
    }

    /**
     * 处理发布状态机时，节点状态的变化
     *
     * @param stateMachineId
     */
    private void deployHandleChange(Map<String, List<Status>> changeMap, Long stateMachineId) {
        //获取旧节点
        List<StateMachineNode> nodeDeploys = nodeDeployMapper.selectByStateMachineId(stateMachineId);
        Map<Long, Status> deployMap = nodeDeploys.stream().filter(x -> x.getStatus() != null).collect(Collectors.toMap(StateMachineNode::getId, StateMachineNode::getStatus));
        List<Long> oldIds = nodeDeploys.stream().map(StateMachineNode::getId).collect(Collectors.toList());
        //获取新节点
        List<StateMachineNodeDraft> nodeDrafts = nodeDraftMapper.selectByStateMachineId(stateMachineId);
        Map<Long, Status> draftMap = nodeDrafts.stream().filter(x -> x.getStatus() != null).collect(Collectors.toMap(StateMachineNodeDraft::getId, StateMachineNodeDraft::getStatus));
        List<Long> newIds = nodeDrafts.stream().map(StateMachineNodeDraft::getId).collect(Collectors.toList());
        //新增的节点
        List<Long> addIds = new ArrayList<>(newIds);
        addIds.removeAll(oldIds);
        List<Status> addStatuses = new ArrayList<>(addIds.size());
        addIds.forEach(addId -> {
            addStatuses.add(draftMap.get(addId));
        });
        //删除的节点
        List<Long> deleteIds = new ArrayList<>(oldIds);
        deleteIds.removeAll(newIds);
        List<Status> deleteStatuses = new ArrayList<>(deleteIds.size());
        deleteIds.forEach(deleteId -> {
            deleteStatuses.add(deployMap.get(deleteId));
        });

        changeMap.put("addList", addStatuses);
        changeMap.put("deleteList", deleteStatuses);
    }

    /**
     * 处理发布状态机时，
     *
     * @param stateMachineId
     */
    private Boolean deployCheckDelete(Long organizationId, Map<String, List<Status>> changeMap, Long stateMachineId) {
        List<Status> deleteStatuses = changeMap.get("deleteList");
        for (Status status : deleteStatuses) {
            Map<String, Object> result = nodeService.checkDelete(organizationId, stateMachineId, status.getId());
            Boolean canDelete = (Boolean) result.get("canDelete");
            if (!canDelete) {
                return false;
            }
        }
        return true;
    }

    @Override
    public StateMachineDTO queryStateMachineWithConfigById(Long organizationId, Long stateMachineId, Boolean isDraft) {
        StateMachine stateMachine = stateMachineMapper.queryById(organizationId, stateMachineId);
        if (stateMachine == null) {
            throw new CommonException("error.stateMachine.notFound");
        }

        //查询草稿时，若为活跃状态，则更新为草稿
        if (isDraft) {
            updateStateMachineStatus(organizationId, stateMachineId);
        }

        List<StateMachineNodeDTO> nodeDTOS = nodeService.queryByStateMachineId(organizationId, stateMachineId, isDraft);
        List<StateMachineTransformDTO> transformDTOS;
        if (isDraft) {
            //获取转换
            StateMachineTransformDraft select = new StateMachineTransformDraft();
            select.setStateMachineId(stateMachineId);
            select.setOrganizationId(organizationId);
            List<StateMachineTransformDraft> transforms = transformDraftMapper.select(select);
            transformDTOS = stateMachineTransformAssembler.toTargetList(transforms, StateMachineTransformDTO.class);
        } else {
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
                transformDTO.setPostpositions(Optional.ofNullable(map.get(ConfigType.ACTION)).orElse(Collections.emptyList()));
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
        stateMachine.setStatus(StateMachineStatus.ACTIVE);
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
    public StateMachineDTO queryDefaultStateMachine(Long organizationId) {
        StateMachine defaultStateMachine = new StateMachine();
        defaultStateMachine.setOrganizationId(organizationId);
        defaultStateMachine.setDefault(true);
        StateMachine stateMachine = stateMachineMapper.selectOne(defaultStateMachine);
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
        if (stateMachineId == null) {
            throw new CommonException("error.updateStateMachineStatus.stateMachineId.notNull");
        }
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
    public Boolean checkName(Long organizationId, String name) {
        StateMachine stateMachine = new StateMachine();
        stateMachine.setOrganizationId(organizationId);
        stateMachine.setName(name);
        StateMachine res = stateMachineMapper.selectOne(stateMachine);
        if (res == null) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean activeStateMachines(Long organizationId, List<Long> stateMachineIds) {
        if (!stateMachineIds.isEmpty()) {
            List<StateMachine> stateMachines = stateMachineMapper.queryByIds(organizationId, stateMachineIds);
            for (StateMachine stateMachine : stateMachines) {
                //若是新建状态机，则发布变成活跃
                if (stateMachine.getStatus().equals(StateMachineStatus.CREATE)) {
                    deploy(organizationId, stateMachine.getId(), false);
                }
            }
        }
        return true;
    }

    @Override
    public Boolean notActiveStateMachines(Long organizationId, List<Long> stateMachineIds) {
        if (!stateMachineIds.isEmpty()) {
            List<StateMachine> stateMachines = stateMachineMapper.queryByIds(organizationId, stateMachineIds);
            for (StateMachine stateMachine : stateMachines) {
                if (stateMachine.getId() == null) {
                    throw new CommonException("error.stateMachineId.null");
                }
                //更新状态机状态为create
                Long stateMachineId = stateMachine.getId();
                stateMachine.setStatus(StateMachineStatus.CREATE);
                updateOptional(stateMachine, "status");
                //删除发布节点
                StateMachineNode node = new StateMachineNode();
                node.setStateMachineId(stateMachineId);
                node.setOrganizationId(organizationId);
                nodeDeployMapper.delete(node);
                //删除发布转换
                StateMachineTransform transform = new StateMachineTransform();
                transform.setStateMachineId(stateMachineId);
                transform.setOrganizationId(organizationId);
                transformDeployMapper.delete(transform);
                //删除发布配置
                StateMachineConfig config = new StateMachineConfig();
                config.setStateMachineId(stateMachineId);
                config.setOrganizationId(organizationId);
                configDeployMapper.delete(config);
            }
        }
        return true;
    }

    @Override
    public List<StateMachineWithStatusDTO> queryAllWithStatus(Long organizationId) {
        //查询出所有状态机，新建的查草稿，活跃的查发布
        StateMachine select = new StateMachine();
        select.setOrganizationId(organizationId);
        List<StateMachine> stateMachines = stateMachineMapper.select(select);
        List<StateMachineWithStatusDTO> stateMachineWithStatusDTOS = modelMapper.map(stateMachines, new TypeToken<List<StateMachineWithStatusDTO>>() {
        }.getType());
        //查询出所有状态
        List<StatusDTO> statusDTOS = statusService.queryAllStatus(organizationId);
        Map<Long, StatusDTO> statusMap = statusDTOS.stream().collect(Collectors.toMap(StatusDTO::getId, x -> x));
        stateMachineWithStatusDTOS.forEach(stateMachine -> {
            List<StatusDTO> status = new ArrayList<>();
            if (stateMachine.getStatus().equals(StateMachineStatus.CREATE)) {
                List<StateMachineNodeDraft> nodeDrafts = nodeDraftMapper.selectByStateMachineId(stateMachine.getId());
                nodeDrafts.forEach(nodeDraft -> {
                    if (!nodeDraft.getType().equals(NodeType.START)) {
                        StatusDTO statusDTO = statusMap.get(nodeDraft.getStatusId());
                        if (statusDTO != null) {
                            status.add(statusDTO);
                        } else {
                            logger.warn("warn nodeDraftId:{} notFound", nodeDraft.getId());
                        }
                    }
                });
            } else {
                List<StateMachineNode> nodeDeploys = nodeDeployMapper.selectByStateMachineId(stateMachine.getId());
                nodeDeploys.forEach(nodeDeploy -> {
                    if (!nodeDeploy.getType().equals(NodeType.START)) {
                        StatusDTO statusDTO = statusMap.get(nodeDeploy.getStatusId());
                        if (statusDTO != null) {
                            status.add(statusDTO);
                        } else {
                            logger.warn("warn nodeDraftId:{} notFound", nodeDeploy.getId());
                        }
                    }
                });
            }
            stateMachine.setStatusDTOS(status);
        });
        return stateMachineWithStatusDTOS;
    }

    @Override
    public List<StateMachineDTO> queryByOrgId(Long organizationId) {
        StateMachine select = new StateMachine();
        select.setOrganizationId(organizationId);
        List<StateMachine> stateMachines = stateMachineMapper.select(select);
        return modelMapper.map(stateMachines, new TypeToken<List<StateMachineDTO>>() {
        }.getType());
    }

    @Override
    public void removeStateMachineNode(Long organizationId, Long stateMachineId, Long statusId) {
        StateMachineNode stateNode = new StateMachineNode();
        stateNode.setOrganizationId(organizationId);
        stateNode.setStateMachineId(stateMachineId);
        stateNode.setStatusId(statusId);
        StateMachineNode res = nodeDeployMapper.selectOne(stateNode);
        if (res == null) {
            throw new RemoveStatusException("error.status.exist");
        }
        nodeDeployMapper.delete(stateNode);
    }
}
