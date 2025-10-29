package com.someone.auto.config;


/*
 * @Author: h+r 
 * @Date: 2025-10-01 11:43:02 
 * @Desc： 求职要求配置
 */

public class ProcessConfig {
    /**
     * 每次处理职位的间隔时间，单位秒，默认3s
     */
    public final static int ProcessInterval = 3;
    /**
     * BOSS网站基础URL
     */
    public static String BossUrl = "https://www.zhipin.com";
    public static String JobUri = "/web/geek/job?";
    public static String UserLoginUri = "/web/user/?ka=header-login";
    public static String JobDetailUri = "/wapi/zpgeek/job/detail.json";
    public static String CityGroupUri = "/wapi/zpCommon/data/cityGroup.json";
    public static String CookiePath = "cookies/cookies.txt";
    public static String CityJsonPath = "cookies/City.json";

    /**
     * 配置文件
     */
    public static String ConfigPath = "config.yaml";

}
