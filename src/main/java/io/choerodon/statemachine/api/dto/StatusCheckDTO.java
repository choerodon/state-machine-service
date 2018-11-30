package io.choerodon.statemachine.api.dto;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/11/30.
 * Email: fuqianghuang01@gmail.com
 */
public class StatusCheckDTO {

    private Boolean statusExist;

    private Long id;

    private String name;

    private String type;

    public void setStatusExist(Boolean statusExist) {
        this.statusExist = statusExist;
    }

    public Boolean getStatusExist() {
        return statusExist;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
