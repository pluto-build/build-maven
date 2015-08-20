package build.pluto.maven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dependency implements Serializable {
    public final Artifact artifact;
    public final List<Artifact> exclusions;
    public final boolean optional;

    public Dependency(Artifact artifact) {
        this.artifact = artifact;
        this.exclusions = new ArrayList<>();
        this.optional = false;
    }

    public Dependency(
            Artifact artifact,
            List<Artifact> exclusions,
            boolean optional) {
        this.artifact = artifact;
        this.exclusions = exclusions;
        this.optional = optional;
    }
}
