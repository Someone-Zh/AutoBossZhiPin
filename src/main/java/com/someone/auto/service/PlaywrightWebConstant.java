package com.someone.auto.service;

public class PlaywrightWebConstant {
    public static String JOB_CARD_SELECTOR = "//ul[contains(@class, 'rec-job-list')]//li[contains(@class, 'job-card-box')]";
    public static String JOB_DETAIL_SELECTOR = "div[class*='job-detail-box']";
    public static String JOB_NAME_SELECTOR = "span[class*='job-name']";
    public static String JOB_LIST_CONTAINER_SELECTOR = "div.job-list-container";
    public static String JOB_LOGIN_HEADER_BTN = "a.header-login-btn";
    public static String JOB_EXPECT_LIST = ".expect-list";
    public static String JOB_EXPECT_LIST_A = ".expect-list >> a";
    //登录页
    public static final String LOGIN_BTN = "//li[@class='nav-figure']";
    public static final String LOGIN_TEXT = "登录";
    public static final long LOGIN_TIMEOUT = 15 * 60 * 1000; // 15分钟

}
