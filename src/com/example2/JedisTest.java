package com.example2;


import com.example2.util.JedisPoolUtil;
import com.example2.util.JedisShardInfoUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class JedisTest {
    
    public static void main(String[] args) {
     //   jedisPoolTest();
        sharedJedisPoolTest();
    }
    
    /**
     * 池化使用JedisPool
     */
    private static void jedisPoolTest(){
        System.out.println("invoke jedisPoolTest method...............");
        Jedis jedis = JedisPoolUtil.getJedis();
        
        String keys = "name_jedisPool_2_newJedis";
        System.out.println("key = " + keys);
        
        System.out.println("del key result : " + jedis.del(keys));// 删数据
        System.out.println("set key result : " + jedis.set(keys, "jedisPool_2_newJedis"));// 存数据
        String value = "";
        try {
            value = jedis.get(keys);// 取数据
        } catch (Exception e) {
            /*
             * 释放redis对象
             * instance出错时，必须调用returnBrokenResource返还给pool，
             * 否则下次通过getResource得到的instance的缓冲区可能还存在数据，出现问题  
             */
            JedisPoolUtil.getPool().returnBrokenResource(jedis); 
        }
        
        System.out.println("set key result : " + value);
        System.out.println();
 //       jedis.flushDB(); //Delete all the keys of the currently selected DB
        JedisPoolUtil.closeJedis(jedis);
    }
    
    
    private static void sharedJedisPoolTest(){
        System.out.println("invoke sharedJedisPoolTest method...............");
        ShardedJedis jedis = JedisShardInfoUtil.getJedis();
        
        String keys = "name_sharedJedisPool_1_newJedis";
        System.out.println("key = " + keys);
        
        
        System.out.println("del key result : " + jedis.del(keys));// 删数据
        System.out.println("set key result : " + jedis.set(keys, "sharedJedisPool_1_newJedis"));// 存数据
        String value = "";
        try {
            value = jedis.get(keys);// 取数据
        } catch (Exception e) {
            /*
             * 释放redis对象
             * instance出错时，必须调用returnBrokenResource返还给pool，
             * 否则下次通过getResource得到的instance的缓冲区可能还存在数据，出现问题  
             */
            JedisShardInfoUtil.getPool().returnBrokenResource(jedis); 
        }
        
        System.out.println("set key result : " + value);
        System.out.println();
        JedisShardInfoUtil.closeJedis(jedis);
    }
}
