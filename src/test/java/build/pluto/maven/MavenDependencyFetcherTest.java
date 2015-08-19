package build.pluto.maven;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.builder.RequiredBuilderFailed;
import build.pluto.maven.Artifact;
import build.pluto.maven.Dependency;
import build.pluto.maven.MavenDependencyFetcher;
import build.pluto.maven.MavenInput;
import build.pluto.maven.MavenHandler;
import build.pluto.maven.Repository;

import build.pluto.test.build.ScopedBuildTest;
import build.pluto.test.build.ScopedPath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.sugarj.common.FileCommands;

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
        Artifact artifact = new Artifact(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        Dependency dependency = new Dependency(artifact);
        dependencyList.add(dependency);
        build();
        String currentVersion = handler.getHighestLocalVersion(artifact);
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
        File summaryLocation = new File(localRepoLocation, "temp");
        MavenInput.Builder inputBuilder = new MavenInput.Builder(
                localRepoLocation,
                dependencyList,
                summaryLocation);
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
        Artifact artifact = new Artifact(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        Dependency dependency = new Dependency(artifact);
        dependencyList.add(dependency);
        build();
        build();
        String currentVersion = handler.getHighestLocalVersion(artifact);
        assertEquals("1.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    @Test
    public void testDoubleExecutionWithNewVersionInRange() throws Exception {
        Artifact artifact = new Artifact(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifact = changeVersionConstraint(artifact, "[1.0,)");
        Dependency dependency = new Dependency(artifact);
        dependencyList.add(dependency);
        build();
        Artifact newArtifact = changeVersionConstraint(artifact, "2.0");
        deployArtifact(newArtifact, "dummy-maven.jar", "pom2.xml", repoList.get(0));
        build();
        String currentVersion = handler.getHighestLocalVersion(artifact);
        assertEquals("2.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    private Artifact changeVersionConstraint(
            Artifact artifact,
            String versionConstraint) {
        return new Artifact(
                artifact.groupID,
                artifact.artifactID,
                versionConstraint,
                artifact.classifier,
                artifact.extension);
    }

    @Test
    public void testDoubleExecutionWithNewVersionOutsideOfRange()
            throws Exception {
        Artifact artifact = new Artifact(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifact = changeVersionConstraint(artifact, "[0.0,2.0)");
        Dependency dependency = new Dependency(artifact);
        dependencyList.add(dependency);
        build();
        Artifact newArtifact = changeVersionConstraint(artifact, "2.0");
        deployArtifact(newArtifact, "dummy-maven.jar", "pom2.xml", repoList.get(0));
        build();
        String currentVersion = handler.getHighestLocalVersion(artifact);
        assertEquals("1.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    @Test
    public void testDoubleExecutionWithNewVersionButToEarly() throws Exception {
        consistencyCheckInterval = 9000L;
        Artifact artifact = new Artifact(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifact = changeVersionConstraint(artifact, "[0.0,)");
        Dependency dependency = new Dependency(artifact);
        dependencyList.add(dependency);
        build();
        Artifact newArtifact = changeVersionConstraint(artifact, "2.0");
        deployArtifact(newArtifact, "dummy-maven.jar", "pom2.xml", repoList.get(0));
        build();
        String currentVersion = handler.getHighestLocalVersion(artifact);
        assertEquals("1.0", currentVersion);
        deleteRepo(repoList.get(0));
    }

    @Test
    public void testDoubleExecutionTwoDependenciesWithNewVersionInRange() throws Exception {
        Artifact artifact1 = new Artifact(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        deployArtifact(artifact1, "dummy-maven.jar", "pom.xml", repoList.get(0));
        Artifact artifact2 = new Artifact(
                "build.pluto",
                "dummy-maven2",
                "1.0",
                null,
                null);
        deployArtifact(artifact2, "dummy-maven.jar", "pom3.xml", repoList.get(0));
        artifact1 = changeVersionConstraint(artifact1, "[0.0,)");
        Dependency dependency1 = new Dependency(artifact1);
        Dependency dependency2 = new Dependency(artifact2);
        dependencyList.add(dependency1);
        dependencyList.add(dependency2);
        build();
        Artifact newArtifact1 = changeVersionConstraint(artifact1, "2.0");
        deployArtifact(newArtifact1, "dummy-maven.jar", "pom2.xml", repoList.get(0));
        build();
        String currentVersion1 = handler.getHighestLocalVersion(artifact1);
        String currentVersion2 = handler.getHighestLocalVersion(artifact2);
        assertEquals("2.0", currentVersion1);
        assertEquals("1.0", currentVersion2);
        deleteRepo(repoList.get(0));
    }

    @Test(expected = RequiredBuilderFailed.class)
    public void testSingleExecutionWithWrongArtifact() throws Exception {
        Artifact artifact = new Artifact(
                "build.pluto",
                "dummy-maven",
                "1.0",
                null,
                null);
        deployArtifact(artifact, "dummy-maven.jar", "pom.xml", repoList.get(0));
        artifact = changeVersionConstraint(artifact, "[2.0,)");
        Dependency dependency = new Dependency(artifact);
        dependencyList.add(dependency);
        build();
    }
}
