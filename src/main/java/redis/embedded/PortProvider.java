package redis.embedded;

/**
 * This interface let you provide dynamic port range for your redis implementations.
 * The package {@code redis.embedded.ports} provides some useful default implementation.
 *
 * A subset of {@link java.util.Iterator} that simply implement the {@code int next()} and {@code boolean hasNext()}
 * methods.
 */
public interface PortProvider {
    int TCP_MAX_PORT_NUMBER = 65535;

    int next();

    boolean hasNext();
}
