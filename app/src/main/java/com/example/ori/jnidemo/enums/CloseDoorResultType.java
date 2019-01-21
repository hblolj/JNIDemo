package com.example.ori.jnidemo.enums;

/**
 * @author: hblolj
 * @date: 2019/1/15 15:42
 * @description: 关门结果类型
 */
public enum CloseDoorResultType {

    SUCCESS("F8", "关门成功!"),
    FAILD("F5", "关门失败!"),
    EXCEPTION("F1", "关门检测到障碍物!重置为开门状态!"),
    ;

    private String typeId;

    private String typeName;

    CloseDoorResultType(String typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }
}
