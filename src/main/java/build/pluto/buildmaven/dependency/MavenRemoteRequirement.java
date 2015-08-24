package build.pluto.buildmaven.dependency;

import build.pluto.builder.BuildUnitProvider;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.buildmaven.util.MavenHandler;
import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavenRemoteRequirement extends RemoteRequirement {
    private File localRepoLocation;
    private List<Repository> repos;
    private List<ArtifactConstraint> artifactConstraints;

    public MavenRemoteRequirement(
            File localRepoLocation,
            List<Repository> repos,
            List<ArtifactConstraint> artifactConstraints,
            File persistentPath,
            long consistencyCheckInterval) {
        super(persistentPath, consistencyCheckInterval);
        this.localRepoLocation = localRepoLocation;
        this.repos = repos;
        this.artifactConstraints = artifactConstraints;
    }

    @Override
    public boolean isConsistentWithRemote() {
        MavenHandler handler = new MavenHandler(localRepoLocation);
        Map<ArtifactConstraint, String> currentVersions = new HashMap<>();
        for (ArtifactConstraint a : artifactConstraints) {
            String version = handler.getHighestLocalVersion(a);
            currentVersions.put(a, version);
        }
        if (currentVersions.keySet().size() != 0) {
            for (ArtifactConstraint a : artifactConstraints) {
                String remoteVersion = handler.getHighestRemoteVersion(a, repos);
                String localVersion = currentVersions.getOrDefault(a, "NO VERSION");
                if (!remoteVersion.equals(localVersion)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean tryMakeConsistent(BuildUnitProvider manager) throws IOException {
        return this.isConsistent();
    }

    @Override
    public String toString() {
        return "MavenRemoteReq(" + localRepoLocation.toString() + ")";
    }
}
