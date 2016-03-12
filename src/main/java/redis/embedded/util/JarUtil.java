package redis.embedded.util;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import redis.embedded.exceptions.*;

import java.io.File;
import java.io.IOException;

public class JarUtil {

    public static File extractExecutableFromJar(String executable) throws IOException {
        File tmpDir = Files.createTempDir();
        tmpDir.deleteOnExit();

        File command = new File(tmpDir, executable);
        FileUtils.copyURLToFile(Resources.getResource(executable), command);
        command.deleteOnExit();
        if (!command.setExecutable(true)) {
            //should not ever happen
            throw new RedisBuildingException("Cannot make file " + command.getName() + " executable.");
        }

        return command;
    }
}
