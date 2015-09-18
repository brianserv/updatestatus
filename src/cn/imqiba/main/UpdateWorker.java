package cn.imqiba.main;

import java.util.List;

import org.apache.log4j.Logger;
import org.bson.NewBSONDecoder;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import cn.imqiba.config.RedisConfig;
import cn.imqiba.util.MongoBank;

public class UpdateWorker implements Runnable
{
	private Logger logger = Logger.getLogger(Class.class);
	@Override
	public void run()
	{
		while(true)
		{
			List<JedisPool> redisPoolList = RedisConfig.getInstance().GetRedisPoolList();
			for(JedisPool redisPool : redisPoolList)
			{
				Jedis redis = null;
		        try
		        {
					redis = redisPool.getResource();
					List<String> data = redis.blpop(1, "update:status");
					if(data != null)
					{
						String uin = data.get(1);
						
						Mongo mongo = MongoBank.getInstance().getMongoInstance();
						DB userDB = mongo.getDB("user");
						DBCollection userCoordCollection = userDB.getCollection("user_coord");
						userCoordCollection.update(new BasicDBObject("uin", Integer.parseInt(uin)), new BasicDBObject("$set",
								new BasicDBObject("update_time", System.currentTimeMillis() / 1000)), false, false);
					}
				}
		        catch (Exception e)
		        {
		        	e.printStackTrace();
		        	logger.error( e.getClass().getName()+" : "+ e.getMessage() );
		        }
		        finally
		        {
		        	if(redis != null)
		        	{
		        		redisPool.returnResourceObject(redis);
		        	}
		        }
			}
		}
	}
}
