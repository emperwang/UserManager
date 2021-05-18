package com.test.wk.service;


import com.alibaba.fastjson.JSONObject;
import com.test.wk.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TemperatureService {
    private final Integer ConnectTimeout = 10000;
    private final Integer SocketTimeout = 10000;

    private Semaphore limits = new Semaphore(100);

    class TemperaturaException extends Exception {
        public TemperaturaException() {
        }

        public TemperaturaException(String message) {
            super(message);
        }
    }

    // http://www.weather.com.cn/data/sk/101190401.html
    public Optional<Integer> getTemperature(String province, String city, String county) throws TemperaturaException {
        Optional<Integer> result = Optional.empty();
        try {
            limits.acquire();
             result = internalTemperature(province, city, county);
        } catch (InterruptedException e) {
            log.info("msg: {} ", e.getMessage());
        } finally {
            limits.release();
        }
        return result;
    }

    private Optional<Integer> internalTemperature(String province, String city, String county) throws TemperaturaException {
        Map<String, String> counties = getCounty(province, city);
        Map<String, String> result = new HashMap<>();
        String provinceCode = counties.get("provinceCode");
        String cityCode = counties.get("cityCode");
        if (StringUtils.isEmpty(provinceCode)){
            throw new TemperaturaException("couldn't find province code");
        }
        if (StringUtils.isEmpty(provinceCode)){
            throw new TemperaturaException("couldn't find city code");
        }
        if (counties != null && !counties.isEmpty() && (counties.get("code").equalsIgnoreCase("200") || counties.get("code").equalsIgnoreCase("201"))){
            String msg = counties.get("message");
            JSONObject jsonObject = JSONObject.parseObject(msg);
            String countyCode = "";
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String tmp = (String)entry.getValue();
                if (county.equalsIgnoreCase(tmp)){
                    countyCode = entry.getKey();
                    break;
                }
            }
            if (StringUtils.isEmpty(countyCode)) {
                throw  new TemperaturaException("couldn't find county code.");
            }
            String url = new StringBuilder().append("http://www.weather.com.cn/data/sk/")
                    .append(provinceCode)
                    .append(cityCode)
                    .append(countyCode)
                    .append(".html").toString();
            result = queryMsg(url);
            JSONObject object = JSONObject.parseObject(result.get("message"));
            String obj = ((JSONObject) object.get("weatherinfo")).getString("temp");
            Double dou = Double.parseDouble(obj);
            return Optional.of(dou.intValue());
        } else {
            log.info("couldn't find city..");
            throw new TemperaturaException("couldn't find county code.");
        }
    }

    // http://www.weather.com.cn/data/city3jdata/china.html
    public Map<String, String> getProvince(){
        Map<String, String> result = queryMsg("http://www.weather.com.cn/data/city3jdata/china.html");
        //String message = result.get("message");
        return result;
    }

    // http://www.weather.com.cn/data/city3jdata/provshi/10119.html
    public Map<String, String> getCity(String province){
        Map<String, String> provinces = getProvince();
        Map<String, String> result = new HashMap<>();
        if (provinces != null && !provinces.isEmpty() && (provinces.get("code").equalsIgnoreCase("200") || provinces.get("code").equalsIgnoreCase("201"))){
            String msg = provinces.get("message");
            JSONObject jsonObject = JSONObject.parseObject(msg);
            String provCode = "";
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String prov = (String)entry.getValue();
                if (province.equalsIgnoreCase(prov)){
                    provCode = entry.getKey();
                    break;
                }
            }
            if (StringUtils.isEmpty(provCode)) {
                return configResult("500","couldn't find province code.");
            }
            String url = new StringBuilder().append("http://www.weather.com.cn/data/city3jdata/provshi/").append(provCode).append(".html").toString();
            result = queryMsg(url);
            result.put("provinceCode",provCode);
            return result;
        } else {
            log.info("couldn't find province..");
            return configResult("500","couldn't find province code.");
        }
    }

    // http://www.weather.com.cn/data/city3jdata/station/1011904.html
    public Map<String, String> getCounty(String province, String city){
        Map<String, String> cities = getCity(province);
        Map<String, String> result = new HashMap<>();
        String provinceCode = cities.get("provinceCode");
        if (StringUtils.isEmpty(provinceCode)){
            return configResult("500","couldn't find province code.");
        }
        if (cities != null && !cities.isEmpty() && (cities.get("code").equalsIgnoreCase("200") || cities.get("code").equalsIgnoreCase("201"))){
            String msg = cities.get("message");
            JSONObject jsonObject = JSONObject.parseObject(msg);
            String cityCode = "";
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String tmp = (String)entry.getValue();
                if (city.equalsIgnoreCase(tmp)){
                    cityCode = entry.getKey();
                    break;
                }
            }
            if (StringUtils.isEmpty(cityCode)) {
                return configResult("500", "couldn't find city code.");
            }
            String url = new StringBuilder().append("http://www.weather.com.cn/data/city3jdata/station/")
                    .append(provinceCode)
                    .append(cityCode).append(".html")
                    .toString();
            result = queryMsg(url);
            result.put("provinceCode",provinceCode);
            result.put("cityCode",cityCode);
            return result;
        } else {
            log.info("couldn't find city..");
            return configResult("500", "couldn't find city code.");
        }
    }

    public Map<String, String> configResult(String code, String message){
        Map<String, String> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        return result;
    }

    public Map<String, String> queryMsg(String url){
        log.info("query url : {}", url);
        CloseableHttpClient httpClient = ClientFactory.httpClientPooled(ConnectTimeout, SocketTimeout);
        HttpConfig httpConfig = HttpConfig.instance().url(url)
                .client(httpClient)
                .methods(HttpMethods.GET);
        Map<String, String> result = new HashMap<>();
        try {
            result = HttpClientUtil.httpGetMethodWithStatusCode(httpConfig);
            log.info(JSONUtil.beanToJson(result));
        } catch (Exception e){
            log.info("query msg exception : {}", e.getMessage());
            for (int i = 0; i < 3; i++){
                backOff(i * 5);
                try {
                    result = HttpClientUtil.httpGetMethodWithStatusCode(httpConfig);
                } catch (Exception ex) {
                    log.info("Error msg: {} ", e.getMessage());
                }
                log.info("query msg : {}", JSONUtil.beanToJson(result));
            }
        }
        return result;
    }


    public void backOff(int secs){
        try {
            TimeUnit.SECONDS.sleep(secs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
