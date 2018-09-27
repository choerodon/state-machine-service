package io.choerodon.statemachine.api.dto;


/**
 * @author shinan.chen
 * @date 2018/9/19
 */
public class ExecuteResult {
    Boolean isSuccess;
    Long resultStatusId;
    String errorMessage;

    public ExecuteResult() {
    }

    public ExecuteResult(Boolean isSuccess, Long resultStatusId, String errorMessage) {
        this.isSuccess = isSuccess;
        this.resultStatusId = resultStatusId;
        this.errorMessage = errorMessage;
    }

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }

    public Long getResultStatusId() {
        return resultStatusId;
    }

    public void setResultStatusId(Long resultStatusId) {
        this.resultStatusId = resultStatusId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
