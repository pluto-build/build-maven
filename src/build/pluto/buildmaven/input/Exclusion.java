package build.pluto.buildmaven.input;

import java.io.Serializable;

public class Exclusion implements Serializable {
    private static final long serialVersionUID = 717645477131460803L;

    public final String groupID;
    public final String artifactID;
    public final String classifier;
    public final String extension;

    /**
     * @param groupID is the id of the group for this exclusion.
     * @param artifactID is the id of the exclusion inside of the group.
     * @param classifier is the classifier of the exclusion, default is null.
     * @param extension is the extension of the exclusion, default is jar.
     */
    public Exclusion(
            String groupID,
            String artifactID,
            String classifier,
            String extension) {
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.classifier = classifier;
        this.extension = extension;
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
		Exclusion other = (Exclusion) obj;
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
		return true;
	}
}
