package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/10/15
 */
public enum InitStatus {
    STATUS1("待处理", StatusType.TODO),
    STATUS2("处理中", StatusType.DOING),
    STATUS4("测试中", StatusType.DOING),
    STATUS5("验证中", StatusType.DOING),
    STATUS3("已完成", StatusType.DONE);
    String name;
    String type;

    InitStatus(String name, String type) {
        this.name = name;
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
}
