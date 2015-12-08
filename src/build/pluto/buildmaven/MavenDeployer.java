package build.pluto.buildmaven;

import java.io.File;
import java.io.Serializable;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.buildmaven.input.Artifact;
import build.pluto.buildmaven.input.Repository;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.stamp.FileHashStamper;

public class MavenDeployer extends Builder<MavenDeployer.Input, None> {

    public static BuilderFactory<Input, None, MavenDeployer> factory = new BuilderFactory<Input, None, MavenDeployer>() {
		private static final long serialVersionUID = -6191333145182092802L;

		@Override
		public MavenDeployer makeBuilder(Input input) {
			return new MavenDeployer(input);
		}

		@Override
		public boolean isOverlappingGeneratedFileCompatible(File overlap, Serializable input, BuilderFactory<?, ?, ?> otherFactory, Serializable otherInput) {
			return this.getClass().isInstance(otherFactory);
		}
	};

    public MavenDeployer(Input input) {
        super(input);
    }

    public static class Input implements Serializable {
    	private static final long serialVersionUID = 8822520971638720547L;
    	
		public final Artifact artifact;
        public final File artifactLocation;
        public final Origin artifactOrigin;
        public final File pomLocation;
        public final Origin pomOrigin;
        public final File localRepository;
        public final Repository deployRepo;
        
        /**
         * @param artifact Artifact description.
         * @param artifactLocation Artifact JAR file.
         * @param artifactOrigin
         * @param pomLocation Pom file.
         * @param pomOrigin
         * @param localRepository Local repository.
         * @param deployRepo Repository to deploy to.
         */
        public Input(Artifact artifact, File artifactLocation, Origin artifactOrigin, File pomLocation, Origin pomOrigin, File localRepository, Repository deployRepo) {
			this.artifact = artifact;
			this.artifactLocation = artifactLocation;
			this.artifactOrigin = artifactOrigin;
			this.pomLocation = pomLocation;
			this.pomOrigin = pomOrigin;
			this.localRepository = localRepository;
			this.deployRepo = deployRepo;
		}
    }
    
    @Override
    protected String description(Input input) {
    	return "Maven deploy " + input.artifact;
    }

    @Override
    public File persistentPath(Input input) {
        return new File(input.localRepository, ".pluto/deploy." + input.deployRepo.id + ":" + input.artifact.artifactID + ".dep");
    }

    @Override
    protected None build(Input input) throws Throwable {
    	requireBuild(input.pomOrigin);
    	require(input.pomLocation, FileHashStamper.instance);
    	requireBuild(input.artifactOrigin);
    	require(input.artifactLocation, FileHashStamper.instance); // use hash to avoid redeploy
    	
    	MavenHandler handler = new MavenHandler(input.localRepository);
    	handler.deployArtifact(input.artifact, input.artifactLocation, input.pomLocation, input.deployRepo);
        return null;
    }
}
