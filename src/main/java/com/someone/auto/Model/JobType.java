package com.someone.auto.Model;


import lombok.Getter;

@Getter
public enum JobType {
    NULL("不限", "0"),
    FULL_TIME("全职", "1901"),
    PART_TIME("兼职", "1903");

    private final String name;
    private final String code;

    JobType(String name, String code) {
        this.name = name;
        this.code = code;
    }
    @BeanTransform
    public static JobType forValue(String value) {
        for (JobType jobType : JobType.values()) {
            if (jobType.name.equals(value)) {
                return jobType;
            }
        }
        return NULL;
    }
}
