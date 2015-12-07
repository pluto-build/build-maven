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
        return artifactConstraint.toString();
    }

    /**
     * Generated using Eclipse.
     */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((artifactConstraint == null) ? 0 : artifactConstraint.hashCode());
		result = prime
				* result
				+ (int) (consistencyCheckInterval ^ (consistencyCheckInterval >>> 32));
		result = prime * result
				+ ((exclusions == null) ? 0 : exclusions.hashCode());
		result = prime * result + (optional ? 1231 : 1237);
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

    /**
     * Generated using Eclipse.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dependency other = (Dependency) obj;
		if (artifactConstraint == null) {
			if (other.artifactConstraint != null)
				return false;
		} else if (!artifactConstraint.equals(other.artifactConstraint))
			return false;
		if (consistencyCheckInterval != other.consistencyCheckInterval)
			return false;
		if (exclusions == null) {
			if (other.exclusions != null)
				return false;
		} else if (!exclusions.equals(other.exclusions))
			return false;
		if (optional != other.optional)
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}
    
}
