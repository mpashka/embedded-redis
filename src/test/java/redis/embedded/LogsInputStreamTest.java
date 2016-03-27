package redis.embedded;

import org.junit.Before;
import org.junit.Test;


import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.nullValue;

public class LogsInputStreamTest {

    private static int cr = '\n';
    private static int space = ' ';

    private LogsInputStream logs;

    @Before
    public void before() {
        logs = new LogsInputStream();
    }

    @Test
    public void test1() {
        assertThat(logs.read(), equalTo(-1));
    }

    @Test
    public void test2() {
        logs.appendLine("");
        assertThat(logs.read(), equalTo(cr));
        assertThat(logs.read(), equalTo(-1));
    }

    @Test
    public void test3() {
        logs.appendLine("something");

        assertThat(logs.read(), equalTo((int) 's'));
        assertThat(logs.read(), equalTo((int) 'o'));
        assertThat(logs.read(), equalTo((int) 'm'));
        assertThat(logs.read(), equalTo((int) 'e'));
        assertThat(logs.read(), equalTo((int) 't'));
        assertThat(logs.read(), equalTo((int) 'h'));
        assertThat(logs.read(), equalTo((int) 'i'));
        assertThat(logs.read(), equalTo((int) 'n'));
        assertThat(logs.read(), equalTo((int) 'g'));
        assertThat(logs.read(), equalTo((int) '\n'));
        assertThat(logs.read(), equalTo(-1));
    }

    @Test
    public void test4() {
        logs.appendLine("line1");
        logs.appendLine("line2");

        assertThat(logs.read(), equalTo((int) 'l'));
        assertThat(logs.read(), equalTo((int) 'i'));
        assertThat(logs.read(), equalTo((int) 'n'));
        assertThat(logs.read(), equalTo((int) 'e'));
        assertThat(logs.read(), equalTo((int) '1'));
        assertThat(logs.read(), equalTo((int) '\n'));
        assertThat(logs.read(), equalTo((int) 'l'));
        assertThat(logs.read(), equalTo((int) 'i'));
        assertThat(logs.read(), equalTo((int) 'n'));
        assertThat(logs.read(), equalTo((int) 'e'));
        assertThat(logs.read(), equalTo((int) '2'));
        assertThat(logs.read(), equalTo((int) '\n'));
        assertThat(logs.read(), equalTo(-1));
    }

    @Test(timeout = 10)
    public void test5() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(logs));
        logs.appendLine("line1");

        assertThat(reader.readLine(), equalTo("line1"));
        assertThat(reader.readLine(), nullValue());
    }


}
