/*
 BSD 3-Clause License
 
 Copyright (c) 2019, Udaybhaskar Sarma Seetamraju
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 * Neither the name of the copyright holder nor the names of its
 contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.ASUX.YAML.NodeImpl;

import org.ASUX.common.Debug;

import org.ASUX.yaml.CmdLineArgsBasic;
import org.ASUX.yaml.CmdLineArgs;
import org.ASUX.yaml.YAMLPath;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.FileNotFoundException;
import java.io.IOException;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <p> This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.
 * </p>
 * <p> This is technically an independent class, but it is semantically a 'subclass' of org.ASUX.yaml.Cmd</p>
 * <p> This class helps process YAML files using the java.util Collections classes, by leveraging the EsotericSoftware's YamlBeans library-</p>
 * <p> This class is the "wrapper-processor" for the various "YAML-commands" (which traverse a YAML file to do what you want).</p>
 * <p> The 4 YAML-COMMANDS are: <b>read/query, list, delete</b> and <b>replace</b>. </p>
 * <p> See full details of how to use these commands - in this GitHub project's wiki<br>
 * - or - in<br>
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project and its wiki.
 * </p>
 *
 * <p>
 * Example: <br>
 * <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml  --double-quote</code><br>
 * Example: <b><code>java org.ASUX.yaml.Cmd</code></b> will show all command
 * line options supported.
 * </p>
 * 
 * @see org.ASUX.yaml.YAMLPath
 * @see org.ASUX.yaml.CmdLineArgsBasic
 *
 * @see org.ASUX.yaml.NodeImpl.ReadYamlEntry
 * @see org.ASUX.yaml.NodeImpl.ListYamlEntry
 * @see org.ASUX.yaml.NodeImpl.DeleteYamlEntry
 * @see org.ASUX.yaml.NodeImpl.ReplaceYamlEntry
 */
public class Cmd {

    public static final String CLASSNAME = Cmd.class.getName();

