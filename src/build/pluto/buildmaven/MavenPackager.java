package build.pluto.buildmaven;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildmaven.input.MavenPackagerInput;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.sugarj.common.Exec;
import org.sugarj.common.Exec.ExecutionError;
import org.sugarj.common.Exec.ExecutionResult;
import org.sugarj.common.FileCommands;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

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
  private final String dirPrefix = "[DEBUG] adding directory ";
  private final String infoPrefix = "[INFO]";

  private String[] installDependencies(String[] outMsgs, boolean removeVerbose) {
    List<String> out = new ArrayList<>();
    for (String line : outMsgs) {
      boolean lineIsVerbose = true;
      if (line.startsWith(classfilePrefix)) {
        String fileName = line.substring(classfilePrefix.length());
        require(new File(fileName), LastModifiedStamper.instance);
      } else if (line.startsWith(dirPrefix)) {
        // TODO should we require directories?
        // dont think so because we require the files with the directories already
      } else if (line.startsWith(infoPrefix))
        lineIsVerbose = false;

      if (removeVerbose && !lineIsVerbose)
        out.add(line);
    }

    if (removeVerbose)
      return out.toArray(new String[out.size()]);
    else
      return outMsgs;
  }
}
