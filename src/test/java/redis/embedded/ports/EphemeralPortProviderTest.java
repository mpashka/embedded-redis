package redis.embedded.ports;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EphemeralPortProviderTest {

    @Test
    public void nextShouldGiveNextFreeEphemeralPort() {
        // Given
        final int portCount = 20;
        final EphemeralPortProvider provider = new EphemeralPortProvider();

        // When
        final List<Integer> ports = new ArrayList<>();
        for (int i = 0; i < portCount; i++) {
            ports.add(provider.next());
        }

        // Then
        assertThat(ports.size(), equalTo(20));
        assertThat(ports, everyItem(greaterThan(1024)));
    }

    @Test
    public void hasNextAlwaysReturnTrue() {
        final EphemeralPortProvider provider = new EphemeralPortProvider();

        assertThat(provider.hasNext(), equalTo(true));

        for (int i = 0; i < 120; i++) {
            provider.next();
        }

        assertThat(provider.hasNext(), equalTo(true));
    }
}