package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.statemachine.api.dto.*;
import io.choerodon.statemachine.api.service.StatusService;
import io.choerodon.statemachine.api.validator.StateValidator;
import io.choerodon.statemachine.domain.Status;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author shinan.chen
 * @since 2018/9/27
 */
@RestController
@RequestMapping(value = "/v1")
public class StatusController extends BaseController {

    @Autowired
    private StatusService statusService;

    @Autowired
    private StateValidator stateValidator;

//    @Permission(level = ResourceLevel.ORGANIZATION)
//    @ApiOperation(value = "分页查询状态列表")
//    @CustomPageRequest
//    @GetMapping("/organizations/{organization_id}/status")
//    public ResponseEntity<Page<StatusDTO>> pagingQuery(@ApiIgnore
//                                                       @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
//                                                       @PathVariable("organization_id") Long organizationId,
//                                                       @RequestParam(required = false) String name,
//                                                       @RequestParam(required = false) String description,
//                                                       @RequestParam(required = false) String type,
//                                                       @RequestParam(required = false) String[] param) {
//        return new ResponseEntity<>(statusService.pageQuery(pageRequest, new StatusDTO(name, description, type, organizationId),
//                param != null ? Arrays.stream(param).collect(Collectors.joining(",")) : null), HttpStatus.OK);
//    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "分页查询状态列表")
    @CustomPageRequest
    @PostMapping("/organizations/{organization_id}/status/list")
    public ResponseEntity<Page<StatusWithInfoDTO>> queryStatusList(@ApiIgnore
                                                                   @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                                   @ApiParam(value = "组织id", required = true)
                                                                   @PathVariable("organization_id") Long organizationId,
                                                                   @ApiParam(value = "status search dto", required = true)
                                                                   @RequestBody StatusSearchDTO statusSearchDTO) {
        return Optional.ofNullable(statusService.queryStatusList(pageRequest, organizationId, statusSearchDTO))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.statusList.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建状态")
    @PostMapping("/organizations/{organization_id}/status")
    public ResponseEntity<StatusDTO> create(@PathVariable("organization_id") Long organizationId,
                                            @RequestBody StatusDTO statusDTO) {
        stateValidator.validate(statusDTO);
        return new ResponseEntity<>(statusService.create(organizationId, statusDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新状态")
    @PutMapping(value = "/organizations/{organization_id}/status/{status_id}")
    public ResponseEntity<StatusDTO> update(@PathVariable("organization_id") Long organizationId,
                                            @PathVariable("status_id") Long statusId,
                                            @RequestBody @Valid StatusDTO statusDTO) {
        statusDTO.setId(statusId);
        statusDTO.setOrganizationId(organizationId);
        stateValidator.validate(statusDTO);
        return new ResponseEntity<>(statusService.update(statusDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除状态")
    @DeleteMapping(value = "/organizations/{organization_id}/status/{status_id}")
    public ResponseEntity<Boolean> delete(@PathVariable("organization_id") Long organizationId,
                                          @PathVariable("status_id") Long statusId) {
        return new ResponseEntity<>(statusService.delete(organizationId, statusId), HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据id查询状态对象")
    @GetMapping(value = "/organizations/{organization_id}/status/{status_id}")
    public ResponseEntity<StatusInfoDTO> queryStatusById(@PathVariable("organization_id") Long organizationId,
                                                         @PathVariable("status_id") Long statusId) {
        return new ResponseEntity<>(statusService.queryStatusById(organizationId, statusId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询状态机下的所有状态")
    @PostMapping(value = "/organizations/{organization_id}/status/query_by_state_machine_id")
    public ResponseEntity<List<StatusDTO>> queryByStateMachineIds(@PathVariable("organization_id") Long organizationId,
                                                                  @RequestBody @Valid List<Long> stateMachineIds) {
        return new ResponseEntity<>(statusService.queryByStateMachineIds(organizationId, stateMachineIds), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询组织下的所有状态")
    @GetMapping(value = "/organizations/{organization_id}/status/query_all")
    public ResponseEntity<List<StatusDTO>> queryAllStatus(@PathVariable("organization_id") Long organizationId) {
        return new ResponseEntity<>(statusService.queryAllStatus(organizationId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询组织下的所有状态,返回map")
    @GetMapping(value = "/organizations/{organization_id}/status/list_map")
    public ResponseEntity<Map<Long, StatusMapDTO>> queryAllStatusMap(
            @ApiParam(value = "组织id", required = true)
            @PathVariable("organization_id") Long organizationId) {
        return new ResponseEntity<>(statusService.queryAllStatusMap(organizationId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR, InitRoleCode.ORGANIZATION_MEMBER})
    @ApiOperation(value = "校验状态名字是否未被使用")
    @GetMapping(value = "/organizations/{organization_id}/status/check_name")
    public ResponseEntity<StatusCheckDTO> checkName(@PathVariable("organization_id") Long organizationId,
                                                    @RequestParam("name") String name) {
        return Optional.ofNullable(statusService.checkName(organizationId, name))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.statusName.check"));

    }

    @Permission(level = ResourceLevel.PROJECT, roles = {InitRoleCode.PROJECT_MEMBER, InitRoleCode.PROJECT_OWNER})
    @ApiOperation(value = "校验状态名字是否未被使用,项目层")
    @GetMapping(value = "/projects/{project_id}/status/project_check_name")
    public ResponseEntity<StatusCheckDTO> checkName(@PathVariable("project_id") Long projectId,
                                                    @RequestParam("organization_id") Long organizationId,
                                                    @RequestParam("name") String name) {
        return Optional.ofNullable(statusService.checkName(organizationId, name))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.statusName.check"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据ids批量查询状态")
    @PostMapping(value = "/status/batch")
    public ResponseEntity<Map<Long, Status>> batchStatusGet(@ApiParam(value = "状态ids", required = true)
                                                            @RequestBody List<Long> ids) {
        return Optional.ofNullable(statusService.batchStatusGet(ids))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.status.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "【敏捷】新增状态")
    @PostMapping(value = "/organizations/{organization_id}/status/create_status_for_agile")
    public ResponseEntity<StatusDTO> createStatusForAgile(@PathVariable("organization_id") Long organizationId,
                                                          @RequestParam("state_machine_id") Long stateMachineId,
                                                          @RequestBody StatusDTO statusDTO) {
        return Optional.ofNullable(statusService.createStatusForAgile(organizationId, stateMachineId, statusDTO))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.status.get"));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "【敏捷】移除状态")
    @DeleteMapping(value = "/organizations/{organization_id}/status/remove_status_for_agile")
    public ResponseEntity removeStatusForAgile(@ApiParam(value = "组织id", required = true)
                                               @PathVariable("organization_id") Long organizationId,
                                               @ApiParam(value = "state machine id", required = true)
                                               @RequestParam Long stateMachineId,
                                               @ApiParam(value = "status id", required = true)
                                               @RequestParam Long statusId) {
        statusService.removeStatusForAgile(organizationId, stateMachineId, statusId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
