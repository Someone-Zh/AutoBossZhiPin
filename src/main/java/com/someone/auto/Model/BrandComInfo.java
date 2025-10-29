package com.someone.auto.Model;

import lombok.Data;

@Data
public class BrandComInfo {
    /**
     * 公司名称
     */
    private String brandName;
    /**
     * 公司阶段 对应 Financing
     */
    private Integer stage;
    private String stageName;
    /**
     * 公司规模 对应 Scale
     */
    private Integer scale;
    private String scaleName;
    /**
     * 公司行业
     */
    private Integer industry;
    private String industryName;
    /***
     * 公司介绍
     */
    private String introduce;
    
    private Long activeTime;

    public BrandComInfo() {
    }

    
  
}
