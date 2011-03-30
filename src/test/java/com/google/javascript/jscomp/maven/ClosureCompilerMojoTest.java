/*
 * Copyright 2001-2011 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.javascript.jscomp.maven;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import com.google.javascript.jscomp.JSSourceFile;

/**
 * Testcase for the ClosureCompilerMojo.
 */
public class ClosureCompilerMojoTest extends AbstractMojoTestCase
{
    /**
     * Test of findJavascriptFiles method, of class ClosureCompilerMojo.
     */
    public final void testFindJavascriptFiles() throws Exception
    {
        // listSystemProperties();
        ClosureCompilerMojo mojo = (ClosureCompilerMojo) lookupMojo("compiler",
            System.getProperty("basedir") + "/target/test-classes/config/compile-no-write-pom.xml");
        Method m = mojo.getClass().getDeclaredMethod("findJavascriptFiles", (Class<?>[])null);
        m.setAccessible(true);
        JSSourceFile[] files = (JSSourceFile[]) m.invoke(mojo, (Object[])null);
        assertNotNull(files);
        assertTrue(files.length == 2);
        for (JSSourceFile file : files) {
            assertTrue(file.getName().endsWith(".js"));
        }
    }

    /**
     * Test that excludes works and overides includes
     * @throws Exception
     */
    public void testFindWithExcludes() throws Exception
    {
        ClosureCompilerMojo mojo = (ClosureCompilerMojo) lookupMojo("compiler",
            System.getProperty("basedir") + "/target/test-classes/config/compile-no-write-excludes-pom.xml");
        Method m = mojo.getClass().getDeclaredMethod("findJavascriptFiles", (Class<?>[])null);
        m.setAccessible(true);
        JSSourceFile[] files = (JSSourceFile[]) m.invoke(mojo, (Object[])null);
        assertNotNull(files);
        assertTrue(files.length == 2);
        for (JSSourceFile file : files) {
            assertTrue(file.getName().endsWith(".js"));
        }
    }

    /**
     * Test full compiler process
     */
    public final void testProcessCompilerOnJavascripts() throws Exception
    {
        ClosureCompilerMojo compMojo = (ClosureCompilerMojo) lookupMojo("compiler",
            System.getProperty("basedir") + "/target/test-classes/config/compile-pom.xml");
        compMojo.execute();
    }

    /**
     * Test that a missing jsFileName is picked up when writing is set to true
     * @throws Exception
     */
    public void testMissingJsFileName() throws Exception
    {
        boolean exceptionThrown = false;
        ClosureCompilerMojo compMojo = (ClosureCompilerMojo) lookupMojo("compiler",
            System.getProperty("basedir") + "/target/test-classes/config/missing-filename-pom.xml");
        try {
            compMojo.execute();
        } catch (MojoExecutionException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    /**
     * Test that a missing jsFileName is picked up when writing is set to true
     * @throws Exception
     */
    public void testMissingJsBuildDirectory() throws Exception
    {
        boolean exceptionThrown = false;
        ClosureCompilerMojo compMojo = (ClosureCompilerMojo) lookupMojo("compiler",
            System.getProperty("basedir") + "/target/test-classes/config/missing-buildpath-pom.xml");
        try {
            compMojo.execute();
        } catch (MojoExecutionException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    /**
     * List all system properties
     */
    public final void listSystemProperties() {
        Properties props = System.getProperties();
        PrintWriter out = new PrintWriter(System.out, true);
        props.list(out);
    }
}
