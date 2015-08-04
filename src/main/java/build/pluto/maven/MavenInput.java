package build.pluto.maven;

import build.pluto.maven.Artifact;

import java.io.File;
import java.io.Serializable;

public class MavenInput implements Serializable {

    private static final long serialVersionUID = 89438938480L;

    public final File localRepoLocation;

    public final Artifact artifact;

    public final long consistencyCheckInterval;
    public final File summaryLocation;


    private MavenInput(Builder builder) {
        this.localRepoLocation = builder.localRepoLocation;
        this.artifact = builder.artifact;
        this.summaryLocation = builder.summaryLocation;
        this.consistencyCheckInterval = builder.consistencyCheckInterval;
    }

    public static class Builder {
        private File localRepoLocation;

        private Artifact artifact;

        private long consistencyCheckInterval = 0;
        private File summaryLocation;

        public Builder (
                File localRepoLocation,
                Artifact artifact,
                File summaryLocation) {
            this.localRepoLocation = localRepoLocation;
            this.artifact = artifact;
            this.summaryLocation = summaryLocation;
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
