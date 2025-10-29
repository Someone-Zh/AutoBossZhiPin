package com.someone.auto.Model;
import lombok.Getter;
@Getter
public enum Education {
    NULL("不限", "0"),
    BELOW_JUNIOR_HIGH_SCHOOL("初中及以下", "209"),
    SECONDARY_VOCATIONAL("中专/中技", "208"),
    HIGH_SCHOOL("高中", "206"),
    JUNIOR_COLLEGE("大专", "202"),
    BACHELOR("本科", "203"),
    MASTER("硕士", "204"),
    DOCTOR("博士", "205");

    private final String name;
    private final String code;

    Education(String name, String code) {
        this.name = name;
        this.code = code;
    }
    @BeanTransform
    public static Education forValue(String value) {
        for (Education degree : Education.values()) {
            if (degree.name.equals(value)) {
                return degree;
            }
        }
        return NULL;
    }

}
