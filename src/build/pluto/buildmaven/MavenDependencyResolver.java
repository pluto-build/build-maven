package build.pluto.buildmaven;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.buildmaven.dependency.MavenRemoteRequirement;
import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.executor.InputParser;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;

public class MavenDependencyResolver extends Builder<MavenInput, Out<List<File>>> {

    public static BuilderFactory<MavenInput, Out<List<File>>, MavenDependencyResolver> factory = new BuilderFactory<MavenInput, Out<List<File>>, MavenDependencyResolver>() {
		private static final long serialVersionUID = -6191333145182092802L;

		@Override
		public MavenDependencyResolver makeBuilder(MavenInput input) {
			return new MavenDependencyResolver(input);
		}

		@Override
		public boolean isOverlappingGeneratedFileCompatible(File overlap, Serializable input, BuilderFactory<?, ?, ?> otherFactory, Serializable otherInput) {
			return this.getClass().isInstance(otherFactory);
		}

		@Override
		public InputParser<MavenInput> inputParser() {
			return null;
		}
	};

    public MavenDependencyResolver(MavenInput input) {
        super(input);
    }

    @Override
    protected String description(MavenInput input) {
    	return "Maven resolve " + input.dependencyList;
    }

    @Override
    public File persistentPath(MavenInput input) {
        return new File(input.localRepoLocation, ".pluto/resolve." + input.repositoryList.hashCode() + ":" + input.dependencyList.hashCode() + ".dep");
    }

    @Override
    protected Out<List<File>> build(MavenInput input) throws Throwable {
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
        return OutputPersisted.of(Collections.unmodifiableList(artifactLocations));
    }
}
