package build.pluto.buildmaven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
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
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionConstraint;
import org.eclipse.aether.version.VersionScheme;

import build.pluto.buildmaven.input.Artifact;
import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.Exclusion;
import build.pluto.buildmaven.input.Repository;

public class MavenHandler {
	
	public static final File DEFAULT_LOCAL = new File(System.getProperty("user.home"), ".m2/repository");
	
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
        this.remote = new RemoteRepository.Builder(remoteID, remoteType, remoteURL).build();
        this.system = this.newRepositorySystem();
        this.session = MavenRepositorySystemUtils.newSession();
    	this.session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, local));
    }

    public MavenHandler(File localRepoLocation) {
        this.local = new LocalRepository(localRepoLocation);
        this.remote = new RemoteRepository.Builder(
                "central",
                "default",
                "https://repo1.maven.org/maven2/").build();
        this.system = this.newRepositorySystem();
        this.session = MavenRepositorySystemUtils.newSession();
    	this.session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, local));
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
            List<Dependency> dependencies,
            List<Repository> repos) throws DependencyResolutionException {
        CollectRequest collectRequest = new CollectRequest();
        for(Dependency d : dependencies) {
            collectRequest.addDependency(createDependency(d));
        }
        collectRequest.addRepository(remote);
        for(Repository r: repos) {
            collectRequest.addRepository(createRemoteRepository(r));
        }
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

    private org.eclipse.aether.graph.Dependency createDependency(Dependency dependency) {
        DefaultArtifact aetherArtifact = createDefaultArtifact(dependency.artifactConstraint);
        List<org.eclipse.aether.graph.Exclusion> exclusions = new ArrayList<>();
        for (Exclusion e : dependency.exclusions) {
            org.eclipse.aether.graph.Exclusion aetherExclusion =
                new org.eclipse.aether.graph.Exclusion(
                        e.groupID,
                        e.artifactID,
                        e.classifier,
                        e.extension);
            exclusions.add(aetherExclusion);
        }

        return new org.eclipse.aether.graph.Dependency(
                aetherArtifact,
                dependency.scope,
                dependency.optional,
                exclusions);
    }

    public DefaultArtifact createDefaultArtifact(ArtifactConstraint artifactConstraint) {
        return new DefaultArtifact(artifactConstraint.toString());
    }

    public DefaultArtifact createDefaultArtifact(Artifact artifact) {
        return new DefaultArtifact(artifact.toString());
    }

    public static Artifact transformToArtifact(ArtifactConstraint artifactConstraint) {
        return new Artifact(
                artifactConstraint.groupID,
                artifactConstraint.artifactID,
                artifactConstraint.versionConstraint,
                artifactConstraint.classifier,
                artifactConstraint.extension);
    }

    public String getHighestRemoteVersion(
            ArtifactConstraint artifactConstraint,
            List<Repository> repos) {
        List<RemoteRepository> reposWithRemote = new ArrayList<>();
        reposWithRemote.add(this.remote);
        for(Repository r : repos) {
            RemoteRepository remoteRepo = createRemoteRepository(r);
            reposWithRemote.add(remoteRepo);
        }
        return this.getHighestVersion(
                artifactConstraint,
                reposWithRemote);
    }

    private RemoteRepository createRemoteRepository(Repository repo) {
        RemoteRepository.Builder builder =
            new RemoteRepository.Builder(repo.id, repo.layout, repo.url);
        if(repo.snapshotPolicy != null)
            builder =
                builder.setSnapshotPolicy(
                        createRepositoryPolicy(repo.snapshotPolicy));
        if(repo.releasePolicy != null)
            builder =
                builder.setReleasePolicy(
                        createRepositoryPolicy(repo.releasePolicy));
        return builder.build();
    }

    private RepositoryPolicy createRepositoryPolicy(Repository.Policy policy) {
            return new RepositoryPolicy(
                    policy.enabled,
                    policy.updatePolicy,
                    policy.checksumPolicy);
    }

    public String getHighestLocalVersion(ArtifactConstraint artifactConstraint) {
    	String artifactPathString = local.getBasedir().getAbsolutePath()
            + getPathToArtifactFolder(artifactConstraint);
        File artifactPath = new File(artifactPathString);
        List<Version> versionList = new ArrayList<>();
        VersionConstraint versionConstraint =
            getVersionConstraint(artifactConstraint.versionConstraint);
        //get versions in constraint
        for(File f : artifactPath.listFiles()) {
            if(f.isDirectory()) {
                Version version = getVersion(f.getName());
                if(version != null && versionConstraint.containsVersion(version)) {
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

    public static String getPathToArtifactFolder(
            ArtifactConstraint artifactConstraint) {
        String groupIDStructure = artifactConstraint.groupID.replace(".", "/");
        String artifactPathString =
            "/" + groupIDStructure + "/" + artifactConstraint.artifactID;
        return artifactPathString;
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
            ArtifactConstraint artifactConstraint,
            List<RemoteRepository> repos) {
        DefaultArtifact aetherArtifact = createDefaultArtifact(artifactConstraint);
        VersionRangeRequest request = new VersionRangeRequest();
        request.setArtifact(aetherArtifact);
        request.setRepositories(repos);
        try {
            VersionRangeResult result =
                system.resolveVersionRange(this.session, request);
            List<String> possibleVersions = new ArrayList<>();
            for(Version v : result.getVersions()) {
                possibleVersions.add(v.toString());
            }
            return possibleVersions;
        } catch (VersionRangeResolutionException e) {
            return new ArrayList<String>();
        }
    }

    private String getHighestVersion(
            ArtifactConstraint artifactConstraint,
            List<RemoteRepository> repos) {
        List<String> possibleVersions =
            this.getPossibleVersionOfRange(artifactConstraint, repos);
        int lastElementIndex = possibleVersions.size() - 1;
        if (lastElementIndex == -1) {
            return null;
        }
        return possibleVersions.get(lastElementIndex);
    }

    public void deployArtifact(
            Artifact artifact,
            File typeLocation,
            File pomLocation,
            Repository repo) throws DeploymentException {
        org.eclipse.aether.artifact.Artifact typeArtifact = this.createDefaultArtifact(artifact).setFile(typeLocation);
        org.eclipse.aether.artifact.Artifact pomArtifact = new SubArtifact(typeArtifact, "", "pom").setFile(pomLocation);

        DeployRequest deployRequest = new DeployRequest();
        deployRequest = deployRequest
            .addArtifact(typeArtifact)
            .addArtifact(pomArtifact);
        deployRequest = deployRequest.setRepository(createRemoteRepository(repo));
        system.deploy(session, deployRequest);
    }

    public boolean isAnyArtifactAvailable(
            ArtifactConstraint artifactConstraint,
            List<Repository> repos) {
        List<RemoteRepository> remoteRepoList = new ArrayList<>();
        for(Repository r : repos) {
            remoteRepoList.add(createRemoteRepository(r));
        }
        // String localVersion = this.getHighestLocalVersion(artifactConstraint);
        // if (localVersion != null) {
        //     return true;
        // }
        List<String> versions =
            this.getPossibleVersionOfRange(artifactConstraint, remoteRepoList);
        return !versions.isEmpty();
    }

    public boolean isArtifactResolvable(
            ArtifactConstraint artifactConstraint,
            List<Repository> repos) {
        VersionRangeRequest request = new VersionRangeRequest();
        request = request.setArtifact(createDefaultArtifact(artifactConstraint));
        List<RemoteRepository> reposWithRemote = new ArrayList<>();
        reposWithRemote.add(this.remote);
        for(Repository r : repos) {
            RemoteRepository remoteRepo = createRemoteRepository(r);
            reposWithRemote.add(remoteRepo);
        }
        request = request.setRepositories(reposWithRemote);
        VersionRangeResult result;
        try {
            result = this.system.resolveVersionRange(this.session, request);
        } catch (VersionRangeResolutionException e) {
            return false;
        }
        return result.getHighestVersion() != null;
    }
}
