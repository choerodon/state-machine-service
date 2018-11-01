package io.choerodon.statemachine.api.service.impl;

import com.alibaba.fastjson.JSON;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.event.DeployStatusPayload;
import io.choerodon.statemachine.domain.event.ProjectCreateAgilePayload;
import io.choerodon.statemachine.domain.event.StatusPayload;
import io.choerodon.statemachine.infra.feign.IssueFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/10/31
 */
@Component
public class SagaServiceImpl {
    private static final String DEPLOY_STATEMACHINE_ADD_STATUS = "deploy-statemachine-add-status";
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private IssueFeignClient issueFeignClient;

    @Saga(code = DEPLOY_STATEMACHINE_ADD_STATUS, description = "发布状态机中增加状态", inputSchemaClass = ProjectCreateAgilePayload.class)
    public void deployStateMachineAddStatus(Long organizationId, Long stateMachineId, List<Status> statuses) {
        List<StatusPayload> statusPayloads = new ArrayList<>(statuses.size());
        statuses.forEach(status -> {
            StatusPayload statusPayload = new StatusPayload();
            statusPayload.setStatusId(status.getId());
            statusPayload.setStatusName(status.getName());
            statusPayload.setType(status.getType());
            statusPayloads.add(statusPayload);
        });
        List<Long> projectIds = issueFeignClient.queryProjectIds(organizationId, stateMachineId).getBody();

        DeployStatusPayload deployStatusPayload = new DeployStatusPayload();
        deployStatusPayload.setProjectIds(projectIds);
        deployStatusPayload.setStatusPayloads(statusPayloads);
        sagaClient.startSaga(DEPLOY_STATEMACHINE_ADD_STATUS, new StartInstanceDTO(JSON.toJSONString(deployStatusPayload), "", ""));
    }
}
