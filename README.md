# p2-maven-plugin [![Build Status](https://secure.travis-ci.org/reficio/p2-maven-plugin.png)](http://travis-ci.org/reficio/p2-maven-plugin)

## Truly mavenize your Eclipse RCP project!

### Intro
Welcome to the p2-maven-plugin! This is an easy-to-use Maven3 plugin responsible for the automation of the third-party dependency management in the Eclipse RCP environment.

### Why should you bother?
Are you familiar with the automated dependency management like in Maven, Gradle or any other fancy tool? You just define a project descriptor, add a bunch of dependencies and everything happens "automagically"... Piece of cake huh?!

Well, there are, however, these RCP "unfortunates" for whom it is not quite that easy... Why's that, you might think? 

Firstly, Eclipse RCP is an OSGi environment which extends the Java dependency model, so you can’t simply take a "jar" file and hope that it's going to work; believe me - it will not. Secondly, there are some additional RCP conventions that have to be followed, which makes it even more complex.

But wait! Isn't Tycho supposed to solve all of these problems? Yeah, well, Tycho can do a lot, but there is definitely "something" missing... What is more, the learning curve is really steep, so it’s very easy to go off down the wrong path wasting a lot of time on simple things.

The following blog entry outlines the problem perfectly: http://bit.ly/PypQEy
The author presents five different approaches how to configure the build and dependency management in a Tycho / Eclipse RCP project and, in the end, she couldn’t really propose a satisfactory solution! Unfortunately, there is no "one-click" easy solution, but if you stick to some best practices and use the right tools you can relax while Maven does most of the hard work for you.

p2-maven-plugin simply tries to bridge the gap between Maven-like and RCP-like dependency management styles so that all Maven features can be seamlessly used with "No Fear!"

Read further to fully understand why dependency management with Maven and Tycho is not that easy.

### Java vs. Maven vs. Eclipse RCP - dependency war
In order to add a third-party dependency to an Eclipse RCP project the dependency has to reside in a P2 update site. 

