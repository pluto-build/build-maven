package build.pluto.buildmaven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.Exclusion;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmaven.input.Repository;
import build.pluto.executor.InputParser;
import build.pluto.executor.config.Config;
import build.pluto.executor.config.yaml.SimpleYamlObject;
import build.pluto.executor.config.yaml.YamlObject;
import build.pluto.test.build.ScopedBuildTest;
import build.pluto.test.build.ScopedPath;

public class MavenInputParserTest extends ScopedBuildTest {
  @ScopedPath("")
  private File workingDir;

  @Test
  public void printRefArtifact() throws Throwable {
    MavenInput result = parse(new File(workingDir, "SingleArtifact.yml"), "test");
    System.out.println(result.localRepoLocation);
      for (Dependency dep : result.dependencyList) {
        System.out.println(dep.artifactConstraint.groupID);
        System.out.println(dep.artifactConstraint.artifactID);
        System.out.println(dep.artifactConstraint.versionConstraint);
        System.out.println(dep.artifactConstraint.classifier);
        System.out.println(dep.artifactConstraint.extension);
        System.out.println(dep.consistencyCheckInterval);
        System.out.println(dep.scope);
        System.out.println(dep.optional);
        for (Exclusion ex : dep.exclusions) {
          System.out.println(ex.groupID);
          System.out.println(ex.artifactID);
          System.out.println(ex.classifier);
          System.out.println(ex.extension);
        }
      }
  }

  /**
   * Checks if one artifact, the local repository and every possible way
   * of defining an exclusion is correctly parsed.
   */
  @Test
  public void testOneArtifact() throws Throwable {
    MavenInput result = parse(new File(workingDir, "SingleArtifact.yml"), "test");
    assertEquals(new File(workingDir, "lib"), result.localRepoLocation);
    Dependency dep = result.dependencyList.get(0);
    assertEquals("group", dep.artifactConstraint.groupID);
    assertEquals("artifact", dep.artifactConstraint.artifactID);
    assertEquals("1.0", dep.artifactConstraint.versionConstraint);
    assertEquals("jar", dep.artifactConstraint.extension);
    assertEquals(1, dep.consistencyCheckInterval);
    assertEquals(true, dep.optional);
    assertEquals("java", dep.scope);

    Exclusion firstEx = dep.exclusions.get(0);
    assertEquals("group", firstEx.groupID);
    assertEquals("art", firstEx.artifactID);
    assertEquals("tests", firstEx.classifier);
    assertEquals("jar", firstEx.extension);

    Exclusion secondEx = dep.exclusions.get(1);
    assertEquals("group1", secondEx.groupID);
    assertEquals("art1", secondEx.artifactID);
    assertEquals("", secondEx.classifier);
    assertEquals("war", secondEx.extension);

    Exclusion thirdEx = dep.exclusions.get(2);
    assertEquals("group2", thirdEx.groupID);
    assertEquals("art2", thirdEx.artifactID);
    assertNull(thirdEx.classifier);
    assertNull(thirdEx.extension);

    assertEquals(0, result.repositoryList.size());
  }

  @Test
  public void testTwoArtifacts() throws Throwable {
    MavenInput result = parse(new File(workingDir, "TwoArtifacts.yml"), "test");
    assertEquals(new File(workingDir, "lib"), result.localRepoLocation);
    Dependency firstDep = result.dependencyList.get(0);
    assertEquals("group", firstDep.artifactConstraint.groupID);
    assertEquals("artifact", firstDep.artifactConstraint.artifactID);
    assertEquals("1.0", firstDep.artifactConstraint.versionConstraint);
    assertEquals("", firstDep.artifactConstraint.classifier);
    assertEquals("jar", firstDep.artifactConstraint.extension);

    Dependency secondDep = result.dependencyList.get(1);
    assertEquals("group1", secondDep.artifactConstraint.groupID);
    assertEquals("artifact1", secondDep.artifactConstraint.artifactID);
    assertEquals("1.0", secondDep.artifactConstraint.versionConstraint);
    assertEquals("doc", secondDep.artifactConstraint.classifier);
    assertNull(secondDep.artifactConstraint.extension);
  }

  @Test
  public void testNoDefinedInterval() throws Throwable {
    MavenInput result = parse(new File(workingDir, "NoDefinedInterval.yml"), "test");
    assertEquals(0, result.dependencyList.get(0).consistencyCheckInterval);
  }

  @Test
  public void testNeverCheckInterval() throws Throwable {
    MavenInput result = parse(new File(workingDir, "NegativeInterval.yml"), "test");
    assertEquals(-1, result.dependencyList.get(0).consistencyCheckInterval);
  }

  @Test
  public void testOneRepository() throws Throwable {
    MavenInput result = parse(new File(workingDir, "OneRepository.yml"), "test");
    Repository rep = result.repositoryList.get(0);
    assertEquals("something", rep.id);
    assertEquals("http://someurl.com", rep.url);
    assertEquals("default", rep.layout);
    Repository.Policy release = rep.releasePolicy;
    assertEquals(true, release.enabled);
    assertEquals("ignore", release.checksumPolicy);
    Repository.Policy snapshot = rep.snapshotPolicy;
    assertEquals(false, snapshot.enabled);
    assertEquals("warn", snapshot.checksumPolicy);
  }

  @Test
  public void testTwoRepositories() throws Throwable {
    MavenInput result = parse(new File(workingDir, "TwoRepositories.yml"), "test");
    Repository firstRep = result.repositoryList.get(0);
    assertEquals("something", firstRep.id);
    assertEquals("http://someurl.com", firstRep.url);

    Repository secondRep = result.repositoryList.get(1);
    assertEquals("other", secondRep.id);
    assertEquals("http://otherurl.com", secondRep.url);
  }

  private MavenInput parse(File yamlFile, String buildTarget) throws Throwable {
    Yaml yaml = new Yaml(new Constructor(Config.class));
    Config config = (Config) yaml.load(new FileInputStream(yamlFile));
    File workingDir = yamlFile.getParentFile();
    config.makePathsAbsolute(workingDir);
    YamlObject input = SimpleYamlObject.of(config.getDependencies().get(0).input);
    InputParser<MavenInput> parser = new MavenInputParser();
    return parser.parse(input, buildTarget, workingDir);
  }
}
