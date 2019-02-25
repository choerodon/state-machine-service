package io.choerodon.statemachine.api.eventhandler;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.statemachine.api.service.InitService;
import io.choerodon.statemachine.domain.event.OrganizationEventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.choerodon.statemachine.infra.utils.SagaTopic.Organization.ORG_CREATE;
import static io.choerodon.statemachine.infra.utils.SagaTopic.Organization.TASK_ORG_CREATE;


/**
 * Created by HuangFuqiang@choerodon.io on 2018/09/28.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class StateMachineEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineEventHandler.class);

    @Autowired
    private InitService initService;

    /**
     * 创建组织事件
     */
    @SagaTask(code = TASK_ORG_CREATE,
            description = "创建组织事件",
            sagaCode = ORG_CREATE,
            seq = 1)
    public String handleOrganizationCreateEvent(String data) {
        LOGGER.info("消费创建组织消息{}", data);
        OrganizationEventPayload organizationEventPayload = JSONObject.parseObject(data, OrganizationEventPayload.class);
        Long organizationId = organizationEventPayload.getId();
        //初始化状态
        initService.initStatus(organizationId);
        //初始化默认状态机
        initService.initDefaultStateMachine(organizationId);
        return data;
    }

}
