package build.pluto.buildmaven.input;

import java.io.Serializable;

public class ArtifactConstraint implements Serializable {
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
}
