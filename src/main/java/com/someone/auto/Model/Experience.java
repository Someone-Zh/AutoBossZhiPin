package com.someone.auto.Model;

import java.util.Arrays;
import java.util.Optional;


import lombok.Getter;

@Getter
public enum Experience {
    NULL("不限", "0"),
    STUDENT("在校生", "108"),
    GRADUATE("应届毕业生", "102"),
    UNLIMITED("经验不限", "101"),
    LESS_THAN_ONE_YEAR("1年以下", "103"),
    ONE_TO_THREE_YEARS("1-3年", "104"),
    THREE_TO_FIVE_YEARS("3-5年", "105"),
    FIVE_TO_TEN_YEARS("5-10年", "106"),
    MORE_THAN_TEN_YEARS("10年以上", "107");

    private final String name;
    private final String code;

    Experience(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<String> getCode(String name) {
        return Arrays.stream(Experience.values()).filter(experience -> experience.name.equals(name)).findFirst().map(experience -> experience.code);
    }
    @BeanTransform
    public static Experience forValue(String value) {
        for (Experience experience : Experience.values()) {
            if (experience.name.equals(value)) {
                return experience;
            }
        }
        return NULL;
    }
}
