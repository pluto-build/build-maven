package build.pluto.maven;

public class Artifact {
    public final String groupID;
    public final String artifactID;
    public final String versionConstraint;
    public final String classifier;
    public final String extension;

    public Artifact(
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
}
