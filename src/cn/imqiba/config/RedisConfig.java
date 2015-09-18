package cn.imqiba.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConfig
{
	public class RedisInfo
	{
		public String m_strServerAddress = null;
		public int m_nServerPort = 0;
		public String m_strChannelKey = null;
		
		public RedisInfo(String serverAddress, int serverPort, String channelKey)
		{
			m_strServerAddress = serverAddress;
			m_nServerPort = serverPort;
			m_strChannelKey = channelKey;
		}
	}
	
	private Logger logger = Logger.getLogger(Class.class);
	private List<RedisInfo> m_stRedisInfoList = new ArrayList<RedisInfo>();
	private List<JedisPool> m_stRedisPoolList = new ArrayList<JedisPool>();
	private static RedisConfig m_stInstance = new RedisConfig();
	
	public static RedisConfig getInstance()
	{
		return m_stInstance;
	}
	
	public boolean parser(String path) throws Exception
	{
		SAXReader reader = new SAXReader();
		InputStream stream = null;
		try
		{
			stream = new FileInputStream(path);
		} 
		catch (FileNotFoundException e)
		{
			logger.error( e.getClass().getName()+" : "+ e.getMessage() );
			return false;
		}
		
		try
		{
			Document doc = reader.read(stream);
			//获取根结点
			Element server = doc.getRootElement();
			
			List<?> paramList = server.elements("node");
			for(Object obj : paramList)
			{
				Element param = (Element)obj;
				int serverID = Integer.parseInt(param.attributeValue("server_id"));
				String serverAddress = param.attributeValue("server_address");
				int serverPort = Integer.parseInt(param.attributeValue("server_port"));
				String channelKey = param.attributeValue("channel_key");
				
				m_stRedisInfoList.add(new RedisInfo(serverAddress, serverPort, channelKey));
				
				JedisPoolConfig config = new JedisPoolConfig();
				//控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
				//如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
				//config.setMaxActive(500);
				config.setMaxTotal(500);
				//控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
				config.setMaxIdle(5);
				//表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
				config.setMaxWaitMillis(1000 * 100);
				//在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
				config.setTestOnBorrow(true);
				m_stRedisPoolList.add(new JedisPool(config, serverAddress, serverPort));
			}
		}
		catch(DocumentException e)
		{
			logger.error( e.getClass().getName()+" : "+ e.getMessage() );
			return false;
		}
		
		return true;
	}
	
	public List<JedisPool> GetRedisPoolList()
	{
		return m_stRedisPoolList;
	}
}
