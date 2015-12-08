package build.pluto.buildmaven.input;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import build.pluto.buildmaven.MavenHandler;

public class MavenInput implements Serializable {
    private static final long serialVersionUID = -813263267417646266L;

    public final File localRepoLocation;
    public final List<Dependency> dependencyList;
    public final List<Repository> repositoryList;

    private MavenInput(Builder builder) {
        this.localRepoLocation = builder.localRepoLocation != null ? builder.localRepoLocation : MavenHandler.DEFAULT_LOCAL;
        this.dependencyList = Collections.unmodifiableList(builder.dependencyList);
        this.repositoryList = Collections.unmodifiableList(builder.repositoryList);
    }

    public static class Builder {
        private File localRepoLocation;

        private List<Dependency> dependencyList;

        private List<Repository> repositoryList;

        public Builder() {
            this.dependencyList = new ArrayList<>();
            this.repositoryList = new ArrayList<>();
        }

        /**
         * @param localRepoLocation where the artifacts that get downloaded are saved
         * @return 
         */
        public Builder setLocalRepoLocation(File localRepoLocation) {
			this.localRepoLocation = localRepoLocation;
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
         * Add repository to use for resolving the artifacts.
         */
        public Builder addRepository(Repository repository) {
            this.repositoryList.add(repository);
            return this;
        }
        
        /**
         * Set repositories to use for resolving the artifacts.
         * @param repositoryList list of repositories
         */
        public Builder setDependencyList(List<Dependency> dependencyList) {
            this.dependencyList = dependencyList;
            return this;
        }
        
        /**
         * Add repository to use for resolving the artifacts.
         */
        public Builder addDependency(Dependency dependency) {
            this.dependencyList.add(dependency);
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
