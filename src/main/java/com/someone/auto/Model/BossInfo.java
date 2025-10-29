package com.someone.auto.Model;



import lombok.Data;

@Data
public class BossInfo {
    public BossInfo() {
    }
    /**
     * boss 名称
     */
    private String name;
    /**
     * boss职位
     */
    private String title;

    /**
     * 活跃时间描述
     */
    private String activeTimeDesc;
    /**
     * 是否在线
     */
    private Boolean bossOnline;
    /**
     * 公司信息
     */
    private String brandName;

    

}
