# p2-maven-plugin [![Build Status](https://secure.travis-ci.org/reficio/p2-maven-plugin.png)](http://travis-ci.org/reficio/p2-maven-plugin)

## Truly mavenize your Eclipse RCP project!

### Intro
Welcome to p2-maven-plugin! This is an easy-to-use Maven3 plugin responsible for the automation of dependency management in the Eclipse RCP environment. It was developed as I practically needed it in one of the commercial projects that I currently work on.

### Why should you bother?
Are you familiar with the automated dependency management like in Maven, Gradle or any other fancy tool? What I mean is that you define a bunch of dependencies in the project descriptor and that is the only thing that you have to take care of. The dependency tree is calculated automatically and all dependencies are resolved and downloaded back-stage, so that you can have a beer while nerdy C++ programmers manually play with their DLLs in the lib folder. Yes, Java Developers like the automated dependency management - it was introduced by Maven more that 8 years ago and it simply revolutionized the build automation. And yes, Java Developers think that the automated dependency management is the de-facto standard, so that they cannot even think of going back in time and doing stuff that should be done by them automagically.

If you do not believe me read the following blog entry, it perfectly describes the problem: http://bit.ly/PypQEy
The author presents five different approaches how to configure the dependency management in an Eclipse RCP project and in the end she could not propose a satisfactory solution.

To better understand what I mean here read this story please: *And now you are thrown into an RCP project. After 5 minutes of excitement you notice that your colleagues on the the project do not use continuous integration and their application is not mavenized. Import of the native eclipse project takes you more than an hour before it compiles successfully. The zip file with the target platform is passed over to you on a pendrive, paths are hardcoded. WTF you think… Between swearing and drinking your 4th coffee you are close to handing in a notice. But, since you are a geek it is not that easy to defeat you. Before you head back home you decide to give it one more shot to enjoy software development once more in your life. Googling around you immediately spot that the bright guys from the Tycho project implemented a set of brilliant plugins to mavenize your RCP app. Your heartbeat increases, your breath becomes nervously shallow, and "voila!" you shout as manage to build your first hello-world example using tycho and maven. But then, something unexpected happens, something that will take all you joy and you will not be the same person any more. You try to add an external dependency to you shiny bright piece of work… How it comes, what the hell is that, or are they all nuttheads - these thoughts penetrate your brain so intensively that you decide to quite your job, move to scala programming and never ever touch the RCP platform again…*

Now you probably understand the main goal of this plugin. It tries to bridge the gap between Maven-like and RCP-like dependency management so that all Java developers can easily use all the Maven features in the RCP development. Tycho does a great job here, but as there was missing piece there I decided to contribute this plugin as IMHO it can help a lot! Read further to fully understand why dependency management in RCP is not that easy and why it is not maven-compliant. Enjoy!

### Java vs. Maven vs. Eclipse RCP - dependency war
Eclipse RCP dependency management is a bit different than a common java model. It is caused by two facts: on the one hand it is an OSGi environment which extends the Java dependency model, on the other hand there are some Eclipse RCP conventions on the top of it which at the first glance may make an awkward impression. So how does it look like, you think? Nothing simpler - in Eclipse RCP every dependency should be stored in an P2 update site. Eclipse provides a set of update sites and if all popular dependencies were there we would be safe and sound, but guess what, they are not there - which is the problem #1. The problem #2 is that because it's an OSGi environment you are only allowed to use bundles - and guess what?, not all java artifacts are bundles, they are not even close to that. So, let's rewind: I have to have all my artifacts as bundles, but they are not bundles and they have to be located in a P2 site, and I don't have that site. How do I do that, you ask? Yes, it is not that difficult, there is a bnd tool written by Peter Kriens that can transform your jars into bundles. There is also a convenience tool provided by Eclipse that can generate a P2 site (in a cumbersome and painful way). Both tools assume that all your jars/bundles are located in a local folder - which means that you have to download the artifacts yourself. You can use Maven you think. Yes that is true. But there is a significant difference in the way how Maven calculates the dependency tree. In OSGi, in an update site, you can have 3 versions of the same dependency, as your bundles may selectively include one class from version X, and seconds class from version Y. In Maven, though, if you specify 2 version of one dependency only one of them will be fetched as you don't want to have 2 almost identical dependencies on your classpath. This is problem #3. So in essence to solve your problems have to do three things by yourself:
* download all required dependencies to a folder
* recognize which dependencies are not OSGi bundles and bundle them using the bnd tool
* take all your bundles and invoke a P2 tool to generate the P2 site.
Ufff, that is a manual, cumbersome, repeatable and stupid activity that may take you a few hours.

