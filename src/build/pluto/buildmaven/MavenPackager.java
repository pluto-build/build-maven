package build.pluto.buildmaven;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.sugarj.common.Exec;
import org.sugarj.common.Exec.ExecutionError;
import org.sugarj.common.Exec.ExecutionResult;
import org.sugarj.common.FileCommands;

import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.builder.factory.BuilderFactoryFactory;
import build.pluto.buildmaven.input.MavenPackagerInput;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;

public class MavenPackager extends Builder<MavenPackagerInput, Out<ExecutionResult>> {
  public static BuilderFactory<MavenPackagerInput, Out<ExecutionResult>, MavenPackager> factory = BuilderFactoryFactory.of(MavenPackager.class, MavenPackagerInput.class);

  public MavenPackager(MavenPackagerInput input) {
    super(input);
  }

  @Override
  protected String description(MavenPackagerInput input) {
    return "Maven package " + input.jarName;

  }

  @Override
  public File persistentPath(MavenPackagerInput input) {
    return new File(input.workingDir, "target/" + input.jarName + ".jar.dep");
  }

  @Override
  protected Out<ExecutionResult> build(MavenPackagerInput input)
      throws Throwable {
    List<String> command = new ArrayList<>();
    command.add("mvn");
    command.add("package");
    command.add("--batch-mode");
    if(input.skipTests)
      command.add("-DskipTests");

    command.add("-Dmaven.compiler.verbose");
    command.add("-Djar.finalName=" + input.jarName);
    command.add("-X"); // debug output to track required files

    requireBuild(input.sourceOrigin);
    requireBuild(input.pomOrigin);

    // need to search them because maven debug does only print stale files
    // builder would not require in the first but in the second execution
    FileFilter javaFilter = new SuffixFileFilter(".java");
    List<File> sourceFiles = FileCommands.listFilesRecursive(input.sourceDir, javaFilter);
    for (File f : sourceFiles)
      require(f, LastModifiedStamper.instance);

    if (!input.skipTests) {
      List<File> testFiles = FileCommands.listFilesRecursive(input.testDir, javaFilter);
      for (File f : testFiles)
        require(f, LastModifiedStamper.instance);
    }

    try {
      ExecutionResult result = Exec.run(input.workingDir, command.toArray(new String[command.size()]));
      String[] outMsgs = installDependencies(result.outMsgs, !input.verbose);
      provide(new File(input.workingDir, "target/" + input.jarName + ".jar"));
      return OutputPersisted.of(new Exec.ExecutionResult(result.cmds, outMsgs, result.errMsgs));
    } catch (ExecutionError e) {
      String[] outMsgs = installDependencies(e.outMsgs, !input.verbose);
      throw e;
    }
  }

  private final String classfilePrefix = "[DEBUG] adding entry ";
  private final String classpathPrefix = "[DEBUG]   (f) classpathElements = [";
  private final String infoPrefix = "[INFO]";

  private String[] installDependencies(String[] outMsgs, boolean removeVerbose) {
    List<String> out = new ArrayList<>();
    Set<File> classpath = new HashSet<>();

    for (String line : outMsgs) {
      boolean lineIsVerbose = true;
      if (line.startsWith(classfilePrefix)) {
        String fileName = line.substring(classfilePrefix.length());
        require(new File(fileName), LastModifiedStamper.instance);
      } else if (line.startsWith(classpathPrefix)) {
        //collect classpath because we do not want to require the same file twice
        String[] cp = line.substring(classpathPrefix.length(), line.length() - 1).split(",");
        for (String s : cp) {
          // do not want to include target/classes and target/test-classes
          File f = new File(s);
          if (FileCommands.getExtension(f) != null)
            classpath.add(f);
        }
      } else if (line.startsWith(infoPrefix))
        lineIsVerbose = false;

      if (removeVerbose && !lineIsVerbose)
        out.add(line);
    }

    // require collected classpath
    for (File f : classpath)
      require(f, LastModifiedStamper.instance);

    if (removeVerbose)
      return out.toArray(new String[out.size()]);
    else
      return outMsgs;
  }
}
