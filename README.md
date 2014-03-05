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

For additional information, please take a look at the [site](http://xored.github.io/integrity-maven-plugin).