That's where p2-maven plugin comes into play. It solves problems #1, #2, #3 and does all the hard work for you. Isn't that just brilliant? I think it is… :)

## How to use it in 2 minutes?
The last thing that you have to know is how to use it. I prepared a quickstart pom.xml file so that you can give it a try right away. We're gonna generate a site and expose it using jetty-maven-plugin. This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/quickstart/pom.xml 

Here's the code:

```xml 
	<?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001 XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
        <modelVersion>4.0.0</modelVersion>
    
        <groupId>org.reficio.rcp</groupId>
        <artifactId>example-p2-site</artifactId>
        <packaging>pom</packaging>
        <version>1.0.0</version>
    
        <build>
            <plugins>
                <plugin>
                    <groupId>org.reficio</groupId>
                    <artifactId>p2-maven-plugin</artifactId>
                    <version>1.0.0-SNAPSHOT</version>
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
There are many more configuration options, but basically that's the thing that you need. Now you only have to invoke mvn p2:site in the folder where the pom.xml file is located and when the process finishes your P2 site is ready!

You will see the following output:
```
    $ mvn p2:site
    
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building example-p2-site 1.0.0
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- p2-maven-plugin:1.0.0-SNAPSHOT:site (generate-p2-site) @ example-p2-site ---
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

Your site will be located in the target/repository folder and will look like this:
```
	pom.xml
	target
    ├── repository
    │   ├── artifacts.jar
    │   ├── category.xml
    │   ├── content.jar
    │   └── plugins
    │       ├── org.apache.commons.io_2.1.0.jar
    │       ├── org.apache.commons.lang_2.4.0.jar
    │       ├── org.apache.commons.lang_2.5.0.jar
    │       ├── org.apache.commons.lang_2.6.0.jar
    │       └── org.apache.commons.lang3_3.1.0.jar        
```

Unfortunately, it's not the end of the story since tycho does not support local repositories (being more precise: repositories located in a local folder). The only way to work it around is too expose our newly created repository using an HTTP server. We're gonna use the jetty-plugin - don't worry, the example above contains a sample jetty-plugin configuration. Just type mvn jetty:run and open the following link http://localhost:8080/site Your p2 update site will be there.

Now, simply reference your site in your target definition and play with your Eclipse RCP project like you were in the Plain Old Java Environment.

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
* DO NOT to use the pomDependencies->consider option as it simply of NO good
* DO NOT define your external dependencies as standard mvn dependcies in the pom.xml (it will work in the console, but it will not work in the Eclipse IDE when you import the project, since the target configuration knows nothing about them)
* Use the MANIFEST-FIRST approach - define all your depencies in the MANIFES.MF files.
* If some of your depencies are not OSGi bundles or are not available in P2 update sites, SIMPLY define them in the p2-maven-plugin config, generate the site and make it available using jetty (or any other mechanism). Then add the URL of the exposed site to the target platform definition. In such a way you will have a consistent, manifest-first dependency management in Eclipse RCP!
* Whenever you have to add another external dependency, simply re-invoke "mvn p2:site" and jetty will re-fetch the site.
* You can automatize the generation/exposition of our site using Jenkins and Apache2


## Examples
There are many more use cases that I am gonna describe here.

### Default options 
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/quickstart/pom.xml

This is the simplest and the shortest setup. Only the identifiers of the dependencies have to be specified. 
What will be the behavior like if we use the configuraiton listed below?

* specified dependencies will be fetched
* transitive dependencies will be fetched
* jars containing source code will NOT be fetched
* jars that are NOT osgi bundles will be "bundled" using bnd tool; if instructions are specified, they will be APPLIED.
* jars that are osgi bundles will be simply included, if instructions are specified, they will be IGNORED (see override example)
* p2 site will be generated

 
This definition of an artifact:
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
        <override>false</override>
        <source>false</source>
        <instructions>
            <Import-Package>*;resolution:=optional</Import-Package>
            <Export-Package>*</Export-Package>
        </instructions>
    </artifact>
