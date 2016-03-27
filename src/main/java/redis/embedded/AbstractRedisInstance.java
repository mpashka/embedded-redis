package redis.embedded;

import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.*;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.io.IOUtils.closeQuietly;

abstract class AbstractRedisInstance implements Redis {
    List<String> args = Collections.emptyList();
    private volatile boolean active = false;
    private Process redisProcess;
    private final int port;

    private LogsInputStream logsInput = new LogsInputStream();

    AbstractRedisInstance(int port) {
        this.port = port;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public synchronized void start() throws EmbeddedRedisException {
        if (active) {
            throw new EmbeddedRedisException("This redis server instance is already running...");
        }
        try {
            redisProcess = createRedisProcessBuilder().start();

            awaitRedisServerReady();

            active = true;
        } catch (IOException | InterruptedException e) {
            throw new EmbeddedRedisException("Failed to start Redis instance", e);
        }
    }

    @Override
    public InputStream errors() {
        return logsInput;
    }

    private void awaitRedisServerReady() throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(redisProcess.getInputStream()));
        try {
            // Wait 200ms to be sure redis have started (it can takes some time and that would cause
            // the exception below to throw.
            Thread.sleep(200);

            String outputLine;
            do {
                outputLine = reader.readLine();
                if (outputLine == null) {
                    //Something goes wrong. Stream is ended before server was activated.
                    throw new RuntimeException("Can't start redis server. Check logs for details.");
                }
                logsInput.appendLine(outputLine);
            } while (!outputLine.matches(redisReadyPattern()));
        } finally {
            closeQuietly(reader);
        }
    }

    protected abstract String redisReadyPattern();

    private ProcessBuilder createRedisProcessBuilder() {
        File executable = new File(args.get(0));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(executable.getParentFile());
        return pb;
    }

    @Override
    public synchronized void stop() throws EmbeddedRedisException {
        if (active) {
            redisProcess.destroy();
            tryWaitFor();
            active = false;
        }
    }

    private void tryWaitFor() {
        try {
            redisProcess.waitFor();
        } catch (InterruptedException e) {
            throw new EmbeddedRedisException("Failed to stop redis instance", e);
        }
    }

    @Override
    public List<Integer> ports() {
        return Collections.singletonList(port);
    }

}
