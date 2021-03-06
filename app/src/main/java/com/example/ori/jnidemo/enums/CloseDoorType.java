package com.example.ori.jnidemo.enums;

/**
 * @author: hblolj
 * @date: 2019/1/3 16:19
 * @description:
 */
public enum CloseDoorType {
    NORMAL("AA", "正常关门操作"),
    FORCE_RECYCLE_PREFIX("BB", "强制回收前置关门操作"),
    WEIGH_PREFIX("CC", "称重前置回收关门操作"),
    ;

    private String typeId;

    private String typeName;

    CloseDoorType(String typeId, String typeName) {
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
