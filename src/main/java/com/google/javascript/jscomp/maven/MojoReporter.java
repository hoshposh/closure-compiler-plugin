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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Simple print stream formatter for use with the Closure compiler.
 */
public class MojoReporter {

    private PrintStream outputStream;
    Writer writer = null;

    public MojoReporter(String encoding) throws IOException {
        this(null, encoding);
    }

    public MojoReporter(File reportFile) throws IOException {
        this((reportFile == null) ? null : new PrintStream(new FileOutputStream(reportFile), true), System.getProperty("file.encoding"));
    }

    public MojoReporter(PrintStream stream, String encoding) throws UnsupportedEncodingException {
        if (stream == null) {
            outputStream = System.out;
        } else {
            outputStream = stream;
        }

        writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), encoding), true);
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    /**
     * @return the outputStream
     */
    public PrintStream getOutputStream() {
        return outputStream;
    }
}
