package build.pluto.maven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dependency implements Serializable {
    public final Artifact artifact;
    public final List<Artifact> exclusions;

    public Dependency(Artifact artifact) {
        this.artifact = artifact;
        this.exclusions = new ArrayList<>();
    }

    public Dependency(
            Artifact artifact,
            List<Artifact> exclusions) {
        this.artifact = artifact;
        this.exclusions = exclusions;
    }
}
