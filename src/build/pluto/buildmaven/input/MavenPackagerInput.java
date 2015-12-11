package build.pluto.buildmaven.input;

import build.pluto.dependency.Origin;

import java.io.File;
import java.io.Serializable;

public class MavenPackagerInput implements Serializable {
  private static final long serialVersionUID = 3737339760071686995L;

  public final String jarName;
  public final File workingDir;
  public final boolean verbose;
  public final Origin pomOrigin;
  public final Origin sourceOrigin;

  private MavenPackagerInput(Builder builder){
    this.workingDir = builder.workingDir;
    this.jarName = builder.jarName;
    this.verbose = builder.verbose;
    this.pomOrigin = builder.pomOrigin;
    this.sourceOrigin = builder.sourceOrigin;
  }

  public static Builder Builder() { return new Builder(); }

  public static class Builder {
    private File workingDir;
    private String jarName;
    private Origin pomOrigin;
    private Origin sourceOrigin;
    private boolean verbose = false;

    public MavenPackagerInput get() {
      return new MavenPackagerInput(this);
    }

    public Builder setWorkingDir(File workingDir) {
      this.workingDir = workingDir;
      return this;
    }

    public Builder setJarName(String jarName) {
      this.jarName = jarName;
      return this;
    }

    public Builder setVerbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    public Builder setPomOrigin(Origin pomOrigin) {
      this.pomOrigin = pomOrigin;
      return this;
    }

    public Builder setSourceOrigin(Origin sourceOrigin) {
      this.sourceOrigin = sourceOrigin;
      return this;
    }
  }
}

