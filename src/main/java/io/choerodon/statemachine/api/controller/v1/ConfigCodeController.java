package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.dto.ConfigCodeDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.service.ConfigCodeService;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/10/10
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/config_codes")
public class ConfigCodeController extends BaseController {

    @Autowired
    private ConfigCodeService configCodeService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取未配置的条件，验证，后置动作等列表")
    @GetMapping(value = "/{transform_id}")
    public ResponseEntity<List<ConfigCodeDTO>> queryByTransformId(@PathVariable("organization_id") Long organizationId,
                                                           @PathVariable("transform_id") Long transformId,
                                                           @RequestParam String type) {
        return new ResponseEntity<>(configCodeService.queryByTransformId(organizationId, transformId, type), HttpStatus.OK);
    }

}
