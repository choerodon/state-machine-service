package io.choerodon.statemachine.api.service.impl;

import io.choerodon.statemachine.api.dto.PropertyData;
import io.choerodon.statemachine.api.dto.RegisterInstancePayloadDTO;
import io.choerodon.statemachine.api.service.RegisterInstanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RegisterInstanceServiceImpl implements RegisterInstanceService {

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void instanceDownConsumer(final RegisterInstancePayloadDTO payload) {
    }

    @Override
    public void instanceUpConsumer(final RegisterInstancePayloadDTO payload) {
        PropertyData propertyData = fetchPropertyData(payload.getInstanceAddress());
        if (propertyData == null) {
            throw new RemoteAccessException("error.instanceUpConsumer.fetchPropertyData");
        } else {
//            propertyDataConsume(propertyData);
        }
    }

    private PropertyData fetchPropertyData(String address) {
        ResponseEntity<PropertyData> response = restTemplate.getForEntity("http://"
                + address + "/statemachine/load_config_code", PropertyData.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RemoteAccessException("error.fetchPropertyData.statusCodeNot2XX");
        }
    }
}
