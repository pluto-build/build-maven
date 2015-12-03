package build.pluto.buildmaven.input;

import java.io.Serializable;

import org.eclipse.aether.repository.RepositoryPolicy;

public class Repository implements Serializable {
    private static final long serialVersionUID = 4329409646442649789L;

    public final static Policy ENABLED_DEFAULT_POLICY = new Policy(true, "warn");
    public final static Policy DISABLED_DEFAULT_POLICY = new Policy(false, "warn");
    
    public final String id;
    public final String url;
    public final String layout;

    public final Policy snapshotPolicy;
    public final Policy releasePolicy;

    public static class Policy {
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
     * @param layout default or legacy (Maven 1.x)
     * @param snapshotPolicy policy for snapshots
     * @param releasePolicy policy for releases
     */
    public Repository(String id,
            String url,
            String layout,
            Policy snapshotPolicy,
            Policy releasePolicy) {
        this.id = id;
        this.url = url;
        this.layout = layout;
        this.snapshotPolicy = snapshotPolicy;
        this.releasePolicy = releasePolicy;
    }
}

