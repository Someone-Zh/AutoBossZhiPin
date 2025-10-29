package com.someone.auto.Model;


import lombok.Data;

@Data
public class JobInfo {
    /**
     * 职位名称
     */
    private String jobName;
    /**
     * 位置 城市级别
     */
    private Integer location;
    private String locationName;
    /**
     * 工作经验
     */
    private String experienceName;
    /**
     * 学历
     */
    private String degreeName;
    /**
     * 职位类型 对应 JobTypeEnum
     */
    private Integer jobType;
    /**
     *  薪资区间
     */
    private String salaryDesc;
    /**
     * 岗位描述
     */
    private String postDescription;
    /**
     * 岗位位置 具体地址
     */
    private String address;
    public JobInfo() {
    }
    
}
