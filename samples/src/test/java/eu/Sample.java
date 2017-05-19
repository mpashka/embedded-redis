package eu;

import org.junit.Test;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;

import java.util.concurrent.TimeUnit;

public class Sample {
    @Test(timeout = 5000L)
    public void testSimpleRunWithExecProvider() throws Exception {

        RedisServer.builder().build();
        RedisServer redisServer = new RedisServer(RedisExecProvider.build(), 32498);
        redisServer.start();

        TimeUnit.SECONDS.sleep(1);

        redisServer.stop();
    }
}