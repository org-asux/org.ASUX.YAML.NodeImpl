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
 * @see org.ASUX.YAML.NodeImpl.ReadYamlEntry
 * @see org.ASUX.YAML.NodeImpl.ListYamlEntry
 * @see org.ASUX.YAML.NodeImpl.DeleteYamlEntry
 * @see org.ASUX.YAML.NodeImpl.ReplaceYamlEntry
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
        final String HDR = CLASSNAME + ": main(String[]): ";
        // org.ASUX.yaml.CmdLineArgsBasic cmdLineArgsBasic = null;
        org.ASUX.yaml.CmdLineArgs cmdlineargs = null;
        final java.io.StringWriter stdoutSurrogate = new java.io.StringWriter();

        try {
            cmdlineargs = CmdLineArgs.create( args );

            org.ASUX.YAML.NodeImpl.CmdInvoker cmdinvoker = new org.ASUX.YAML.NodeImpl.CmdInvoker( cmdlineargs.verbose, cmdlineargs.showStats );
            if (cmdlineargs.verbose) System.out.println( HDR +"getting started with cmdline args = " + cmdlineargs + " " );

            cmdinvoker.setYamlLibrary( cmdlineargs.YAMLLibrary );
            if (cmdlineargs.verbose) System.out.println( HDR +" set YAML-Library to [" + cmdlineargs.YAMLLibrary + " and [" + cmdinvoker.getYamlLibrary() + "]" );

            //=============================================================
            // read input, whether it's System.in -or- an actual input-file
            if (cmdlineargs.verbose) System.out.println( HDR +" about to load file: " + cmdlineargs.inputFilePath );
            final java.io.InputStream is1 = ( cmdlineargs.inputFilePath.equals("-") ) ? System.in
                    : new java.io.FileInputStream(cmdlineargs.inputFilePath);
            final java.io.Reader filereader = new java.io.InputStreamReader(is1);

            final Node inputNode = cmdinvoker.getYamlScanner().load( filereader );

            if (cmdlineargs.verbose) System.out.println( HDR +" loaded data = " + inputNode + " " );
            if (cmdlineargs.verbose) System.out.println( HDR +" loaded data of type [" + inputNode.getType() + "]" );

            // -----------------------
            // PRE-YAML processing
            switch ( cmdlineargs.cmdType ) {
                case READ:
                case LIST:
                case DELETE:
                case INSERT:
                case REPLACE:
                case TABLE:
                case MACRO:
                case MACROYAML:
                case BATCH:
                    break; // do nothing for now.
            }

            //======================================================================
            // run the command requested by user
            final Object outputAsIs = cmdinvoker.processCommand( cmdlineargs, inputNode );
            final Object output = (outputAsIs != null) ? outputAsIs : NodeTools.getEmptyYAML( GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter() );
            if (cmdlineargs.verbose) System.out.println( HDR +" processing of entire command returned [" + (output.getClass().getName()) + "]" );

            //======================================================================
            final java.io.Writer javawriter = ( cmdlineargs.outputFilePath.equals("-") )
                ? stdoutSurrogate // new java.io.FileWriter(TMP FILE)
                : new java.io.FileWriter(cmdlineargs.outputFilePath);

            final GenericYAMLWriter writer = cmdinvoker.getYamlWriter();
            final DumperOptions dumperopts = cmdinvoker.dumperopt;
            if (cmdlineargs.verbose) System.out.println( HDR +" GenericYAMLWriter writer has YAML-Library set to [" + writer.getYamlLibrary() + "]" );
            writer.prepare( javawriter, dumperopts );

            //======================================================================
            // post-completion of YAML processing
            switch ( cmdlineargs.cmdType ) {
                case READ:
                case LIST:
                case TABLE:
                case DELETE:
                case INSERT:
                case REPLACE:
                case MACRO:
                case MACROYAML:
                case BATCH:
                    Object humanFriendlyOutput = output; // if we have an array of just 1 element, let's dump the element and NOT the array.
                    if ( output instanceof SequenceNode ) {
                        final SequenceNode seqnode = (SequenceNode) output;
                        final java.util.List<Node> seqs = seqnode.getValue();
                        if ( seqs.size() == 1 ) {  // if it's a single 'item' that we found.. provide the user a more humanly-meaningful format. .. .. just provide that single element/Node.
                            if ( seqs.get(0) instanceof ScalarNode ) {
                                humanFriendlyOutput = seqs.get(0);
                            } // 2nd inner if
                        } // 1st inner if
                    } // outermost if

                    if (cmdlineargs.verbose) System.out.println( HDR +" final output is of type " + humanFriendlyOutput.getClass().getName() + "]" );
                    writer.write( humanFriendlyOutput, dumperopts );
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
                if (cmdlineargs.verbose) System.out.println( HDR +" dumping the final output to STDOUT" );
                String outputStr = stdoutSurrogate.toString();
                System.out.println( outputStr );
            }

        } catch ( org.apache.commons.cli.ParseException pe ) {
            // ATTENTION: If CmdLineArgs.java  and its subclasses threw an ParseException, they'll catch it themselves, showHelp(), and write debug output.
            // so.. do NOTHING in this class (Cmd.java)
            System.exit(9);
        } catch (YAMLPath.YAMLPathException ye) {
            if ( cmdlineargs == null || cmdlineargs.verbose ) ye.printStackTrace(System.err);
            System.err.println( ye +"\n"+ HDR +"\n\nERROR: YAML-Path pattern is invalid.\nCmdline arguments provided are: " + cmdlineargs + "\n" );
            System.exit(8);
        } catch (java.io.FileNotFoundException fnfe) {
            if ( cmdlineargs == null || cmdlineargs.verbose ) fnfe.printStackTrace(System.err);
            System.err.println( fnfe +"\n"+ HDR +"\n\nERROR: INPUT-File Not found: '" + cmdlineargs.inputFilePath + "'\nFYI: Cmdline arguments provided are: " + cmdlineargs + "\n" );
            System.exit(8);
        } catch (java.io.IOException ioe) {
            if ( cmdlineargs == null || cmdlineargs.verbose ) ioe.printStackTrace(System.err);
            System.err.println( ioe +"\n"+ HDR +"\n\nERROR: OUTPUT-File Not found: '" + cmdlineargs.outputFilePath + "'\nFYI: Cmdline arguments provided are: " + cmdlineargs + "\n" );
            System.exit(7);
        } catch (Exception e) {
            if ( cmdlineargs == null || cmdlineargs.verbose ) e.printStackTrace(System.err);
            System.err.println( e +"\n"+ HDR +"\n\nINTERNAL ERROR!\tFYI: Cmdline arguments provided are: " + cmdlineargs + "\n" );
            System.exit(6);
        } catch (Throwable t) {
            t.printStackTrace(System.err); // main() unit-testing
            System.err.println( t +"\n"+ HDR +"\n\nINTERNAL ERROR!\tFYI: Cmdline arguments provided are: " + cmdlineargs + "\n" );
            System.exit(6);
        }

    }

}
