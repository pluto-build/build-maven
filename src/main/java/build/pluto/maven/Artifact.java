package build.pluto.maven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Artifact implements Serializable {
    public final String groupID;
    public final String artifactID;
    public final String versionConstraint;
    public final String classifier;
    public final String extension;
    public final List<Artifact> exclusions;
    public final boolean optional;

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
        this.exclusions = new ArrayList<>();
        this.optional = false;
    }

    public Artifact(
            String groupID,
            String artifactID,
            String versionConstraint,
            String classifier,
            String extension,
            List<Artifact> exclusions,
            boolean optional) {
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.versionConstraint = versionConstraint;
        this.classifier = classifier;
        this.extension = extension;
        this.exclusions = exclusions;
        this.optional = optional;
    }
}
