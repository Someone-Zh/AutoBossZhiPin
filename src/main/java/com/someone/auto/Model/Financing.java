package com.someone.auto.Model;


import lombok.Getter;
@Getter
public enum Financing {
    NULL("不限", "0"),
    UNFUNDED("未融资", "801"),
    ANGEL_ROUND("天使轮", "802"),
    A_ROUND("A轮", "803"),
    B_ROUND("B轮", "804"),
    C_ROUND("C轮", "805"),
    D_AND_ABOVE("D轮及以上", "806"),
    LISTED("已上市", "807"),
    NO_NEED("不需要融资", "808");

    private final String name;
    private final String code;

    Financing(String name, String code) {
        this.name = name;
        this.code = code;
    }
    @BeanTransform
    public static Financing forValue(String value) {
        for (Financing financing : Financing.values()) {
            if (financing.name.equals(value)) {
                return financing;
            }
        }
        return NULL;
    }
}
