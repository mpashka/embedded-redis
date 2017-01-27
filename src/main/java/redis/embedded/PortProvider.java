package redis.embedded;

/**
 * This interface let you provide dynamic port range for your redis implementations.
 * The package {@code redis.embedded.ports} provides some useful default implementation.
 * <p>
 * A subset of {@link java.util.Iterator} that simply implement the {@code int next()} and {@code boolean hasNext()}
 * methods.
 */
public interface PortProvider {
    int TCP_MAX_PORT_NUMBER = 65535;

    int next();

    boolean hasNext();

    /**
     * This will copy this {@link PortProvider} in a new one with the same parameters
     * BUT it does NOT conserve the iterator status.
     * <p>
     * For example, a PortProvider backed by a [0, 1, 2] list will have the following state:
     * <p>
     * <pre>
     * {@code
     * void test(PortProvider provider) {
     *     assert(provider.next() == 0);
     *     assert(provider.next() == 1);
     *     final PortProvider copy = provider.copy();
     *     assert(copy.next() == 0);
     * }
     * }
     * </pre>
     *
     * @return a new copy of this {@link PortProvider}
     */
    PortProvider copy();
}
