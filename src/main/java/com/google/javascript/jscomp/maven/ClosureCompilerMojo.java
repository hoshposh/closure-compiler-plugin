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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DirectoryScanner;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.WarningLevel;

/**
 * Compile and validate javascript files with Google closure compiler tool.
 *
 * @goal compiler
 * @phase package
 */
public class ClosureCompilerMojo extends AbstractMojo
{
    /**
     * The default maven project object
     *
     * @parameter
     *     expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Single directory for extra files to include in the WAR.
     *
     * @parameter default-value="${basedir}/src/main/webapp"
     * @required
     */
    private File resourceDirectory;

    /**
     * The root path used to generate the compiled javascript file.
     * Only required if the <code>writeCompiledCode</code> is set to
     * true.
     *
     * @parameter default-value="${basedir}/target"
     */
    private File jsBuildDirectory;

    /**
     * The name of the compiled javascript file
     * Only required if the <code>writeCompiledCode</code> is set to
     * true.
     *
     * @parameter expression="${closure.jsFileName}
     */
    private String jsFileName;

    /**
     * Output file name.
     *
     * @parameter expression="${closure.loggerFileName}"
     */
    private String loggerFileName;

    /**
     * compilation level which can be
     * <ul><li>WHITESPACE_ONLY</li>
     * <li>SIMPLE_OPTIMIZATIONS</li>
     * <li>ADVANCED_OPTIMIZATIONS</li></ul>
     *
     * @parameter expression="${closure.compilationLevel}" default-value="SIMPLE_OPTIMIZATIONS"
     */
    private String compilationLevel;

    /**
     * warning level, which can be:
     * <ul><li>QUIET</li>
     * <li>DEFAULT</li>
     * <li>VERBOSE</li></ul>
     *
     * @parameter expression="${closure.warningLevel}" default-value="DEFAULT"
     */
    private String warningLevel;

    /**
     * boolean value to know if the compiled code has to be output in the target file.
     * False by default, to only audit javascript code.
     *
     * @parameter expression="${closure.writeCompiledCode}" default-value="false"
     */
    private boolean writeCompiledCode;

    /**
     * A set of file patterns to include for processing
     * @parameter
     */
    private final String[] includes = new String[]{"**/*.js"};

    /**
     * A set of file patterns to exclude for processing
     * @parameter
     */
    private String[] excludes;

    /**
     * A parameter that allows the compilation to occur regardless of the staleness
     * of the generated files
     *
     * @parameter default-value="false"
     */
    private boolean force;


    private File m_outputFile;
    private JSSourceFile[] m_jsFiles;

    @Override
    public void execute() throws MojoExecutionException
    {
        checkParameters();
        processCompilerOnJavascripts();
    }

    private void checkParameters() throws MojoExecutionException
    {
        if (writeCompiledCode) {
            StringBuilder content = new StringBuilder("writeCompiledCode is set to true and ");
            if (jsBuildDirectory == null) {
                content.append("jsBuildDirectory is not set.");
                getLog().error(content);
                throw new MojoExecutionException(content.toString());
            }
            if (jsFileName == null || jsFileName.trim().length() == 0) {
                content.append("jsFileName is not set.");
                getLog().error(content);
                throw new MojoExecutionException(content.toString());
            }
        }
    }

    /**
     * process closure compiler on javascript files found in folder hierarchy (by default "/target" under basedir)
     *
     * @param jsBasedir
     * @throws MojoExecutionException Thrown if the compilation of Javascript fails
     */
    private void processCompilerOnJavascripts() throws MojoExecutionException
    {
        // If needed, create the output root folder hierarchy
        jsBuildDirectory.mkdirs();
        if (writeCompiledCode) {
            m_outputFile = new File(jsBuildDirectory, jsFileName);
            if (!m_outputFile.getParentFile().exists()) {
                m_outputFile.getParentFile().mkdirs();
            }
        }

        //  find js files
        getLog().debug("find js files");
        m_jsFiles = findJavascriptFiles();
        if (0 != m_jsFiles.length && isStale()) {
            MojoReporter reporter = null;
            File loggerFile = (loggerFileName == null || loggerFileName.trim().length() == 0) ? null :
                new File(jsBuildDirectory + File.separator + loggerFileName);
            try {
                reporter = new MojoReporter(loggerFile);
                String jsCompiled = processCompiler(reporter.getOutputStream());
                writeJsCompiledFile(jsCompiled);
            } catch (IOException ex) {
                getLog().error(ex);
            } finally {
                if (reporter != null) {
                    try {
                        reporter.close();
                    } catch (IOException ex) {
                        getLog().error(ex);
                    }
                }
            }
        }
    }

