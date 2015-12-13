package build.pluto.buildmaven;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sugarj.common.Exec;
import org.sugarj.common.Exec.ExecutionResult;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.buildmaven.input.MavenPackagerInput;
import build.pluto.test.build.ScopedBuildTest;
import build.pluto.test.build.ScopedPath;
import build.pluto.test.build.TrackingBuildManager;
import org.sugarj.common.FileCommands;

public class MavenPackagerTest extends ScopedBuildTest {
	@ScopedPath(value = "")
	private File rootDir;

	@ScopedPath(value = "A.java")
	private File classASource;
	@ScopedPath(value = "B.java")
	private File classBSource;

	@Before
	public void clean() {
		Exec.run(rootDir, "mvn clean".split(" "));
	}

	@Test
	public void testCleanBuild() throws Throwable {
		MavenPackagerInput input = new MavenPackagerInput
				.Builder()
				.setJarName("test")
				.setVerbose(true)
				.setSourceDir(rootDir)
				.setWorkingDir(rootDir)
				.get();
		assertRun(input);
	}

	@Test
	public void testNoRebuild() throws Throwable {
		MavenPackagerInput input = new MavenPackagerInput
				.Builder()
				.setJarName("test")
				.setVerbose(true)
				.setSourceDir(rootDir)
				.setWorkingDir(rootDir)
				.get();
		assertRun(input);
		assertNoRun(input);
	}

	@Test
	public void testRebuildAfterChanges() throws Throwable {
		MavenPackagerInput input = new MavenPackagerInput
				.Builder()
				.setJarName("test")
				.setVerbose(true)
				.setSourceDir(rootDir)
				.setWorkingDir(rootDir)
				.get();
		assertRun(input);

		//change source file
		FileCommands.writeToFile(classASource,
				"class A { public static void main(String[] args) { System.out.println(\"Altered Class A has run.\"); } }");

		assertRun(input);
	}

	public void assertRun(MavenPackagerInput input) throws Throwable {
		TrackingBuildManager m = new TrackingBuildManager();
		m.require(new BuildRequest<>(MavenPackager.factory, input));
		assertEquals(1, m.getRequiredInputs().size());
		assertEquals(1, m.getExecutedInputs().size());
	}

	public void assertNoRun(MavenPackagerInput input) throws Throwable {
		TrackingBuildManager m = new TrackingBuildManager();
		m.require(new BuildRequest<>(MavenPackager.factory, input));
		assertEquals(1, m.getRequiredInputs().size());
		assertEquals(0, m.getExecutedInputs().size());
	}
}
