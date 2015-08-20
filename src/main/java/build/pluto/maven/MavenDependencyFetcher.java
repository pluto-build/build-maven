package build.pluto.maven;

import build.pluto.builder.BuilderFactory;
import build.pluto.builder.RemoteAccessBuilder;
import build.pluto.maven.dependency.MavenRemoteRequirement;
import build.pluto.output.None;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MavenDependencyFetcher extends RemoteAccessBuilder<MavenInput, None> {

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
        if(input.summaryLocation != null) {
            return new File(input.summaryLocation, "maven.dep");
        }
        return new File("./maven.dep");
    }

    @Override
    protected File timestampPersistentPath(MavenInput input) {
        if(input.summaryLocation != null) {
            return new File(input.summaryLocation, "maven.ts");
        }
        return new File("./maven.ts");
    }

    @Override
    protected None build(MavenInput input, File tsPersistentPath) throws Throwable {
        if(!input.isValid()) {
            throw new IllegalArgumentException("The given dependencies could not be resolved");
        }
        List<Artifact> artifactList = new ArrayList<>();
        for (Dependency d : input.dependencyList) {
            artifactList.add(d.artifact);
        }
        MavenRemoteRequirement mavenRequirement = new MavenRemoteRequirement(
                input.localRepoLocation,
                input.repositoryList,
                artifactList,
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
}
