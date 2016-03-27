package redis.embedded;

import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

// TODO Define used Charset
class LogsInputStream extends InputStream {

    private Queue<String> strings = new LinkedBlockingQueue<>();

    private byte[] currentString = null;

    private int pos = 0;

    @Override
    public int read() {
        if (currentString == null) {
            if (strings.size() > 0) {
                currentString = strings.poll().getBytes();
                pos = 0;
                return read();
            }
        } else {
            int c;
            if (pos < currentString.length) {
                c = currentString[pos];
            } else if (pos == currentString.length) {
                c = '\n';
            } else if (pos > currentString.length && strings.size() > 0) {
                currentString = strings.poll().getBytes();
                pos = 0;
                return read();
            } else {
                c = -1;
            }

            pos++;
            return c;
        }


        return -1;
    }

    void appendLine(String line) {
        strings.add(line);
    }
}
