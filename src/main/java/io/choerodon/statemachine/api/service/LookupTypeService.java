package io.choerodon.statemachine.api.service;


import io.choerodon.statemachine.api.dto.LookupTypeDTO;

import java.util.List;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/09/27.
 * Email: fuqianghuang01@gmail.com
 */
public interface LookupTypeService {

    List<LookupTypeDTO> listLookupType(Long organizationId);
}