package redis.embedded.ports;

import org.junit.*;
import org.junit.rules.*;
import redis.embedded.exceptions.RedisBuildingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PredefinedPortProviderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void nextShouldGiveNextPortFromAssignedList() {
        //given
        Collection<Integer> ports = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final PredefinedPortProvider provider = new PredefinedPortProvider(ports);

        //when
        final List<Integer> returnedPorts = new ArrayList<>();
        for (int i = 0; i < ports.size(); i++) {
            returnedPorts.add(provider.next());
        }

        //then
        assertThat(returnedPorts, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void nextShouldThrowExceptionWhenRunOutsOfPorts() {
        //given
        Collection<Integer> ports = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final PredefinedPortProvider provider = new PredefinedPortProvider(ports);

        //when
        for (int i = 0; i < ports.size(); i++) {
            provider.next();
        }

        exception.expect(RedisBuildingException.class);
        exception.expectMessage("Run out of Redis ports!");
        provider.next();
    }

    @Test
    public void hasNextShouldTellIfPortRemain() {
        Collection<Integer> ports = Arrays.asList(1, 2);
        final PredefinedPortProvider provider = new PredefinedPortProvider(ports);

        assertThat(provider.hasNext(), equalTo(true));
        assertThat(provider.next(), equalTo(1));

        assertThat(provider.hasNext(), equalTo(true));
        assertThat(provider.next(), equalTo(2));

        assertThat(provider.hasNext(), equalTo(false));
    }

    @Test
    public void copyShouldNotModifyOriginal() {
        Collection<Integer> ports = Arrays.asList(1, 2);
        final PredefinedPortProvider provider1 = new PredefinedPortProvider(ports);
        final PredefinedPortProvider provider2 = provider1.copy();

        assertThat(provider1.next(), equalTo(1));
        assertThat(provider1.next(), equalTo(2));
        assertThat(provider1.hasNext(), equalTo(false));

        assertThat(provider2.next(), equalTo(1));
    }
}