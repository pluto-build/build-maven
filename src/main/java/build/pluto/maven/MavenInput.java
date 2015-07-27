package build.pluto.maven;

import java.io.File;
import java.io.Serializable;

public class MavenInput implements Serializable {

    private static final long serialVersionUID = 89438938480L;

    public final String groupID;
    public final String artifactID;
    public final String version;
    public final String type;
    public final String classifier;

    public final long consistencyCheckInterval;
    public final File summaryLocation;


    private MavenInput(Builder builder) {
        this.groupID = builder.groupID;
        this.artifactID = builder.artifactID;
        this.version = builder.version;
        this.summaryLocation = builder.summaryLocation;
        this.type = builder.type;
        this.classifier = builder.classifier;
        this.consistencyCheckInterval = builder.consistencyCheckInterval;
    }

    public static class Builder {
        private String groupID;
        private String artifactID;
        private String version;
        private File summaryLocation;

        private String type = "jar";
        private String classifier = null;

        private long consistencyCheckInterval = 0;

        public Builder (
                String groupID,
                String artifactID,
                String version,
                File summaryLocation) {
            this.groupID = groupID;
            this.artifactID = artifactID;
            this.version = version;
            this.summaryLocation = summaryLocation;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setClassifier(String classifier) {
            this.classifier = classifier;
            return this;
        }

        public Builder setConsistencyCheckInterval(long consistencyCheckInterval) {
            this.consistencyCheckInterval = consistencyCheckInterval;
            return this;
        }

        public MavenInput build() {
            return new MavenInput(this);
        }
    }
}
