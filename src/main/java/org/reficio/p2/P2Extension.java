package org.reficio.p2;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.eclipse.sisu.equinox.EquinoxServiceFactory;
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;
import org.eclipse.tycho.core.maven.TychoMavenLifecycleParticipant;
import org.reficio.p2.domain.Configuration;
import org.reficio.p2.utils.BundleWrapper;
import org.reficio.p2.utils.CategoryPublisher;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "p2")
public class P2Extension extends AbstractMavenLifecycleParticipant implements Initializable {

    private static final String BUNDLES_DESTINATION_FOLDER = "/source/plugins";
    private static final String VANILLA_DESTINATION_FOLDER = "/jars";
    private static final String DEFAULT_CATEGORY_FILE = "category.xml";
    private static final String DEFAULT_CATEGORY_CLASSPATH_LOCATION = "/";

    @Requirement
    private Logger log;

    @Requirement
    private RuntimeInformation runtime;

    @Requirement
    private EquinoxServiceFactory p2;

    @Requirement
    private P2ApplicationLauncher launcher;

    @Requirement
    private RepositorySystem repoSystem;

    @Requirement
    private PlexusContainer plexus;

    @Requirement
    private TychoMavenLifecycleParticipant tychoMavenLifecycleParticipant;

    private Configuration configuration;

    private String buildDirectory;

    private String destinationDirectory;

    private String categoryFileURL;


    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        log.info("p2extension afterProjectsRead executed");
    }

    public void initialize() throws InitializationException {
        log.info("p2extension initialize executed");
//        configuration = readConfiguration();
//        buildDirectory = ".";
//        destinationDirectory = "/repository";
//
//        try {
//            List<org.sonatype.aether.artifact.Artifact> a = resolveDependencies();
////            Set<org.sonatype.aether.artifact.Artifact> b = new HashSet<org.sonatype.aether.artifact.Artifact>(a);
////            boolean executionProceeded = executeBndWrapper();
////            if (executionProceeded == false) {
////                return;
////            }
////            executeP2PublisherPlugin();
////            executeCategoryPublisher();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    private File getConfigFile() {
        String configFilePath = System.getProperty("p2.configuration");
        if (configFilePath != null) {
            File file = new File(configFilePath);
            if (file.exists()) {
                return file;
            }
        } else {
            File baseDir = new File(".");
            File config = new File(baseDir, "p2.xml");
            if (config.exists()) {
                return config;
            }
        }
        throw new RuntimeException("No p2.xml configuration found");
    }

    public Configuration readConfiguration() {
        Configuration config = Configuration.readConfiguration(getConfigFile());
        return config;
    }


    protected List<org.sonatype.aether.artifact.Artifact> resolveDependencies() throws DependencyCollectionException, DependencyResolutionException {
        Dependency dependency =
                new Dependency(new DefaultArtifact("org.apache.maven:maven-profile:2.2.1"), "compile");

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        for (String repositoryAddress : configuration.getRepositories()) {
            RemoteRepository r = new RemoteRepository();
            r.setUrl(repositoryAddress);
            collectRequest.addRepository(r);
        }


        MavenRepositorySystemSession repoSession = new MavenRepositorySystemSession();
        LocalRepository localRepo = new LocalRepository("target/local-repo");
        repoSession.setLocalRepositoryManager(repoSystem.newLocalRepositoryManager(localRepo));

        DependencyNode node = repoSystem.collectDependencies(repoSession, collectRequest).getRoot();
        DependencyRequest dependencyRequest = new DependencyRequest(node, null);
        repoSystem.resolveDependencies(repoSession, dependencyRequest);
        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg);
        return nlg.getArtifacts(false);

    }

    protected boolean executeBndWrapper(Set<Artifact> artifactsToWrap) throws Exception {
        File bundlesDestinationFolder = new File(buildDirectory, BUNDLES_DESTINATION_FOLDER);
        File artifactsDestinationFolder = new File(buildDirectory, VANILLA_DESTINATION_FOLDER);
        BundleWrapper wrapper = new BundleWrapper(configuration.getParameters().isPedantic());
        return wrapper.execute(artifactsToWrap, artifactsDestinationFolder, bundlesDestinationFolder);
    }

    protected void executeP2PublisherPlugin() throws MojoExecutionException, IOException {
        File repositoryDirectory = new File(destinationDirectory);
        FileUtils.deleteDirectory(repositoryDirectory);
        executeMojo(
                plugin(
                        groupId("org.eclipse.tycho.extras"),
                        artifactId("tycho-p2-extras-plugin"),
                        version("0.14.0")
                ),
                goal("publish-features-and-bundles"),
                configuration(
                        element(name("compress"), Boolean.toString(configuration.getParameters().isCompressSite())),
                        element(name("additionalArgs"), configuration.getParameters().getAdditionalArgs())
                ),
                executionEnvironment(
                        null,
                        null,
                        null
                )
        );
    }

    private void executeCategoryPublisher() throws AbstractMojoExecutionException, IOException {
        prepareCategoryLocationFile();
        CategoryPublisher publisher = CategoryPublisher.factory()
                .p2ApplicationLauncher(launcher)
                .additionalArgs(configuration.getParameters().getAdditionalArgs())
                .forkedProcessTimeoutInSeconds(configuration.getParameters().getForkTimeoutInSeconds())
                .create();
        publisher.execute(categoryFileURL, destinationDirectory);
    }

    private void prepareCategoryLocationFile() throws IOException {
        if (StringUtils.isBlank(configuration.getParameters().getCategoryFileURL())) {
            InputStream is = getClass().getResourceAsStream(DEFAULT_CATEGORY_CLASSPATH_LOCATION + DEFAULT_CATEGORY_FILE);
            File destinationFolder = new File(destinationDirectory);
            destinationFolder.mkdirs();
            File categoryDefinitionFile = new File(destinationFolder, DEFAULT_CATEGORY_FILE);
            FileWriter writer = new FileWriter(categoryDefinitionFile);
            IOUtils.copy(is, writer, "UTF-8");
            IOUtils.closeQuietly(writer);
            categoryFileURL = categoryDefinitionFile.getAbsolutePath();
        }
    }


}


