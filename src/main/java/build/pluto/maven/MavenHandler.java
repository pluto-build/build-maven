package build.pluto.maven;

import build.pluto.maven.Artifact;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.Exclusion;
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
            Artifact artifact) throws DependencyResolutionException {
        return resolveDependencies(Arrays.asList(artifact));
    }


    public List<File> resolveDependencies(
            List<Artifact> artifacts) throws DependencyResolutionException {
        CollectRequest collectRequest = new CollectRequest();
        for(Artifact a : artifacts) {
            collectRequest.addDependency(createDependency(a));
        }
        collectRequest.addRepository(remote);
        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setCollectRequest(collectRequest);
        List<ArtifactResult>  artifactResultList =
            system.resolveDependencies(session, dependencyRequest)
                  .getArtifactResults();
        List<File> locationList = new ArrayList<>();
        for (ArtifactResult ar : artifactResultList) {
            locationList.add(ar.getArtifact().getFile());
        }
        return locationList;
    }

    private Dependency createDependency(Artifact artifact) {
        DefaultArtifact aetherArtifact = createDefaultArtifact(artifact);
        Dependency result = new Dependency(aetherArtifact, JavaScopes.COMPILE);
        List<Exclusion> exclusions = new ArrayList<>();
        for (Artifact a : artifact.exclusions) {
            Exclusion e = new Exclusion(a.groupID, a.artifactID, a.classifier, a.extension);
            exclusions.add(e);
        }
        result.setExclusions(exclusions);
        result.setOptional(artifact.optional);
        //TODO:scope?
        return result;
    }

    public DefaultArtifact createDefaultArtifact(Artifact artifact) {
        StringBuffer sb = new StringBuffer();
        sb.append(artifact.groupID).append(":");
        sb.append(artifact.artifactID);
        if (artifact.extension != null) {
            sb.append(":");
            sb.append(artifact.extension);
            if (artifact.classifier != null) {
                sb.append(":");
                sb.append(artifact.classifier);
            }
        }
        sb.append(":");
        sb.append(artifact.versionConstraint);
        return new DefaultArtifact(sb.toString());
    }

    public String getHighestRemoteVersion(
            Artifact artifact) throws VersionRangeResolutionException {
        return this.getHighestVersion(
                artifact,
                Arrays.asList(this.remote));
    }

    public String getHighestLocalVersion(
            Artifact artifact) throws VersionRangeResolutionException {
        String groupIDStructure = artifact.groupID.replace(".", "/");
        String artifactPathString = local.getBasedir().getAbsolutePath()
            + "/" + groupIDStructure + "/" + artifact.artifactID;
        AbsolutePath artifactPath = new AbsolutePath(artifactPathString);
        List<Version> versionList = new ArrayList<>();
        VersionConstraint versionConstraint =
            getVersionConstraint(artifact.versionConstraint);
        //get versions in constraint
        for(RelativePath p : FileCommands.listFiles(artifactPath)) {
            File f = p.getFile();
            if(f.isDirectory()) {
                    Version version = getVersion(f.getName());
                    if(version != null
                            && versionConstraint.containsVersion(version)) {
                        versionList.add(version);
                    }
            }
        }
        //sort versions
        Collections.sort(versionList);
        int indexOfLastElement = versionList.size() - 1;
        if (indexOfLastElement == -1) {
            return null;
        }
        return versionList.get(indexOfLastElement).toString();
    }

    private VersionConstraint getVersionConstraint(String versionConstraint) {
        try {
            VersionScheme versionScheme = new GenericVersionScheme();
            return versionScheme.parseVersionConstraint(versionConstraint);
        } catch(InvalidVersionSpecificationException e) {
            System.out.println("VersionRange is not valid");
            return null;
        }
    }

    private Version getVersion(String version) {
        try {
            VersionScheme versionScheme = new GenericVersionScheme();
            return versionScheme.parseVersion(version);
        } catch(InvalidVersionSpecificationException e) {
            return null;
        }
    }

    private List<String> getPossibleVersionOfRange(
            Artifact artifact,
            List<RemoteRepository> repos) throws VersionRangeResolutionException {
        DefaultArtifact aetherArtifact = createDefaultArtifact(artifact);
        VersionRangeRequest request = new VersionRangeRequest();
        request.setArtifact(aetherArtifact);
        request.setRepositories(repos);
        VersionRangeResult result =
            system.resolveVersionRange(this.session, request);
        List<String> possibleVersions = new ArrayList<>();
        for(Version v : result.getVersions()) {
            possibleVersions.add(v.toString());
        }
        return possibleVersions;
    }

    private String getHighestVersion(
            Artifact artifact,
            List<RemoteRepository> repos) throws VersionRangeResolutionException {
        List<String> possibleVersions =
            this.getPossibleVersionOfRange(artifact, repos);
        int lastElementIndex = possibleVersions.size() - 1;
        if (lastElementIndex == -1) {
            return null;
        }
        return possibleVersions.get(lastElementIndex);
    }
}