    // private static final String TMP FILE = System.getProperty("java.io.tmpdir") +"/org.ASUX.yaml.STDOUT.txt";

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This is NOT testing code. It's actual means by which user's command line arguments are read and processed
     * @param args user's commandline arguments
     */
    public static void main( String[] args )
    {
        CmdLineArgsBasic cmdLineArgsBasic = null;
        CmdLineArgs cmdlineargs = null;
        final java.io.StringWriter stdoutSurrogate = new java.io.StringWriter();

        try {
            cmdLineArgsBasic = new CmdLineArgsBasic( args );
            if (cmdLineArgsBasic.verbose) { System.out.print( CLASSNAME + ": >>>>>>>>>>>>> "); for( String s: args) System.out.print(s);  System.out.println(); }

            cmdlineargs = cmdLineArgsBasic.getSpecificCmd();
            if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(): cmdlineargs=["+ cmdlineargs +"]" );

            org.ASUX.YAML.NodeImpl.CmdInvoker cmdinvoker = new org.ASUX.YAML.NodeImpl.CmdInvoker( cmdlineargs.verbose, cmdlineargs.showStats );
            if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(String[]): getting started with cmdline args = " + cmdlineargs + " " );

            cmdinvoker.setYamlLibrary( cmdLineArgsBasic.YAMLLibrary );
            if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(String[]): set YAML-Library to [" + cmdLineArgsBasic.YAMLLibrary + "]" );

            //======================================================================
            // read input, whether it's System.in -or- an actual input-file
            if (cmdLineArgsBasic.verbose) System.out.println(CLASSNAME + ": about to load file: " + cmdlineargs.inputFilePath );
            final java.io.InputStream is1 = ( cmdlineargs.inputFilePath.equals("-") ) ? System.in
                    : new java.io.FileInputStream(cmdlineargs.inputFilePath);
            final java.io.Reader filereader = new java.io.InputStreamReader(is1);

            // final org.ASUX.common.Output.Object<?> inputObj = cmdinvoker.getYamlScanner().load( filereader );
            final org.yaml.snakeyaml.nodes.Node inputObj = cmdinvoker.getYamlScanner().load( filereader );
            // if ( inputObj.getType() != OutputType.Type_LinkedHashMap && inputObj.getType() != OutputType.Type_KVPairs )
            //     throw new Exception("The input provided by '"+ cmdlineargs.inputFilePath +"' did Not return a proper YAML.  Got = "+ inputObj );

            if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(String[]): loaded data = " + inputObj + " " );
            if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(String[]): loaded data of type [" + inputObj.getType() + "]" );

            // -----------------------
            // PRE-YAML processing
            switch ( cmdLineArgsBasic.cmdType ) {
                case READ:
                case LIST:
                case DELETE:
                case INSERT:
                case REPLACE:
                case TABLE:
                case MACRO:
                case BATCH:
                    break; // do nothing for now.
            }

            //======================================================================
            // run the command requested by user
            final Object output = cmdinvoker.processCommand( cmdlineargs, inputObj );
            if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(String[]): processing of entire command returned [" + (output==null?"null":output.getClass().getName()) + "]" );

            //======================================================================
            final java.io.Writer javawriter = ( cmdlineargs.outputFilePath.equals("-") )
                ? stdoutSurrogate // new java.io.FileWriter(TMP FILE)
                : new java.io.FileWriter(cmdlineargs.outputFilePath);

            final GenericYAMLWriter writer = cmdinvoker.getYamlWriter();
            // writer.prepare( stdoutSurrogate, cmdlineargs.outputFilePath );
            writer.prepare( javawriter );

            //======================================================================
            // post-completion of YAML processing
            switch ( cmdLineArgsBasic.cmdType ) {
                case READ:
                case LIST:
                case TABLE:
                    // @SuppressWarnings("unchecked")
                    // final Node list = ( Node ) output;
                    // writer.write( list );
                    // break;
                case DELETE:
                case INSERT:
                case REPLACE:
                case MACRO:
                case BATCH:
                    // if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(String[]): saving the final output " + output + "]" );
                    if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(String[]): final output is of type " + output.getClass().getName() + "]" );
                    writer.write( output );
                    break;
            }

            //======================================================================
            // cleanup & close-out things.    This will actually do work for DELETE, INSERT, REPLACE and MACRO commands
            if ( cmdlineargs.outputFilePath.equals("-") ) {
                // if we're writing to STDOUT/System.out ..
                if (writer != null) writer.close(); // Yes! Even for stdout/System.out .. we need to call close(). This is driven by one the YAML libraries (eso teric soft ware)
            } else {
                if (writer != null) writer.close(); // close the actual file.
            }
            stdoutSurrogate.flush();

            // Now since we have a surrogate for STDOUT for use by , let's dump its output onto STDOUT!
            if ( cmdlineargs.outputFilePath.equals("-") ) {
                if (cmdLineArgsBasic.verbose) System.out.println( CLASSNAME + ": main(String[]): dumpingh the final output to STDOUT" );
                final String outputStr = stdoutSurrogate.toString();
                System.out.println( outputStr );
                // try {
                //     final java.io.InputStream istrm = new java.io.FileInputStream( TMP FILE );
                //     final java.io.Reader reader6 = new java.io.StringReader( outputStr );
                //     final java.util.Scanner scanner = new java.util.Scanner( reader6 );
                //     while (scanner.hasNextLine()) {
                //         System.out.println( scanner.nextLine() );
                //     }
                // } catch (java.io.IOException e) {
                //     e.printStackTrace(System.err);
                //     System.err.println( CLASSNAME + ": openBatchFile(): Failure to read Command-output contents ["+ outputStr +"]" );
                //     System.exit(102);
                // } catch (Exception e) {
                //     e.printStackTrace(System.err);
                //     System.err.println( CLASSNAME + ": openBatchFile(): Unknown Internal error: re: Command-output contents ["+ outputStr +"]" );
                //     System.exit(103);
                // }
            }

        } catch (YAMLPath.YAMLPathException e) {
            e.printStackTrace(System.err);
            System.err.println( "YAML-Path pattern is invalid: '" + cmdlineargs + "'.");
            System.exit(8);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace(System.err);
            System.err.println( "INPUT-File Not found: '" + cmdlineargs.inputFilePath + "'.");
            System.exit(8);
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( "OUTPUT-File Not found: '" + cmdlineargs.outputFilePath + "'.");
            System.exit(7);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( "Internal error: '" + cmdlineargs.outputFilePath + "'.");
            System.exit(6);
        }

    }

}
