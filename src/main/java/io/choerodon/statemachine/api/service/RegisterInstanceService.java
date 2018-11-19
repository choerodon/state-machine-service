package io.choerodon.statemachine.api.service;

import io.choerodon.eureka.event.EurekaEventPayload;

public interface RegisterInstanceService {

    void instanceDownConsumer(final EurekaEventPayload payload);

    void instanceUpConsumer(final EurekaEventPayload payload);

}
