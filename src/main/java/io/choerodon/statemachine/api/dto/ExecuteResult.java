package io.choerodon.statemachine.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shinan.chen
 * @date 2018/9/19
 */
@Getter
@Setter
public class ExecuteResult {
    Boolean isSuccess;
    Long resultStateId;
    String errorMessage;

    public ExecuteResult() {
    }

    public ExecuteResult(Boolean isSuccess, Long resultStateId, String errorMessage) {
        this.isSuccess = isSuccess;
        this.resultStateId = resultStateId;
        this.errorMessage = errorMessage;
    }
}
