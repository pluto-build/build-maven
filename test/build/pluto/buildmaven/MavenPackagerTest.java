package build.pluto.buildmaven;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.buildmaven.input.MavenPackagerInput;
import build.pluto.test.build.ScopedBuildTest;
import build.pluto.test.build.ScopedPath;
import build.pluto.test.build.TrackingBuildManager;

import org.junit.Test;
import org.sugarj.common.Exec.ExecutionResult;

import java.io.File;

import static junit.framework.Assert.assertEquals;

public class MavenPackagerTest extends ScopedBuildTest {
	@ScopedPath(value = "")
	private File rootDir;

	@Test
	public void testSimplePackage() throws Throwable {
		MavenPackagerInput input = new MavenPackagerInput.Builder()
				.setJarName("test")
				.setWorkingDir(rootDir.getParentFile())
				.get();
		ExecutionResult result = pack(input);
	}

	@Test
	public void testDoubleExecution() throws Throwable {
		MavenPackagerInput input = new MavenPackagerInput.Builder()
				.setJarName("test")
				.setWorkingDir(rootDir)
				.get();
		ExecutionResult result = pack(input);

		assertNoRun(input);
	}

	private ExecutionResult pack(MavenPackagerInput input) throws Throwable {
		return BuildManagers.build(new BuildRequest<>(MavenPackager.factory, input)).val();
	}

	public void assertNoRun(MavenPackagerInput input) throws Throwable {
		TrackingBuildManager m = new TrackingBuildManager();
		m.require(new BuildRequest<>(MavenPackager.factory, input));
		assertEquals(1, m.getRequiredInputs().size());
		assertEquals(0, m.getExecutedInputs().size());
	}
}
