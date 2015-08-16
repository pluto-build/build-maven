package build.pluto.maven;

import java.io.Serializable;

import org.eclipse.aether.repository.RepositoryPolicy;

public class Repository implements Serializable {

    public final String id;
    public final String url;
    public final String name;
    public final String layout;

    public final Policy snapshotPolicy;
    public final Policy releasePolicy;

    public class Policy {
        public final boolean enabled;
        // this field gets covered by the checkConsistencyInterval in
        // RemoteRequirement, therefore everytime the dependencies get
        // resolved it needs to be checked.
        public final String updatePolicy = RepositoryPolicy.UPDATE_POLICY_ALWAYS;
        public final String checksumPolicy;

        /**
         * @param enabled is repo enabled for this policy
         * @param checksumPolicy fail, ignore or warn
         */
        public Policy(boolean enabled, String checksumPolicy) {
            this.enabled = enabled;
            this.checksumPolicy = checksumPolicy;
        }

        public RepositoryPolicy transform() {
            return new RepositoryPolicy(enabled,
                    RepositoryPolicy.UPDATE_POLICY_ALWAYS,
                    checksumPolicy);
        }
    }

    /**
     * @param id id of the repository
     * @param url location of the repository
     * @param name name of the repository
     * @param layout default or legacy (Maven 1.x)
     * @param snapshotPolicy policy for snapshots
     * @param releasePolicy policy for releases
     */
    public Repository(String id,
            String url,
            String name,
            String layout,
            Policy snapshotPolicy,
            Policy releasePolicy) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.layout = layout;
        this.snapshotPolicy = snapshotPolicy;
        this.releasePolicy = releasePolicy;
    }
}

