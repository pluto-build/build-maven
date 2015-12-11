package build.pluto.buildmaven;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;

import org.sugarj.common.Exec;
import org.sugarj.common.Exec.ExecutionError;
import org.sugarj.common.Exec.ExecutionResult;

import java.io.File;
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
  protected Out<ExecutionResult> build(MavenPackagerInput input) throws Throwable {
    String command = String.format("mvn package --batch-mode -DskipTests -Djar.finalName=%s -X", input.jarName);
    requireBuild(input.classOrigin);
    requireBuild(input.pomOrigin);

    try {
      ExecutionResult result = Exec.run(input.workingDir, command.split(" "));
      String[] outMsgs = installDependencies(result.outMsgs, !input.verbose);
      provide(new File(input.workingDir, "target/" + input.jarName + ".jar"));
      return OutputPersisted.of(new Exec.ExecutionResult(result.cmds, outMsgs, result.errMsgs));
    } catch (ExecutionError e) {
      String[] outMsgs = installDependencies(e.outMsgs, !input.verbose);
      throw e;
    }
  }

  private final String filePrefix = "[DEBUG] adding entry ";
  private final String dirPrefix = "[DEBUG] adding directory ";
  private final String infoPrefix = "[INFO]";
  private String[] installDependencies(String[] outMsgs, boolean removeVerbose) {
    List<String> out = new ArrayList<>();
    for(String line : outMsgs) {
      boolean lineIsVerbose = true;
      if (line.startsWith(filePrefix)) {
        String fileName = line.substring(filePrefix.length());
        require(new File(fileName), LastModifiedStamper.instance);
      } else if(line.startsWith(dirPrefix)) {
        // TODO should we require directories?
        // dont think so because we require the files with the directories already
      } else if (line.startsWith(infoPrefix))
        lineIsVerbose = false;

      if (removeVerbose && !lineIsVerbose) {
        out.add(line);
      }
    }

    if(removeVerbose)
      return out.toArray(new String[out.size()]);
    else
      return outMsgs;
  }
}
