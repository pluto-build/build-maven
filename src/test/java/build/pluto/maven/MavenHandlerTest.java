package build.pluto.maven;

import org.junit.Test;

import java.io.File;
import java.util.List;

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

    @Test
    public void testResolveDependenciesOnOtherRepo() throws Exception {
        File localRepo = new File("test2");
        MavenHandler handler = new MavenHandler(
                localRepo,
                null,
                "default",
                "https://raw.githubusercontent.com/pluto-build/pluto-build.github.io/master/mvnrepository/");
        String version =  handler.getHighestRemoteVersion("build.pluto", "pluto", "[0,)");
        assertEquals("1.4.0-SNAPSHOT", version);
    }

    @Test
    public void testGetHighestRemoteVersion() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        String newestVersion = handler.getHighestRemoteVersion("com.google.android", "android", "[0,)");
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestLocalVersionOfRange() throws Exception {
        File localRepo = new File("test5");
        MavenHandler handler = new MavenHandler(localRepo);
        String groupID = "com.google.android";
        String artifactID = "android";
        String version = "4.1.1.4";
        handler.resolveDependencies(groupID, artifactID, null, version);
        version = "4.0.1.2";
        handler.resolveDependencies(groupID, artifactID, null, version);
        version = "2.3.3";
        handler.resolveDependencies(groupID, artifactID, null, version);
        String newestVersion = handler.getHighestLocalVersion(groupID, artifactID, "[0,)");
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestLocalVersionOfSingle() throws Exception {
        File localRepo = new File("test5");
        MavenHandler handler = new MavenHandler(localRepo);
        String groupID = "com.google.android";
        String artifactID = "android";
        String version = "4.1.1.4";
        handler.resolveDependencies(groupID, artifactID, null, version);
        String newestVersion = handler.getHighestLocalVersion(groupID, artifactID, version);
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestRemoteVersionSmallerThan() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        String newestVersion = handler.getHighestRemoteVersion("com.google.android", "android", "(,4.1.1.4)");
        assertEquals("4.0.1.2", newestVersion);
    }

    @Test
    public void testGetNoVersionWithBiggerThan() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        String newestVersion = handler.getHighestRemoteVersion("com.google.android", "android", "(4.1.1.4,)");
        assertEquals(null, newestVersion);
    }

    @Test
    public void testGetNoVersionWithExactVersion() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        String newestVersion = handler.getHighestRemoteVersion("com.google.android", "android", "[1.5_r0]");
        assertEquals(null, newestVersion);
    }
}
