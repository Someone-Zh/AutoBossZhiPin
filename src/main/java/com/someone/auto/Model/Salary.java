package com.someone.auto.Model;

import lombok.Getter;

@Getter
public enum Salary {
    NULL("不限", "0"),
    BELOW_3K("3K以下", "402"),
    FROM_3K_TO_5K("3-5K", "403"),
    FROM_5K_TO_10K("5-10K", "404"),
    FROM_10K_TO_20K("10-20K", "405"),
    FROM_20K_TO_50K("20-50K", "406"),
    ABOVE_50K("50K以上", "407");

    private final String name;
    private final String code;

    Salary(String name, String code) {
        this.name = name;
        this.code = code;
    }
    @BeanTransform
    public static Salary forValue(String value) {
        for (Salary salary : Salary.values()) {
            if (salary.name.equals(value)) {
                return salary;
            }
        }
        return NULL;
    }
}
