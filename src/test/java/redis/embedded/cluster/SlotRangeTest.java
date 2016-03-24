package redis.embedded.cluster;

import org.junit.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SlotRangeTest {

    @Test
    public void getRange() {
        SlotRange range = new SlotRange(1, 4);

        assertThat(range.getRange(), equalTo(new int[]{1, 2, 3, 4}));
    }

    @Test
    public void shouldHaveNiceToString() {
        SlotRange range = new SlotRange(1, 4);

        assertThat(range.toString(), equalTo("[1, 4]"));
    }
}
