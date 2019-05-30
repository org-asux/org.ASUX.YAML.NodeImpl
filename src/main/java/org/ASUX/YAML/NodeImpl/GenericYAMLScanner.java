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
import org.yaml.snakeyaml.composer.Composer; // needed within this class
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
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

/*
    USEast1: !Equals
    <NodeTuple keyNode=<org.yaml.snakeyaml.nodes.ScalarNode (tag=tag:yaml.org,2002:str, value=USEast1)>; valueNode=<org.yaml.snakeyaml.nodes.SequenceNode (tag=!Equals, value= [ an array of what is in 2 lines below ]
        - !Ref 'AWS::Region'
        <org.yaml.snakeyaml.nodes.ScalarNode (tag=!Ref, value=AWS::Region)>
        - us-east-1
        <org.yaml.snakeyaml.nodes.ScalarNode (tag=tag:yaml.org,2002:str, value=us-east-1)>
        - !Condition ${ASUX::USEast1}
        <org.yaml.snakeyaml.nodes.ScalarNode (tag=!Condition, value=${ASUX::USEast1})>
        - !Condition USEast2
        <org.yaml.snakeyaml.nodes.ScalarNode (tag=!Condition, value=USEast2)>

Per the SnakeYAML Reference implementation: The '!___' assumes only ALPHANUMERICs _ - after the BANG/exclamation.  Pretty-Much a rigid JAVA CLASSNAME naming-convention-requirement.
See ALPHA_S inside https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/scanner/Constant.java

*/




    /**
     * This method will use the YAML-Library specified via {@link #setYamlLibrary} and load the YAML content (pointed to by the _inreader paramater).
     * @param _inreader either a StringReader or a FileReader
     * @return instance of {@link org.ASUX.common.Output.Object}
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public Node load( final java.io.Reader _inreader ) throws Exception
    {
        if (this.verbose) System.out.println( CLASSNAME + ": load(java.io.Reader): this.getYamlLibrary()="+ this.getYamlLibrary() );

        // -----------------------
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                // https://yaml.org/spec/1.2/spec.html#id2762107
                // per https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/examples/CustomMapExampleTest.java
                // See also https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-collections
                final org.yaml.snakeyaml.reader.StreamReader snkrdr = new org.yaml.snakeyaml.reader.StreamReader( _inreader );
                final Composer composer = new Composer( new org.yaml.snakeyaml.parser.ParserImpl(snkrdr), new org.yaml.snakeyaml.resolver.Resolver() ); // last/2nd CANNOT be null.  Resolver.class instance is required.

                // final Node rootNode = composer.getSingleNode();

                int numOfYamlDocuments = 0;
                final ArrayList<Node> docuArray = new ArrayList<Node>();
                Node outputObj = null;

                // This while loop .. is about loading MULTIPLE (>= 1) YAML-documents -- separated by a '--'
                while ( composer.checkNode() ) { // Check if further documents are available.
                    // getNode(): Reads and composes the next document.
                    final Node n = composer.getNode(); // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/Node.java
                    if ( this.verbose ) System.out.println( CLASSNAME +" load(): document # "+ numOfYamlDocuments + " is of type "+ n.getNodeId() +" " );
                    if ( this.verbose ) System.out.println( NodeTools.Node2YAMLString( n )  ) ;

                    outputObj = n;

                    numOfYamlDocuments ++;
                    docuArray.add( outputObj );
                } // while

                if ( numOfYamlDocuments <= 0 ) {
                    return new ScalarNode( Tag.NULL, "null", null, null, DumperOptions.ScalarStyle.PLAIN ); // This should be representing an empty YAML.  I hope!
                } else if ( numOfYamlDocuments == 1 ) {
                    return outputObj;
                } else {
                    throw new Exception( CLASSNAME +" load(): we have "+ numOfYamlDocuments + " documents within a single YAML file.  org.ASUX.YAML libraries are currently not sophisticated to handle multiple YAML in a single file." );
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

}
