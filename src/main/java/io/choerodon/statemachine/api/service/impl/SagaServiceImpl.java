package io.choerodon.statemachine.api.service.impl;

import com.alibaba.fastjson.JSON;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.event.DeployStateMachinePayload;
import io.choerodon.statemachine.infra.feign.IssueFeignClient;
import io.choerodon.statemachine.infra.feign.dto.ChangeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
@Component
public class SagaServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(SagaServiceImpl.class);
    private static final String DEPLOY_STATE_MACHINE = "deploy-state-machine";
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private IssueFeignClient issueFeignClient;

    @Saga(code = DEPLOY_STATE_MACHINE, description = "发布状态机", inputSchemaClass = DeployStateMachinePayload.class)
    public void deployStateMachine(Long organizationId, Long stateMachineId, Map<String, List<Status>> changeMap) {
        //新增的状态
        List<Status> addList = changeMap.get("addList");
        Map<Long, Status> statusMap = addList.stream().collect(Collectors.toMap(Status::getId, x -> x));
        //移除的状态
        List<Status> deleteList = changeMap.get("deleteList");
        List<Long> addStatusIds = addList.stream().map(Status::getId).collect(Collectors.toList());
        List<Long> deleteStatusIds = deleteList.stream().map(Status::getId).collect(Collectors.toList());
        ChangeStatus changeStatus = new ChangeStatus(addStatusIds, deleteStatusIds);
        DeployStateMachinePayload deployStateMachinePayload = issueFeignClient.handleStateMachineChangeStatusByStateMachineId(organizationId, stateMachineId, changeStatus).getBody();
        //新增的状态赋予实体
        deployStateMachinePayload.getAddStatusWithProjects().forEach(addStatusWithProject -> {
            List<Status> statuses = new ArrayList<>(addStatusWithProject.getAddStatusIds().size());
            addStatusWithProject.getAddStatusIds().forEach(addStatusId -> {
                Status status = statusMap.get(addStatusId);
                if (status != null) {
                    statuses.add(status);
                }
            });
            addStatusWithProject.setAddStatuses(statuses);
        });
        sagaClient.startSaga(DEPLOY_STATE_MACHINE, new StartInstanceDTO(JSON.toJSONString(deployStateMachinePayload), "", ""));
        logger.info("startSaga deploy-state-machine addStatusIds: {}, deleteStatusIds: {}", changeStatus.getAddStatusIds(), changeStatus.getDeleteStatusIds());
    }
}
