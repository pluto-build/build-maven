package build.pluto.maven;

import build.pluto.maven.MavenHandler;

import java.io.File;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MavenHandlerTest {

    @Test
    public void testResolveDependencies() throws Exception {
        File localRepo = new File("test");
        MavenHandler handler = new MavenHandler(localRepo);
        List<File> jarLocations =
            handler.resolveDependencies("com.google.android", "android", null, "4.1.1.4");
        assertEquals(9, jarLocations.size()); 
    }

    // @Test
    public void testResolveDependencies2() throws Exception {
        File localRepo = new File("test2");
        MavenHandler handler = new MavenHandler(
                localRepo,
                "central",
                "default",
                "https://raw.githubusercontent.com/pluto-build/pluto-build.github.io/master/mvnrepository/");
        List<File> x =
            handler.resolveDependencies("build.pluto", "pluto", null, "1.4.0");
        System.out.println(x.size());
    }

    @Test
    public void testGetHighestRemoteVersion() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        String newestVersion = handler.getHighestRemoteVersion("com.google.android", "android");
        assertEquals("4.1.1.4", newestVersion);
    }
}
