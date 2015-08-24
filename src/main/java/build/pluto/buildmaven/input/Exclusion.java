package build.pluto.buildmaven.input;

import java.io.Serializable;

public class Exclusion implements Serializable {
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
}
