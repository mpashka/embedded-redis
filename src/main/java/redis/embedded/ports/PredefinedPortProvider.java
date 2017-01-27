package redis.embedded.ports;

import redis.embedded.PortProvider;
import redis.embedded.exceptions.RedisBuildingException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link PortProvider} returning a collection of port that is provided via a {@link Collection}.
 */
public class PredefinedPortProvider implements PortProvider {
    private final List<Integer> givenPorts;
    private final Iterator<Integer> current;

    public PredefinedPortProvider(Collection<Integer> ports) {
        this.givenPorts = new LinkedList<>();
        this.givenPorts.addAll(ports);
        this.current = this.givenPorts.iterator();
    }

    @Override
    public synchronized int next() {
        if (!current.hasNext()) {
            throw new RedisBuildingException("Run out of Redis ports!");
        }
        return current.next();
    }

    @Override
    public boolean hasNext() {
        return current.hasNext();
    }

    @Override
    public PredefinedPortProvider copy() {
        return new PredefinedPortProvider(givenPorts);
    }
}
