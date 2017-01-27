package redis.embedded.ports;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import redis.embedded.PortProvider;
import redis.embedded.exceptions.RedisBuildingException;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SequencePortProviderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void provideTCPMaxPortNumber() {
        assertThat(SequencePortProvider.TCP_MAX_PORT_NUMBER, equalTo(65535));
    }

    @Test
    public void defaultValueShouldBeSet() {
        final SequencePortProvider provider = new SequencePortProvider();

        for (int i = 26379; i < PortProvider.TCP_MAX_PORT_NUMBER; i++) {
            assertThat(provider.next(), equalTo(i));
        }
    }

    @Test
    public void nextShouldIncrementPortsByOne() {
        final int startPort = 10;
        final SequencePortProvider provider = new SequencePortProvider(startPort);

        for (int i = startPort; i < 15; i++) {
            assertThat(provider.next(), equalTo(i));
        }
    }

    @Test
    public void shouldBeAbleToConfigurePortRange() {
        final int startPort = 10;
        final int endPort = 15;
        final SequencePortProvider provider = new SequencePortProvider(startPort, endPort);

        for (int i = startPort; i <= endPort; i++) {
            assertThat(provider.next(), equalTo(i));
        }
    }

    @Test
    public void maxPortShouldBeInclusive() {
        final int startPort = 0;
        final int endPort = 1;
        final SequencePortProvider provider = new SequencePortProvider(startPort, endPort);

        assertThat(provider.next(), equalTo(0));
        assertThat(provider.next(), equalTo(1));

        assertThat(provider.hasNext(), equalTo(false));

        exception.expect(RedisBuildingException.class);
        exception.expectMessage("Run out of Redis ports!");
        provider.next();
    }


    @Test
    public void copyShouldNotModifyOriginal() {
        final int startPort = 0;
        final int endPort = 1;
        final SequencePortProvider provider1 = new SequencePortProvider(startPort, endPort);
        final SequencePortProvider provider2 = provider1.copy();

        assertThat(provider1.next(), equalTo(0));
        assertThat(provider1.next(), equalTo(1));
        assertThat(provider1.hasNext(), equalTo(false));

        assertThat(provider2.next(), equalTo(0));
    }
}