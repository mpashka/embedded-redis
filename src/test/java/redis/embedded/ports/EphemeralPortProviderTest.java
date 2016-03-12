package redis.embedded.ports;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class EphemeralPortProviderTest {

    @Test
    public void nextShouldGiveNextFreeEphemeralPort() throws Exception {
        // Given
        final int portCount = 20;
        final EphemeralPortProvider provider = new EphemeralPortProvider();

        // When
        final List<Integer> ports = new ArrayList<>();
        for (int i = 0; i < portCount; i++) {
            ports.add(provider.next());
        }

        // Then
        assertEquals(20, ports.size());
        assertThat(ports, everyItem(greaterThan(1024)));
    }
}