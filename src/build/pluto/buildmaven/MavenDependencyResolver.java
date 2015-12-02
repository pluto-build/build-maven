package build.pluto.buildmaven;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildmaven.dependency.MavenRemoteRequirement;
import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavenDependencyResolver extends Builder<MavenInput, Out<ArrayList<File>>> {

    public static BuilderFactory<MavenInput, Out<ArrayList<File>>, MavenDependencyResolver> factory
        = BuilderFactoryFactory.of(MavenDependencyResolver.class, MavenInput.class);

    public MavenDependencyResolver(MavenInput input) {
        super(input);
    }

    @Override
    protected String description(MavenInput input) {
        return "Gets an Artifact from defined Maven repositories and Central.";
    }

    @Override
    public File persistentPath(MavenInput input) {
        return new File(input.localRepoLocation, "maven.dep");
    }

    @Override
    protected Out<ArrayList<File>> build(MavenInput input) throws Throwable {
        Map<Long, List<ArtifactConstraint>> dependencyGroups = new HashMap<>();
        for (Dependency dep : input.dependencyList) {
            if (!dependencyGroups.containsKey(dep.consistencyCheckInterval)) {
                dependencyGroups.put(dep.consistencyCheckInterval, new ArrayList<ArtifactConstraint>());
            }
            List<ArtifactConstraint> group =
                dependencyGroups.get(dep.consistencyCheckInterval);
            group.add(dep.artifactConstraint);
        }
        for (Long interval : dependencyGroups.keySet()) {
            List<ArtifactConstraint> constraints = dependencyGroups.get(interval);
            File tsPersistentPath = new File(
                    input.localRepoLocation,
                    interval + ".maven.dep.time");
            RemoteRequirement mavenRequirement = new MavenRemoteRequirement(
                    input.localRepoLocation,
                    input.repositoryList,
                    constraints,
                    tsPersistentPath,
                    interval);
            this.requireOther(mavenRequirement);

        }
        MavenHandler handler = new MavenHandler(input.localRepoLocation);
        ArrayList<File> artifactLocations = new ArrayList<>(
            handler.resolveDependencies(input.dependencyList, input.repositoryList));

        for(File f : artifactLocations) {
            this.provide(f);
        }
        return OutputPersisted.of(artifactLocations);
    }
}
