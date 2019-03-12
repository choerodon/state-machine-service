package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/10/23
 */
public enum InitStatus {
    PREPARE("准备", "prepare", StatusType.PREPARE),
    CREATE("待处理", "create", StatusType.TODO),
    PROCESSING("处理中", "processing", StatusType.DOING),
    COMPLETE("已完成", "complete", StatusType.DONE);
    String name;
    String code;
    String type;

    InitStatus(String name, String code, String type) {
        this.name = name;
        this.code = code;
        this.type = type;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
