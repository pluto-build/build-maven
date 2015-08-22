package build.pluto.maven.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dependency implements Serializable {
    public final ArtifactConstraint artifactConstraint;
    public final List<Exclusion> exclusions;

    /**
     * @param artifactConstraint is the artifactConstraint that the dependency wants to download.
     */
    public Dependency(ArtifactConstraint artifactConstraint) {
        this.artifactConstraint = artifactConstraint;
        this.exclusions = new ArrayList<>();
    }

    /**
     * @param artifactConstraint is the artifactConstraint that the dependency wants to download.
     * @param exclusions are the artifacts that the artifactConstraint depends on but you do
     * not want to download.
     */
    public Dependency(
            ArtifactConstraint artifactConstraint,
            List<Exclusion> exclusions) {
        this.artifactConstraint = artifactConstraint;
        this.exclusions = exclusions;
    }
}
