package build.pluto.buildmaven.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.util.artifact.JavaScopes;

public class Dependency implements Serializable {
    private static final long serialVersionUID = -6947544476611313880L;

    public final ArtifactConstraint artifactConstraint;
    public final List<Exclusion> exclusions;
    public final long consistencyCheckInterval;
    public final String scope;
    public final boolean optional;

    /**
     * @param artifactConstraint is the artifactConstraint that the dependency wants to download.
     */
    public Dependency(ArtifactConstraint artifactConstraint, long consistencyCheckInterval) {
        this(artifactConstraint, new ArrayList<Exclusion>(), consistencyCheckInterval);
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
        this(artifactConstraint, exclusions, JavaScopes.COMPILE, false, consistencyCheckInterval);
    }

    /**
     * @param artifactConstraint is the artifactConstraint that the dependency wants to download.
     * @param exclusions are the artifacts that the artifactConstraint depends on but you do
     * not want to download.
     */
    public Dependency(
            ArtifactConstraint artifactConstraint,
            List<Exclusion> exclusions,
            String scope,
            boolean optional,
            long consistencyCheckInterval) {
        this.artifactConstraint = artifactConstraint;
        this.exclusions = exclusions;
        this.scope = scope;
        this.optional = optional;
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
