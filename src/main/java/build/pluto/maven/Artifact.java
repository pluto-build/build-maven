package build.pluto.maven;

import java.io.Serializable;

public class Artifact implements Serializable {
    public final String groupID;
    public final String artifactID;
    public final String version;
    public final String classifier;
    public final String extension;

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
}
