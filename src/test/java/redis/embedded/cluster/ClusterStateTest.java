package redis.embedded.cluster;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ClusterStateTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void getStateOk() {
        assertThat(ClusterState.getStateByStr("ok"), equalTo(ClusterState.OK));
    }

    @Test
    public void getStateFail() {
        assertThat(ClusterState.getStateByStr("fail"), equalTo(ClusterState.FAIL));
    }

    @Test
    public void getStateThrow() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("illegal cluster state: unknown");

        ClusterState.getStateByStr("unknown");
    }
}
