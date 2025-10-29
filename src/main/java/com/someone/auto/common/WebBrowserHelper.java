package com.someone.auto.common;

import java.util.Map;

public class WebBrowserHelper {
    public static Map<String, String> getBrowserHttpOptions(){
       return Map.of(
                    "sec-ch-ua", "\"Google Chrome\";v=\"141\", \"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"141\"",
                    "sec-ch-ua-mobile", "?0",
                    "sec-ch-ua-platform", "\"Windows\"",
                    "accept-language", "zh-CN,zh;q=0.9",
                    "referer", "https://www.zhipin.com/",
                    "sec-fetch-dest", "empty",
                    "sec-fetch-mode", "cors",
                    "sec-fetch-site", "same-origin");
    }
    public static String getStealthScript(){
        String stealthScript = """
                Object.defineProperty(navigator, 'webdriver', {get: () => undefined});
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Array;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_JSON;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Object;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Promise;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Proxy;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Symbol;
                delete window.cdc_adoQpoasnfa76pfcZLmcfl_Window;
                window.navigator.chrome = { runtime: {} };
                Object.defineProperty(navigator, 'languages', {get: () => ['zh-CN', 'zh']});
                Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3]});
                Object.defineProperty(navigator, 'injected', {get: () => 123});
                """;
        return stealthScript;
    }
}
