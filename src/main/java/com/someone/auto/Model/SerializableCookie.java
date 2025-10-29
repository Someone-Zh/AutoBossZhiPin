package com.someone.auto.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;

import lombok.Data;

@Data
public class SerializableCookie {
    private String name;
    private String value;
    private String domain;
    private String url;
    private String path;
    private Boolean secure;
    private Boolean httpOnly;
    private SameSiteAttribute sameSite;
    private Double expires;

    // 默认构造函数（Jackson需要）
    public SerializableCookie() {}

    // 从Playwright Cookie创建包装对象
    public SerializableCookie(Cookie cookie) {
        this.name = cookie.name;
        this.url = cookie.url;
        this.value = cookie.value;
        this.domain = cookie.domain;
        this.path = cookie.path;
        this.secure = cookie.secure;
        this.httpOnly = cookie.httpOnly;
        this.sameSite = cookie.sameSite != null ? cookie.sameSite : null;
        this.expires = cookie.expires;
    }

    // 使用JsonCreator注解的构造函数
    @JsonCreator
    public SerializableCookie(
            @JsonProperty("name") String name,
            @JsonProperty("value") String value,
            @JsonProperty("url") String url,
            @JsonProperty("domain") String domain,
            @JsonProperty("path") String path,
            @JsonProperty("secure") Boolean secure,
            @JsonProperty("httpOnly") Boolean httpOnly,
            @JsonProperty("sameSite") SameSiteAttribute sameSite,
            @JsonProperty("expires") Double expires) {
        this.name = name;
        this.value = value;
        this.url = url;
        this.domain = domain;
        this.path = path;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.sameSite = sameSite;
        this.expires = expires;
    }

    // 转换回Playwright Cookie对象
    public Cookie toPlaywrightCookie() {
        Cookie builder = new Cookie(name, value);
        if (domain != null) builder.setDomain(domain);
        if (path != null) builder.setPath(path);
        if (url != null) builder.setUrl(url);
        if (secure != null) builder.setSecure(secure);
        if (httpOnly != null) builder.setHttpOnly(httpOnly);
        if (sameSite != null) {
            builder.setSameSite(sameSite);
        }
        if (expires != null) builder.setExpires(expires);
        return builder;
    }

    @Override
    public String toString() {
        return "SerializableCookie{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", value='" + value + '\'' +
                ", domain='" + domain + '\'' +
                ", path='" + path + '\'' +
                ", secure=" + secure +
                ", httpOnly=" + httpOnly +
                ", sameSite=" + sameSite +
                ", expires=" + expires +
                '}';
    }
}
