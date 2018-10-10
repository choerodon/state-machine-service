package io.choerodon.statemachine.api.service;

import io.choerodon.statemachine.api.dto.RegisterInstancePayloadDTO;

public interface RegisterInstanceService {

    void instanceDownConsumer(final RegisterInstancePayloadDTO payload);

    void instanceUpConsumer(final RegisterInstancePayloadDTO payload);

}
