package redis.embedded.util;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import redis.embedded.exceptions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;

public class JarUtil {

    public static File extractExecutableFromJar(String executable) throws IOException {
        File tmpDir = Files.createTempDir();
        tmpDir.deleteOnExit();

        File copiedExecutable = new File(tmpDir, executable);
        URL resource = getResource(executable);

        FileUtils.copyURLToFile(resource, copiedExecutable);
        copiedExecutable.deleteOnExit();

        if (!copiedExecutable.setExecutable(true)) {
            //should not ever happen
            throw new RedisBuildingException("Cannot make file " + copiedExecutable.getName() + " executable.");
        }

        return copiedExecutable;
    }

    /**
     * Imported from guava to introduce a default {@link ClassLoader}.
     * <p>
     * Returns a {@code URL} pointing to {@code resourceName} if the resource is
     * found using the {@linkplain Thread#getContextClassLoader() context class
     * loader}. In simple environments, the context class loader will find
     * resources from the class path. In environments where different threads can
     * have different class loaders, for example app servers, the context class
     * loader will typically have been set to an appropriate loader for the
     * current thread.
     * <p>
     * <p>In the unusual case where the context class loader is null, the class
     * loader that loaded this class ({@code Resources}) will be used instead.
     *
     * @throws IllegalArgumentException if the resource is not found
     * @see Resources#getResource(String)
     */
    private static URL getResource(String resourceName) {
        URL url = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // Try in the current context class loader
        if (loader != null) {
            url = loader.getResource(resourceName);
        }

        // If nothing, try in this class ClassLoader
        if (url == null) {
            loader = JarUtil.class.getClassLoader();
            url = loader.getResource(resourceName);
        }

        // Not found ?
        checkArgument(url != null, "resource %s not found.", resourceName);

        return url;
    }
}
