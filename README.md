Xored Integrity Maven Plugin
============================

Overview
--------

This plugin helps you to ensure build integrity in case of a large multi-module project with branchy structure.
For such a project, it is easy to forget to add a `pom.xml` file for a new module, or add a new module to list
of nested modules of the parent module. Especially, when you can run the project without Maven, e.g. when it's
an Eclipse RCP application, which is built by Maven/Tycho on CI server, but developers run it from the workspace in
Eclipse.

Things get worse, when many people with wide range of experience and responsibilities contribute to the project
(some of them might not know about Maven at all), or when the fact, that a module is missing, is not so easy to notice
(e.g. missing Eclipse plugin typically doesn't break a build).

This plugin scans project directory structure in attempt to find missed (not included to the build) modules.
If it finds any missed modules, it marks the build failed.

Basic Setup
-----------

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
                <version>LATEST_VERSION*</version>
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

(\* When I was writing this doc, the latest version was 1.0.1.)

After that, you can continue to build you project as usual (e.g. `mvn clean install`).

The plugin gets a list of all module directories of your project (reactor project directories). Then, it determines
the root of the entire project (the common root of all reactor project directories). When it's done, it scans the filesystem
recursively starting from the root. For each directory, which looks like a module, it checks,
if it exists in the list of reactor projects. If it doesn't, it is a missed module, and the plugin marks the build failed.

Configuration & Example
-----------------------

How does the plugin decide, if a directory looks like a module? When no configuration is given, each directory,
which contains `pom.xml` file, is a module. You may redefine this behaviour by specifying includes/excludes
configuration elements. They are [Ant patterns](http://ant.apache.org/manual/dirtasks.html#patterns), which are applied
to paths, relative to the project root. It is implied, that each pattern starts and ends with `**/`. You **should not**
specify this explicitly.

Suppose, you have the following config in your parent pom:

    <plugin>
        <groupId>com.xored.maven</groupId>
        <artifactId>integrity-maven-plugin</artifactId>
        <version>1.0.1</version>
        <executions>
            <execution>
                <id>verify-modules</id>
                <phase>validate</phase>
                <goals>
                    <goal>verify-modules</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <includes>
                <include>pom.xml</include>
                <include>feature.xml</include>
                <include>META-INF/MANIFEST.MF</include>
            </includes>
            <excludes>
                <exclude>repository</exclude>
                <exclude>product/**/bin</exclude>
                <exclude>platform/**/META-INF/maven/**/pom.xml</exclude>
                <exclude>*.updatesite/build</exclude>
            </excludes>
        </configuration>
    </plugin>

This means, that
* Sub-directories, which match any of the patterns `**/pom.xml/**`, `**/feature.xml/**`,
  `**/META-INF/MANIFEST.MF/**`, are modules. Note, that exact module directory is determined by the first path element,
  which matches meaningful part of the pattern (not `**/`). So, if you have `a/b/c/META-INF/MANIFEST.MF`, `a/b/c` would
  be a module, but not `a/b/c/META-INF`. If you want `a/b/c/META-INF` to be a module, you should specify
  `<include>MANIFEST.MF</include>` instead.
* Sub-directories, which match any of the patterns `**/repository/**`, `**/product/**/bin/**`,
  `**/platform/**/META-INF/maven/**/pom.xml/**`, `**/*.updatesite/build/**`, are **not** modules, even if they match
  any of the `includes` patterns.

Suppose, that this is your project structure:

    .
    ├── platform
    │   ├── features
    │   │   ├── feature-1
    │   │   │   ├── feature.xml
    │   │   │   └── pom.xml
    │   │   ├── feature-1.updatesite
    │   │   │   └── build
    │   │   │       └── feature.xml
    │   │   └── feature-2
    │   │       └── feature.xml
    │   ├── plugins
    │   │   ├── plugin-1
    │   │   │   ├── META-INF
    │   │   │   │   └── MANIFEST.MF
    │   │   │   └── pom.xml
    │   │   ├── plugin-2
    │   │   │   └── META-INF
    │   │   │       ├── maven
    │   │   │       │   └── a
    │   │   │       │       └── pom.xml
    │   │   │       └── MANIFEST.MF
    │   │   └── plugin-3
    │   │       └── META-INF
    │   │           └── this-is-not-a-module.txt
    │   ├── repository
    │   │   └── q
    │   │       └── pom.xml
    │   └── pom.xml
    ├── product
    │   └── a
    │       └── b
    │           └── bin
    │               └── META-INF
    │                   └── MANIFEST.MF
    └── releng
        └── parent
            └── pom.xml

As you can see, this resembles Eclipse RCP project, which is built by Maven/Tycho. Suppose also, that you run it
like this:

    $ cd releng/parent
    $ mvn verify

In this case, modules are:
* `platform/features/feature-1`
* `platform/features/feature-2`
* `platform/plugins/plugin-1`
* `platform/plugins/plugin-2`
* `platform`
* `releng/parent`

Any other directory is not a module.

Modules `platform/features/feature-2` and `platform/plugins/plugin-2` are surely missed, since they lack `pom.xml`.
So, the build will fail. The other modules may be missed as well, if they are not included to the parent poms, thus
not included to the reactor build.

Notes
-----

The plugin automatically excludes build directories of reactor projects, so you **should not** explicitly specify
anything like `<exclude>path/to/my/module/target</exclude>`.
