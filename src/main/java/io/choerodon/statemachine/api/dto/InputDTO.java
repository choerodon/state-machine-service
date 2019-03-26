package io.choerodon.statemachine.api.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/11/8
 */
public class InputDTO {
    @ApiModelProperty(value = "实例id（issueId）")
    private Long instanceId;
    @ApiModelProperty(value = "反射的方法")
    private String invokeCode;
    @ApiModelProperty(value = "输入数据的json")
    private String input;
    @ApiModelProperty(value = "状态机配置列表")
    private List<StateMachineConfigDTO> configs;

    public String getInvokeCode() {
        return invokeCode;
    }

    public void setInvokeCode(String invokeCode) {
        this.invokeCode = invokeCode;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public List<StateMachineConfigDTO> getConfigs() {
        return configs;
    }

    public void setConfigs(List<StateMachineConfigDTO> configs) {
        this.configs = configs;
    }
}
