package io.choerodon.statemachine.api.dto;


/**
 * @author shinan.chen
 * @date 2018/9/19
 */
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

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }

    public Long getResultStateId() {
        return resultStateId;
    }

    public void setResultStateId(Long resultStateId) {
        this.resultStateId = resultStateId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
