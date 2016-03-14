package redis.embedded;

import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.*;
import java.util.List;

public interface Redis {

    /**
     * @return whether this Redis implementation is active or not
     */
    boolean isActive();

    /**
     * Start the Redis implementation.
     *
     * @throws EmbeddedRedisException
     */
    void start() throws EmbeddedRedisException;

    /**
     * Stop the Redis implementation.
     *
     * @throws EmbeddedRedisException
     */
    void stop() throws EmbeddedRedisException;

    /**
     * @return a list of ports used by this Redis implementation
     */
    List<Integer> ports();

    /**
     * @param outputStream a stream which will receive the error output of the underlying Redis implementation
     */
    void errors(OutputStream outputStream);
}
