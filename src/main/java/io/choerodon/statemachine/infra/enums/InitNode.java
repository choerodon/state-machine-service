package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/10/15
 */
public enum InitNode {
    START("初始节点", 25L, 0L, 50L, 50L, NodeType.START),
    INIT("待处理", 0L, 120L, 100L, 50L, NodeType.INIT),
    NODE2("处理中", 0L, 0L, 0L, 0L, NodeType.CUSTOM),
    NODE3("测试中", 0L, 0L, 0L, 0L, NodeType.CUSTOM),
    NODE4("验证中", 0L, 0L, 0L, 0L, NodeType.CUSTOM),
    NODE5("已完成", 0L, 0L, 0L, 0L, NodeType.CUSTOM);
    String statusName;
    Long positionX;
    Long positionY;
    Long width;
    Long height;
    String type;

    InitNode(String statusName, Long positionX, Long positionY, Long width, Long height, String type) {
        this.statusName = statusName;
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Long getPositionX() {
        return positionX;
    }

    public void setPositionX(Long positionX) {
        this.positionX = positionX;
    }

    public Long getPositionY() {
        return positionY;
    }

    public void setPositionY(Long positionY) {
        this.positionY = positionY;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
