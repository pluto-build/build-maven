package build.pluto.buildmaven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.sugarj.common.FileCommands;

import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.Exclusion;
import build.pluto.buildmaven.input.Repository;
import build.pluto.test.build.ScopedBuildTest;
import build.pluto.test.build.ScopedPath;

public class MavenHandlerTest extends ScopedBuildTest {

    @ScopedPath("")
    private File localRepo;

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
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("junit", "junit", "4.12", null, null);
        Dependency dependency = new Dependency(artifactConstraint, 0l);
        List<File> jarLocations = handler.resolveDependencies(Arrays.asList(dependency), Arrays.<Repository>asList());
        assertEquals(2, jarLocations.size());
    }

    @Test
    public void testResolveDependencies() throws Exception {
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint1 = new ArtifactConstraint("junit", "junit", "4.12", null, null);
        Dependency dependency1 = new Dependency(artifactConstraint1, 0l);
        ArtifactConstraint artifactConstraint2 = new ArtifactConstraint("org.slf4j", "slf4j-simple", "1.7.13", null, null);
        Dependency dependency2 = new Dependency(artifactConstraint2, 0l);
        List<File> jarLocations =
            handler.resolveDependencies(
                    Arrays.asList(dependency1, dependency2),
                    Arrays.<Repository>asList());
        assertEquals(4, jarLocations.size());
    }

    @Test
    public void testResolveDependencyExclusion() throws Exception {
        MavenHandler handler = new MavenHandler(localRepo);
        Exclusion exclusion = new Exclusion("org.hamcrest", "hamcrest-core", "*", "*");
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("junit", "junit", "4.12", null, null);
        Dependency dependency = new Dependency(artifactConstraint, Arrays.asList(exclusion), 0l);
        List<File> jarLocations = handler.resolveDependencies(Arrays.asList(dependency), Arrays.<Repository>asList());
        assertEquals(1, jarLocations.size());
        for (File f : jarLocations)
        	assertFalse(FileCommands.fileName(f).contains("hamcrest"));
    }

    @Test
    public void testResolveDependenciesOnOtherRepo() throws Exception {
        MavenHandler handler = new MavenHandler(localRepo);
        Repository repo = new Repository(
                "pluto-build",
                "https://raw.githubusercontent.com/sugar-lang/sugar-lang.github.io/master/mvnrepository/",
                "default",
                null,
                null);
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("org.sugarj", "common", "1.5.0-SNAPSHOT", null, null);
        Dependency dependency = new Dependency(artifactConstraint, 0l);
        List<File> jarLocations = handler.resolveDependencies(Arrays.asList(dependency), Arrays.asList(repo));
        assertEquals(2, jarLocations.size());
    }

    @Test
    public void testGetHighestRemoteVersion() throws Exception {
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("junit", "junit", "[0,4.10]", null, null);
        String newestVersion = handler.getHighestRemoteVersion(artifactConstraint, Arrays.<Repository>asList());
        assertEquals("4.10", newestVersion);
    }

    @Test
    public void testGetHighestRemoteVersionSmallerThan() throws Exception {
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("junit", "junit", "(,4.12)", null, null);
        String newestVersion = handler.getHighestRemoteVersion(artifactConstraint, Arrays.<Repository>asList());
        assertEquals("4.12-beta-3", newestVersion);
    }

    @Test
    public void testGetHighestLocalVersionOfRange() throws Exception {
        MavenHandler handler = new MavenHandler(localRepo);
        List<Repository> repos = new ArrayList<>();
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("junit", "junit", "4.10", null, null);
        handler.resolveDependencies(Arrays.asList(new Dependency(artifactConstraint, 0l)), repos);
        
        ArtifactConstraint artifactConstraint2 = new ArtifactConstraint("junit", "junit", "[0,4.12]", null, null);
        handler.resolveDependencies(Arrays.asList(new Dependency(artifactConstraint2, 0l)), repos);
        
        String newestVersion = handler.getHighestLocalVersion(artifactConstraint2);
        assertEquals("4.12", newestVersion);
    }

    @Test
    public void testGetHighestLocalVersionOfSingle() throws Exception {
    	MavenHandler handler = new MavenHandler(localRepo);
        List<Repository> repos = new ArrayList<>();
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("junit", "junit", "4.10", null, null);
        handler.resolveDependencies(Arrays.asList(new Dependency(artifactConstraint, 0l)), repos);
        
        String newestVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("4.10", newestVersion);
    }

    @Test
    public void testGetNoVersionWithBiggerThan() throws Exception {
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("junit", "junit", "(20.1,)", null, null);
        String newestVersion = handler.getHighestRemoteVersion(artifactConstraint, Arrays.<Repository>asList());
        assertEquals(null, newestVersion);
    }

    @Test
    public void testGetNoVersionWithExactVersion() throws Exception {
        MavenHandler handler = new MavenHandler(localRepo);
        ArtifactConstraint artifactConstraint = new ArtifactConstraint("junit", "junit", "[20.1]", null, null);
        String newestVersion = handler.getHighestRemoteVersion(artifactConstraint, Arrays.<Repository>asList());
        assertEquals(null, newestVersion);
    }
}
