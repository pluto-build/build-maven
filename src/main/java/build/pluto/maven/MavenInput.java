package build.pluto.maven;

import java.io.File;
import java.io.Serializable;

public class MavenInput implements Serializable {
    public final String groupID;
    public final String artifactID;
    public final String version;
    public final File summaryLocation;
    public MavenInput (
            String groupID,
            String artifactID,
            String version,
            File summaryLocation) {
        this.groupID = groupID;
        this.artifactID = artifactID;
        this.version = version;
        this.summaryLocation = summaryLocation;
    }
}