    /**
     * Process compilation using {@link Compiler} possibilities
     *
     * @param ps output stream used to trace compiler logs
     *
     * @return String of compiled source
     * @throws MojoExecutionException Thrown if the compilation fails
     */
    private String processCompiler(PrintStream ps) throws MojoExecutionException
    {
        Compiler compiler = new Compiler(ps);
        CompilerOptions options = new CompilerOptions();
        CompilationLevel level = convertStringToEnumValue(CompilationLevel.class, compilationLevel);
        level.setOptionsForCompilationLevel(options);
        WarningLevel wLevel = convertStringToEnumValue(WarningLevel.class, warningLevel);
        wLevel.setOptionsForWarningLevel(options);

        Result result = compiler.compile(new JSSourceFile[]{}, m_jsFiles, options);
        if (!result.success) {
            throw new MojoExecutionException("Javascript compilation failed.");
        }
        return compiler.toSource();
    }

    /**
     * If {@link #writeCompiledCode} is set to true, try to write compiled code to the target file
     *
     * @param m_jsFiles array of javascript files given to compiler
     * @param jsCompiledCode array of compiled code (in the same order than jsFiles).
     */
    private void writeJsCompiledFile(String jsCompiledCode)
    {
        getLog().debug("write js files: " + writeCompiledCode);
        if (writeCompiledCode) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(m_outputFile);
                fw.write(jsCompiledCode);
                fw.flush();
            } catch (IOException ex) {
                getLog().error(ex);
            } finally {
                try {
                    fw.close();
                } catch (IOException ex) {
                    getLog().error(ex);
                }
            }
        }
    }

    /**
     * Search all files ended with ".js" extension
     *
     * @return the array of {@link JSSourceFile}s found. Return an empty array when no file is found.
     */
    private JSSourceFile[] findJavascriptFiles()
    {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(resourceDirectory);
        if (includes != null && includes.length > 0) {
            ds.setIncludes(includes);
        }
        if (excludes != null && excludes.length > 0) {
            ds.setExcludes(excludes);
        }
        ds.scan();
        String[] filesFound = ds.getIncludedFiles();

        List<SourceFile> v = new ArrayList<SourceFile>();
        if (null != filesFound) {
            for (String jsFile : filesFound) {
                v.add(JSSourceFile.fromFile(resourceDirectory + File.separator + jsFile));
            }
        }
        return (v.toArray(new JSSourceFile[v.size()]));
    }

    /**
     * Utility method to match an enum Object of a Class using his String name
     *
     * @param classX the class containing the enum values
     * @param enumValue the string name of the enum occur
     * @return the enum Object found of null by default.
     */
    private <T extends Enum<T>> T convertStringToEnumValue(Class<T> classX, String enumValue)
    {
        try {
            if (null != classX) {
                return Enum.valueOf(classX, enumValue);
            }
        } catch (IllegalArgumentException ex) {
            getLog().warn(ex);
        }
        return null;
    }

    /**
     * Determine if compilation must actually happen, i.e. if any input file
     * (extern or source) has changed after the outputFile was last modified.
     *
     * @return true if compilation should happen
     */
    private boolean isStale()
    {
        boolean ret = true;
        if (writeCompiledCode && !(force)) {
            long lastRun = m_outputFile.lastModified();
            long sourcesLastModified = getLastModifiedTime(m_jsFiles);

            ret = lastRun <= sourcesLastModified;
        }
        return ret;
    }

    private long getLastModifiedTime(JSSourceFile[] fileLists) {
        long lastModified = 0;
        for (JSSourceFile source : fileLists) {
            File file = new File(source.getOriginalPath());
            long fileLastModified = file.lastModified();
            // If the file is absent, we don't know if it changed (maybe
            // was deleted), so assume it has just changed.
            if (fileLastModified == 0) {
                fileLastModified = new Date().getTime();
            }
            lastModified = Math.max(fileLastModified, lastModified);
        }
        return lastModified;
    }
}
