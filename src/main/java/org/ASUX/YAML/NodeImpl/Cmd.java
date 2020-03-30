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

import org.ASUX.yaml.YAMLCmdANTLR4Parser;
import org.ASUX.yaml.YAMLPath;
import org.ASUX.yaml.YAML_Libraries;
import org.ASUX.yaml.YAMLImplementation;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

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
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

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
     *  <p>This is the "ignition" for starting up the YAML-implementation and associated Factory-methods.</p>
     *  <p>This method is either invoked by {@link #main(String[])} (in response to end-user's cmdline) or by {@link org.ASUX.yaml.YAMLImplementation#startupYAMLImplementationFactory} (by Non-YAML github projects like org.ASUX.AWS-SDK or org.ASUX.AWS.CFN)</p>
     *  @param <T> either SnakeYAML's Node.class or LinkedHashMap&lt;String,Object&gt; for EsotericSoftware's YAML implementation
     *  @param _cmdLineArgs Everything passed as commandline arguments to the Java program {@link org.ASUX.yaml.CmdLineArgsCommon}
     *  @param _cmdInvoker NotNull instance (invariably a subclass of {@link org.ASUX.yaml.CmdInvoker})
     *  @return a NotNull reference to a subclass of {@link org.ASUX.yaml.YAMLImplementation} (specifically {@link org.ASUX.YAML.NodeImpl.NodeTools})
     *  @throws Exception Any potential thrown while starting up the YAML implementations (Example: {@link org.ASUX.yaml.YAMLImplementation#startupYAMLImplementationFactory} )
     */
    public static <T> org.ASUX.yaml.YAMLImplementation startYAMLImplementation(
                                final org.ASUX.yaml.CmdLineArgsCommon _cmdLineArgs, final org.ASUX.yaml.CmdInvoker<T> _cmdInvoker ) throws Exception
    {   final String HDR = CLASSNAME +": startYAMLImplementation("+ _cmdLineArgs +"): ";

        assertTrue( YAML_Libraries.isNodeImpl( _cmdLineArgs.YAMLLibrary ) );

        // first ensure key instance and class variables are NOT NULL
        if ( NodeTools.getDefaultDumperOptions() == null )
            NodeTools.setDefaultDumperOptions( GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter() );

        // create new instance of YAML-implementator code
        final NodeTools nt = new NodeTools( _cmdLineArgs.verbose );
        nt.setYAMLScanner( new GenericYAMLScanner( _cmdLineArgs.verbose ) );
        nt.setYAMLWriter ( new GenericYAMLWriter ( _cmdLineArgs.verbose ) );
        nt.getYAMLScanner().setYAMLLibrary( YAML_Libraries.SNAKEYAML_Library );
        nt.getYAMLWriter().setYAMLLibrary ( YAML_Libraries.SNAKEYAML_Library );
        nt.setDumperOptions( NodeTools.getDefaultDumperOptions() );

        // Store this YAML-implementor code .. for anyone to 'lookup' (including 20 lines below at the bottom of this method itself)
        YAMLImplementation.use( YAML_Libraries.SNAKEYAML_Library, nt ); // telling YAMLImplementation-factory about the specific YAMLImplementation Implementation
        YAMLImplementation.use( YAML_Libraries.NodeImpl_Library,  nt ); // telling YAMLImplementation-factory about the specific YAMLImplementation Implementation
        // Mote: 'SNAKEYAML_Library' and 'NodeImpl_Library' are equivalent

        //----------------------------------------
        // Configure based on command-line options provided by user
        switch( _cmdLineArgs.quoteType ) {
            case DOUBLE_QUOTED: nt.getDumperOptions().setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.DOUBLE_QUOTED );  break;
            case SINGLE_QUOTED: nt.getDumperOptions().setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.SINGLE_QUOTED );  break;
            case LITERAL:       nt.getDumperOptions().setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.LITERAL );        break;
            case PLAIN:         nt.getDumperOptions().setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN );          break;
            case FOLDED:        nt.getDumperOptions().setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.FOLDED );         break;
            default:            nt.getDumperOptions().setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.FOLDED );         break;
        }

        if (_cmdLineArgs.verbose) org.ASUX.YAML.NodeImpl.NodeTools.printDumperOptions( nt.getDumperOptions() );

        //----------------------------------------
        // tell the YAMLImplementation FACTORY about the new implementation
        final YAMLImplementation<Node> yi = YAMLImplementation.create( _cmdLineArgs.verbose, YAML_Libraries.SNAKEYAML_Library );

        // ensure the YAML implementation defined/specified (all of the above) is now known the rest of the code-base
        _cmdInvoker.setYAMLImplementation( (YAMLImplementation<T>) yi );
        assertSame( yi, nt ); // we should get back the same reference, that we 'stored' via use() invocation - about 20 lines above.
        if (_cmdLineArgs.verbose) System.out.println( HDR +" set YAML-Library to [" + _cmdLineArgs.YAMLLibrary );
        if (_cmdLineArgs.verbose) System.out.println( HDR +"while _cmdInvoker.getYAMLImplementation().getYAMLLibrary() =[" + _cmdInvoker.getYAMLImplementation().getYAMLLibrary() + "]" );

        return _cmdInvoker.getYAMLImplementation();
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This method is invoked from within {@link YAMLCmdANTLR4Parser}, whenever that class completely parses a YAML-command-line string, it invokes this method.
     * @param _cmdLineArgs user's commandline arguments
     */
    public static void go( org.ASUX.yaml.CmdLineArgsCommon _cmdLineArgs )
    {
        final String HDR = CLASSNAME + ": go(_cmdLineArgs): ";
        final java.io.StringWriter stdoutSurrogate = new java.io.StringWriter();

        try {
            // Step 1: create 'cmdinvoker'
            org.ASUX.YAML.NodeImpl.CmdInvoker cmdinvoker = new org.ASUX.YAML.NodeImpl.CmdInvoker( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            if (_cmdLineArgs.verbose) System.out.println( HDR +"getting started with cmdline args = " + _cmdLineArgs + " " );

            // Steps 2 & 3: Startup the factory for YAML-implementation.
            Cmd.startYAMLImplementation( _cmdLineArgs, cmdinvoker );
            // For other projects in the org.ASUX family, especially those that do NOT want to know the YAML-implementation.. use the following line instead.
            // YAMLImplementation<Node> startupYAMLImplementationFactory( _cmdLineArgs.getYAMLLibrary(), _cmdLineArgs, _cmdInvoker );

            //=============================================================
            // Step 4 on.. start processing...

            // read input, whether it's System.in -or- an actual input-file
            if (_cmdLineArgs.verbose) System.out.println( HDR +" about to load file: " + _cmdLineArgs.inputFilePath );
            final java.io.InputStream is1 = ( _cmdLineArgs.inputFilePath.equals("-") ) ? System.in
                    : new java.io.FileInputStream(_cmdLineArgs.inputFilePath);
            final java.io.Reader filereader = new java.io.InputStreamReader(is1);

            final Node inputNode = cmdinvoker.getYAMLImplementation().load( filereader );

            if (_cmdLineArgs.verbose) System.out.println( HDR +" loaded data = " + inputNode + " " );
            if (_cmdLineArgs.verbose) System.out.println( HDR +" loaded data of type [" + inputNode.getType() + "]" );

            // -----------------------
            // PRE YAML-Cmd processing
            switch ( _cmdLineArgs.cmdType ) {
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
            final Object outputAsIs = cmdinvoker.processCommand( _cmdLineArgs, inputNode );
            final NodeTools nodetools =(NodeTools) cmdinvoker.getYAMLImplementation();
            final Object output = (outputAsIs != null) ? outputAsIs : NodeTools.getEmptyYAML( nodetools.getDumperOptions() );
            if (_cmdLineArgs.verbose) System.out.println( HDR +" processing of entire command returned [" + (output.getClass().getName()) + "]" );

            //======================================================================
            final java.io.Writer javawriter = ( _cmdLineArgs.outputFilePath.equals("-") )
                    ? stdoutSurrogate // new java.io.FileWriter(TMP FILE)
                    : new java.io.FileWriter(_cmdLineArgs.outputFilePath);

            // final GenericYAMLWriter writer = cmdinvoker.getYamlWriter();
            // if (_cmdLineArgs.verbose) System.out.println( HDR +" GenericYAMLWriter writer has YAML-Library set to [" + writer.getYamlLibrary() + "]" );
            // cmdinvoker.getYAMLImplementation().prepare( javawriter, cmdinvoker.dumperopt );

            //======================================================================
            // post-completion of YAML-Cmd processing
            if ( output != null ) {
                switch ( _cmdLineArgs.cmdType ) {
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
                            if (_cmdLineArgs.verbose) System.out.println( HDR +" Human-friendly output possible.  SequenceNode has "+ seqs.size() +"elements." );
                            if ( seqs.size() == 1 ) {  // if it's a single 'item' that we found.. provide the user a more humanly-meaningful format. .. .. just provide that single element/Node.
                                // if ( seqs.get(0) instanceof ScalarNode ) { // we don't care if it's a ScalarNode, MappingNode or SequenceNode
                                humanFriendlyOutput = seqs.get(0);
                                // } // 2nd inner if
                            } // 1st inner if
                        } // outermost if

                        if (_cmdLineArgs.verbose) System.out.println( HDR +" final output is of type " + humanFriendlyOutput.getClass().getName() + "]" );
                        cmdinvoker.getYAMLImplementation().write( javawriter, humanFriendlyOutput );
                        break;
                } // switch
            } // if output != null

            //======================================================================
            // cleanup & close-out things.    This will actually do work for DELETE, INSERT, REPLACE and MACRO commands
            if ( _cmdLineArgs.outputFilePath.equals("-") ) {
                // if we're writing to STDOUT/System.out ..
                if ( output == null ) System.out.println("null");
                cmdinvoker.getYAMLImplementation().close(); // Yes! Even for stdout/System.out .. we need to call close(). This is driven by one the YAML libraries (eso teric soft ware's implementation)
            } else {
                cmdinvoker.getYAMLImplementation().close(); // close the actual file.
            }
            stdoutSurrogate.flush();

            // Now since we have a surrogate for STDOUT for use by , let's dump its output onto STDOUT!
            if ( _cmdLineArgs.outputFilePath.equals("-") ) {
                if (_cmdLineArgs.verbose) System.out.println( HDR +" dumping the final output to STDOUT" );
                String outputStr = stdoutSurrogate.toString();
                System.out.println( outputStr );
            }

        } catch ( org.apache.commons.cli.ParseException pe ) {
            // ATTENTION: If CmdLineArgs.java  and its subclasses threw an ParseException, they'll catch it themselves, showHelp(), and write debug output.
            // so.. do NOTHING in this class (Cmd.java)
            System.exit(9);
        } catch (YAMLPath.YAMLPathException ye) {
            if ( _cmdLineArgs == null || _cmdLineArgs.verbose ) ye.printStackTrace(System.err);
            System.err.println( ye +"\n"+ HDR +"\n\nERROR: YAML-Path pattern is invalid.\nCmdline arguments provided are: " + _cmdLineArgs + "\n" );
            System.exit(8);
        } catch (java.io.FileNotFoundException fnfe) {
            if ( _cmdLineArgs == null || _cmdLineArgs.verbose ) fnfe.printStackTrace(System.err);
            System.err.println( fnfe +"\n"+ HDR +"\n\nERROR: INPUT-File Not found: '" + _cmdLineArgs.inputFilePath + "'\nFYI: Cmdline arguments provided are: " + _cmdLineArgs + "\n" );
            System.exit(8);
        } catch (java.io.IOException ioe) {
            if ( _cmdLineArgs == null || _cmdLineArgs.verbose ) ioe.printStackTrace(System.err);
            System.err.println( ioe +"\n"+ HDR +"\n\nERROR: OUTPUT-File Not found: '" + _cmdLineArgs.outputFilePath + "'\nFYI: Cmdline arguments provided are: " + _cmdLineArgs + "\n" );
            System.exit(7);
        } catch (Exception e) {
            if ( _cmdLineArgs == null || _cmdLineArgs.verbose ) e.printStackTrace(System.err);
            System.err.println( e +"\n"+ HDR +"\n\nINTERNAL ERROR!\tFYI: Cmdline arguments provided are: " + _cmdLineArgs + "\n" );
            System.exit(6);
        } catch (Throwable t) {
            t.printStackTrace(System.err); // main() unit-testing
            System.err.println( t +"\n"+ HDR +"\n\nINTERNAL ERROR!\tFYI: Cmdline arguments provided are: " + _cmdLineArgs + "\n" );
            System.exit(6);
        }

    } // go()

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This is NOT testing code. It's actual means by which user's command line arguments are read and processed
     * @param args user's commandline arguments
     */
    public static void main( String[] args )
    {
        org.ASUX.yaml.Cmd.main( args );

        // final String HDR = CLASSNAME + ": main(String[]): ";
        // org.ASUX.yaml.CmdLineArgs cmdLineArgs = null;
        // org.ASUX.yaml.CmdLineArgsBasic cmdLineArgsBasic = null;
        //
        // try {
        //     // cmdLineArgs = CmdLineArgs.create( args );
        //     cmdLineArgsBasic = new org.ASUX.yaml.CmdLineArgsBasic();
        //     cmdLineArgsBasic.parse( args );
        //     // Until we get past the above statement, we don't know about 'verbose'
        //     if (cmdLineArgsBasic.verbose) { System.out.print( HDR +" >>>>>>>>>>>>> "); for( String s: args) System.out.print(s);  System.out.println(); }
        //
        //     new YAMLCmdANTLR4Parser( cmdLineArgsBasic.verbose ).parseYamlCommandLine( String.join(" ", args) );
        //
        // } catch (Exception e) {
        //     if ( cmdLineArgs == null || cmdLineArgs.verbose ) e.printStackTrace(System.err);
        //     System.err.println( e +"\n"+ HDR +"\n\nINTERNAL ERROR!\tFYI: Cmdline arguments provided are: " + cmdLineArgs + "\n" );
        //     System.exit(6);
        // } catch (Throwable t) {
        //     t.printStackTrace(System.err); // main() unit-testing
        //     System.err.println( t +"\n"+ HDR +"\n\nINTERNAL ERROR!\tFYI: Cmdline arguments provided are: " + cmdLineArgs + "\n" );
        //     System.exit(6);
        // }

    } // main()

}
