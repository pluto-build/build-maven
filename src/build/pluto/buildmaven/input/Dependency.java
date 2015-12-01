package build.pluto.buildmaven.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dependency implements Serializable {
    private static final long serialVersionUID = -6947544476611313880L;

    public final ArtifactConstraint artifactConstraint;
    public final List<Exclusion> exclusions;
    public final long consistencyCheckInterval;

    /**
     * @param artifactConstraint is the artifactConstraint that the dependency wants to download.
     */
    public Dependency(ArtifactConstraint artifactConstraint) {
        this(artifactConstraint, new ArrayList<Exclusion>(), 0L);
    }

    /**
     * @param artifactConstraint is the artifactConstraint that the dependency wants to download.
     * @param exclusions are the artifacts that the artifactConstraint depends on but you do
     * not want to download.
     */
    public Dependency(
            ArtifactConstraint artifactConstraint,
            List<Exclusion> exclusions) {
        this(artifactConstraint, exclusions, 0L);
    }

    /**
     * @param artifactConstraint is the artifactConstraint that the dependency wants to download.
     * @param exclusions are the artifacts that the artifactConstraint depends on but you do
     * not want to download.
     */
    public Dependency(
            ArtifactConstraint artifactConstraint,
            List<Exclusion> exclusions,
            long consistencyCheckInterval) {
        this.artifactConstraint = artifactConstraint;
        this.exclusions = exclusions;
        this.consistencyCheckInterval = consistencyCheckInterval;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(artifactConstraint.toString());
        //exclusion is not important
        sb.append(consistencyCheckInterval);
        return sb.toString();
    }
}
