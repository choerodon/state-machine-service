package io.choerodon.statemachine.infra.enums;

/**
 * @author shinan.chen
 * @date 2018/10/15
 */
public enum InitNode {
    START("start", 21L, 0L, 20L, 20L, NodeType.START),
    INIT("create", 0L, 50L, 62L, 26L, NodeType.INIT),
    NODE1("processing", 0L, 100L, 62L, 26L, NodeType.CUSTOM),
    NODE2("complete", 0L, 150L, 62L, 26L, NodeType.CUSTOM);
    String code;
    Long positionX;
    Long positionY;
    Long width;
    Long height;
    String type;

    InitNode(String code, Long positionX, Long positionY, Long width, Long height, String type) {
        this.code = code;
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
