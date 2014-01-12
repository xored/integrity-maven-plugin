Xored Integrity Maven Plugin
============================

Overview
--------

This plugin helps you to ensure build integrity in case of a large multi-module project with branchy structure.
For such a project, it is easy to forget to add a `pom.xml` file for a new module, or add a new module to list
of nested modules of the parent module. Especially, when you can run the project without Maven, e.g. when it's
an Eclipse RCP application, which can be built by Maven/Tycho, or run from the workspace by Eclipse.

Things get worse, when many people with different knowledge bases and responsibilities contribute to the project
(some of them might not know about Maven at all), or when the fact, that a module is missing, is not so easy to notice
(e.g. missing Eclipse plugin typically doesn't break a build).

This plugin scans project directory structure in attempt to find missed (not included to the build) modules.
If it finds any missed modules, it marks the build failed.

Configuration
-------------

Add this to the parent pom of your project:

    <pluginRepositories>
        <pluginRepository>
            <id>xored-releases</id>
            <url>http://maven.xored.com/nexus/content/repositories/releases/</url>
        </pluginRepository>
    </pluginRepositories>

    <build>
        <plugins>
            <plugin>
                <groupId>com.xored.maven</groupId>
                <artifactId>integrity-maven-plugin</artifactId>
                <version>1.0.1</version>
                <executions>
                    <execution>
                        <phase>validate</phase>
                        <goals>
                            <goal>verify-modules</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

After that, you can continue to build you project as usual (e.g. `mvn clean install`).

Plugin gets a list of all module directories of your project. Then, it determines the root of the entire project
by finding a directory, which is the common root for all of the modules. When it's done, it scans the filesystem
recursively starting from the project root directory. For each directory, which looks like a module, it checks,
if it exists in the list of modules. If it doesn't, it is a missed module, and the plugin marks the build failed.

How does the plugin decide, if a directory looks like a module? When no configuration is given, each directory,
which contains `pom.xml` file, is a module. You may redefine this behaviour by specifying includes/excludes
configuration elements. They are [Ant patterns](http://ant.apache.org/manual/dirtasks.html#patterns), which are applied
to paths, relative to the project root. For instance, the following configuration may be suitable for Eclipse RCP
projects, which are built using Maven/Tycho:

    <plugin>
        <groupId>com.xored.maven</groupId>
        <artifactId>integrity-maven-plugin</artifactId>
        <version>1.0.1</version>
        <configuration>
            <includes>
                <include>pom.xml</include>
                <include>feature.xml</include>
                <include>META-INF/MANIFEST.MF</include>
                <include>*.product</include>
            </includes>
            <excludes>
                <exclude>thirdparty</exclude>
                <exclude>*.updatesite</exclude>
                <exclude>product/**/bin</exclude>
            </excludes>
        </configuration>
        <executions>
          <execution>
            <phase>validate</phase>
            <goals>
                <goal>verify-modules</goal>
            </goals>
          </execution>
        </executions>
    </plugin>

All sub-directories of the project root, which have `pom.xml`, `feature.xml`, `*.product` files, or
`META-INF` directory with `MANIFEST.MF` file in it, are treated like modules. `META-INF` directory itself is **not**
treated as a module (if you want it to be a module, you need to specify `<include>MANIFEST.MF</include>` instead).
It is implied, that each pattern starts and ends with `**/`. You **should not** specify this explicitly.

`<exclude>` elements define directories, that are not treated as modules, even if they match any of `includes`
patterns. In the given example, `**/thirdparty/**`, `**/experimental/**`, `**/*.updatesite/**` and
`**/product/**/bin/**` directories are not modules.

The plugin automatically excludes build directories, so you **should not** explicitly specify anything like
`<exclude>target</exclude>`.
