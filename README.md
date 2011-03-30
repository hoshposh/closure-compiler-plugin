Maven Closure Compiler Plugin
=============================

Intro
-----

Within the closure-compiler Google code project (http://code.google.com/p/closure-compiler/), they have added a couple
of convenience changes to make it easier to use the closure compiler within a Maven project.

1. The closure compiler can be resolved as a maven dependency:</p>

    <dependency>
      <groupId>com.google.javascript</groupId>
      <artifactId>closure-compiler</artifactId>
      <version>r916</version>
    </dependency>

2. The provide an ant task in their source tree within the following class:
[com.google.javascript.jscomp.ant.CompileTask.java](http://code.google.com/p/closure-compiler/source/browse/trunk/src/com/google/javascript/jscomp/ant/CompileTask.java)

Unfortunately, to use the compiler within a maven project still requires you to rely on the maven
ant plugin to expose the ant task to your project.

This small project has a goal of providing a native maven plugin that can be declared and used in
maven projects without the ant wrapper, with the hopeful benefit of making the usage pattern
cleaner.