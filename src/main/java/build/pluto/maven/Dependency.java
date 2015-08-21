package build.pluto.maven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dependency implements Serializable {
    public final ArtifactConstraint artifactConstraint;
    public final List<ArtifactConstraint> exclusions;

    public Dependency(ArtifactConstraint artifactConstraint) {
        this.artifactConstraint = artifactConstraint;
        this.exclusions = new ArrayList<>();
    }

    public Dependency(
            ArtifactConstraint artifactConstraint,
            List<ArtifactConstraint> exclusions) {
        this.artifactConstraint = artifactConstraint;
        this.exclusions = exclusions;
    }
}
