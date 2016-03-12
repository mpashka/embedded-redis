package redis.embedded;

import org.junit.*;
import redis.clients.jedis.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class RedisServerSlavesTest {

    private RedisServer redisServer1;
    private RedisServer redisServer2;

    @Before
    public void setUp() throws Exception {
        redisServer1 = RedisServer.builder()
                .port(6300)
                .build();

        redisServer2 = RedisServer.builder()
                .port(6301)
                .slaveOf("localhost", 6300)
                .build();

        redisServer1.start();
        redisServer2.start();
    }

    @Test
    public void testSimpleOperationsAfterRun() throws Exception {
        try (JedisPool pool = new JedisPool("localhost", 6300);
             Jedis jedis = pool.getResource()) {

            jedis.mset("abc", "1", "def", "2");

            assertThat(jedis.mget("abc"), contains("1"));
            assertThat(jedis.mget("def"), contains("2"));
            assertThat(jedis.mget("xyz"), contains(nullValue()));
        }
    }


    @After
    public void tearDown() throws Exception {
        redisServer1.stop();
        redisServer2.stop();
    }
}
