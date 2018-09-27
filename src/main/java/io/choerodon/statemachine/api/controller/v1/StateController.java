package io.choerodon.statemachine.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.statemachine.api.dto.StateDTO;
import io.choerodon.statemachine.api.service.StateService;
import io.choerodon.statemachine.api.validator.StateValidator;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author peng.jiang@hand-china.com
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/states")
public class StateController extends BaseController {

    @Autowired
    private StateService stateService;

    @Autowired
    private StateValidator stateValidator;

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询状态列表")
    @CustomPageRequest
    @GetMapping
    public ResponseEntity<Page<StateDTO>> pagingQuery(@ApiIgnore
                                                      @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                      @PathVariable("organization_id") Long organizationId,
                                                      @RequestParam(required = false) String name,
                                                      @RequestParam(required = false) String description,
                                                      @RequestParam(required = false) String type,
                                                      @RequestParam(required = false) String[] param) {
        return new ResponseEntity<>(stateService.pageQuery(pageRequest, new StateDTO(name, description, type, organizationId),
                Arrays.stream(param).collect(Collectors.joining(","))), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "创建状态")
    @PostMapping
    public ResponseEntity<StateDTO> create(@PathVariable("organization_id") Long organizationId,
                                           @RequestBody StateDTO stateDTO) {
        stateValidator.validate(stateDTO);
        return new ResponseEntity<>(stateService.create(organizationId, stateDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "更新状态")
    @PutMapping(value = "/{state_id}")
    public ResponseEntity<StateDTO> update(@PathVariable("organization_id") Long organizationId,
                                           @PathVariable("state_id") Long stateId,
                                           @RequestBody @Valid StateDTO stateDTO) {
        stateDTO.setId(stateId);
        stateDTO.setOrganizationId(organizationId);
        stateValidator.validate(stateDTO);
        return new ResponseEntity<>(stateService.update(stateDTO), HttpStatus.CREATED);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除状态")
    @DeleteMapping(value = "/{state_id}")
    public ResponseEntity<Boolean> delete(@PathVariable("organization_id") Long organizationId,
                                          @PathVariable("state_id") Long stateId) {
        return new ResponseEntity<>(stateService.delete(organizationId, stateId), HttpStatus.NO_CONTENT);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "根据id查询状态对象")
    @GetMapping(value = "/{state_id}")
    public ResponseEntity<StateDTO> queryStateById(@PathVariable("organization_id") Long organizationId,
                                                   @PathVariable("state_id") Long stateId) {
        return new ResponseEntity<>(stateService.queryStateById(organizationId, stateId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "查询组织下的所有状态")
    @GetMapping(value = "/query_all")
    public ResponseEntity<List<StateDTO>> queryAllState(@PathVariable("organization_id") Long organizationId) {
        return new ResponseEntity<>(stateService.queryAllState(organizationId), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "校验状态名字是否未被使用")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(@PathVariable("organization_id") Long organizationId,
                                             @RequestParam(value = "state_id", required = false) Long stateId,
                                             @RequestParam("name") String name) {
        return new ResponseEntity<>(stateService.checkName(organizationId, stateId, name), HttpStatus.OK);
    }

}
