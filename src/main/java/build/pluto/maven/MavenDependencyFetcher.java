package build.pluto.maven;

import build.pluto.builder.Builder;
import build.pluto.builder.BuildManager;
import build.pluto.builder.BuilderFactory;
import build.pluto.maven.Artifact;
import build.pluto.maven.Dependency;
import build.pluto.maven.dependency.MavenRemoteRequirement;
import build.pluto.maven.MavenInput;
import build.pluto.output.None;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sugarj.common.FileCommands;

public class MavenDependencyFetcher extends Builder<MavenInput, None> {

    public static BuilderFactory<MavenInput, None, MavenDependencyFetcher> factory
        = BuilderFactory.of(MavenDependencyFetcher.class, MavenInput.class);

    public MavenDependencyFetcher(MavenInput input) {
        super(input);
    }

    protected String description(MavenInput input) {
        return "Gets an Artifact from defined Maven repositories and Central.";
    }

    protected File persistentPath(MavenInput input) {
        if(input.summaryLocation != null) {
            return new File(input.summaryLocation, "maven.dep");
        }
        return new File("./maven.dep");
    }

    protected None build(MavenInput input) throws Throwable {
        String tsFileName = "maven.ts";
        File timeStampPersistentPath = new File(input.summaryLocation, tsFileName);
        List<Artifact> artifactList = new ArrayList<>();
        for (Dependency d : input.dependencyList) {
            artifactList.add(d.artifact);
        }
        MavenRemoteRequirement mavenRequirement = new MavenRemoteRequirement(
                input.localRepoLocation,
                input.repositoryList,
                artifactList,
                timeStampPersistentPath,
                input.consistencyCheckInterval);
        this.requireOther(mavenRequirement);
        MavenHandler handler = new MavenHandler(input.localRepoLocation);
        List<File> artifactLocations =
            handler.resolveDependencies(input.dependencyList, input.repositoryList);

        //Write timestamp to file
        Thread currentThread = Thread.currentThread();
        long currentTime = BuildManager.requireInitiallyTimeStamps.get(currentThread);
        FileCommands.createFile(timeStampPersistentPath);
        FileCommands.writeToFile(timeStampPersistentPath, String.valueOf(currentTime));

        for(File f : artifactLocations) {
            this.provide(f);
        }
        return None.val;
    }
}
