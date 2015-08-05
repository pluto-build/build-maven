package build.pluto.maven;

import build.pluto.maven.Artifact;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MavenHandlerTest {

    @Test
    public void testResolveDependency() throws Exception {
        File localRepo = new File("test");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact = new Artifact(
                "com.google.android",
                "android",
                "4.1.1.4",
                null,
                null);
        List<File> jarLocations = handler.resolveDependencies(artifact);
        assertEquals(9, jarLocations.size());
    }

    @Test
    public void testResolveDependenciesOnOtherRepo() throws Exception {
        File localRepo = new File("test2");
        MavenHandler handler = new MavenHandler(
                localRepo,
                null,
                "default",
                "https://raw.githubusercontent.com/pluto-build/"
                + "pluto-build.github.io/master/mvnrepository/");
        Artifact artifact =
            new Artifact("build.pluto", "pluto", "[0,)", null, null);
        String version =  handler.getHighestRemoteVersion(artifact);
        assertEquals("1.4.0-SNAPSHOT", version);
    }

    @Test
    public void testGetHighestRemoteVersion() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact =
            new Artifact("com.google.android", "android", "[0,)", null, null);
        String newestVersion = handler.getHighestRemoteVersion(artifact);
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestLocalVersionOfRange() throws Exception {
        File localRepo = new File("test5");
        MavenHandler handler = new MavenHandler(localRepo);
        String groupID = "com.google.android";
        String artifactID = "android";
        Artifact artifact =
            new Artifact(groupID, artifactID, "[0,)", null, null);
        String newestVersion = handler.getHighestLocalVersion(artifact);
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestLocalVersionOfSingle() throws Exception {
        File localRepo = new File("test5");
        MavenHandler handler = new MavenHandler(localRepo);
        String groupID = "com.google.android";
        String artifactID = "android";
        String version = "4.1.1.4";
        Artifact artifact = new Artifact(groupID, artifactID, version, null, null);
        String newestVersion = handler.getHighestLocalVersion(artifact);
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestRemoteVersionSmallerThan() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact =
            new Artifact("com.google.android", "android", "(,4.1.1.4)", null, null);
        String newestVersion = handler.getHighestRemoteVersion(artifact);
        assertEquals("4.0.1.2", newestVersion);
    }

    @Test
    public void testGetNoVersionWithBiggerThan() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact =
            new Artifact("com.google.android", "android", "(4.1.1.4,)", null, null);
        String newestVersion = handler.getHighestRemoteVersion(artifact);
        assertEquals(null, newestVersion);
    }

    @Test
    public void testGetNoVersionWithExactVersion() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact =
            new Artifact("com.google.android", "android", "[1.5_r0]", null, null);
        String newestVersion = handler.getHighestRemoteVersion(artifact);
        assertEquals(null, newestVersion);
    }
}
