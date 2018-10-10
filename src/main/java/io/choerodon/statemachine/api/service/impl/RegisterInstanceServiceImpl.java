package io.choerodon.statemachine.api.service.impl;

import io.choerodon.statemachine.api.dto.PropertyData;
import io.choerodon.statemachine.api.service.ConfigCodeService;
import io.choerodon.statemachine.domain.event.RegisterInstancePayload;
import io.choerodon.statemachine.api.service.RegisterInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RegisterInstanceServiceImpl implements RegisterInstanceService {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ConfigCodeService configCodeService;

    @Override
    public void instanceDownConsumer(final RegisterInstancePayload payload) {
    }

    @Override
    public void instanceUpConsumer(final RegisterInstancePayload payload) {
        PropertyData propertyData = fetchPropertyData(payload.getInstanceAddress());
        if (propertyData == null) {
            throw new RemoteAccessException("error.instanceUpConsumer.fetchPropertyData");
        } else {
            //处理获取到的新启动服务的数据
            configCodeService.handlePropertyData(propertyData);
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
