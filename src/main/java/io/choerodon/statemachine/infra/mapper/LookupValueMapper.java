package io.choerodon.statemachine.infra.mapper;


import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.statemachine.domain.LookupTypeWithValues;
import io.choerodon.statemachine.domain.LookupValue;

public interface LookupValueMapper extends BaseMapper<LookupValue> {

    LookupTypeWithValues queryLookupValueByCode(String typeCode);

}
