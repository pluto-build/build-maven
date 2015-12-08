package build.pluto.buildmaven;

import java.io.File;
import java.io.Serializable;

import org.sugarj.common.FileCommands;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.buildmaven.input.Artifact;
import build.pluto.output.None;
import build.pluto.stamp.FileHashStamper;

public class PomGenerator extends Builder<PomGenerator.Input, None> {

    public static BuilderFactory<Input, None, PomGenerator> factory = new BuilderFactory<Input, None, PomGenerator>() {
		private static final long serialVersionUID = -6191333145182092802L;

		@Override
		public PomGenerator makeBuilder(Input input) {
			return new PomGenerator(input);
		}

		@Override
		public boolean isOverlappingGeneratedFileCompatible(File overlap, Serializable input, BuilderFactory<?, ?, ?> otherFactory, Serializable otherInput) {
			return this.getClass().isInstance(otherFactory);
		}
	};

    public PomGenerator(Input input) {
        super(input);
    }

    public static class Input implements Serializable {
    	private static final long serialVersionUID = 8822520971638720547L;
    	
		public final Artifact artifact;
        public final File pomFile;
        
        /**
         * @param artifact Artifact description.
         * @param pomFile Pom file.
         */
        public Input(Artifact artifact, File pomFile) {
			this.artifact = artifact;
			this.pomFile = pomFile;
		}
    }
    
    @Override
    protected String description(Input input) {
    	return "Generate pom " + input.pomFile;
    }

    @Override
    public File persistentPath(Input input) {
        return new File(input.pomFile.getAbsolutePath() + ".dep");
    }

    @Override
    protected None build(Input input) throws Throwable {
    	String pomTemplate = 
	    	"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
	    	"        xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
	    	"    <modelVersion>4.0.0</modelVersion>\n" +
	    	"    <packaging>jar</packaging>\n" +
	    	"    <groupId>%s</groupId>\n" +
	    	"    <artifactId>%s</artifactId>\n" +
	    	"    <version>%s</version>\n" +
	    	"</project>\n";
    	
    	String pomContent = String.format(pomTemplate, 
    			input.artifact.groupID,
    			input.artifact.artifactID,
    			input.artifact.version);
    	
    	FileCommands.writeToFile(input.pomFile, pomContent);
    	provide(input.pomFile, FileHashStamper.instance);
    	
        return null;
    }
}
