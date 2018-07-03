# jdocgit-maven-plugin
Jdocgit-Maven-Plugin is my 'hello world' Maven plugin. 
Running this mvn plugin generates JavaDoc for your project and pushes 
it to your remote Github repository. 

## Prerequisites

- Maven 3.x
- Master branch checked out
- Git push not prompting for credentials with your current setup.
- To host your Javadocs on GitHub Pages you need to enable building them
  from /docs folder in your repository settings.
  
## Usage

After installing this plugin to your local Maven repository, add this to your pom.xml's <project> tag.

```
    <build>
        <plugins>
            <plugin>
                <groupId>pl.szotaa</groupId>
                <artifactId>jdocgit-maven-plugin</artifactId>
                <version>1</version>
            </plugin>
        </plugins>
    </build>
```

To customize, you can specify variables like this

```
    <build>
         <plugins>
             <plugin>
                 <groupId>pl.szotaa</groupId>
                 <artifactId>jdocgit-maven-plugin</artifactId>
                 <version>1</version>
                 <configuration>
                     <subpackages>net</subpackages>
                     <commitMessage>javadocs update</commitMessage>
                     <timeout>100</timeout>
                 </configuration>
             </plugin>
         </plugins>
     </build>
```

- subpackages - which folder and its subdirectories should be 'javadoc'ed. Default = 'com'.
- commitMessage - commit message added to javadoc updating commit. Default = 'Update javadocs'.
- timeout - time in seconds after which plugin assumes it failed. 
Increase if you have a slow internet connection or generated Javadoc is huge. Default = '10'

Every variable is optional.

## Documentation

You can view JavaDocs for this Maven plugin <a href="https://szotaa.github.io/jdocgit-maven-plugin/"> here</a>.
