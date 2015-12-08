package build.pluto.buildmaven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.Before;
import org.junit.Test;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.buildmaven.input.Artifact;
import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.Exclusion;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmaven.input.Repository;
import build.pluto.test.build.ScopedBuildTest;
import build.pluto.test.build.ScopedPath;

public class MavenDependencyResolverTest extends ScopedBuildTest {
    @ScopedPath("")
    private File localRepoLocation;

    private List<Repository> repoList;
    private List<Dependency> dependencyList;
    private MavenHandler handler;

    @Before
    public void init() {
        File workingDir = new File("");
        Repository repo = new Repository(
                "test-repo",
                "file://" + workingDir.getAbsolutePath() + "/repository",
                "default",
                null,
                null);
        repoList = Arrays.asList(repo);
        dependencyList = new ArrayList<>();
        handler = new MavenHandler(localRepoLocation);
    }

    @Test
    public void testSingleSimpleExecution() throws Throwable {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        Dependency dependency = new Dependency(artifactConstraint, 0l);
        dependencyList.add(dependency);
        build();
        String currentVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("1.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    private void deployArtifact(
            Artifact artifact,
            String jarPath,
            String pomPath,
            Repository repo) throws Throwable {
        File dummyMaven = new File("testdata/dummy-maven");
        File jarLocation = new File(dummyMaven, jarPath);
        File pomLocation = new File(dummyMaven, pomPath);
        MavenDeployer.Input input = new MavenDeployer.Input(artifact, jarLocation, null, pomLocation, null, localRepoLocation, repo);
        BuildManagers.build(new BuildRequest<>(MavenDeployer.factory, input));
    }

    private void build() throws Throwable {
        MavenInput input = 
        		new MavenInput.Builder(localRepoLocation)
        		.setRepositoryList(this.repoList)
        		.setDependencyList(dependencyList)
        		.build();
        BuildRequest<?, ?, ?, ?> buildRequest =
            new BuildRequest<>(MavenDependencyResolver.factory, input);
        BuildManagers.build(buildRequest);
    }

    private void deleteRepo(Repository repo) throws Exception {
        try {
            URI repoURI = new URI(repo.url);
            File repoLocation = new File(repoURI);
            FileCommands.delete(repoLocation);
        } catch(IOException e) {
            fail("Could not delete repo");
        } catch(URISyntaxException e) {
            fail("Path to repo is not valid");
        }
    }

    @Test
    public void testDoubleExecutionWithoutNewVersion() throws Throwable {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        Dependency dependency = new Dependency(artifactConstraint, 0l);
        dependencyList.add(dependency);
        build();
        build();
        String currentVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("1.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    @Test
    public void testDoubleExecutionWithNewVersionInRange() throws Throwable {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifactConstraint = changeVersionConstraint(artifactConstraint, "[1.0,)");
        Dependency dependency = new Dependency(artifactConstraint, 0l);
        dependencyList.add(dependency);
        build();
        ArtifactConstraint newArtifactConstraint = changeVersionConstraint(artifactConstraint, "2.0");
        Artifact newArtifact = MavenHandler.transformToArtifact(newArtifactConstraint);
        deployArtifact(newArtifact, "dummy-maven.jar", "pom2.xml", repoList.get(0));
        build();
        String currentVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("2.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    private ArtifactConstraint changeVersionConstraint(
            ArtifactConstraint artifactConstraint,
            String versionConstraint) {
        return new ArtifactConstraint(
                artifactConstraint.groupID,
                artifactConstraint.artifactID,
                versionConstraint,
                artifactConstraint.classifier,
                artifactConstraint.extension);
    }

    @Test
    public void testDoubleExecutionWithNewVersionOutsideOfRange()
            throws Throwable {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifactConstraint = changeVersionConstraint(artifactConstraint, "[0.0,2.0)");
        Dependency dependency = new Dependency(artifactConstraint, 0l);
        dependencyList.add(dependency);
        build();
        ArtifactConstraint newArtifactConstraint = changeVersionConstraint(artifactConstraint, "2.0");
        Artifact newArtifact = MavenHandler.transformToArtifact(newArtifactConstraint);
        deployArtifact(newArtifact, "dummy-maven.jar", "pom2.xml", repoList.get(0));
        build();
        String currentVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("1.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    @Test
    public void testDoubleExecutionWithNewVersionButToEarly() throws Throwable {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifactConstraint = changeVersionConstraint(artifactConstraint, "[0.0,)");
        Dependency dependency = new Dependency(artifactConstraint, new ArrayList<Exclusion>(), 90000L);

        dependencyList.add(dependency);
        build();
        ArtifactConstraint newArtifactConstraint = changeVersionConstraint(artifactConstraint, "2.0");
        Artifact newArtifact = MavenHandler.transformToArtifact(newArtifactConstraint);
        deployArtifact(newArtifact, "dummy-maven.jar", "pom2.xml", repoList.get(0));
        build();
        String currentVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("1.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    @Test
    public void testDoubleExecutionTwoDependenciesWithNewVersionInRange() throws Throwable {
        ArtifactConstraint artifactConstraint1 = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact1 = MavenHandler.transformToArtifact(artifactConstraint1);
        deployArtifact(artifact1, "dummy-maven.jar", "pom.xml", repoList.get(0));
        ArtifactConstraint artifactConstraint2 = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven2",
                "1.0",
                null,
                null);
        Artifact artifact2 = MavenHandler.transformToArtifact(artifactConstraint2);
        deployArtifact(artifact2, "dummy-maven.jar", "pom3.xml", repoList.get(0));
        artifactConstraint1 = changeVersionConstraint(artifactConstraint1, "[0.0,)");
        Dependency dependency1 = new Dependency(artifactConstraint1, 0l);
        Dependency dependency2 = new Dependency(artifactConstraint2, 0l);
        dependencyList.add(dependency1);
        dependencyList.add(dependency2);
        build();
        ArtifactConstraint newArtifactConstraint1 = changeVersionConstraint(artifactConstraint1, "2.0");
        Artifact newArtifact1 = MavenHandler.transformToArtifact(newArtifactConstraint1);
        deployArtifact(newArtifact1, "dummy-maven.jar", "pom2.xml", repoList.get(0));
        build();
        String currentVersion1 = handler.getHighestLocalVersion(artifactConstraint1);
        String currentVersion2 = handler.getHighestLocalVersion(artifactConstraint2);
        assertEquals("2.0", currentVersion1);
        assertEquals("1.0", currentVersion2);
        deleteRepo(repoList.get(0));
    }

    @Test(expected = DependencyResolutionException.class)
    public void testSingleExecutionWithWrongArtifact() throws Throwable {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "DOES-NOT-EXIST-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifactConstraint = changeVersionConstraint(artifactConstraint, "[2.0,)");
        Dependency dependency = new Dependency(artifactConstraint, 0l);
        dependencyList.add(dependency);
        build();
    }
}
