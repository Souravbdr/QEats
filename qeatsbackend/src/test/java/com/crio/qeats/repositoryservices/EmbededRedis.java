package com.crio.qeats.repositoryservices;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import com.crio.qeats.configs.RedisConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

//@Component
public class EmbededRedis {

  @Value("${spring.redis.port}")
  private int redisPort;

  @Autowired
  private RedisConfiguration redisConfiguration;

  private RedisServer redisServer;

  @PostConstruct
  public void startRedis() throws IOException {
    redisConfiguration.setRedisPort(redisPort);
    redisServer = new RedisServer(redisPort);
    redisServer.start();
  }

  @PreDestroy
  public void stopRedis() {
    redisServer.stop();
  }
}
