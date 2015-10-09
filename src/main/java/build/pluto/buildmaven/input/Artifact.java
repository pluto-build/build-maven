package build.pluto.buildmaven.input;

import java.io.Serializable;

public class Artifact implements Serializable {
    private static final long serialVersionUID = -2994151826680192229L;

    public final String groupID;
    public final String artifactID;
    public final String version;
    public final String classifier;
    public final String extension;

    /**
     * @param groupID is the id of the group for this artifactConstraint.
     * @param artifactID is the id of the artifactConstraint inside of the group.
     * @param version is the version which you want to deploy.
     * @param classifier is the classifier of the artifactConstraint, default is null.
     * @param extension is the extension of the artifactConstraint, default is jar.
     */
    public Artifact(
            String groupID,
            String artifactID,
            String version,
            String classifier,
            String extension) {
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.version = version;
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
        sb.append(version);
        return sb.toString();
    }
}