```     

Other instructions, such as, Bundle-SymbolicName, Bundle-Name, Bundle-Version, are calculated according to the following rules: http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html If you specify any instructions yourself they will be used as the default ones, if
the bundle is not already an osgi bundle - othwerise you have to use the override option - please see the "override" example located here: /examples/override/pom.xml

### Source option
This is the configuration snippet that enables you to include source jars and generate source bundles for all dependencies. <source>true</source> section has to be included to enable this option.

Example:
```xml 
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <source>true</source>
    </artifact>
```


### Transitive option
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/transitive/pom.xml

This is the configuration snippet that enables you to exclude transitive dependencies. <transitive>false</transitive> section has to be included to enable this option.

What will be the behavior like if we use the configuraiton listed below?

* specified dependencies will be fetched
* transitive dependencies will NOT be fetched
* jars containing source code will NOT be fetched
* jars that are NOT osgi bundles will be "bundled" using bnd tool; if you specify instructions they will be APPLIED
* jars that are osgi bundles will be simply included; if you specify instructions they will be IGNORED
* p2 site will be generated

This definition of an artifact:
```xml
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <transitive>false</transitive>
    </artifact>
```

is an equivalent of the following definition:
```xml
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <transitive>false</transitive>
        <override>false</override>
        <source>false</source>
        <instructions>
            <Import-Package>*;resolution:=optional</Import-Package>
            <Export-Package>*</Export-Package>
        </instructions>
    </artifact>
```

### Maven phase binding
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/phase/pom.xml

You can also bind the invocation of the plugin to a Maven option. Just specify the following binding and your p2 plugin will be invoked during the 'mvn compile' phase.
```xml
	<id>generate-p2-site</id>
    <phase>compile</phase>
    <goals>
    	<goal>site</goal>
    </goals>
```

### Override option
This example is located here: https://github.com/reficio/p2-maven-plugin/blob/master/examples/override/pom.xml

This is the configuration snippet that enables you to override the default MANIFEST.MF files in jars that are already OSGi bundles. <override>true</override> section has to be included to enable this opion

If you want to override the default MANIFEST.MF you also have to disable the fetch of the transitive dependencies, otherwise it would not make much sense. When you override the file generated by the provider of the jar you probably know what you are doing - which means you are fine-tuning the MANIFEST.MF options. If transitive dependencies were included your fine-tuning would be applied to all of them, which in our opinion does not make sense and may lead to unexpected behavior. If you do not specify <transitive>false</transitive> along the <override>true</override> an exception will be thrown.

If you do not specify bnd instructions the MANIFEST.MF will be overridden using the default instructions. Please see the "default" example located here: src/main/example/default/pom.xml for more info.

What will be the behavior like if we use the configuraiton listed below?

* specified dependencies will be fetched
* transitive dependencies will NOT be fetched
* jars that are NOT osgi bundles will be "bundled" using bnd tool; if you specify instructions they will be APPLIED
* jars that are osgi bundles will be "bundled" using bnd tool; if you specify instructions they will be APPLIED
* p2 site will be generated

This definition of an artifact should look like this:
```xml
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <transitive>false</transitive>
        <override>true</override>
    </artifact>
```

you can also specify some instructions (what makes sense with override):
```xml
    <artifact>
        <id>commons-io:commons-io:2.1</id>
        <transitive>false</transitive>
        <override>true</override>
        <source>false</source>
        <instructions>
            <Import-Package>*;resolution:=optional</Import-Package>
            <Export-Package>*</Export-Package>
        </instructions>
    </artifact>
```


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
* Jenkins -> https://reficio.ci.cloudbees.com/job/p2-maven-plugin/
* Sonar -> coming soon...
* Site -> http://projects.reficio.org/p2-maven-plugin/1.0.0-SNAPSHOT/manual.html

### Who's behind it?
Tom Bujok [tom.bujok@gmail.com]

Reficio™ - Reestablish your software!
www.reficio.org