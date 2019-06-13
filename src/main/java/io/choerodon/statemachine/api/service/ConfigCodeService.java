package io.choerodon.statemachine.api.service;

import io.choerodon.statemachine.api.dto.ConfigCodeDTO;
import io.choerodon.statemachine.api.dto.PropertyData;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/10/10
 */
public interface ConfigCodeService {

    /**
     * 根据类型获取ConfigCode
     *
     * @param type
     * @return
     */
    List<ConfigCodeDTO> queryByType(String type);

    /**
     * 根据转换id获取未设置的ConfigCode
     *
     * @param organizationId
     * @param transformId
     * @param type
     * @return
     */
    List<ConfigCodeDTO> queryByTransformId(Long organizationId, Long transformId, String type);

    /**
     * 处理eureka发现新服务时扫描到的ConfigCode
     *
     * @param propertyData
     */
    void handlePropertyData(PropertyData propertyData);
}
