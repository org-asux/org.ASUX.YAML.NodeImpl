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

import org.ASUX.common.Tuple;
import org.ASUX.common.Output;
import org.ASUX.common.Debug;

import org.ASUX.yaml.YAML_Libraries;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.serializer.Serializer;

import static org.junit.Assert.*;

// DumperOptions.ScalarStyle = DOUBLE_QUOTED('"'), SINGLE_QUOTED('\''), LITERAL('|'), FOLDED('>'), PLAIN(null);
// https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

// import org.yaml.snakeyaml.constructor.SafeConstructor;

//=================================================================================
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//=================================================================================

public class GenericYAMLWriter {

    public static final String CLASSNAME = GenericYAMLWriter.class.getName();

    private boolean verbose;

    // https://yaml.org/spec/1.2/spec.html#id2762107
    protected org.yaml.snakeyaml.Yaml snakeYaml;
    protected java.io.Writer javaWriter;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    private YAML_Libraries sYAMLLibrary = YAML_Libraries.SNAKEYAML_Library;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     */
    public GenericYAMLWriter( final boolean _verbose ) {
        this.verbose = _verbose;
        init();
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * Tells you what internal implementation of the YAML read/parsing is, and by implication what the internal implementation for YAML-output generation is.
     * @return a reference to the YAML Library in use. See {@link org.ASUX.yaml.YAML_Libraries} for legal values.
     */
    public YAML_Libraries getYamlLibrary() {
        return this.sYAMLLibrary;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link org.ASUX.yaml.YAML_Libraries} for legal values to this parameter
     */
    public void setYamlLibrary( final YAML_Libraries _l ) {
        this.sYAMLLibrary = _l;
    }

    /**
     * Invoke this method to re-initialize this class, after completing a sequence of {@link #prepare} {@link #write} {@link close}
     */
    public void init() {
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  This method takes the java.io.Writer (whether StringWriter or FileWriter) and prepares the YAML library to write to it.
     *  @param _javawriter StringWriter or FileWriter (cannot be null)
     *  @param _dumperoptions important to pass in a non-null object, in case you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     *  @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void prepare( final java.io.Writer _javawriter, final DumperOptions _dumperoptions ) throws Exception
    {
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                // https://yaml.org/spec/1.2/spec.html#id2762107
                // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java
                this.snakeYaml = new org.yaml.snakeyaml.Yaml( _dumperoptions ); // GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter()
                this.javaWriter = _javawriter;
                break;

            case CollectionsImpl_Library:
            case ESOTERICSOFTWARE_Library:
            case ASUXYAML_Library:
            // default:
                final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
    } //function

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Write the YAML content (_output parameter) using the YAML-Library specified via {@link #setYamlLibrary} and to the java.io.Writer reference provided via {@link #prepare(java.io.Writer)}.
     * @param _output the content you want written out as a YAML file.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    // public void write( final Object _output ) throws Exception
    // {
    //     final org.yaml.snakeyaml.DumperOptions dumperOptions = GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter();
    //     this.write( _output, dumperOptions );
    // }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Write the YAML content (_output parameter) using the YAML-Library specified via {@link #setYamlLibrary}
     * and to the java.io.Writer reference provided via {@link #prepare}.
     * @param _output the content you want written out as a YAML file.
     * @param _dumperoptions a non-null reference to org.yaml.snakeyaml.DumperOptions instance.  CmdInvoker can provide this reference.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void write( final Object _output, final DumperOptions _dumperoptions ) throws Exception
    {   final String HDR = CLASSNAME +": write(): ";

        if ( _output == null ) {
            this.javaWriter.write("null");
            this.javaWriter.flush();
            return;
        }

        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                // https://yaml.org/spec/1.2/spec.html#id2762107
                // per https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/examples/CustomMapExampleTest.java
                // See also https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-collections
                if ( this.snakeYaml != null || this.javaWriter != null ) {
                    if ( this.verbose ) System.out.println( HDR +"_output is of type: "+ _output.getClass().getName() );
                    assertTrue( _output == null || _output instanceof Node || _output instanceof String );
                    @SuppressWarnings("unchecked")
                    final Node _outputNode = (Node) _output;
                    // this.snakeYaml.dump( _output, this.javaWriter );
                    final org.yaml.snakeyaml.emitter.Emitter snakeemitter = new Emitter( this.javaWriter, _dumperoptions );
                    final org.yaml.snakeyaml.resolver.Resolver resolver = new Resolver(); // we cannot pass NULL as 2nd parameter for BELOW-constructor for Serializer.  ugh!
                    // final org.yaml.snakeyaml.nodes.Tag tag = Tag.YAML; // Do not pass this as last parameter of BELOW-constructor for Serializer).  It will cause the 1st line to have '!!YAML'
                    final org.yaml.snakeyaml.serializer.Serializer serializer = new Serializer( snakeemitter, resolver, _dumperoptions, null );
                    try {
                        serializer.open();
                        serializer.serialize( _outputNode );
                        // while ( _outputdata.hasNext()) {
                        //    Node node = representer.represent(data.next()); // do NOT use org.yaml.snakeyaml.representer.Representer!!!!  It might cause as MANY problems as using org.yaml.snakeyaml.constructor.Constructor.
                        //    serializer.serialize(node);
                        // }
                        serializer.close();
                    } catch (java.io.IOException e) {
                        throw new Exception(e);
                    }
                    // //-------------------------------------------------
                    // org.yaml.snakeyaml.emitter.Emitter emitter = new org.yaml.snakeyaml.emitter.Emitter( _javawriter, new org.yaml.snakeyaml.DumperOptions() );
                    // try {
                    //     for ( org.yaml.snakeyaml.events.Event event : document) {
                    //         emitter.emit(event);
                    //     }
                    //     fail("Loading must fail for " + files[i].getAbsolutePath());
                    //     System.out.println("Loading must fail for " + files[i].getAbsolutePath());
                    // } catch( org.yaml.snakeyaml.error.YAMLException e ) {
                    // } catch (Exception e) {
                    // }
                } else {
                    throw new Exception( CLASSNAME +" write("+ this.getYamlLibrary() +"): cannot invoke write() before prepare()." );
                } // if esotericsoftwareWriter !=   null
                break;

            case CollectionsImpl_Library:
            case ESOTERICSOFTWARE_Library:
            case ASUXYAML_Library:
            // default:
                final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Call this in exactly the way you'd close a file after writing to it.  This method should be called ONLY after {@link #write} will no longer be invoked.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void close() throws Exception {
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                // Nothing to close if we use new org.yaml.snakeyaml.Yaml().dump();
                break;

            case CollectionsImpl_Library:
            case ESOTERICSOFTWARE_Library:
            case ASUXYAML_Library:
            default:
                final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * This method returns the default configuration options (that influece HOW YAML is written by SnakeYaml library).
     * @return the configuration as an object-instance of type org.yaml.snakeyaml.DumperOptions
     * @throws Exception some of the methods of org.yaml.snakeyaml.DumperOptions throw exceptions
     */
    public static final org.yaml.snakeyaml.DumperOptions defaultConfigurationForSnakeYamlWriter() throws Exception
    {

        // DumperOptions.ScalarStyle = DOUBLE_QUOTED('"'), SINGLE_QUOTED('\''), LITERAL('|'), FOLDED('>'), PLAIN(null);
        // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java
        final org.yaml.snakeyaml.DumperOptions dopt = new org.yaml.snakeyaml.DumperOptions(); // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java
        dopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.SINGLE_QUOTED );
                                                // other value are: PLAIN(a.k.a. nothing), DOUBLE_QUOTED, FOLDED('>')
        // dopt.setIndent( 3 );
        dopt.setCanonical( false );
        dopt.setPrettyFlow( true );
        dopt.setDefaultFlowStyle( org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK ); // BLOCK, FLOW or AUTO
        // dopt.setWidth( 80 ); // default is 80
        dopt.setSplitLines( false ); // do NOT Split up long lines
        // dopt.setTags( Map<String, String> _tags);
        // Not yet available in latest release:- dopt.setNonPrintableStyle( org.yaml.snakeyaml.DumperOptions.NonPrintableStyle.ESCAPE ); // When String contains non-printable characters SnakeYAML convert it to binary data with the !!binary tag. Set this to ESCAPE to keep the !!str tag and escape the non-printable chars with \\x or \\u
        return dopt;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
