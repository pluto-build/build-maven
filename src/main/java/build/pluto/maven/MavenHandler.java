package build.pluto.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;

public class MavenHandler {
    private RemoteRepository remote;
    private LocalRepository local;
    private RepositorySystem system;
    private DefaultRepositorySystemSession session;

    public MavenHandler(
            File localRepoLocation,
            String remoteID,
            String remoteType,
            String remoteURL) {
        this.local = new LocalRepository(localRepoLocation);
        this.remote =
            new RemoteRepository.Builder(remoteID, remoteType, remoteURL).build();
        this.system = this.newRepositorySystem();
        this.session = MavenRepositorySystemUtils.newSession();
        this.session.setLocalRepositoryManager(
                system.newLocalRepositoryManager(session, local));
    }

    public MavenHandler(File localRepoLocation) {
        this.local = new LocalRepository(localRepoLocation);
        this.remote = new RemoteRepository.Builder(
                "central",
                "default",
                "http://central.maven.org/maven2/").build();
        this.system = this.newRepositorySystem();
        this.session = MavenRepositorySystemUtils.newSession();
        this.session.setLocalRepositoryManager(
                system.newLocalRepositoryManager(session, local));
    }

    private RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator =
            MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(
                RepositoryConnectorFactory.class,
                BasicRepositoryConnectorFactory.class );
        locator.addService(
                TransporterFactory.class,
                FileTransporterFactory.class);
        locator.addService(
                TransporterFactory.class,
                HttpTransporterFactory.class);
        return locator.getService( RepositorySystem.class );
    }

    public List<File> resolveDependencies(
            String groupID,
            String artifactID,
            String classifier,
            String version) throws DependencyResolutionException {

        Artifact artifact =
            new DefaultArtifact(groupID+":"+artifactID+":"+version);
        DependencyFilter dependencyFilter =
            DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(Arrays.asList(this.remote));

        DependencyRequest dependencyRequest =
            new DependencyRequest(collectRequest, dependencyFilter);

        List<ArtifactResult> artifactResultList =
            system.resolveDependencies(session, dependencyRequest)
                  .getArtifactResults();
        List<File> locationList = new ArrayList<>();
        for (ArtifactResult ar : artifactResultList) {
            Artifact a = ar.getArtifact();
            locationList.add(a.getFile());
        }
        return locationList;
    }

    public String getHighestRemoteVersion(String groupID,String artifactID) throws VersionRangeResolutionException {
        Artifact artifact =
            new DefaultArtifact(groupID+":"+artifactID+":[0,)");
        VersionRangeRequest request = new VersionRangeRequest();
        request.setArtifact(artifact);
        request.setRepositories(Arrays.asList(this.remote));
        VersionRangeResult result = system.resolveVersionRange(this.session, request);
        for(Version v : result.getVersions()) {
            System.out.println(v.toString());
        }
        Version newestVersion = result.getHighestVersion();
        return newestVersion.toString();
    }
}
