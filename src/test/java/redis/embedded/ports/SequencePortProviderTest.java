package redis.embedded.ports;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SequencePortProviderTest {

    @Test
    public void nextShouldIncrementPortsByOne() throws Exception {
        final int startPort = 10;
        final SequencePortProvider provider = new SequencePortProvider(startPort);

        for (int i = startPort; i < 15; i++) {
            int port = provider.next();
            assertThat(port, equalTo(i));
        }
    }
}