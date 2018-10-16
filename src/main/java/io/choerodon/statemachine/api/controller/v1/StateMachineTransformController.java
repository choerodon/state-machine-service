package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.statemachine.api.dto.StateMachineTransformDTO;
import io.choerodon.statemachine.api.service.StateMachineTransformService;
import io.choerodon.statemachine.api.validator.StateMachineTransformValidator;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/state_machine_transforms")
public class StateMachineTransformController extends BaseController {

    @Autowired
    private StateMachineTransformService transformService;

    @Autowired
    private StateMachineTransformValidator transformValidator;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建转换（草稿）")
    @PostMapping
    public ResponseEntity<StateMachineTransformDTO> create(@PathVariable("organization_id") Long organizationId,
                                                           @RequestBody StateMachineTransformDTO transformDTO) {
        transformValidator.createValidate(transformDTO);
        return new ResponseEntity<>(transformService.create(organizationId, transformDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新转换（草稿）")
    @PutMapping(value = "/{transform_id}")
    public ResponseEntity<StateMachineTransformDTO> update(@PathVariable("organization_id") Long organizationId,
                                                           @PathVariable("transform_id") Long transformId,
                                                           @RequestBody StateMachineTransformDTO transformDTO) {
        transformValidator.updateValidate(transformDTO);
        return new ResponseEntity<>(transformService.update(organizationId, transformId, transformDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除转换（草稿）")
    @DeleteMapping(value = "/{transform_id}")
    public ResponseEntity<Boolean> delete(@PathVariable("organization_id") Long organizationId,
                                          @PathVariable("transform_id") Long transformId) {
        return new ResponseEntity<>(transformService.delete(organizationId, transformId), HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验转换名字是否未被使用")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(@PathVariable("organization_id") Long organizationId,
                                             @RequestParam(value = "state_machine_id") Long stateMachineId,
                                             @RequestParam(value = "transform_id", required = false) Long transformId,
                                             @RequestParam("name") String name) {
        return new ResponseEntity<>(transformService.checkName(organizationId, stateMachineId, transformId, name), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据id获取转换（草稿）")
    @GetMapping(value = "/{transform_id}")
    public ResponseEntity<StateMachineTransformDTO> queryById(@PathVariable("organization_id") Long organizationId,
                                                              @PathVariable("transform_id") Long transformId) {
        return new ResponseEntity<>(transformService.queryById(organizationId, transformId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建【全部】转换，所有节点均可转换到当前节点（草稿）")
    @PostMapping(value = "/create_type_all")
    public ResponseEntity<StateMachineTransformDTO> createAllStatusTransform(@PathVariable("organization_id") Long organizationId,
                                                                             @RequestParam("endNodeId") Long endNodeId) {
        return new ResponseEntity<>(transformService.createAllStatusTransform(organizationId, endNodeId), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除【全部】转换（草稿）")
    @DeleteMapping(value = "/delete_type_all/{transform_id}")
    public ResponseEntity<Boolean> deleteAllStatusTransform(@PathVariable("organization_id") Long organizationId,
                                                            @PathVariable("transform_id") Long transformId) {
        return new ResponseEntity<>(transformService.deleteAllStatusTransform(organizationId, transformId), HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更改条件策略")
    @GetMapping(value = "/update_condition_strategy/{transform_id}")
    public ResponseEntity<Boolean> updateConditionStrategy(@PathVariable("organization_id") Long organizationId,
                                                           @PathVariable("transform_id") Long transformId,
                                                           @RequestParam("condition_strategy") String conditionStrategy) {
        return new ResponseEntity<>(transformService.updateConditionStrategy(organizationId, transformId, conditionStrategy), HttpStatus.CREATED);
    }
}
