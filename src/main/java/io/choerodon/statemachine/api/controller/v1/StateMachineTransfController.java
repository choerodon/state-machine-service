package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.dto.StateMachineTransfDTO;
import io.choerodon.statemachine.api.service.StateMachineTransfService;
import io.choerodon.statemachine.api.validator.StateMachineTransfValidator;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author peng.jiang@hand-china.com
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/state_machine_transf")
public class StateMachineTransfController extends BaseController {

    @Autowired
    private StateMachineTransfService transfService;

    @Autowired
    private StateMachineTransfValidator transfValidator;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建转换")
    @PostMapping
    public ResponseEntity<StateMachineTransfDTO> create(@PathVariable("organization_id") Long organizationId, @RequestBody StateMachineTransfDTO transfDTO) {
        transfValidator.createValidate(transfDTO);
        return new ResponseEntity<>(transfService.create(organizationId, transfDTO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新转换")
    @PutMapping(value = "/{transf_id}")
    public ResponseEntity<StateMachineTransfDTO> update(@PathVariable("organization_id") Long organizationId, @PathVariable("transf_id") Long transfId,
                                                        @RequestBody StateMachineTransfDTO transfDTO) {
        transfValidator.updateValidate(transfDTO);
        return new ResponseEntity<>(transfService.update(organizationId, transfId, transfDTO), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除转换")
    @DeleteMapping(value = "/{transf_id}")
    public ResponseEntity<Boolean> delete(@PathVariable("organization_id") Long organizationId, @PathVariable("transf_id") Long transfId) {
        return new ResponseEntity<>(transfService.delete(organizationId, transfId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验转换名字是否未被使用")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(@PathVariable("organization_id") Long organizationId,
                                             @RequestParam(value = "state_machine_id") Long stateMachineId,
                                             @RequestParam(value = "transf_id", required = false) Long transfId,
                                             @RequestParam("name") String name) {
        return new ResponseEntity<>(transfService.checkName(stateMachineId, transfId, name), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据id获取转换")
    @GetMapping(value = "/getById/{transf_id}")
    public ResponseEntity<StateMachineTransfDTO> getById(@PathVariable("organization_id") Long organizationId,
                                                         @PathVariable("transf_id") Long transfId) {
        return new ResponseEntity<>(transfService.getById(organizationId, transfId), HttpStatus.OK);
    }


}
