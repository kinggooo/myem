package com.wangnz.myem.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/4/21.
 */
@Controller
public class TestController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "redisTemplate")
    private RedisTemplate template;

    @RequestMapping(value = "/test1")
    @ResponseBody
    public String test1() {
        log.info("info");
        log.debug("debug");

//        template.execute(new RedisCallback<Boolean>() {
//            @Override
//            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
//                byte [] key = "tempkey".getBytes();
//                byte[] value = "tempvalue".getBytes();
//                connection.set(key, value);
//                return true;
//            }
//        });
        HashMap map1 = new HashMap();
        map1.put("aa", "上海");
        HashMap map2 = new HashMap();
        map2.put("bb", "北京");
        ArrayList ls = new ArrayList();
        ls.add(map1);
        ls.add(map2);
        ValueOperations val = template.opsForValue();
        val.set("city", "上海",30,TimeUnit.SECONDS);
        //ArrayList ls1 = (ArrayList) val.get("city");
        log.info((String) val.get("city"));
//        log.info((String) val.get("nokey"));
        return "ok";
    }

    @RequestMapping(value = "/test2")
    @ResponseBody
    public String test2() {
        log.info("info");
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(10);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        JedisPool jedisPool = new JedisPool(config, "192.168.182.129", 6379, 3000);
        Jedis jedis = jedisPool.getResource();
        jedis.set("tempkey", "tempvalue");
        return "ok";
    }
}
