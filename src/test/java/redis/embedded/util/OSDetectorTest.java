package redis.embedded.util;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import redis.embedded.exceptions.OsDetectionException;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OSDetector.class})
public class OSDetectorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void before() {
        mockStatic(System.class);
    }

    @Test
    public void getOSShouldReturnWindows() {
        when(System.getProperty("os.name")).thenReturn("windows x");

        assertThat(OSDetector.getOS(), equalTo(OS.WINDOWS));
    }

    @Test
    public void getOSShouldReturnOSX() {
        when(System.getProperty("os.name")).thenReturn("mac os x");

        assertThat(OSDetector.getOS(), equalTo(OS.MAC_OS_X));
    }

    @Test
    public void getOSShouldReturnUnix() {
        System.setProperty("os.name", "Linux");
        when(System.getProperty("os.name")).thenReturn("Linux");
        assertThat(OSDetector.getOS(), equalTo(OS.UNIX));

        when(System.getProperty("os.name")).thenReturn("AIX");
        assertThat(OSDetector.getOS(), equalTo(OS.UNIX));

        when(System.getProperty("os.name")).thenReturn("Digital Unix");
        assertThat(OSDetector.getOS(), equalTo(OS.UNIX));
    }

    @Test
    public void getArchitectureShouldThrowIfUnrecognizedOS() {
        when(System.getProperty("os.name")).thenReturn("FreeBSD");

        exception.expect(OsDetectionException.class);
        exception.expectMessage("Unrecognized OS: freebsd");
        OSDetector.getArchitecture();
    }

    @Test
    public void getArchitectureShouldWorkForWindows() {
        when(System.getProperty("os.name")).thenReturn("windows x");

        when(System.getenv("PROCESSOR_ARCHITECTURE")).thenReturn("x86");
        assertThat(OSDetector.getArchitecture(), equalTo(Architecture.x86));

        when(System.getenv("PROCESSOR_ARCHITECTURE")).thenReturn("amd64");
        assertThat(OSDetector.getArchitecture(), equalTo(Architecture.x86_64));

        when(System.getenv("PROCESSOR_ARCHITECTURE")).thenReturn("x86");
        when(System.getenv("PROCESSOR_ARCHITEW6432")).thenReturn("amd64");
        assertThat(OSDetector.getArchitecture(), equalTo(Architecture.x86_64));
    }

    @Test
    public void getArchitectureShouldWorkForUnix86() throws IOException {
        when(System.getProperty("os.name")).thenReturn("unix");

        mockExec("uname -m", "x86");

        assertThat(OSDetector.getArchitecture(), equalTo(Architecture.x86));
    }

    @Test
    public void getArchitectureShouldWorkForUnix64() {
        when(System.getProperty("os.name")).thenReturn("unix");

        mockExec("uname -m", "x86_64");

        assertThat(OSDetector.getArchitecture(), equalTo(Architecture.x86_64));
    }

    @Test
    public void getArchitectureShouldThrowForUnix() {
        when(System.getProperty("os.name")).thenReturn("unix");

        mockExec("sysctl hw", null);

        exception.expect(OsDetectionException.class);
        exception.expectMessage("java.lang.NullPointerException");

        OSDetector.getArchitecture();
    }

    @Test
    public void getArchitectureShouldWorkForOSX86() throws IOException {
        when(System.getProperty("os.name")).thenReturn("mac os x");

        // cpu64bit_capable should not appear
        mockExec("sysctl hw", "cpu64bit_capable: 0");

        assertThat(OSDetector.getArchitecture(), equalTo(Architecture.x86));
    }

    @Test
    public void getArchitectureShouldWorkForOSX64() {
        when(System.getProperty("os.name")).thenReturn("mac os x");

        mockExec("sysctl hw", "cpu64bit_capable: 1");

        assertThat(OSDetector.getArchitecture(), equalTo(Architecture.x86_64));
    }

    @Test
    public void getArchitectureShouldThrowForOSX() {
        when(System.getProperty("os.name")).thenReturn("mac os x");

        mockExec("sysctl hw", null);

        exception.expect(OsDetectionException.class);
        exception.expectMessage("java.lang.NullPointerException");

        OSDetector.getArchitecture();
    }

    private static void mockExec(String command, String returned) {
        Runtime mockedRuntime = mock(Runtime.class);
        Process mockedProcess = mock(Process.class);

        try {
            when(mockedRuntime.exec(command)).thenReturn(mockedProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (returned != null) {
            when(mockedProcess.getInputStream()).thenReturn(IOUtils.toInputStream(returned));
        }

        Whitebox.setInternalState(OSDetector.class, mockedRuntime);
    }
}
