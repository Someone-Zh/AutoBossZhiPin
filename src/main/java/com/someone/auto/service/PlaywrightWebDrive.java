package com.someone.auto.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import com.someone.auto.Model.BossInfo;
import com.someone.auto.Model.BrandComInfo;
import com.someone.auto.Model.Education;
import com.someone.auto.Model.Experience;
import com.someone.auto.Model.Financing;
import com.someone.auto.Model.JobInfo;
import com.someone.auto.Model.Scale;
import com.someone.auto.Model.SerializableCookie;
import com.someone.auto.common.BeanHelper;
import com.someone.auto.common.JsonHelper;
import com.someone.auto.common.ThreadHelper;
import com.someone.auto.common.WebBrowserHelper;
import com.someone.auto.config.ProcessConfig;
import com.someone.auto.config.RequireConfig;

/*
 * @Author: h+r 
 * @Date: 2025-10-01 11:43:02 
 * @Desc： Playwright web浏览器驱动实现 非安全单例 并发须修改逻辑
 */
@Component
public class PlaywrightWebDrive implements ActionDrive {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightWebDrive.class);

    public static String UNLIMITED_CODE = "0";
    /**
     * Playwright 实例
     */
    private Playwright playwright;

    /**
     * 浏览器实例
     */
    private Browser browser;
    /**
     * 桌面浏览器上下文
     */
    private BrowserContext desktopContext;
    /**
     * 操作的页面
     */
    private Page page;

    public PlaywrightWebDrive() {
         // 启动Playwright
        playwright = Playwright.create();
        // 创建浏览器实例
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false) // 非无头模式，可视化调试
                .setSlowMo(50)); // 放慢操作速度，便于调试

         // 创建桌面浏览器上下文
        desktopContext = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080) // 设置视口大小分辨率
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36"));
        
        page = desktopContext.newPage();
        // 设置浏览器请求头和注入防检测脚本
        desktopContext.setExtraHTTPHeaders(WebBrowserHelper.getBrowserHttpOptions());
        desktopContext.addInitScript(WebBrowserHelper.getStealthScript());
        page.addInitScript(WebBrowserHelper.getStealthScript());
        // 尝试加载Cookies
        loadCookies();
        
    }
    @Override
    public void navigateTo(String url) {
        page.navigate(url);
    }

    public void search(String baseUrl, RequireConfig config) {
        String searchUrl = getSearchUrl(baseUrl, config);
        page.navigate(searchUrl);
        page.waitForLoadState(LoadState.LOAD); // 快速基本加载
        log.info("search url:{}",searchUrl);
    }
    public void applyJob(){
        
        //点击“立即沟通”按钮
        Locator charBtn = page.locator("a.op-btn-chat");
        if (charBtn.count() == 0) {
            log.info("当前职位不可投递");
            return;
        }
        charBtn.first().click();
        ThreadHelper.sleepByRandom(1);
        Locator cancelBtn = page.locator("a.cancel-btn");
        // 取消按钮的判定次数 防止未取消
        int count = 0;
        while(count<2){
            if (count==0 && cancelBtn.count() == 0) {
                log.info("未找到取消按钮等待再次判定");
                ThreadHelper.sleepByRandom(0);
                if (count > 3) {
                    log.info("未找到取消按钮，放弃操作");
                    throw new RuntimeException("未找到取消按钮");
                }
            }else if (count==1 && cancelBtn.count() == 0){
                break;
            }else{
                cancelBtn.all().forEach(Locator::click);
            }
            count ++;
        }
    }
    /**
     * 响应处理 拦截详细请求
     * @param requireConfig
     */
    public void jobHandle(RequireConfig requireConfig,Response response) {
        String text = response.text();
        JSONObject jsonObject = new JSONObject(text);
        // 投递延时 模拟真人
        ThreadHelper.sleepByRandom(ProcessConfig.ProcessInterval);
        /**
         * 招聘者信息
         */
        JSONObject bossInfoJson = jsonObject.getJSONObject("zpData").getJSONObject("bossInfo");
        BossInfo bossInfo = new BossInfo();
        /**
         * 公司信息
         */
        JSONObject brandComInfoJson = jsonObject.getJSONObject("zpData").getJSONObject("brandComInfo");
        BrandComInfo brandComInfo = new BrandComInfo();
        /**
         * 职位信息
         */
        JSONObject jobInfoJson = jsonObject.getJSONObject("zpData").getJSONObject("jobInfo");
        JobInfo jobInfo = new JobInfo();
        try {
            BeanHelper.setBean(bossInfo, bossInfoJson.toMap());
            BeanHelper.setBean(brandComInfo, brandComInfoJson.toMap());
            BeanHelper.setBean(jobInfo, jobInfoJson.toMap());
        } catch (Exception e) {
            log.error("解析职位信息失败: {}", e.getMessage());
            return;
        }
        Boolean alreadySend = jsonObject.getJSONObject("zpData").getJSONObject("oneKeyResumeInfo").getBoolean("alreadySend");
        if (alreadySend) {
            log.info("投递过的职位不在重复：{} | 公司名称：{}| 薪资区间：{}", jobInfo.getJobName(),brandComInfo.getBrandName(),jobInfo.getSalaryDesc());
            return;
        }
        RequireChecker requireChecker = new RequireChecker(bossInfo, brandComInfo, jobInfo, requireConfig);
        if (!requireChecker.checkAll()) {
            return;
        }
        log.info("投递岗位：{} | 公司名称：{}| 薪资区间：{}", jobInfo.getJobName(),brandComInfo.getBrandName(),jobInfo.getSalaryDesc());
        ThreadHelper.sleepByRandom(1);
        applyJob();
    }
    private void saveCookies() {
        String cookiePath = ProcessConfig.CookiePath;
        List<Cookie> cookies = desktopContext.cookies();
        try {
            Files.createDirectories(Paths.get(cookiePath).getParent());
        } catch (IOException e) {
            log.error("Error creating cookie directory: {}", e.getMessage());
        }
        try (FileWriter file = new FileWriter(cookiePath)) {
            file.write(JsonHelper.toJsonWithJackson(cookies));
            log.info("Cookies saved to {}", cookiePath);
        } catch (Throwable e) {
            log.error("Error saving cookies: {}", e.getMessage());
        }
    }
    private Boolean loadCookies() {
        String cookiePath = ProcessConfig.CookiePath;
        File cookieFile = new File(cookiePath);
        if (cookieFile.exists()) {
            try {
                String content = Files.readString(Paths.get(cookiePath));
                List<SerializableCookie> cookies = JsonHelper.fromJsonWithJackson(content, new com.fasterxml.jackson.core.type.TypeReference<List<SerializableCookie>>() {});
                desktopContext.addCookies(cookies.stream().map(SerializableCookie::toPlaywrightCookie).collect(Collectors.toList()));
                log.info("Cookies loaded from {}", cookiePath);
                return true;
            } catch (Throwable e) {
                log.error("Error loading cookies: {}", e.getMessage());
            }
        } else {
            log.warn("Cookie file not found: {}", cookiePath);
        }
        return false;
    }
    private void login() {
        // 访问登录页面
        page.navigate(ProcessConfig.BossUrl + ProcessConfig.UserLoginUri);
        ThreadHelper.sleepByRandom(1);
        // 1. 如果已经登录，则直接返回
        try {
            Locator loginBtnLocator = page.locator(PlaywrightWebConstant.LOGIN_BTN);
            if (loginBtnLocator.count() > 0 && !PlaywrightWebConstant.LOGIN_TEXT.equals(loginBtnLocator.textContent())) {
                log.info("已经登录，无需重复登录");
                return;
            }
        } catch (Exception ignored) {
        }
        log.info("等待登录...");
        try {
            long startTime = System.currentTimeMillis();
            while (true) {
                // 判断是否超时
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= PlaywrightWebConstant.LOGIN_TIMEOUT) {
                    log.error("超过15分钟未登录，结束");
                    System.exit(1);
                }
                try {
                    // 判断页面上是否出现职位列表容器
                    Locator jobList = page.locator(PlaywrightWebConstant.JOB_LIST_CONTAINER_SELECTOR);
                    if (jobList.isVisible()) {
                        // 登录成功，保存Cookie
                        saveCookies();
                        break;
                    }
                } catch (Exception e) {
                    log.error("检测元素时异常: {}", e.getMessage());
                }
                // 每2秒左右检查一次
                ThreadHelper.sleepByRandom(2);
            }
        } catch (Exception e) {
            log.error("未找到二维码登录按钮，登录失败", e);
        }
    }

    private boolean isLogin(){
        try{
            page.waitForSelector(PlaywrightWebConstant.JOB_CARD_SELECTOR);
        }catch(Throwable t){
            //ignore
        }
        Locator loginBtn = page.locator(PlaywrightWebConstant.JOB_LOGIN_HEADER_BTN);
        if(loginBtn.count()>0){
            return false;
        }
        return true;
    }
    /**
     * 刷新职位并投递
     * @param requireConfig
     */
    public void refreshJob(RequireConfig requireConfig) {
        search(ProcessConfig.BossUrl + ProcessConfig.JobUri, requireConfig);
        if ( !isLogin()){
            login();
        }
        
        /**
         * 期望选择
         */
        if (requireConfig.getExpectedCount() > 0) {
            page.waitForSelector(PlaywrightWebConstant.JOB_EXPECT_LIST_A,new Page.WaitForSelectorOptions().setTimeout(3000)); // 防止元素未加载
            Locator explist = page.locator(PlaywrightWebConstant.JOB_EXPECT_LIST_A);
            if (explist.all().size() < requireConfig.getExpectedCount()){
                log.info("期望参数错误：{},当前期望：{}",requireConfig.getExpectedCount(),explist.allTextContents());
            }else{
                explist.nth(requireConfig.getExpectedCount()-1).click();
            }
        }else{
            //再次刷新防止第一个未获取到
            search(ProcessConfig.BossUrl + ProcessConfig.JobUri, requireConfig);
        }
        try{
            page.waitForSelector(PlaywrightWebConstant.JOB_CARD_SELECTOR);
        }catch(Throwable t){
            //ignore
        }
        int i = 0;
        while (true){
            Locator cards = page.locator(PlaywrightWebConstant.JOB_CARD_SELECTOR);
            int count = cards.count();
            // 判断是否继续滑动
            if (i == count) {
                break; // 没有新内容，跳出循环
            }
            try{
                // 提前点击第二个防止第一个请求无法拦截
                cards.nth(1).click();
                for (; i < count; i++) {
                    Locator nth = cards.nth(i);
                    // 滚动到元素并确保可见
                    try{
                        nth.scrollIntoViewIfNeeded();
                        Response response = page.waitForResponse(res->{
                            Request request = res.request();
                            return request.url().startsWith(ProcessConfig.BossUrl+ProcessConfig.JobDetailUri);
                        },new Page.WaitForResponseOptions().setTimeout(2000),()->{
                            nth.click();
                        });
                        jobHandle(requireConfig, response); 
                     }catch(Throwable t){
                        log.error("请求捕捉错误：{}", t);
                        continue;
                     }
                }
                page.evaluate("window.scrollTo(0, document.body.scrollHeight);");
                //等待新数据加载
                ThreadHelper.sleepByRandom(1);
            }catch(Exception t){
                log.error("迭代出现错误：{}",t.getMessage());
            }
        }
        
    }

    private static String getSearchUrl(String baseUrl, RequireConfig config) {
        return baseUrl + appendParam("city", config.getCityCode()) +
                appendParam("jobType", config.getJobType().getCode()) +
                appendParam("salary", config.getSalary().getCode()) +
                appendListParam("experience", config.getExperiences().stream().map(Experience::getCode).collect(Collectors.toList())) +
                appendListParam("degree", config.getEducations().stream().map(Education::getCode).collect(Collectors.toList())) +
                appendListParam("scale", config.getScales().stream().map(Scale::getCode).collect(Collectors.toList())) +
                appendListParam("stage", config.getFinancings().stream().map(Financing::getCode).collect(Collectors.toList())) +
                (StringUtils.hasText(config.getKeyword()) ? "&query=" + config.getKeyword() : "");
    }
    public static String appendParam(String name, String value) {
        return Optional.ofNullable(value)
                .filter(v -> !Objects.equals(UNLIMITED_CODE, v))
                .map(v -> "&" + name + "=" + v)
                .orElse("");
    }

    public static String appendListParam(String name, List<String> values) {
        return Optional.ofNullable(values)
                .filter(list -> !list.isEmpty() && !Objects.equals(UNLIMITED_CODE, list.getFirst()))
                .map(list -> "&" + name + "=" + String.join(",", list))
                .orElse("");
    }
}