Eclipse (and other providers) provide a set of public update sites, but obviously not all popular and publicly available dependencies are there (that is the problem number #1). Pretty often you would also like to add a corporate / internal dependency - and you do not have to be a genius to figure out that it is not somewhere on the web… 

Since Eclipse RCP is an OSGi environment in order to add a dependency to a p2 update site the dependency has to be an OSGi bundle (that is the problem number #2).

So, let's sum up for now: all our artifacts have to be OSGi bundles, but they are not always bundles and they have to be located in a P2 site, but we do not have that site. How do we proceed then? 

It is not that difficult, there is a 'bnd' tool written by Peter Kriens that can transform your jars into bundles. There is also a convenience tool provided by Eclipse RCP that can generate a P2 site (in a cumbersome and painful way though). Both tools assume that all your jars/bundles are located in a local folder - which means that you have to download them by-hand. You could use Maven to automate it a bit, but there is a significant difference in the way how Maven calculates a dependency tree and this is not always compatible with the OSGi way (that is the problem number #3). Let us elaborate on it a bit more.

In a P2 update site, there may be three versions of the same dependency, as bundles may selectively include one class from version X, and a second class from version Y (that is normal in the world of OSGi). In Maven, though, if you specify two versions of a dependency only one of them will be fetched as you don't want to have two almost identical dependencies on your classpath (Java simply cannot deal with that). 

So in essence, to solve all problems mentioned above you have to do three things by-hand:

* download all required dependencies to a folder,
* recognize which dependencies are not OSGi bundles and bundle them using the 'bnd' tool,
* take all your bundles and invoke a P2 tool to generate a P2 update site.

Ufff, that is a mundane, cumbersome, repeatable and stupid activity that may take you a few hours - imagine now that you have to do it multiple times…

That's where p2-maven-plugin comes into play. It solves problems #1, #2, #3 and does all the hard work for you. Isn't that just brilliant? I think it is... :)

## How to use it in 2 minutes?
Using p2-maven-plugin is really simple. I have prepared a quickstart pom.xml file so that you can give it a try right away. We're going to generate a site and expose it using the jetty-maven-plugin. This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/quickstart/pom.xml 

Here's the repo location where you can check the newest version id: http://repo.reficio.org/maven/org/reficio/p2-maven-plugin/

Here's the pom.xml:

```xml 
	<?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001 XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
        <modelVersion>4.0.0</modelVersion>
    
        <groupId>org.reficio.rcp</groupId>
        <artifactId>example-p2-site</artifactId>
        <packaging>pom</packaging>
        <version>1.2.0-SNAPSHOT</version>
    
        <build>
            <plugins>
                <plugin>
                    <groupId>org.reficio</groupId>
                    <artifactId>p2-maven-plugin</artifactId>
                    <version>1.2.0-SNAPSHOT</version>
                    <executions>
                        <execution>
                            <id>default-cli</id>
                            <configuration>
                                <artifacts>
                                	<!-- specify your depencies here -->
                                	<!-- groupId:artifactId:version -->
                                    <artifact><id>commons-io:commons-io:2.1</id></artifact>
                                    <artifact><id>commons-lang:commons-lang:2.4</id></artifact>
                                    <artifact><id>commons-lang:commons-lang:2.5</id></artifact>
                                    <artifact><id>commons-lang:commons-lang:2.6</id></artifact>
                                    <artifact><id>org.apache.commons:commons-lang3:3.1</id></artifact>
                                </artifacts>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
                
                <plugin>
	                <groupId>org.mortbay.jetty</groupId>
    	            <artifactId>jetty-maven-plugin</artifactId>
        	        <version>8.1.5.v20120716</version>
            	    <configuration>
                	    <scanIntervalSeconds>10</scanIntervalSeconds>
                    	<webAppSourceDirectory>${basedir}/target/repository/</webAppSourceDirectory>
                    	<webApp>
                       		<contextPath>/site</contextPath>
                    	</webApp>
	               </configuration>
            	</plugin>
                
            </plugins>
        </build>
    
        <pluginRepositories>
            <pluginRepository>
                <id>reficio</id>
                <url>http://repo.reficio.org/maven/</url>
            </pluginRepository>
        </pluginRepositories>
    
    </project>
```

The artifacts may be specified using the following notation:

```
    <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
```

There are many more config options, but basically that's the thing that you need for now. in order to generate the site invoke the following command 'mvn p2:site' in the folder where the pom.xml file resides. When the process finishes your P2 site is ready!

You will see the following output:
```
    $ mvn p2:site
    
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building example-p2-site 1.0.0
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- p2-maven-plugin:1.0.0:site (generate-p2-site) @ example-p2-site ---
    [INFO] Command line:
        /bin/sh -c cd /opt/workspaces/reficio/p2-maven-plugin/src/main/resources && /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/
        Home/bin/java -jar /opt/maven-ext/org/eclipse/tycho/tycho-bundles-external/0.14.0/eclipse/plugins/
        org.eclipse.equinox.launcher_1.3.0.v20111107-1631.jar -nosplash -application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher 
        -artifactRepository file:/opt/workspaces/reficio/p2-maven-plugin/src/main/resources/target/repository -metadataRepository file:/opt/
        workspaces/reficio/p2-maven-plugin/src/main/resources/target/repository -publishArtifacts -compress -source /opt/workspaces/reficio/p2-
        maven-plugin/src/main/resources/target/source
    Generating metadata for ..
    Generation completed with success [0 seconds].
    [INFO] Command line:
        /bin/sh -c cd /opt/workspaces/reficio/p2-maven-plugin/src/main/resources/target/repository && /System/Library/Java/JavaVirtualMachines/
        1.6.0.jdk/Contents/Home/bin/java -jar /opt/maven-ext/org/eclipse/tycho/tycho-bundles-external/0.14.0/eclipse/plugins/
        org.eclipse.equinox.launcher_1.3.0.v20111107-1631.jar -nosplash -application org.eclipse.equinox.p2.publisher.CategoryPublisher -
        categoryDefinition file:/opt/workspaces/reficio/p2-maven-plugin/src/main/resources/target/repository/category.xml -metadataRepository 
        file:/opt/workspaces/reficio/p2-maven-plugin/src/main/resources/target/repository/        
    Generating metadata for ..
    Generation completed with success [0 seconds].
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 5.164s
    [INFO] Finished at: Sat Jul 07 16:28:49 CEST 2012
    [INFO] Final Memory: 7M/81M
    [INFO] ------------------------------------------------------------------------
```

Your p2 site is located in the target/repository folder and looks like this:
```
	pom.xml
	target
    ├── repository
    │   ├── artifacts.jar
    │   ├── category.xml
    │   ├── content.jar
    │   └── plugins
    │   │   ├── org.apache.commons.io_2.1.0.jar
    │   │   ├── org.apache.commons.lang_2.4.0.jar
    │   │   ├── org.apache.commons.lang_2.5.0.jar
    │   │   ├── org.apache.commons.lang_2.6.0.jar
    │   │   └── org.apache.commons.lang3_3.1.0.jar
```

Unfortunately, it's not the end of the story since tycho does not support local repositories (being more precise: repositories located in a local folder). The only way to work it around is to expose our newly created update site using an HTTP server. We're going to use the jetty-plugin - don't worry, the example above contains a sample jetty-plugin set-up. Just type 'mvn jetty:run' and open the following link http://localhost:8080/site. Your P2 update site will be there!

Now, simply reference your site in your target definition and play with your Eclipse RCP project like you were in the Plain Old Java Environment. Remember to enable the "Group items by category" option, otherwise you will not see any bundles.

```
	$ mvn jetty:run
	
    [INFO] Scanning for projects...
    [INFO]
    [INFO] ------------------------------------------------------------------------
    [INFO] Building example-p2-site 1.0.0
    [INFO] ------------------------------------------------------------------------
    [INFO]
    [INFO] >>> jetty-maven-plugin:8.1.5.v20120716:run (default-cli) @ example-p2-site >>>
    [INFO]
    [INFO] <<< jetty-maven-plugin:8.1.5.v20120716:run (default-cli) @ example-p2-site <<<
    [INFO]
    [INFO] --- jetty-maven-plugin:8.1.5.v20120716:run (default-cli) @ example-p2-site ---
    [INFO] Configuring Jetty for project: example-p2-site
    [INFO] Webapp source directory = /opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/repository
    [INFO] Reload Mechanic: automatic
    [INFO] Classes directory /opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/classes does not exist
    [INFO] Context path = /site
    [INFO] Tmp directory = /opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/tmp
    [INFO] Web defaults = org/eclipse/jetty/webapp/webdefault.xml
    [INFO] Web overrides =  none
    [INFO] web.xml file = null
    [INFO] Webapp directory = /opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/repository
    2012-08-20 14:20:13.473:INFO:oejs.Server:jetty-8.1.5.v20120716
    2012-08-20 14:20:13.582:INFO:oejpw.PlusConfiguration:No Transaction manager found - if your webapp requires one, please configure one.
    2012-08-20 14:20:13.878:INFO:oejsh.ContextHandler:started o.m.j.p.JettyWebAppContext{/site,file:/opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/repository/},file:/opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/repository/
    2012-08-20 14:20:13.878:INFO:oejsh.ContextHandler:started o.m.j.p.JettyWebAppContext{/site,file:/opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/repository/},file:/opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/repository/
    2012-08-20 14:20:13.878:INFO:oejsh.ContextHandler:started o.m.j.p.JettyWebAppContext{/site,file:/opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/repository/},file:/opt/workspaces/reficio/p2-maven-plugin/examples/quickstart/target/repository/
    2012-08-20 14:20:13.938:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:8080
    [INFO] Started Jetty Server
    [INFO] Starting scanner at interval of 10 seconds.
```

## Best Practices
* **DO NOT** to use the Tycho's pomDependencies->consider option as it is simply of NO good
* **DO NOT** define your external dependencies in the `dependencies` section of the pom.xml (mvn compilation will work in the console, but it will not work in the Eclipse IDE when you import the project, since the 'Target Configuration' knows nothing about the dependencies defined there)
* Use the MANIFEST-FIRST approach - define all your dependencies in the MANIFEST.MF files.
* If some of your dependencies are not OSGi bundles or are not available in P2 update sites, SIMPLY define them in the p2-maven-plugin config, generate the site and make it available using jetty (or any other mechanism). Then add the URL of the exposed site to the target platform definition. In such a way you will have a consistent, manifest-first dependency management in Eclipse RCP project!
* Whenever you have to add another external dependency, simply re-invoke "mvn p2:site" and the site will be regenerated.
* You can automate the generation/exposition of our site using for example Jenkins and Apache2

## Maven compatibility
p2-maven-plugin is compatible with Maven 3.x (we test it against 3.0.x, 3.1.x, 3.2.x)

## Examples
There are many more use examples, just have a look:

### Default options 
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/quickstart/pom.xml

This is the simplest and the shortest setup. Only the identifiers of the dependencies have to be specified. 
What will be the behavior like if we use the configuration listed below?

* specified dependencies will be fetched
* transitive dependencies will be fetched
* jars containing source code will NOT be fetched
* jars that are **NOT** osgi bundles will be "bundled" using bnd tool; if instructions are specified, they will be APPLIED.
* jars that are osgi bundles will be simply included, if instructions are specified, they will be **IGNORED** (see override example)
* p2 site will be generated

How the instructions works:

* instructions are applied only to the root artifact that you specify!
* instructions are not applied to the TRANSITIVE dependencies!
* transitive dependencies are never overridden (see `<override>` option)
* transitive dependencies are bundled using the default instructions quoted below.
*  other instructions, such as, Bundle-SymbolicName, Bundle-Name, Bundle-Version, etc. are calculated according to the following rules: http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html
*  if you specify any instructions they will be applied only if the jar is not already an OSGi bundle - otherwise you have to use the override option - please see the /examples/override/pom.xml example

The default instructions are:
```xml
      <instructions>
          <Import-Package>*;resolution:=optional</Import-Package>
          <Export-Package>*</Export-Package>
      </instructions>
```
 
The following definition of an artifact:
```xml 
    <artifact>
        <id>commons-io:commons-io:2.1</id>
    </artifact>
```

is an equivalent of the following definition:
```xml 
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <transitive>true</transitive>
		<source>false</source>
        <override>false</override>
        <singleton>false</singleton>
        <instructions>
            <Import-Package>*;resolution:=optional</Import-Package>
            <Export-Package>*</Export-Package>
        </instructions>
        <excludes/>
    </artifact>
```     

### Source option
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/source/pom.xml

This is the configuration snippet that enables you to include the source jars and generate the source bundles for all the dependencies. `<source>true</source>` section has to be included to enable this option.
If enabled together with the transitive option it will fetch sources of transitive dependencies as well.

Example:
```xml 
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <source>true</source>
    </artifact>
```


### Transitive option
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/transitive/pom.xml

This is the configuration snippet that enables you to exclude transitive dependencies. `<transitive>false</transitive>` section has to be included to enable this option.

Expected behavior:

* specified dependencies will be fetched
* transitive dependencies will NOT be fetched

Example usage:
```xml
    <artifact>
        <id>org.mockito:mockito-core:1.9.0</id>
        <transitive>false</transitive>
    </artifact>
```

### Maven phase binding
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/phase/pom.xml

You can also bind the invocation of the plugin to a Maven phase. Just specify the following binding and your p2-maven-plugin will be invoked during the 'mvn compile' phase.
```xml
	<id>generate-p2-site</id>
    <phase>compile</phase>
    <goals>
    	<goal>site</goal>
    </goals>
```

### Override option
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/override/pom.xml

This is the configuration snippet that enables you to override the default MANIFEST.MF files in jars that are already OSGi bundles. `<override>true</override>` section has to be included to enable this option

To manually set the instructions please specify the `<instructions>` section in the configuration of the artifact.
If you do not specify any instructions the MANIFEST.MF file will be overridden with the default instructions.
Please see the examples/quickstart/pom.xml for more info.

Remember:

* override flag does not apply to the transitive dependencies
* instructions are not applied to the transitive dependencies

Expected behavior:

* jars that are OSGi bundles will be "bundled" once more using the bnd tool overriding the default MANIFEST.MF
* if you specify instructions for these jars they will be APPLIED (not to the transitive dependencies though)

The following example presents how to enable the override option:
```xml
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <override>true</override>
    </artifact>
```    

The following example presents how to enable the override option specifying the instructions:
```xml
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <override>true</override>
        <singleton>false</singleton>
        <instructions>
            <Import-Package>*;resolution:=optional</Import-Package>
            <Export-Package>*</Export-Package>
        </instructions>
    </artifact>
```

This definition of an artifact should look like this:
```xml
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <transitive>false</transitive>
        <override>true</override>
    </artifact>
```

### Excludes option
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/excludes/pom.xml
This examples presents how to selectively exclude some of the transitive dependencies of an artifact.
In order to enable this functionality the `<excludes>` section has to be included
in the configuration of the artifact.
If the fetch of the transitive dependencies is disabled through the `<transitive>false</transitive>` switch
the `<excludes>` section will be ignored.

The `<excludes>` resolver reuses the org.sonatype.aether.util.filter.PatternExclusionsDependencyFilter
that works in the following way:
    "PatternExclusionsDependencyFilter is a simple filter to exclude artifacts specified by patterns.
    The artifact pattern syntax has the following format: [groupId]:[artifactId]:[extension]:[version].
    Each segment of the pattern is optional and supports 'full' and 'partial' wildcards (*).
    An empty pattern segment is treated as an implicit wildcard.
    Version can be a range in case a {@link VersionScheme} is specified."

Examples of `<exclude>` values:

* `<exclude>org.apache.*</exclude>` matches artifacts whose group-id begins with 'org.apache.'
* `<exclude>:::*-SNAPSHOT</exclude>` matches all snapshot artifacts
* `<exclude>:objenesis::</exclude>` matches artifacts whose artifactId is objenesis
* `<exclude>*</exclude>` matches all artifacts
* `<exclude>:::</exclude>` (or `<exclude>*:*:*:*</exclude>`) matches all artifacts
* `<exclude></exclude>` matches all artifacts

Expected behavior:

* selected transitive dependencies will be fetched

Example usage:
```xml
    <artifact>
        <id>org.mockito:mockito-core:1.9.0</id>
        <source>false</source>
        <transitive>true</transitive>
        <excludes>
            <exclude>org.objenesis:objenesis:jar:1.0</exclude>
        </excludes>
    </artifact>
```

### Singleton option
This is the configuration snippet that enables you to generate singleton bundles. `<singleton>true</singleton>` section has to be included to enable this option.

Expected behavior:

* bundle will have a "singleton:=true" string added to its symbolic name
* remember that the build may fail if the artifact is already a bundle and the singleton is set to true and the override option is skipped or set to false.

Example usage:
```xml
    <artifact>
        <id>org.mockito:mockito-core:1.9.0</id>
        <singleton>true</singleton>
    </artifact>
```

### P2 Resolver
The plugin also includes the P2 resolver which means that you can include bundles residing in P2 repositories in the generated site.
Have a look at the P2 example located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/p2/pom.xml

In order to have a P2 artifact resolved include its definition in the `<p2>` tag (not in the `<artifacts>` tag)
```xml
    <p2>
        <artifact>
	    <id>org.junit:4.11.0.v201303080030</id>
	</artifact>
    </p2>
```

Next, add a P2 repository in the `<repositories>` tag (its layout has to be set to `<layout>p2</layout>`):
```xml
    <repositories>
        <repository>
            <id>kepler</id>
            <url>http://download.eclipse.org/tools/orbit/downloads/drops/R20130517111416/repository/</url>
            <layout>p2</layout>
        </repository>
    </repositories>
```

If there's more P2 repositories defined, they will be taken in the top-down manner, until the artifact resolution has been successfully executed.

As a result the the specified P2 artifact will be downloaded and included in the generated P2 site.
It will not be bundled, since all P2 artifacts are alread OSGi bundles by definition.

### Eclipse Features
You can also add eclipse feature bundles to maven and include them in the generated p2 repository.

* build may fail if the maven artifact is not a valid eclipse feature jar
* source and transitive must both be false for feature artifacts
* plugins must be added separately, adding the feature will not add the corresponding plugins

Example usage:
```xml
	<configuration>
		<artifacts>
			<artifact><id>org.apache.commons:commons-lang3:3.1</id></artifact>
		</artifacts>
		<features>
			<artifact>
				<id>org.reficio:test.feature:1.0.0</id>
				<source>false</source>
				<transitive>false</transitive>
			</artifact>
		</features>
	</configuration>
```

Your p2 site will look like this:
```
	pom.xml
	target
    ├── repository
    │   ├── artifacts.jar
    │   ├── category.xml
    │   ├── content.jar
    │   └── plugins
    │   │   └── org.apache.commons.lang3_3.1.0.jar
    │   │
    │   └── features
	│       └── com.example.feature_1.0.0.jar
```

You can have a look at two integration test cases of this feature that are located in the src/test/integration folder:

* feature-bundle-01-it
* feature-bundle-02-it


### Other tricks
* p2-maven-plugin will tweak the version of a snapshot dependency replacing the SNAPSHOT string with a timestamp in the following format "yyyyMMddHHmmss" (feature #14)
* It's possible to add a classifier to the artifact definition - supported notation: `<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>`; for example:  `<id>groupid:artifactid:jar:tests:version</id>` (feature #28)


## General configuration options
There are some other plugin options that you can specify in the configuration:

<table>
<tr><th>Option</th><th>Default value</th><th>Description</th></tr>
<tr>
    <td>destinationDirectory</td>
    <td>${project.basedir}/target/repository</td>
    <td>Folder where the generated p2 site should be copied to</td>
</tr>
<tr>
    <td>categoryFileURL</td>
    <td>default category file (all plugins in one category)</td>
    <td>URL of the category.xml file which should be used for the p2 site generation</td>
</tr>
<tr>
    <td>pedantic</td>
    <td>false</td>
    <td>Specifies if the bnd tool should be pedantic</td>
</tr>
<tr>
    <td>compressSite</td>
    <td>true</td>
    <td>Specifies if to compress the descriptors of the generated site</td>
</tr>
<tr>
    <td>forkedProcessTimeoutInSeconds</td>
    <td>0 (infinite)</td>
    <td>Kill the p2 forked process after a certain number of seconds.</td>
</tr>
<tr>
    <td>additionalArgs</td>
    <td></td>
    <td>Specifies additional arguments to p2Launcher, for example -consoleLog -debug -verbose</td>
</tr>
<tr>
    <td>skipInvalidArtifacts</td>
    <td>false</td>
    <td>Controls if the processing should be continued if bundling errors occur.</td>
</tr>
This flag .
</table>


Sample configuration snippet with the additional options:
```xml
	<configuration>
    	<pedantic>false</pedantic>
    	<additionalArgs>-consoleLog -debug -verbose</additionalArgs>
    	<compressSite>true</compressSite>
    	<forkedProcessTimeoutInSeconds>0</forkedProcessTimeoutInSeconds>
	</configuration>
```

## Last but not least

### How can I hack around?
* GitHub -> https://github.com/reficio/p2-maven-plugin
* Jenkins -> https://reficio.ci.cloudbees.com/view/p2-maven-plugin/
* Site -> http://projects.reficio.org/p2-maven-plugin/1.2.0-SNAPSHOT/manual.html
* Coverage -> http://projects.reficio.org/p2-maven-plugin/1.2.0-SNAPSHOT/clover/index.html

### Reporting bugs
Please describe the issue thoroughly. Please include a minimal pom.xml file that can be used to reproduce the problem.

### Pull requests
If you submit a pull request please make sure to add an unit/integration test case that covers the feature. Pull requests without a proper test coverage may not be pulled at all.

### Running integration tests
Invoke the following command to run the integration tests suite:

```
	$ mvn package
```	

### Who's behind it?
Do you like the project? Star it on GitHub and follow me on Twitter! Thanks!

http://twitter.com/tombujok

Reficio™ - Reestablish your software!
www.reficio.org


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/reficio/p2-maven-plugin/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

