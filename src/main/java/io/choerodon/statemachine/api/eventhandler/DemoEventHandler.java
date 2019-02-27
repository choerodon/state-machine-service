package io.choerodon.statemachine.api.eventhandler;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.asgard.saga.annotation.SagaTask;
import io.choerodon.statemachine.api.service.InitService;
import io.choerodon.statemachine.domain.event.OrganizationEventPayload;
import io.choerodon.statemachine.domain.event.OrganizationRegisterEventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.choerodon.statemachine.infra.utils.SagaTopic.Organization.ORG_CREATE;
import static io.choerodon.statemachine.infra.utils.SagaTopic.Organization.TASK_ORG_CREATE;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/2/27.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class DemoEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoEventHandler.class);

    private static final String REGISTER_STATEMACHINE_INIT_ORG = "register-stateMachine-init-org";
    private static final String REGISTER_ORG = "register-org";

    @Autowired
    private InitService initService;

    /**
     * 创建组织事件
     */
    @SagaTask(code = REGISTER_STATEMACHINE_INIT_ORG,
            description = "demo创建组织事件",
            sagaCode = REGISTER_ORG,
            seq = 50)
    public OrganizationRegisterEventPayload OrgCreatedForDemoInit(String data) {
        LOGGER.info("demo消费创建组织消息{}", data);
        OrganizationRegisterEventPayload organizationEventPayload = JSONObject.parseObject(data, OrganizationRegisterEventPayload.class);
        Long organizationId = organizationEventPayload.getOrganization().getId();
        //初始化状态
        initService.initStatus(organizationId);
        //初始化默认状态机
        initService.initDefaultStateMachine(organizationId);
        return organizationEventPayload;
    }
}
