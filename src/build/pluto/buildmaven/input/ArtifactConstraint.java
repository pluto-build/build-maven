package build.pluto.buildmaven.input;

import java.io.Serializable;

public class ArtifactConstraint implements Serializable {
    private static final long serialVersionUID = 2283901698966843801L;

    public final String groupID;
    public final String artifactID;
    public final String versionConstraint;
    public final String classifier;
    public final String extension;

    /**
     * @param groupID is the id of the group for this artifactConstraint.
     * @param artifactID is the id of the artifactConstraint inside of the group.
     * @param versionConstraint is the version constraint which you want to have.
     * @param classifier is the classifier of the artifactConstraint, default is null.
     * @param extension is the extension of the artifactConstraint, default is jar.
     */
    public ArtifactConstraint(
            String groupID,
            String artifactID,
            String versionConstraint,
            String classifier,
            String extension) {
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.versionConstraint = versionConstraint;
        this.classifier = classifier;
        this.extension = extension;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(groupID).append(":");
        sb.append(artifactID);
        if (extension != null) {
            sb.append(":");
            sb.append(extension);
        }
        if (classifier != null) {
            sb.append(":");
            sb.append(classifier);
        }
        sb.append(":");
        sb.append(versionConstraint);
        return sb.toString();
    }

	/**
     * Generated using Eclipse.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifactID == null) ? 0 : artifactID.hashCode());
		result = prime * result
				+ ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result
				+ ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + ((groupID == null) ? 0 : groupID.hashCode());
		result = prime
				* result
				+ ((versionConstraint == null) ? 0 : versionConstraint
						.hashCode());
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
		ArtifactConstraint other = (ArtifactConstraint) obj;
		if (artifactID == null) {
			if (other.artifactID != null)
				return false;
		} else if (!artifactID.equals(other.artifactID))
			return false;
		if (classifier == null) {
			if (other.classifier != null)
				return false;
		} else if (!classifier.equals(other.classifier))
			return false;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;
		if (groupID == null) {
			if (other.groupID != null)
				return false;
		} else if (!groupID.equals(other.groupID))
			return false;
		if (versionConstraint == null) {
			if (other.versionConstraint != null)
				return false;
		} else if (!versionConstraint.equals(other.versionConstraint))
			return false;
		return true;
	}
}
