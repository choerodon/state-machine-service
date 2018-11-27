package io.choerodon.statemachine.api.dto;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/11/27.
 * Email: fuqianghuang01@gmail.com
 */
public class StatusSearchDTO {

    private String name;

    private String description;

    private String type;

    private String param;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
