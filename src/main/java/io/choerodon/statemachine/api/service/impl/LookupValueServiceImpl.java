package io.choerodon.statemachine.api.service.impl;

import io.choerodon.statemachine.api.dto.LookupTypeWithValuesDTO;
import io.choerodon.statemachine.api.dto.LookupValueDTO;
import io.choerodon.statemachine.api.service.LookupValueService;
import io.choerodon.statemachine.domain.LookupTypeWithValues;
import io.choerodon.statemachine.infra.mapper.LookupValueMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/09/27.
 * Email: fuqianghuang01@gmail.com
 */
@Service
public class LookupValueServiceImpl implements LookupValueService {

    @Autowired
    private LookupValueMapper lookupValueMapper;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public LookupTypeWithValuesDTO queryLookupValueByCode(Long organizationId, String typeCode) {
        LookupTypeWithValues typeWithValues = lookupValueMapper.queryLookupValueByCode(typeCode);
        LookupTypeWithValuesDTO result = modelMapper.map(typeWithValues, LookupTypeWithValuesDTO.class);
        result.setLookupValues(modelMapper.map(typeWithValues.getLookupValues(), new TypeToken<List<LookupValueDTO>>() {
        }.getType()));
        return result;
    }
}