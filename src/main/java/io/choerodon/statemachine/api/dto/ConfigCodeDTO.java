package io.choerodon.statemachine.api.dto;

/**
 * @author shinan.chen
 * @date 2018/10/9
 */
public class ConfigCodeDTO {
    private String code;
    private String name;
    private String description;
    private String type;
    private String service;

    public  ConfigCodeDTO(){
    }

    public ConfigCodeDTO(String code, String name, String description, String type) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return "ConfigCodeDTO{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", service='" + service + '\'' +
                '}';
    }
}
