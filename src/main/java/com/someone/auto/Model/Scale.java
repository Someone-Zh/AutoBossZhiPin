package com.someone.auto.Model;

import lombok.Getter;
@Getter
public enum Scale {
    NULL("不限", "0"),
    ZERO_TO_TWENTY("0-20人", "301"),
    TWENTY_TO_NINETY_NINE("20-99人", "302"),
    ONE_HUNDRED_TO_FOUR_NINETY_NINE("100-499人", "303"),
    FIVE_HUNDRED_TO_NINE_NINETY_NINE("500-999人", "304"),
    ONE_THOUSAND_TO_NINE_NINE_NINE_NINE("1000-9999人", "305"),
    TEN_THOUSAND_ABOVE("10000人以上", "306");

    private final String name;
    private final String code;

    Scale(String name, String code) {
        this.name = name;
        this.code = code;
    }
    @BeanTransform
    public static Scale forValue(String value) {
        for (Scale scale : Scale.values()) {
            if (scale.name.equals(value)) {
                return scale;
            }
        }
        return NULL;
    }
}