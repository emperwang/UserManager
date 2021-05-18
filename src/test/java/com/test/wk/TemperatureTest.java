package com.test.wk;


import com.test.wk.service.TemperatureService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

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
        service.queryMsg("http://127.9.0.1/data/sk/10119040111111.html");
    }

    @Test
    public void funcProvince(){
        service.getProvince();
    }

    @Test
    public void funcCity(){
        service.getCity("江苏");
    }


    @Test
    public void funcCounty(){
        service.getCounty("江苏","南京");
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
