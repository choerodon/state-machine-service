package io.choerodon.statemachine.api.service;


import io.choerodon.statemachine.api.dto.LookupTypeWithValuesDTO;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/09/27.
 * Email: fuqianghuang01@gmail.com
 */
public interface LookupValueService {

    LookupTypeWithValuesDTO queryLookupValueByCode(Long organizationId, String typeCode);

}