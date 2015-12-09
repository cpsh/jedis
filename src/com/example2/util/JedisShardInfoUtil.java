package com.example2.util;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class JedisShardInfoUtil {
    private static ShardedJedisPool pool;
    private static ResourceBundle bundle;

    static {
        bundle = ResourceBundle.getBundle("redis");
        if (bundle == null) {
            throw new IllegalArgumentException(
                    "[redis.properties] is not found!");
        }

    }

    /**
     * 构建redis连接池
     * 
     * @param ip
     * @param port
     * @return JedisPool
     * 
     *         * maxActive：控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；如果赋值为-1
     *         ，则表示不限制； 如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted。
     * 
     *         maxIdle：控制一个pool最多有多少个状态为idle(空闲)的jedis实例；
     * 
     *         whenExhaustedAction：表示当pool中的jedis实例都被allocated完时，pool要采取的操作；
     *         默认有三种。 WHEN_EXHAUSTED_FAIL -->
     *         表示无jedis实例时，直接抛出NoSuchElementException； WHEN_EXHAUSTED_BLOCK -->
     *         则表示阻塞住，或者达到maxWait时抛出JedisConnectionException；
     *         WHEN_EXHAUSTED_GROW --> 则表示新建一个jedis实例，也就说设置的maxActive无用；
     * 
     *         maxWait：表示当borrow一个jedis实例时，最大的等待时间，如果超过等待时间，
     *         则直接抛出JedisConnectionException；
     * 
     *         testOnBorrow：在borrow一个jedis实例时，是否提前进行alidate操作；如果为true，
     *         则得到的jedis实例均是可用的；
     * 
     *         testOnReturn：在return给pool时，是否提前进行validate操作；
     * 
     *         testWhileIdle：如果为true，表示有一个idle object evitor线程对idle
     *         object进行扫描，如果validate失败
     *         ，此object会被从pool中drop掉；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
     *         ；
     * 
     *         timeBetweenEvictionRunsMillis：表示idle object
     *         evitor两次扫描之间要sleep的毫秒数；
     * 
     *         numTestsPerEvictionRun：表示idle object evitor每次扫描的最多的对象数；
     * 
     *         minEvictableIdleTimeMillis：表示一个对象至少停留在idle状态的最短时间，然后才能被idle
     *         object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；
     * 
     *         softMinEvictableIdleTimeMillis：在minEvictableIdleTimeMillis基础上，
     *         加入了至少minIdle个对象已经在pool里面了。如果为-1，evicted不会根据idle
     *         time驱逐任何对象。如果minEvictableIdleTimeMillis
     *         >0，则此项设置无意义，且只有在timeBetweenEvictionRunsMillis大于0时才有意义；
     * 
     *         lifo：borrowObject返回对象时，是采用DEFAULT_LIFO（last in first
     *         out，即类似cache的最频繁使用队列），如果为False，则表示FIFO队列；
     * 
     *         其中JedisPoolConfig对一些参数的默认设置如下： testWhileIdle=true
     *         minEvictableIdleTimeMills=60000
     *         timeBetweenEvictionRunsMillis=30000 numTestsPerEvictionRun=-1
     * 
     * 
     */
    public static ShardedJedisPool getPool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            // 最大分配的对象数
            // config.setMaxActive(Integer.valueOf(bundle.getString("redis.pool.maxActive")));//common-pool-1.x版本

            config.setMaxTotal(Integer.valueOf(bundle
                    .getString("redis.pool.maxActive")));// common-pool2-2.x版本

            // 最大能够保持idel状态的对象数 最大空闲连接数
            config.setMaxIdle(Integer.valueOf(bundle
                    .getString("redis.pool.maxIdle")));

            /*
             * 当池内没有返回对象时，最大等待时间, 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),
             * 如果超时就抛异常, 小于零:阻塞不确定的时间, 默认-1
             */
            // config.setMaxWait(Long.valueOf(bundle.getString("redis.pool.maxWait")));
            config.setMaxWaitMillis(Long.valueOf(bundle
                    .getString("redis.pool.maxWait")));

            // 当调用borrow Object方法获取连接的时候是否进行有效性检查 , 默认false
            config.setTestOnBorrow(Boolean.valueOf(bundle
                    .getString("redis.pool.testOnBorrow")));

            // 当调用return Object方法时，是否进行有效性检查
            config.setTestOnReturn(Boolean.valueOf(bundle
                    .getString("redis.pool.testOnReturn")));

            JedisShardInfo jedisShardInfo1 = new JedisShardInfo(
                    bundle.getString("redis.ip"), Integer.valueOf(bundle
                            .getString("redis.port")));

            /*
             * JedisShardInfo jedisShardInfo2 = new JedisShardInfo(
             * bundle.getString("redis2.ip"), Integer.valueOf(bundle
             * .getString("redis.port2")));
             */

            List<JedisShardInfo> list = new LinkedList<JedisShardInfo>();
            list.add(jedisShardInfo1);
            // list.add(jedisShardInfo2);

            pool = new ShardedJedisPool(config, list);
        }
        return pool;
    }

    /**
     * 获取Jedis实例
     * 
     * @return
     */
    public synchronized static ShardedJedis getJedis() {
        try {
            ShardedJedis jedis = getPool().getResource();
            return jedis;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 释放Jedis实例回连接池
     * 
     * @param redis
     */
    public static void closeJedis(ShardedJedis redis) {
        if (redis != null) {
            getPool().returnResource(redis);
        }
    }

}
