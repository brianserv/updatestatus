package cn.imqiba.main;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cn.imqiba.config.RedisConfig;
import cn.imqiba.util.MongoBank;

public class UpdateStatus
{

	static
	{
		PropertyConfigurator.configure(System.getProperty("user.dir") + File.separator + "log4j.properties");
	}
	
	private static Logger logger = Logger.getLogger(Class.class);
	
	public static boolean initConfig()
	{
		boolean result = false;
		try
		{
			result = RedisConfig.getInstance().parser(System.getProperty("user.dir") + File.separator + "redis_config.xml");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return result;
		}
		
		return result;
	}
	
	public static void main(String[] args)
	{
		if(!initConfig())
		{
			return;
		}
		
		MongoBank.getInstance().addMongoInstance("10.171.97.110", 54178);
//		MongoBank.getInstance().addMongoInstance("127.0.0.1", 27017);
		
		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		threadPool.execute(new UpdateWorker());
		
        return;
	}

}
