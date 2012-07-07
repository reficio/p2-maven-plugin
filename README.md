# p2-maven-plugin

## Truly mavenize your Eclipse RCP project!

### Intro
Welcome to p2-maven-plugin! This is an easy-to-use Maven3 plugin responsible for the automation of dependency management in an Eclipse RCP environment.

### Why should you bother?
Are you familiar with the automated dependency management like in Maven, Gradle or any other pretty fancy tool? What I mean is that you define a bunch of dependencies in your project descriptor and that is the only thing that you have to take care of? The dependency tree is calculated and all dependcies are resolved and downloaded back-stage so that you can have a beer while nerdy C++ programmers manually play with DLLs in the lib folder. Yes, Java Developers like automated dependency management that was introduced by Maven 8 years? ago and that revolutionized the build automation. And yes, Java Developers think that automated dependency management is the de-facto standard, so that they cannot even think of going back in time and doing stuff that should be done by them automagically.

And now you are thrown into a RCP project. After 5 minutes of excitmenet, you notice, that your colleagues that are on the this very project do not use continuous integration and their application is not mavenized. Import of the native maven project takes you around an hourg. Between swaering and drinking your 4th coffee you are close to handing in a notice. But it is not that easy to defeat you. Before you head back home you decide to solve all of these issues and enjoy software development for the last time in your life. You immediately spot that bright guys from tycho implemented a set of brilliant plugins to mavenize your RCP app. Your heartbeat increases, your breath becomes nervously shallow, and voila! you shout. You managed to build your first hello-world example using tycho and maven. But then, something unexpected happens, something that will take all you joy and you will not be the same person any more. You try to add a dependency to you shiny bright piece of work… How it comes, what the hell is that, or are they all nuttheads - these thoughs penetrate your brain so intensively that you decide to move to scala programming and never ever touch RCP again…

Now you probably understand the main goal of this plugin. It tries to bridge the gap between Maven-like and RCP-like dependency management so that all Java developers can easily use all the Maven features in RCP development. Tycho does a greate job here, but as there was missing piece there I decided to contribute this plugin as IMHO it can help a lot! Read further to fully understand why depenedncy management in RCP is not that easy and why it is not maven-compliant. Enjoy!

### Java vs. Maven vs. Eclipse RCP - dependency war
Eclipse RCP dependency management is a bit different than a common java model. It is caused by two facts: on the one hand it is a OSGi environment which extends that Java dependency model a bit, on the other hand there are some Eclipse RCP conventions on the top of it which at the first glance may make an awkward impression. So how does it look like you think? Nothing simpler - in Eclipse RCP every dependency should be stored in an P2 update site. Eclipse provides a set of update sites and if all dependencies were there we would be safe and sound, but guess what, there are not. That is problem #1. Problem #2 is that because it's an OSGi environment you are only allowed to use bundles - and guess what?, not all java artifacts are bundles, they are not even close to that. So, let's rewind: I have to have all my artifacts as bundles, but they are not bundles and they have to be located in a P2 site, and I don't have that site. How do I do that you ask? Yes, it is not that difficult, there is a bnd tool written by Peter Kriens that can transform your jars to bundles. There is also a convenience tool provided by Eclipse that can generate a P2 site (in a cumbersome and painful way). Both tools assume that all your jars/bundles are located in a folder - which means that you have to download the artifacts yourself. You can use Maven you think. Yes that is true. But there is a significant difference in the way how Maven calculates the dependency tree. In OSGi, in an update site, you can have 3 versions of the same dependency, as your bundles may selectively include one class from version X, and seconds class from version Y. In Maven though if you specify 2 version of one dependency only one of them will be fetched as you don't want to have 2 almost identical dependencies on your classpath. This is problem #3. So in essence to solve your problems have to do three thingsby yourself:
* download all required depencies to a folder
* recognize which dependencies are not OSGi bundles and bundle them using bnd tool
* take all your bundles and invoke a P2 tool to generate the P2 site.
Ufff, that is a cumbersome, manual, repeatable and stupid activity.

That's where p2-maven plugin comes into play. It solves problems #1, #2, #3 and does all the hard work for you. Isn't that brilliant?

### How to use it
The last thing that you have to know is how to use it. I prepared an example pom.xml so that you can give it a try right away. Here's the code:

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
                            <id>generate-p2-site</id>
                            <phase>compile</phase>
                            <goals>
                                <goal>site</goal>
                            </goals>
                            <configuration>
                                <artifacts>
                                	<!-- specify your depencies here -->
                                	<!-- groupId:artifactId:version -->
                                    <artifact>commons-io:commons-io:2.1</artifact>
                                    <artifact>commons-lang:commons-lang:2.4</artifact>
                                    <artifact>commons-lang:commons-lang:2.5</artifact>
                                    <artifact>commons-lang:commons-lang:2.6</artifact>
                                    <artifact>org.apache.commons:commons-lang3:3.1</artifact>
                                </artifacts>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    
        <repositories>
            <repository>
                <id>reficio</id>
                <url>http://repo.reficio.org/maven/</url>
            </repository>
        </repositories>
    
    </project>
```
There are many more configuration options, but basically that's the thing that you need. Now you only have to invoke mvn compile in the folder where the pom.xml file is located and when the process finishes your P2 site is ready!

You will see the following output:
```
    $ mvn compile
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
	target
    ├── repository
    │   ├── artifacts.jar
    │   ├── category.xml
    │   ├── content.jar
    │   └── plugins
    │       ├── org.apache.commons.io_2.1.0.jar
    │       ├── org.apache.commons.lang3_3.1.0.jar
    │       ├── org.apache.commons.lang_2.4.0.jar
    │       ├── org.apache.commons.lang_2.5.0.jar
    │       └── org.apache.commons.lang_2.6.0.jar
```
