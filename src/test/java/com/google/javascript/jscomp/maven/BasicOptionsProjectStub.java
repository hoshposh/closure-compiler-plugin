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

import java.util.ArrayList;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

/**
 * <code>BasicOptionsProjectStub</code> acts as a project stub
 * for the ClosureCompilerMojoTest.java tests - the mojo needs
 * access to basic project information
 */
public class BasicOptionsProjectStub extends MavenProjectStub
{
    //-------------------
    // member variables
    //-------------------
    protected Build build = new Build();

    public BasicOptionsProjectStub()
    {
        //setup stubs information so the mojo behaves correctly
        setCompileSourceRoots(new ArrayList<String>(0));
        setBuildOutputDirectory(System.getProperty("basedir")+"/target");
    }

    @Override
    public Build getBuild()
    {
        return build;
    }

    /**
     * <code>setBuildOutputDirectory</code> sets the outputdirectory
     * for the tests that the mojo uses
     *
     * @param buildOutputDirectory a <code>String</code> value
     */
    public void setBuildOutputDirectory(String buildOutputDirectory)
    {
        System.out.println("setting outputdir: " + buildOutputDirectory);
        build.setOutputDirectory(buildOutputDirectory);
    }
}
