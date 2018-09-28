package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.dto.ConfigEnumDTO;
import io.choerodon.statemachine.api.dto.StateMachineConfigDTO;
import io.choerodon.statemachine.api.service.StateMachineConfigService;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/state_machine_configs")
public class StateMachineConfigController extends BaseController {

    @Autowired
    private StateMachineConfigService configService;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建配置")
    @PostMapping(value = "/{state_machine_id}")
    public ResponseEntity<StateMachineConfigDTO> create(@PathVariable("organization_id") Long organizationId,
                                                        @PathVariable("state_machine_id") Long stateMachineId,
                                                        @RequestBody StateMachineConfigDTO configDTO) {
        return new ResponseEntity<>(configService.create(organizationId, stateMachineId, configDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除配置")
    @DeleteMapping(value = "/{config_id}")
    public ResponseEntity<Boolean> delete(@PathVariable("organization_id") Long organizationId, @PathVariable("config_id") Long configId) {
        return new ResponseEntity<>(configService.delete(organizationId, configId), HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取转换下的配置")
    @GetMapping(value = "/query")
    public ResponseEntity<List<StateMachineConfigDTO>> queryByTransformId(@PathVariable("organization_id") Long organizationId,
                                                                       @RequestParam Long transformId,
                                                                       @RequestParam String type) {
        return new ResponseEntity<>(configService.queryByTransformId(organizationId,transformId, type), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "获取条件，验证等列表")
    @GetMapping(value = "/queryConfig/{transform_id}")
    public ResponseEntity<List<ConfigEnumDTO>> queryConfig(@PathVariable("organization_id") Long organizationId,
                                                           @PathVariable("transform_id") Long transformId,
                                                           @RequestParam String type) {
        return new ResponseEntity<>(configService.queryConfig(organizationId, transformId, type), HttpStatus.OK);
    }

}
