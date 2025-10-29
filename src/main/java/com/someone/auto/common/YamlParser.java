package com.someone.auto.common;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import com.someone.auto.config.ProcessConfig;

import java.io.*;
import java.util.*;

/**
 * SnakeYAML 版本的 YAML 工具类
 * 
 * @author h+r
 * @version 1.0
 */
public class YamlParser {

    private static final Yaml YAML;
    
    static {
        // 配置序列化选项
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(2);
        dumperOptions.setExplicitStart(true);
        dumperOptions.setExplicitEnd(true);
        // 创建安全的表示器
        Representer representer = new Representer(dumperOptions);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        // 创建YAML实例
        YAML = new Yaml(representer, dumperOptions);
    }

    /**
     * 将YAML字符串解析为Map
     * 
     * @param yamlContent YAML字符串内容
     * @return 解析后的Map对象
     * @throws YAMLException 如果解析失败
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseYaml(String yamlContent) throws YAMLException {
        return parseYaml(yamlContent, Map.class);
    }

    /**
     * 将YAML字符串解析为Map
     * 
     * @param yamlContent YAML字符串内容
     * @return 解析后的Map对象
     * @throws YAMLException 如果解析失败
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseYaml(InputStream stream) throws YAMLException {
        return parseYaml(stream, Map.class);
    }
    /**
     * 将YAML字符串解析为指定类型
     * 
     * @param yamlContent YAML字符串内容
     * @param targetType 目标类型（如 Map.class, List.class, 自定义类.class）
     * @return 解析后的对象
     * @throws YAMLException 如果解析失败
     */
    public static <T> T parseYaml(InputStream stream, Class<T> targetType) throws YAMLException {
        return YAML.loadAs(stream, targetType);
    }
    /**
     * 将YAML字符串解析为指定类型
     * 
     * @param yamlContent YAML字符串内容
     * @param targetType 目标类型（如 Map.class, List.class, 自定义类.class）
     * @return 解析后的对象
     * @throws YAMLException 如果解析失败
     */
    public static <T> T parseYaml(String yamlContent, Class<T> targetType) throws YAMLException {
        return YAML.loadAs(yamlContent, targetType);
    }

    /**
     * 将YAML文件解析为Map
     * 
     * @param filePath YAML文件路径
     * @return 解析后的Map对象
     * @throws IOException 如果文件读取失败
     * @throws YAMLException 如果解析失败
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseYamlFile(String filePath) throws IOException, YAMLException {
        return parseYamlFile(filePath, Map.class);
    }

    /**
     * 将YAML文件解析为指定类型
     * 
     * @param filePath 文件路径
     * @param targetType 目标类型
     * @return 解析后的对象
     * @throws IOException 如果文件读取失败
     * @throws YAMLException 如果解析失败
     */
    public static <T> T parseYamlFile(String filePath, Class<T> targetType) throws IOException, YAMLException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return parseYaml(content.toString(), targetType);
        }
    }

    /**
     * 将对象转换为YAML字符串
     * 
     * @param object 要转换的对象
     * @return YAML格式的字符串
     */
    public static String toYaml(Object object) {
        return YAML.dump(object);
    }

    /**
     * 将对象写入YAML文件
     * 
     * @param object 要写入的对象
     * @param filePath 文件路径
     * @throws IOException 如果写入失败
     */
    public static void toYamlFile(Object object, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(toYaml(object));
        }
    }

    public static void main(String[] args) {
        InputStream is = YamlParser.class.getClassLoader().getResourceAsStream(ProcessConfig.ConfigPath);
        Map<String, Object> result = parseYaml(is);
        System.out.println(result);
    }
}