package io.choerodon.statemachine.api.eventhandler;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.statemachine.api.service.InitService;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.statemachine.domain.event.OrganizationEventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.choerodon.statemachine.infra.utils.SagaTopic.Organization.*;


/**
 * Created by HuangFuqiang@choerodon.io on 2018/09/28.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class StateMachineHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineHandler.class);

    @Autowired
    private InitService initService;

    private void loggerInfo(Object o) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("data: {}", o);
        }
    }

    /**
     * 创建组织事件
     */
    @SagaTask(code = TASK_ORG_CREATE,
            description = "创建组织事件",
            sagaCode = ORG_CREATE,
            maxRetryCount = 0,
            seq = 1)
    public String handleOrganizationCreateEvent(String payload) {
        OrganizationEventPayload organizationEventPayload = JSONObject.parseObject(payload, OrganizationEventPayload.class);
        loggerInfo(organizationEventPayload);
        //初始化状态
        initService.initStatus(organizationEventPayload.getId());
        return payload;
    }

    /**
     * 注册组织事件
     */
    @SagaTask(code = TASK_ORG_REGISTER,
            description = "注册组织事件",
            sagaCode = ORG_REGISTER,
            maxRetryCount = 0,
            seq = 1)
    public String handleOrganizationRegisterEvent(String payload) {
        OrganizationEventPayload organizationEventPayload = JSONObject.parseObject(payload, OrganizationEventPayload.class);
        loggerInfo(organizationEventPayload);
        //初始化状态
        initService.initStatus(organizationEventPayload.getId());
        return payload;
    }

}
