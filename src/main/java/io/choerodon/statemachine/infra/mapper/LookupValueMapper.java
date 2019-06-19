package io.choerodon.statemachine.infra.mapper;


import io.choerodon.mybatis.common.Mapper;
import io.choerodon.statemachine.domain.LookupTypeWithValues;
import io.choerodon.statemachine.domain.LookupValue;

public interface LookupValueMapper extends Mapper<LookupValue> {

    LookupTypeWithValues queryLookupValueByCode(String typeCode);

}
