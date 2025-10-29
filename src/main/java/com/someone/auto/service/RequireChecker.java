package com.someone.auto.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.someone.auto.Model.BossInfo;
import com.someone.auto.Model.BrandComInfo;
import com.someone.auto.Model.Financing;
import com.someone.auto.Model.JobInfo;
import com.someone.auto.config.RequireConfig;

public class RequireChecker {
    private static final Logger log = LoggerFactory.getLogger(RequireChecker.class);

    private BossInfo  bossInfo;
    private BrandComInfo  brandComInfo;
    private JobInfo  jobInfo;
    private RequireConfig requireConfig;


    public RequireChecker(BossInfo bossInfo, BrandComInfo brandComInfo, JobInfo jobInfo, RequireConfig requireConfig) {
        this.bossInfo = bossInfo;
        this.brandComInfo = brandComInfo;
        this.jobInfo = jobInfo;
        this.requireConfig = requireConfig;
    }
    public boolean checkAll() {
        return checkBossInfo() && checkBrandComInfo() && checkJobInfo();
    }

    private boolean checkJobInfo() {
        // 职位描述
        String postDescription = jobInfo.getPostDescription();
        Optional<String> exclusion = requireConfig.getKeywordExclusions().stream().filter(keyword -> postDescription.contains(keyword)).findFirst();
        if (exclusion.isPresent()) {
            log.info("跳过职位，职位描述包含排除关键词：{}，公司名称：{}", exclusion.get(), brandComInfo.getBrandName());
            return false;
        }
        // 区域
        if (!requireConfig.getCityCode().equals(jobInfo.getLocation().toString())) {
            log.info("跳过职位，职位区域不匹配：{}，公司名称：{}", jobInfo.getLocationName(), brandComInfo.getBrandName());
            return false;
        }
        
        // 薪资
        String salaryDesc = jobInfo.getSalaryDesc();
        Integer[] salary = new Integer[2];
        try{
            salaryDesc = salaryDesc.substring(0,salaryDesc.indexOf("K"));
            String[] arr = salaryDesc.split("-");
            salary[0] = Integer.parseInt(arr[0]);
            salary[1] = Integer.parseInt(arr[1]);
        }catch (Exception e){
            log.error("薪资解析失败：{}，公司名称：{}", salaryDesc, brandComInfo.getBrandName());
            return false;
        }
        if (requireConfig.getMinimumWage() != null){
            if ( requireConfig.getMinimumWage() > salary[0]) {
                log.info("跳过职位，薪资不满足最低要求：{}，公司名称：{}", jobInfo.getSalaryDesc(), brandComInfo.getBrandName());
                return false;
            }
        }
        if (requireConfig.getMaxWage() != null){
            if (requireConfig.getMaxWage() <= salary[0]) {
                log.info("跳过职位，薪资区间超过最大限制：{}，公司名称：{}", jobInfo.getSalaryDesc(), brandComInfo.getBrandName());
                return false;
            }
        }
        if (requireConfig.getExpectWage() != null){
            if (requireConfig.getExpectWage() > salary[1]) {
                log.info("跳过职位，薪资区间不满足：{}，公司名称：{}", jobInfo.getSalaryDesc(), brandComInfo.getBrandName());
                return false;
            }
        }
        
        String jobName = jobInfo.getJobName();
        Optional<String> jobNameOptional = requireConfig.getJobNameExclusions().stream().filter(keyword -> jobName.contains(keyword)).findFirst();
        if (jobNameOptional.isPresent()) {
            log.info("跳过职位，职位名称包含排除关键词：{}，公司名称：{}", jobNameOptional.get(), brandComInfo.getBrandName());
            return false;
        }
        return true;
    }

    private boolean checkBossInfo() {
         // 公司名称
        if (requireConfig.getCompanyBlacklist().contains(bossInfo.getBrandName())) {
            log.info("跳过职位，黑名单公司：{}", bossInfo.getBrandName());
            return false;
        }

        // 活跃时间
        if (!requireConfig.getActiveTimeDesc().contains(bossInfo.getActiveTimeDesc())) {
            log.info("跳过职位，招聘者活跃时间不满足：{}，公司名称：{}", bossInfo.getActiveTimeDesc(), bossInfo.getBrandName());
            return false;
        }
        // 招聘人职位
        String title = bossInfo.getTitle();
        Optional<String> recruiterExclusion = requireConfig.getRecruiterExclusions().stream().filter(exclusion -> title.contains(exclusion)).findFirst();
        if (recruiterExclusion.isPresent()) {
            log.info("跳过职位，招聘人职位包含排除关键词：{}，公司名称：{}", recruiterExclusion.get(), bossInfo.getBrandName());
            return false;
        }
        return true;
    }

    private boolean checkBrandComInfo() {
        //发展阶段
        String stage = brandComInfo.getStage().toString();
        Optional<Financing> financing = requireConfig.getFinancings().stream().filter(keyword -> Financing.NULL.equals(keyword)).findFirst();
        if (!financing.isPresent() || requireConfig.getFinancings().stream().anyMatch(f -> f.getCode().equals(stage))) {
            log.info("跳过职位，公司发展阶段不满足：{}，公司名称：{}", brandComInfo.getStageName(), brandComInfo.getBrandName());
            return false;
        }
        // 行业
        boolean industryMatch = requireConfig.getIndustries().stream().anyMatch(industry -> brandComInfo.getIndustryName().contains(industry));
        if (requireConfig.getIndustries().size()>0 && !industryMatch) {
            log.info("跳过职位，行业不满足：{}，公司名称：{}", brandComInfo.getIndustryName(), brandComInfo.getBrandName());
            return false;
        }
        return true;
    }
}
