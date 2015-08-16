package build.pluto.maven.dependency;

import build.pluto.builder.BuildUnitProvider;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.maven.MavenHandler;
import build.pluto.maven.Artifact;
import build.pluto.maven.Repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class MavenRemoteRequirement extends RemoteRequirement {
    private File localRepoLocation;
    private List<Repository> repos;
    private List<Artifact> artifacts;

    public MavenRemoteRequirement(
            File localRepoLocation,
            List<Repository> repos,
            List<Artifact> artifacts,
            File persistentPath,
            long consistencyCheckInterval) {
        super(persistentPath, consistencyCheckInterval);
        this.localRepoLocation = localRepoLocation;
        this.repos = repos;
        this.artifacts = artifacts;
    }

    @Override
    public boolean isConsistentWithRemote() {
        MavenHandler handler = new MavenHandler(localRepoLocation);
        Map<Artifact, String> currentVersions = new HashMap<>();
        for (Artifact a : artifacts) {
            String version = handler.getHighestLocalVersion(a);
            currentVersions.put(a, version);
        }
        if (currentVersions.keySet().size() != 0) {
            for (Artifact a : artifacts) {
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
