package build.pluto.maven;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.maven.dependency.MavenRemoteRequirement;
import build.pluto.maven.input.ArtifactConstraint;
import build.pluto.maven.input.Dependency;
import build.pluto.maven.input.MavenInput;
import build.pluto.maven.util.MavenHandler;
import build.pluto.output.None;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MavenDependencyFetcher extends Builder<MavenInput, None> {

    public static BuilderFactory<MavenInput, None, MavenDependencyFetcher> factory
        = BuilderFactory.of(MavenDependencyFetcher.class, MavenInput.class);

    public MavenDependencyFetcher(MavenInput input) {
        super(input);
    }

    @Override
    protected String description(MavenInput input) {
        return "Gets an Artifact from defined Maven repositories and Central.";
    }

    @Override
    protected File persistentPath(MavenInput input) {
        return new File(input.localRepoLocation, "maven.dep");
    }

    @Override
    protected None build(MavenInput input) throws Throwable {
        if(!isInputValid(input)) {
            throw new IllegalArgumentException("The given dependencies could not be resolved");
        }
        File tsPersistentPath = new File(input.localRepoLocation, "maven.dep.time");
        List<ArtifactConstraint> artifactConstraintList = new ArrayList<>();
        for (Dependency d : input.dependencyList) {
            artifactConstraintList.add(d.artifactConstraint);
        }
        MavenRemoteRequirement mavenRequirement = new MavenRemoteRequirement(
                input.localRepoLocation,
                input.repositoryList,
                artifactConstraintList,
                tsPersistentPath,
                input.consistencyCheckInterval);
        this.requireOther(mavenRequirement);
        MavenHandler handler = new MavenHandler(input.localRepoLocation);
        List<File> artifactLocations =
            handler.resolveDependencies(input.dependencyList, input.repositoryList);

        for(File f : artifactLocations) {
            this.provide(f);
        }
        return None.val;
    }

    private boolean isInputValid(MavenInput input) {
        MavenHandler handler = new MavenHandler(input.localRepoLocation);
        for(Dependency d : input.dependencyList) {
            if(!handler.isAnyArtifactAvailable(d.artifactConstraint, input.repositoryList)) {
                return false;
            }
        }
        return true;
    }
}
