package com.springExample;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

public class UserDaoImpl implements UserDao {

   @Autowired
    private RedisTemplate<Serializable, Serializable> redisTemplate;
   
   @Autowired
   private person person;
   
    /**
     * 传入参数，需要final标识，禁止方法内修改。 调用RedisConnection的set方法实现Redis的SET命令。
     * 不管是Key，还是Value都需要进行Serialize。 序列化操作，最好使用RedisTemplate提供的Serializer来完成。
     */
    @Override
    public void save(final User user) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection)
                    throws DataAccessException  {
                connection.set(
                        redisTemplate.getStringSerializer().serialize(
                                "user.uid." + user.getUid()),
                        redisTemplate.getStringSerializer().serialize(
                                user.getAddress()));
                return null;
            }
        });
    }

    /**
     * 记得使用泛型，如RedisCallback<User>() 使用同一的序列化/反序列化Serializer
     * 建议使用connection.exists(key)判别键值是否存在，避免无用功
     */
    @Override
    public User read(final String uid) {
        return redisTemplate.execute(new RedisCallback<User>() {
            @Override
            public User doInRedis(RedisConnection connection)
                    throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize(
                        "user.uid." + uid);
                if (connection.exists(key)) {
                    byte[] value = connection.get(key);
                    String address = redisTemplate.getStringSerializer()
                            .deserialize(value);
                    User user = new User();
                    user.setAddress(address);
                    user.setUid(uid);
                    return user;
                }
                return null;
            }
        });
    }

    @Override
    public void delete(final String uid) {
        redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) {
                connection.del(redisTemplate.getStringSerializer().serialize(
                        "user.uid." + uid));
                return null;
            }
        });
    }

}
