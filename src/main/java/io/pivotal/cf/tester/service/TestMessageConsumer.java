package io.pivotal.cf.tester.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.codahale.metrics.annotation.Timed;

public class TestMessageConsumer implements MessageListener {
	private static Logger log = LoggerFactory.getLogger(TestMessageConsumer.class);

	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;
	
	@Value("${vcap.application.instance_id:cf-tester}")
	private String instanceId;
	
	@Value("${vcap.application.instance_index:0}")
	private int instanceIndex;

	@Autowired(required=false)
	private RedisTemplate< String, Object > redisTemplate;
	
	@Autowired
	private StateService stateService;
	
	@Timed
	@Override
	public void onMessage(Message message) {
		long messageId = Long.parseLong( message.getMessageProperties().getMessageId() );
		log.debug("{} [{}] ({}) RCV id:{} {}", 
				applicationName, instanceId, instanceIndex,
				messageId,
				Util.DTF.print(message.getMessageProperties().getTimestamp().getTime()));
		
		if(redisTemplate == null) {
			log.debug("Redis Service unavailable");
			stateService.setRedisDown();
			return;
		}
		
		try {
			if( setBit(applicationName, messageId, true) ) {
				log.warn("{} [{}] ({}) RCV DUP id:{} {}", 
						applicationName, instanceId, instanceIndex,
						messageId,
						Util.DTF.print(message.getMessageProperties().getTimestamp().getTime()));
			}
			stateService.setRedisUp();
		}
		catch(Exception ex) {
			log.debug("Checkpoint in Redis failed", ex);
			
			stateService.setRedisDown();
		}
	}
	
	private Boolean setBit( final String key, final long offset, final boolean value ) {
		return redisTemplate.execute(
			new RedisCallback< Boolean >() {
				@SuppressWarnings("unchecked")
				@Override
				public Boolean doInRedis( RedisConnection connection ) throws DataAccessException {
					return connection.setBit( ( ( RedisSerializer< String > )redisTemplate.getKeySerializer() ).serialize( key ), offset, value );
				}
			}
		);
	}

}
