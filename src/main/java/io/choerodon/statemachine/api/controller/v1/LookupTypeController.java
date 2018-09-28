package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.dto.LookupTypeDTO;
import io.choerodon.statemachine.api.service.LookupTypeService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Optional;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/09/27.
 * Email: fuqianghuang01@gmail.com
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/lookup_types")
public class LookupTypeController {


    @Autowired
    private LookupTypeService lookupTypeService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("查询所有lookup type类型")
    @GetMapping
    public ResponseEntity<List<LookupTypeDTO>> listLookupType(@ApiParam(value = "项目id", required = true)
                                                              @PathVariable(name = "organization_id") Long organizationId) {
        return Optional.ofNullable(lookupTypeService.listLookupType(organizationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.lookupTypeList.get"));
    }

}