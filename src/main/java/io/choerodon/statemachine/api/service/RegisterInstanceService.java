package io.choerodon.statemachine.api.service;

import io.choerodon.statemachine.domain.event.RegisterInstancePayload;

public interface RegisterInstanceService {

    void instanceDownConsumer(final RegisterInstancePayload payload);

    void instanceUpConsumer(final RegisterInstancePayload payload);

}
