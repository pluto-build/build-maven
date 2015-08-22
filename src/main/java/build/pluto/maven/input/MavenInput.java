package build.pluto.maven.input;

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

    private MavenInput(Builder builder) {
        this.localRepoLocation = builder.localRepoLocation;
        this.dependencyList = builder.dependencyList;
        this.repositoryList = builder.repositoryList;
        this.consistencyCheckInterval = builder.consistencyCheckInterval;
    }

    public static class Builder {
        private File localRepoLocation;

        private List<Dependency> dependencyList;

        private List<Repository> repositoryList;

        private long consistencyCheckInterval = 0;

        /**
         * @param localRepoLocation where the artifacts that get downloaded
         * are saved
         * @param dependencyList the dependencies that you want to get resolved
         */
        public Builder (
                File localRepoLocation,
                List<Dependency> dependencyList) {
            this.localRepoLocation = localRepoLocation;
            this.dependencyList = dependencyList;
            this.repositoryList = new ArrayList<>();
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
