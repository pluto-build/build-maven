package build.pluto.buildmaven.input;

import build.pluto.dependency.Origin;

import java.io.File;
import java.io.Serializable;

public class MavenPackagerInput implements Serializable {
  private static final long serialVersionUID = 3737339760071686995L;

  public final String jarName;
  public final File workingDir;
  public final File sourceDir;
  public final File testDir;
  public final boolean verbose;
  public final boolean skipTests;
  public final Origin pomOrigin;
  public final Origin sourceOrigin;

  private MavenPackagerInput(Builder builder){
    this.workingDir = builder.workingDir;
    this.sourceDir = builder.sourceDir;
    this.testDir = builder.testDir;
    this.jarName = builder.jarName;
    this.verbose = builder.verbose;
    this.pomOrigin = builder.pomOrigin;
    this.sourceOrigin = builder.sourceOrigin;
    this.skipTests = builder.skipTests;
  }

  @Override
  public String toString() {
    return "MavenPackagerInput(workingDir=" + workingDir + ", verbose=" + verbose + ", skipTests=" + skipTests + ", jarName=" + jarName + ")";
  }

  public static Builder Builder() { return new Builder(); }

  public static class Builder {
    private File workingDir;
    private File sourceDir = new File(workingDir, "src/main/java");
    private File testDir = new File(workingDir, "src/test/java");
    private String jarName;
    private Origin pomOrigin;
    private Origin sourceOrigin;
    private boolean verbose = false;
    private boolean skipTests = false;

    public MavenPackagerInput get() {
      return new MavenPackagerInput(this);
    }

    public Builder setWorkingDir(File workingDir) {
      this.workingDir = workingDir;
      return this;
    }

    public Builder setSourceDir(File sourceDir) {
      this.sourceDir = sourceDir;
      return this;
    }

    public Builder setTestDir(File testDir) {
      this.testDir = testDir;
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

    public Builder setSkipTests(boolean skipTests) {
      this.skipTests = skipTests;
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

