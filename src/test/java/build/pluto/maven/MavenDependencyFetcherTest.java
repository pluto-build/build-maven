package build.pluto.maven;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.RequiredBuilderFailed;
import build.pluto.test.build.ScopedBuildTest;
import build.pluto.test.build.ScopedPath;
import org.junit.Before;
import org.junit.Test;
import org.sugarj.common.FileCommands;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MavenDependencyFetcherTest extends ScopedBuildTest {
    @ScopedPath("")
    private File localRepoLocation;

    private List<Repository> repoList;
    private List<Dependency> dependencyList;
    private long consistencyCheckInterval;
    private MavenHandler handler;

    @Before
    public void init() {
        Repository repo = new Repository(
                "test-repo",
                "file:///Users/andiderp/repos/maven-builder/repository",
                "test-repo",
                "default",
                null,
                null);
        repoList = Arrays.asList(repo);
        dependencyList = new ArrayList<>();
        consistencyCheckInterval = 0L;
        handler = new MavenHandler(localRepoLocation);
    }

    @Test
    public void testSingleSimpleExecution() throws Exception {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        Dependency dependency = new Dependency(artifactConstraint);
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
            Repository repo) throws Exception {
        File dummyMaven = new File("src/test/resources/dummy-maven");
        File jarLocation = new File(dummyMaven, jarPath);
        File pomLocation = new File(dummyMaven, pomPath);
        handler.deployArtifact(
                artifact,
                jarLocation,
                pomLocation,
                repo);
    }

    private void build() throws IOException {
        MavenInput.Builder inputBuilder = new MavenInput.Builder(
                localRepoLocation,
                dependencyList);
        inputBuilder.setConsistencyCheckInterval(consistencyCheckInterval);
        inputBuilder.setRepositoryList(this.repoList);
        MavenInput input = inputBuilder.build();
        BuildRequest<?, ?, ?, ?> buildRequest =
            new BuildRequest(MavenDependencyFetcher.factory, input);
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
    public void testDoubleExecutionWithoutNewVersion() throws Exception {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        Dependency dependency = new Dependency(artifactConstraint);
        dependencyList.add(dependency);
        build();
        build();
        String currentVersion = handler.getHighestLocalVersion(artifactConstraint);
        assertEquals("1.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    @Test
    public void testDoubleExecutionWithNewVersionInRange() throws Exception {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifactConstraint = changeVersionConstraint(artifactConstraint, "[1.0,)");
        Dependency dependency = new Dependency(artifactConstraint);
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
            throws Exception {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifactConstraint = changeVersionConstraint(artifactConstraint, "[0.0,2.0)");
        Dependency dependency = new Dependency(artifactConstraint);
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
    public void testDoubleExecutionWithNewVersionButToEarly() throws Exception {
        consistencyCheckInterval = 90000L;
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifactConstraint = changeVersionConstraint(artifactConstraint, "[0.0,)");
        Dependency dependency = new Dependency(artifactConstraint);
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
    public void testDoubleExecutionTwoDependenciesWithNewVersionInRange() throws Exception {
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
        Dependency dependency1 = new Dependency(artifactConstraint1);
        Dependency dependency2 = new Dependency(artifactConstraint2);
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

    @Test(expected = RequiredBuilderFailed.class)
    public void testSingleExecutionWithWrongArtifact() throws Exception {
        ArtifactConstraint artifactConstraint = new ArtifactConstraint(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        Artifact artifact = MavenHandler.transformToArtifact(artifactConstraint);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifactConstraint = changeVersionConstraint(artifactConstraint, "[2.0,)");
        Dependency dependency = new Dependency(artifactConstraint);
        dependencyList.add(dependency);
        build();
    }
}
