package build.pluto.buildmaven.dependency;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import build.pluto.builder.BuildUnitProvider;
import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Repository;
import build.pluto.buildmaven.util.MavenHandler;
import build.pluto.dependency.RemoteRequirement;

public class MavenRemoteRequirement extends RemoteRequirement {
    private static final long serialVersionUID = 531892600767381323L;
    
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
        Map<ArtifactConstraint, String> localVersions = new HashMap<>();
        for (ArtifactConstraint a : artifactConstraints) {
            String localVersion = handler.getHighestLocalVersion(a);
            if (localVersion != null) {
                localVersions.put(a, localVersion);
            }
        }
        // if there is no local version for any of the constraints
        if (localVersions.size() == 0) {
            return false;
        }
        for (ArtifactConstraint a : localVersions.keySet()) {
            String remoteVersion = handler.getHighestRemoteVersion(a, repos);
            String localVersion = localVersions.get(a);
            if (localVersion == null || !localVersion.equals(remoteVersion)) {
                return false;
            }
        }
        return true;
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
