package build.pluto.maven;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MavenInput implements Serializable {

    private static final long serialVersionUID = 89438938480L;

    public final File localRepoLocation;

    public final List<Dependency> dependencyList;

    public final List<Repository> repositoryList;

    public final long consistencyCheckInterval;
    public final File summaryLocation;

    private MavenInput(Builder builder) {
        this.localRepoLocation = builder.localRepoLocation;
        this.dependencyList = builder.dependencyList;
        this.repositoryList = builder.repositoryList;
        this.summaryLocation = builder.summaryLocation;
        this.consistencyCheckInterval = builder.consistencyCheckInterval;
    }

    public static class Builder {
        private File localRepoLocation;

        private List<Dependency> dependencyList;

        private List<Repository> repositoryList;

        private long consistencyCheckInterval = 0;
        private File summaryLocation;

        /**
         * @param localRepoLocation where the artifacts that are downloaded
         * are saved
         * @param dependencyList the artifacts that you want to get resolved
         * @param summaryLocation where the summary of the builder that uses
         * this Input is located
         */
        public Builder (
                File localRepoLocation,
                List<Dependency> dependencyList,
                File summaryLocation) {
            this.localRepoLocation = localRepoLocation;
            this.dependencyList = dependencyList;
            this.repositoryList = new ArrayList<>();
            this.summaryLocation = summaryLocation;
        }

        /**
         * Sets the interval in which the consistency with the remote is
         * checked.
         * @param consistencyCheckInterval interval in milliseconds
         */
        public Builder setConsistencyCheckInterval(long consistencyCheckInterval) {
            this.consistencyCheckInterval = consistencyCheckInterval;
            return this;
        }


        /**
         * Set repositories to use for resolving the artifacts.
         * @param repositoryList list of repositories
         */
        public Builder setRepositoryList(List<Repository> repositoryList) {
            this.repositoryList = repositoryList;
            return this;
        }

        /**
         * @returns MavenInput that got configured by this class.
         */
        public MavenInput build() {
            return new MavenInput(this);
        }
    }
}
