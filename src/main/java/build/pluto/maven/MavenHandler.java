package build.pluto.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionConstraint;
import org.eclipse.aether.version.VersionScheme;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.RelativePath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public String getHighestRemoteVersion(
            String groupID,
            String artifactID,
            String version) throws VersionRangeResolutionException {
        return this.getHighestVersion(
                groupID,
                artifactID,
                version,
                Arrays.asList(this.remote));
    }

    private List<String> getPossibleVersionOfRange(
            String groupID,
            String artifactID,
            String version,
            List<RemoteRepository> repos) throws VersionRangeResolutionException {
        Artifact artifact =
            new DefaultArtifact(groupID + ":" + artifactID + ":" + version);
        VersionRangeRequest request = new VersionRangeRequest();
        request.setArtifact(artifact);
        request.setRepositories(repos);
        VersionRangeResult result = system.resolveVersionRange(this.session, request);
        List<String> possibleVersions = new ArrayList<>();
        System.out.println("RANGE" + version);
        for(Version v : result.getVersions()) {
            System.out.println(v.toString());
            possibleVersions.add(v.toString());
        }
        return possibleVersions;
    }
    
    private String getHighestVersion(
            String groupID,
            String artifactID,
            String version,
            List<RemoteRepository> repos) throws VersionRangeResolutionException {
        List<String> possibleVersions =
            this.getPossibleVersionOfRange(groupID, artifactID, version, repos);
        int lastElementIndex = possibleVersions.size() - 1;
        if (lastElementIndex == -1) {
            return null;
        }
        return possibleVersions.get(lastElementIndex);
    }
}
