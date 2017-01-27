package redis.embedded.ports;

import redis.embedded.PortProvider;
import redis.embedded.exceptions.RedisBuildingException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link PortProvider} returning a sequential port on each call to {@code int next()} between a given
 * {@code startPort} (which default at {@code 26379}) and {@code maxPort} (inclusive, default at {@code 65535})
 */
public class SequencePortProvider implements PortProvider {
    private AtomicInteger currentPort = new AtomicInteger(26379);
    private final int startPort;
    private final int maxPort;

    public SequencePortProvider() {
        this(26379, TCP_MAX_PORT_NUMBER);
    }

    public SequencePortProvider(int startPort) {
        this(startPort, TCP_MAX_PORT_NUMBER);
    }

    public SequencePortProvider(int startPort, int maxPort) {
        this.currentPort = new AtomicInteger(startPort);
        this.startPort = startPort;
        this.maxPort = maxPort;
    }

    @Override
    public int next() {
        int port = currentPort.getAndIncrement();

        if (port > maxPort) {
            throw new RedisBuildingException("Run out of Redis ports!");
        }

        return port;
    }

    @Override
    public boolean hasNext() {
        return currentPort.get() <= maxPort;
    }

    @Override
    public SequencePortProvider copy() {
        return new SequencePortProvider(startPort, maxPort);
    }
}
