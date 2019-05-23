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

// import java.io.FileNotFoundException;
// import java.io.IOException;

import java.util.ArrayList;
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
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions;

// import org.yaml.snakeyaml.constructor.SafeConstructor;

//##################################################################################
public class GenericYAMLScanner {

    public static final String CLASSNAME = GenericYAMLScanner.class.getName();
    private boolean verbose;

    private YAML_Libraries sYAMLLibrary = YAML_Libraries.ASUXYAML_Library;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     */
    public GenericYAMLScanner( final boolean _verbose ) {
        this.verbose = _verbose;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * Tells you what internal implementation of the YAML read/parsing is, and by implication what the internal implementation for YAML-output generation is.
     * @return a reference to the YAML Library in use. See {@link GenericYAMLScanner.YAML_Libraries} for legal values.
     */
    public YAML_Libraries getYamlLibrary() {
        return this.sYAMLLibrary;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link GenericYAMLScanner.YAML_Libraries} for legal values to this parameter
     */
    public void setYamlLibrary( final YAML_Libraries _l ) {
        this.sYAMLLibrary = _l;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This method will use the YAML-Library specified via {@link #setYamlLibrary} and load the YAML content (pointed to by the _inreader paramater).
     * @param _inreader either a StringReader or a FileReader
     * @return instance of {@link org.ASUX.common.Output.Object}
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public Node load( final java.io.Reader _inreader ) throws Exception
    {
        Node outputObj = null;
        if (this.verbose) System.out.println( CLASSNAME + ": load(java.io.Reader): this.getYamlLibrary()="+ this.getYamlLibrary() );

        // -----------------------
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                // https://yaml.org/spec/1.2/spec.html#id2762107
                // per https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/examples/CustomMapExampleTest.java
                // See also https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-collections
                // class MyCustomConstructor extends org.yaml.snakeyaml.constructor.Constructor {
                //     @Override
                //     protected Ma p<Object, Object> createDefaultMap(int initSize) {
                //         final Ma p<Object, Object> retval = (Ma p<Object, Object>) new LinkedHashMap<Object, Object>();
                //         return retval;
                //     }
                //     // @Override
                //     // protected Class<?> getClassForNode(Node node) {
                //     //     Class<? extends Object> classForTag = typeTags.get(node.getTag());
                //     //     if (classForTag == null) {
                //     //         Class<?> cl;
                //     //         try {
                //     //             String name = node.getTag().getClassName();
                //     //             cl = getClassForName(name);
                //     //         } catch (ClassNotFoundException e) {
                //     //             // This is where we override the PARENT class's definition of this function/method.
                //     //             // throw new YAMLException("Class not found: " + name);
                //     //             cl = String.class;
                //     //         } catch (org.yaml.snakeyaml.error.YAMLException e) {
                //     //             cl = String.class;
                //     //         }
                //     //         typeTags.put(node.getTag(), cl);
                //     //         return cl;
                //     //     } else {
                //     //         return classForTag;
                //     //     }
                //     // }
                // }; // inline class definition
                // Yaml yaml = new Yaml( new MyCustomConstructor() ); // see class-definition for MyCustomConstructor in above few lines
                // // Yaml.load() accepts a String or an InputStream object
                // // List<Object> list = (List<Object>) yaml.load( is1 );
                // @SuppressWarnings("unchecked")
                // final LinkedHashMap<String, Object> lhm22 = (LinkedHashMap<String, Object>) yaml.load( is1 );
                final org.yaml.snakeyaml.reader.StreamReader snkrdr = new org.yaml.snakeyaml.reader.StreamReader(_inreader);
                final Composer composer = new Composer( new org.yaml.snakeyaml.parser.ParserImpl(snkrdr), new org.yaml.snakeyaml.resolver.Resolver() );
                // final Node rootNode = composer.getSingleNode();
// {
//     System.out.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
//     final Composer composer2 = new Composer( new org.yaml.snakeyaml.parser.ParserImpl(snkrdr), new org.yaml.snakeyaml.resolver.Resolver() );
//     composer2.checkNode();
//     final java.io.StringWriter stdoutSurrogate = new java.io.StringWriter();
//     final GenericYAMLWriter snakewr = new GenericYAMLWriter( this.verbose );
//     snakewr.test( stdoutSurrogate, composer2.getNode() );
//     snakewr.close();
//     stdoutSurrogate.close();
//     final String outputStr = stdoutSurrogate.toString();
//     System.out.println(outputStr);
//     System.out.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
// }

                int numOfYamlDocuments = 0;
                final ArrayList<Node> docuArray = new ArrayList<Node>();

                // This while loop .. is about loading MULTIPLE (>= 1) YAML-documents -- separated by a '--'
                while ( composer.checkNode() ) { // Check if further documents are available.
                    // getNode(): Reads and composes the next document.
                    final Node n = composer.getNode(); // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/Node.java
                    if ( this.verbose ) System.out.println( CLASSNAME +" load(): document # "+ numOfYamlDocuments + " is of type "+ n.getNodeId() +" " );

                    outputObj = n; // Tools.recursiveConversion( n );

                    numOfYamlDocuments ++;
                    docuArray.add( outputObj );
                } // while

                if ( numOfYamlDocuments == 1 ) {
                    return outputObj;
                    // inputData.values().iterator().next(); // if this throws exception.. I've no idea what's going on.
                } else {
                    return docuArray.get(0);
                }
                // break;

            case CollectionsImpl_Library:
            case ESOTERICSOFTWARE_Library:
            case ASUXYAML_Library:
            default:
                final String es = CLASSNAME + ": main(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
        // return null;
    } //function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * Generates an empty YAML, which when output as JSON gives '{}', and as YAML gives you an empty file/string.
     */
    public Node getEmptyYAML() throws Exception {
        throw new Exception( CLASSNAME +".getEmptyYAML(): This method is NOT yet implemented! " );
        // new Mark( "startMark", 1, 1, 1, "String buffer", 1)
        // new Mark( "endMark",   1, 1, 1, "String buffer", 2)
        // new MappingNode( Tag.MAP, false, new List<NodeTuple>(), Mark startMark, Mark endMark, DumperOptions.FlowStyle.BLOCK ) ;
    }
}
