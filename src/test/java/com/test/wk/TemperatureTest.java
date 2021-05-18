package com.test.wk;


import com.test.wk.service.TemperatureService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

// function test
@Slf4j
public class TemperatureTest {

    private TemperatureService service;

    @Before
    public void init() {
        service = new TemperatureService();
    }

    @Test
    public void testRetry(){
        Map<String, String> result = service.queryMsg("http://127.9.0.1/data/sk/10119040111111.html");
        Assert.assertEquals(null,result.get("code"));
    }

    @Test
    public void funcProvince(){
        Map<String, String> province = service.getProvince();
        Assert.assertEquals("200",province.get("code"));
    }

    @Test
    public void funcCity(){
        Map<String, String> res = service.getCity("江苏");
        Assert.assertEquals("200", res.get("code"));
    }


    @Test
    public void funcCounty(){
        Map<String, String> res = service.getCounty("江苏", "南京");
        Assert.assertEquals("200", res.get("code"));
    }

    @Test
    public void funcTemperature(){
        try {
            Optional<Integer> temperature = service.getTemperature("江苏", "南京", "南京");
            if (temperature.isPresent()){
                log.info("get temp : {}", temperature.get());
            }
        } catch (Exception e) {
            log.info("exception: {} ", e.getMessage());
        }
    }

}
