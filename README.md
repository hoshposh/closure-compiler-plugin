Maven Closure Compiler Plugin
=============================

Intro
-----

Within the closure-compiler Google code project (http://code.google.com/p/closure-compiler/), they have added a couple
of convenience changes to make it easier to use the closure compiler within a Maven project.

1. The closure compiler can be resolved as a maven dependency:</p>


        <input name="email" type="email" title="A valid email, please" class="validate-email">

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

closure-compiler Dependencies
-----------------------------

This plugin has a dependency on the r916 version of the closure compiler.

Using Plugin
------------

Add this snippet to your pom.xml:

        <plugins>
          <plugin>
            <groupId>com.google.javascript</groupId>
            <artifactId>closure-compiler-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
          </plugin>
        </plugins>

By default this will
* bind to the package phase.
* look for resources that have a .js as an extension
* look in the src/main/webapp folder for resources
* audit the javascript, not write out compiled files
* use a compilation level of SIMPLE_OPTIMIZATIONS
* use a warning level of DEFAULT

An example of a pom.xml snippet to alter some of those defaults might look as follows:

        <plugins>
          <plugin>
            <groupId>com.google.javascript</groupId>
            <artifactId>closure-compiler-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
              <execution>
                <phase>package</phase>
                <goals>
                  <goal>compiler</goal>
                </goals>
              </execution>
            </executions>
            <configuration>
              <resourceDirectory>${basedir}/src/test/resources</resourceDirectory>
              <jsBuildDirectory>${basedir}/target/compiled-test</jsBuildDirectory>
              <jsFileName>test-js-build-file-min.js</jsFileName>
              <compilationLevel>SIMPLE_OPTIMIZATIONS</compilationLevel>
              <warningLevel>DEFAULT</warningLevel>
              <writeCompiledCode>true</writeCompiledCode>
              <force>true</force>
            </configuration>
          </plugin>
        </plugins>
