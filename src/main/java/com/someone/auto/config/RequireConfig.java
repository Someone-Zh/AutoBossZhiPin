package com.someone.auto.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.someone.auto.Model.Education;
import com.someone.auto.Model.Experience;
import com.someone.auto.Model.Financing;
import com.someone.auto.Model.JobType;
import com.someone.auto.Model.Salary;
import com.someone.auto.Model.Scale;
import com.someone.auto.common.BeanHelper;
import com.someone.auto.common.YamlParser;

import lombok.Data;

/*
 * @Author: h+r 
 * @Date: 2025-10-01 11:43:02 
 * @Desc： 求职要求配置
 */
@Component
@Data
public class RequireConfig {
    /**
     * 期望岗位(BOSS 上推荐旁边的选项，自己创建的求职期望)
     */
    private int expectedCount = 0;

    /**
     * 城市编码
     */
    private String cityCode;

     /**
     * 工作类型
     */
    private JobType jobType;

    /**
     * 期望薪资
     */
    private Salary salary;

    /**
     * 最低薪资
     */
    private Integer minimumWage;
    /**
     * 最大薪资限制
     */
    private Integer maxWage;
    /**
     * 期望薪资
     */
    private Integer expectWage;
    /**
     * 工作经验
     */
    private List<Experience> experiences;
    /**
     * 学历要求
     */
    private List<Education> educations;
    /**
     * 期望行业
     */
    private List<String> industries;
    /**
     * 期望融资阶段
     */
    private List<Financing> financings;
    /**
     * 期望公司规模
     */
    private List<Scale> scales;

     /**
     * 搜索关键词
     */
    private String keyword;
    /**
     * 活跃时间要求
     */
    private List<String> activeTimeDesc;

    /**
     * 公司黑名单
     */
    private List<String> companyBlacklist;
    /**
     * 关键词排除
     */
    private List<String> keywordExclusions;
    /**
     * 岗位名称关键词排除
     */
    private List<String> jobNameExclusions;

    /**
     * 招聘人员排除
     */
    private List<String> recruiterExclusions;

    
    public RequireConfig() {
        try {
            loadConfig();
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件失败: " + e.getMessage(), e);
        }
    }
    @SuppressWarnings("unchecked")
    private void loadConfig() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(ProcessConfig.ConfigPath);
        
        Map<String, Object> cfg = YamlParser.parseYaml(is);
        is.close();
        String key = this.getClass().getSimpleName().toLowerCase().replaceAll("config", "");
        if (!cfg.containsKey(key)) {
            throw new RuntimeException("配置文件缺少 " + key + " 节点");
        }
        Object object = cfg.get(key);

        try {
            BeanHelper.setBean(this, (Map<String, Object>) object);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("配置初始化失败: " + e.getMessage(), e);
        }
        System.out.println(this);
        try{
            String body = null;
            File cache = new File(ProcessConfig.CityJsonPath);
            if (cache.exists()) {
                try {
                    body = Files.readString(Paths.get(ProcessConfig.CityJsonPath));
                } catch (Throwable e) {
                    throw new RuntimeException("Cache 文件读取失败，请删除 "+ProcessConfig.CityJsonPath+"文件");
                }
            } else {
                // 创建HttpClient实例
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                
                // GET请求示例
                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(ProcessConfig.BossUrl + ProcessConfig.CityGroupUri))
                        .timeout(Duration.ofSeconds(30))
                        .header("User-Agent", "Java HttpClient")
                        .header("Accept", "application/json")
                        .GET() // 显式指定GET方法
                        .build();
                HttpResponse<String> getResponse = client.send(getRequest, 
                        HttpResponse.BodyHandlers.ofString());
                body = getResponse.body();
                try (FileWriter file = new FileWriter(ProcessConfig.CityJsonPath)) {
                    file.write(body);
                } catch (Throwable e) {
                }
            }
            JSONObject jsonObject = new JSONObject(body);
            JSONArray jsonArray = jsonObject.getJSONObject("zpData").getJSONArray("cityGroup");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject cityGroup = jsonArray.getJSONObject(i);
                JSONArray cities = cityGroup.getJSONArray("cityList");
                for (int j = 0; j < cities.length(); j++) {
                    JSONObject city = cities.getJSONObject(j);
                    if (this.getCityCode().equals(city.getString("name"))) {
                        this.setCityCode(city.getBigInteger("code").toString());
                        return;
                    }
                }
            }
        }catch(Exception e){
            throw new RuntimeException("城市编码获取失败: " + e.getMessage(), e);
        }
    }
    

}
