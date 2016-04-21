package build.pluto.buildmaven;

import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.Exclusion;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmaven.input.Repository;
import build.pluto.executor.InputParser;
import build.pluto.executor.config.yaml.SimpleYamlObject;
import build.pluto.executor.config.yaml.YamlObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MavenInputParser implements InputParser<MavenInput> {

  @Override
  public MavenInput parse(YamlObject yamlObject, String target, File workingDir) throws Throwable {
    MavenInput.Builder builder = new MavenInput.Builder();
    Map<String, YamlObject> map = yamlObject.asMap();

    File localRepo = new File(map.get("local").asString());
    if (!localRepo.isAbsolute())
      localRepo = new File(workingDir, localRepo.getPath());
    builder = builder.setLocalRepoLocation(localRepo);

    YamlObject artifacts = map.get("artifacts");
    for (YamlObject a : artifacts.asList()) {
      builder = builder.addDependency(constructDependency(a));
    }

    YamlObject repositories = map.get("repositories");
    for (YamlObject r : repositories.asList()) {
      builder = builder.addRepository(constructRepository(r));
    }
    return builder.build();
  }

  private Dependency constructDependency(YamlObject input) {
    Map<String, YamlObject> artifactConfig = input.asMap();
    ArtifactConstraint constraint = constructArtifactConstraint(artifactConfig.get("name"));
    long interval = (long) artifactConfig.get("interval").asInt();
    boolean optional = artifactConfig.get("optional").asBoolean();
    String scope = artifactConfig.get("scope").asString();
    List<Exclusion> exclusions = new ArrayList<>();
    for (YamlObject e : artifactConfig.get("exclusions").asList()) {
      exclusions.add(constructExclusion(e));
    }
    return new Dependency(constraint, exclusions, scope, optional, interval);
  }

  private ArtifactConstraint constructArtifactConstraint(YamlObject input)  {
    String artifactDescription = input.asString();
    String[] artifactParts = artifactDescription.split(":");
    String groupId = artifactParts[0];
    String artifactId = artifactParts[1];
    String versionConstraint = artifactParts[2];
    String classifier = artifactParts.length > 3 ? artifactParts[3] : null;
    String extension = artifactParts.length > 4 ? artifactParts[4] : null;
    return new ArtifactConstraint(groupId, artifactId, versionConstraint, classifier, extension);
  }

  private Exclusion constructExclusion(YamlObject input) {
    String exclusionDescription = input.asString();
    String[] exclusionParts = exclusionDescription.split(":");
    String groupId = exclusionParts[0];
    String artifactId = exclusionParts[1];
    String classifier = exclusionParts.length > 2 ? exclusionParts[2] : null;
    String extension = exclusionParts.length > 3 ? exclusionParts[3] : null;
    return new Exclusion(groupId, artifactId, classifier, extension);
  }

  private Repository constructRepository(YamlObject input) {
    Map<String, YamlObject> rMap = input.asMap();
    String id = rMap.get("id").asString();
    String url = rMap.get("url").asString();
    String layout = rMap.get("layout").asString();
    YamlObject snapshot = rMap.get("snapshot");
    YamlObject release = rMap.get("release");
    Repository.Policy snapshotPolicy = snapshot != null ? constructPolicy(snapshot) : null;
    Repository.Policy releasePolicy = release != null ? constructPolicy(release) : null;
    return new Repository(id, url, layout, snapshotPolicy, releasePolicy);
  }

  private Repository.Policy constructPolicy(YamlObject input) {
    Map<String, YamlObject> values = input.asMap();
    boolean enabled = values.get("enabled").asBoolean();
    String checksum = values.get("checksum").asString();
    return new Repository.Policy(enabled, checksum);
  }
}
