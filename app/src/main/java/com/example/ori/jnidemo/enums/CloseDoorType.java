package com.example.ori.jnidemo.enums;

/**
 * @author: hblolj
 * @date: 2019/1/3 16:19
 * @description:
 */
public enum CloseDoorType {
    NORMAL(1, "正常关门操作"),
    FORCE_RECYCLE_PREFIX(2, "强制回收前置关门操作"),
    WEIGH_PREFIX(3, "称重前置回收关门操作"),
    ;

    private Integer typeId;

    private String typeName;

    CloseDoorType(Integer typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }
}
