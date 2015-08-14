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
        List<File> jarLocations =
            handler.resolveDependencies(Arrays.asList(artifact), Arrays.asList());
        assertEquals(9, jarLocations.size());
    }

    @Test
    public void testResolveDependencies() throws Exception {
        File localRepo = new File("test10");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact1 = new Artifact(
                "com.google.android",
                "android",
                "4.1.1.4",
                null,
                null);
        Artifact artifact2 = new Artifact(
                "org.eclipse.jgit",
                "org.eclipse.jgit",
                "4.0.1.201506240215-r",
                null,
                null);
        List<File> jarLocations =
            handler.resolveDependencies(
                    Arrays.asList(artifact1, artifact2),
                    Arrays.asList());
        assertEquals(13, jarLocations.size());
    }

    @Test
    public void testResolveDependencyExclusion() throws Exception {
        File localRepo = new File("test11");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact exclusion = new Artifact(
                "org.json",
                "json",
                null,
                "*",
                "*");
        Artifact artifact = new Artifact(
                "com.google.android",
                "android",
                "4.1.1.4",
                null,
                null,
                Arrays.asList(exclusion),
                true);
        List<File> jarLocations =
            handler.resolveDependencies(Arrays.asList(artifact), Arrays.asList());
        File jsonLocation =
            new File(localRepo, "org/json/json/20080701/json-20080701.jar");
        assertFalse(containsArtifact(jsonLocation, jarLocations));
    }

    private boolean containsArtifact(
            File artifactLocation,
            List<File> artifactList) {
        List<String> artifactLocationStringList = new ArrayList<>();
        for(File f : artifactList) {
            artifactLocationStringList.add(f.getAbsolutePath());
        }
        String artifactLocationString = artifactLocation.getAbsolutePath();
        return artifactLocationStringList.contains(artifactLocationString);
    }

    @Test
    public void testResolveDependenciesOnOtherRepo() throws Exception {
        File localRepo = new File("test2");
        MavenHandler handler = new MavenHandler(localRepo);
        Repository repo = new Repository(
                "pluto-build",
                "https://raw.githubusercontent.com/pluto-build/"
                + "pluto-build.github.io/master/mvnrepository/",
                "",
                "default",
                null,
                null);
        Artifact artifact =
            new Artifact("build.pluto", "pluto", "[0,)", null, null);
        List<File> jarLocations =
            handler.resolveDependencies(Arrays.asList(artifact), Arrays.asList(repo));
        assertEquals(7, jarLocations.size());
    }

    @Test
    public void testGetHighestRemoteVersion() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact =
            new Artifact("com.google.android", "android", "[0,)", null, null);
        String newestVersion =
            handler.getHighestRemoteVersion(artifact, Arrays.asList());
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
        String newestVersion =
            handler.getHighestRemoteVersion(artifact, Arrays.asList());
        assertEquals("4.0.1.2", newestVersion);
    }

    @Test
    public void testGetNoVersionWithBiggerThan() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact =
            new Artifact("com.google.android", "android", "(4.1.1.4,)", null, null);
        String newestVersion =
            handler.getHighestRemoteVersion(artifact, Arrays.asList());
        assertEquals(null, newestVersion);
    }

    @Test
    public void testGetNoVersionWithExactVersion() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        Artifact artifact =
            new Artifact("com.google.android", "android", "[1.5_r0]", null, null);
        String newestVersion =
            handler.getHighestRemoteVersion(artifact, Arrays.asList());
        assertEquals(null, newestVersion);
    }
}
