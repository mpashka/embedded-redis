package redis.embedded.ports;

import redis.embedded.PortProvider;
import redis.embedded.exceptions.RedisBuildingException;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * A {@link PortProvider} returning a random available port on each call to {@code int next()}.
 */
public class EphemeralPortProvider implements PortProvider {

    @Override
    public int next() {
        try {
            final ServerSocket socket = new ServerSocket(0);
            socket.setReuseAddress(false);
            int port = socket.getLocalPort();
            socket.close();
            return port;
        } catch (IOException e) {
            //should not ever happen
            throw new RedisBuildingException("Could not provide ephemeral port", e);
        }
    }

    @Override
    public boolean hasNext() {
        // The only case when this should return false is if there is no local port available on your machine
        return true;
    }

    @Override
    public EphemeralPortProvider copy() {
        return new EphemeralPortProvider();
    }
}
