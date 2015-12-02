package build.pluto.buildmaven.input;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MavenInput implements Serializable {
    private static final long serialVersionUID = -813263267417646266L;

    public final File localRepoLocation;
    public final List<Dependency> dependencyList;
    public final List<Repository> repositoryList;

    private MavenInput(Builder builder) {
        this.localRepoLocation = builder.localRepoLocation;
        this.dependencyList = builder.dependencyList;
        this.repositoryList = builder.repositoryList;
    }

    public static class Builder {
        private File localRepoLocation;

        private List<Dependency> dependencyList;

        private List<Repository> repositoryList;

        /**
         * @param localRepoLocation where the artifacts that get downloaded
         * are saved
         * @param dependencyList the dependencies that you want to get resolved
         */
        public Builder (List<Dependency> dependencyList) {
            this(new File(System.getProperty("user.home"), ".m2/repository"), dependencyList);
        }

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
         * Set repositories to use for resolving the artifacts.
         * @param repositoryList list of repositories
         */
        public Builder setRepositoryList(List<Repository> repositoryList) {
            this.repositoryList = repositoryList;
            return this;
        }
        
        /**
         * Add repository to use for resolving the artifacts.
         */
        public Builder addRepository(Repository repository) {
            this.repositoryList.add(repository);
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
