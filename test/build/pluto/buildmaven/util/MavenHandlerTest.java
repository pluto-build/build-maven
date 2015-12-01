package build.pluto.buildmaven.util;

import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.Exclusion;
import build.pluto.buildmaven.input.Repository;
import build.pluto.buildmaven.util.MavenHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.sugarj.common.FileCommands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class MavenHandlerTest {

    @AfterClass
    public static void deleteTempDirs() {
        try {
            for (int i = 1;i < 7;i++) {
                FileCommands.delete(new File("test" + i));
            }
        } catch (IOException e) {
            fail("Could not delete temporary directories");
        }
    }
    @Test
    public void testResolveDependency() throws Exception {
        File localRepo = new File("test1");
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "com.google.android",
                "android",
                "4.1.1.4",
                null,
                null);
        Dependency dependency = new Dependency(artifactConstraint);
        List<File> jarLocations =
            handler.resolveDependencies(Arrays.asList(dependency), Arrays.<Repository>asList());
        assertEquals(9, jarLocations.size());
    }

    @Test
    public void testResolveDependencies() throws Exception {
        File localRepo = new File("test2");
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint1 = new ArtifactConstraint(
                "com.google.android",
                "android",
                "4.1.1.4",
                null,
                null);
        Dependency dependency1 = new Dependency(artifactConstraint1);
        ArtifactConstraint artifactConstraint2 = new ArtifactConstraint(
                "org.eclipse.jgit",
                "org.eclipse.jgit",
                "4.0.1.201506240215-r",
                null,
                null);
        Dependency dependency2 = new Dependency(artifactConstraint2);
        List<File> jarLocations =
            handler.resolveDependencies(
                    Arrays.asList(dependency1, dependency2),
                    Arrays.<Repository>asList());
        assertEquals(13, jarLocations.size());
    }

    @Test
    public void testResolveDependencyExclusion() throws Exception {
        File localRepo = new File("test3");
        MavenHandler handler = new MavenHandler(localRepo);
        Exclusion exclusion = new Exclusion(
                "org.json",
                "json",
                "*",
                "*");
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "com.google.android",
                "android",
                "4.1.1.4",
                null,
                null);
        Dependency dependency =
            new Dependency(artifactConstraint, Arrays.asList(exclusion));
        List<File> jarLocations =
            handler.resolveDependencies(Arrays.asList(dependency), Arrays.<Repository>asList());
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
        File localRepo = new File("test4");
        MavenHandler handler = new MavenHandler(localRepo);
        Repository repo = new Repository(
                "pluto-build",
                "https://raw.githubusercontent.com/pluto-build/"
                + "pluto-build.github.io/master/mvnrepository/",
                "",
                "default",
                null,
                null);
        ArtifactConstraint artifactConstraint =
            new ArtifactConstraint("build.pluto", "pluto", "1.6.0-SNAPSHOT", null, null);
        Dependency dependency = new Dependency(artifactConstraint);
        List<File> jarLocations =
            handler.resolveDependencies(Arrays.asList(dependency), Arrays.asList(repo));
        assertEquals(5, jarLocations.size());
    }

    @Test
    public void testGetHighestRemoteVersion() throws Exception {
        File localRepo = new File("test5");
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint =
            new ArtifactConstraint("com.google.android", "android", "[0,)", null, null);
        String newestVersion =
            handler.getHighestRemoteVersion(artifactConstraint, Arrays.<Repository>asList());
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestLocalVersionOfRange() throws Exception {
        File localRepo = new File("test6");
        MavenHandler handler = new MavenHandler(localRepo);
        String groupID = "com.google.android";
        String artifactID = "android";
        List<Repository> repos = new ArrayList<>();
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(groupID, artifactID, "4.0.1.2", null, null);
        Dependency dep = new Dependency(artifactConstraint);
        artifactConstraint =
            new ArtifactConstraint(groupID, artifactID, "[0,)", null, null);
        String newestVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestLocalVersionOfSingle() throws Exception {
        File localRepo = new File("test6");
        MavenHandler handler = new MavenHandler(localRepo);
        String groupID = "com.google.android";
        String artifactID = "android";
        String version = "4.1.1.4";
        List<Repository> repos = new ArrayList<>();
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(groupID, artifactID, version, null, null);
        Dependency dep = new Dependency(artifactConstraint);
        handler.resolveDependencies(Arrays.asList(dep), repos);
        String newestVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("4.1.1.4", newestVersion);
    }

    @Test
    public void testGetHighestRemoteVersionSmallerThan() throws Exception {
        File localRepo = new File("test5");
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint =
            new ArtifactConstraint("com.google.android", "android", "(,4.1.1.4)", null, null);
        String newestVersion =
            handler.getHighestRemoteVersion(artifactConstraint, Arrays.<Repository>asList());
        assertEquals("4.0.1.2", newestVersion);
    }

    @Test
    public void testGetNoVersionWithBiggerThan() throws Exception {
        File localRepo = new File("test5");
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint =
            new ArtifactConstraint("com.google.android", "android", "(4.1.1.4,)", null, null);
        String newestVersion =
            handler.getHighestRemoteVersion(artifactConstraint, Arrays.<Repository>asList());
        assertEquals(null, newestVersion);
    }

    @Test
    public void testGetNoVersionWithExactVersion() throws Exception {
        File localRepo = new File("test5");
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint =
            new ArtifactConstraint("com.google.android", "android", "[1.5_r0]", null, null);
        String newestVersion =
            handler.getHighestRemoteVersion(artifactConstraint, Arrays.<Repository>asList());
        assertEquals(null, newestVersion);
    }
}
