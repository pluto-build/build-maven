package build.pluto.buildmaven;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildmaven.dependency.MavenRemoteRequirement;
import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmaven.util.MavenHandler;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MavenDependencyFetcher extends Builder<MavenInput, Out<ArrayList<File>>> {

    public static BuilderFactory<MavenInput, Out<ArrayList<File>>, MavenDependencyFetcher> factory
        = BuilderFactoryFactory.of(MavenDependencyFetcher.class, MavenInput.class);

    public MavenDependencyFetcher(MavenInput input) {
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
        isInputValid(input);
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
        ArrayList<File> artifactLocations = new ArrayList<>(
            handler.resolveDependencies(input.dependencyList, input.repositoryList));

        for(File f : artifactLocations) {
            this.provide(f);
        }
        return OutputPersisted.of(artifactLocations);
    }

    private void isInputValid(MavenInput input) {
        MavenHandler handler = new MavenHandler(input.localRepoLocation);
        for(Dependency d : input.dependencyList) {
            if(!handler.isAnyArtifactAvailable(d.artifactConstraint, input.repositoryList)) {
                String artifactString = d.artifactConstraint.toString();
                throw new IllegalArgumentException("The artifact " +  artifactString + " can not be found");
            }
        }
    }
}
