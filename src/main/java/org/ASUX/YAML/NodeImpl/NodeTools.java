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

import org.ASUX.yaml.YAML_Libraries;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

// https://yaml.org/spec/1.2/spec.html#id2762107
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


import static org.junit.Assert.*;

/**
 *  <p>This class is a bunch of tools to help make it easy to work with the java.util.Map objects that the YAML library creates.</p>
 *  <p>One example is the work around required when replacing the 'Key' - within the MACRO command Processor.</p>
 *  <p>If the key is already inside single or double-quotes.. then the replacement ends up as <code>'"newkeystring"'</code></p>
 */
public class NodeTools {

    public static final String CLASSNAME = NodeTools.class.getName();
    private CmdInvoker cmdInvoker;

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  Takes any STRING-form JSON as input - it better be valid JSON - and reads it back as org.yaml.snakeyaml.nodes.Node (compatible with SnakeYAML Library).
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _jsonString a java.lang.String object
     *  @param _dumperoptions important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     *  @return a org.yaml.snakeyaml.nodes.Node object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public static org.yaml.snakeyaml.nodes.Node  JSONString2Node( final boolean _verbose, final String  _jsonString, final DumperOptions _dumperoptions )
                    throws java.io.IOException, Exception
    {
        final LinkedHashMap<String, Object> map = new Tools(_verbose).JSONString2Map(_jsonString);
        if ( _verbose ) System.out.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
        if ( _verbose ) System.out.println( "_dumperoptions = "+ _dumperoptions.getDefaultScalarStyle() +" "+ _dumperoptions.getDefaultFlowStyle() );
        if ( _verbose ) System.out.println( map );
        if ( _verbose ) System.out.println( "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" );
        final org.yaml.snakeyaml.nodes.Node n = NodeTools.Map2Node( _verbose, map, _dumperoptions );
        return n;
    }
    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** Takes YAML input - as a org.yaml.snakeyaml.nodes.Node instance - and converts into a simple String-YAML (which if you printed out / stored to a file - would be YAML!)
     *  @param _orig a org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML library
     *  @return a new java.lang.String object - it will NOT BE NULL.  Instead you'll get an exception.
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings
     */
    public static final String Node2YAMLString(final Node _orig) throws Exception // !!!!!!!!!! ATTENTION !!!!!!!!! This is used for dumping YAML-content SPECIFICALLY for ____DEBUGGING___ purposes.
    {                                               // !!!!!! Hence, No need for a DumperOptions parameter
        final String HDR = CLASSNAME + ": Node2YAMLString(): ";

        try {
            final GenericYAMLWriter yamlwriter = new GenericYAMLWriter( false );
            yamlwriter.setYamlLibrary( YAML_Libraries.NodeImpl_Library );
            final DumperOptions dumperopts = GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter();
            final java.io.StringWriter strwrtr3 = new java.io.StringWriter();

            yamlwriter.prepare( strwrtr3, dumperopts );
            yamlwriter.write( _orig, dumperopts );
            yamlwriter.close();

            return strwrtr3.toString();

        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( HDR +"Failure to read/write the contents: '" + _orig +"'." );
            throw e;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( HDR +"Unknown Internal error re: '" + _orig +"'." );
            throw e;
        }
    } // function

    /**
     *  Takes any STRING-form JSON as input - it better be valid JSON - and reads it back as org.yaml.snakeyaml.nodes.Node.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _yamlString a java.lang.String object
     *  @return org.yaml.snakeyaml.nodes.Node object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public static org.yaml.snakeyaml.nodes.Node  YAMLString2Node( final String  _yamlString )
                    throws java.io.IOException, Exception
    {
        final String HDR = CLASSNAME + ": YAMLString2Node(): ";

        try {
            final GenericYAMLScanner yamlscanner = new GenericYAMLScanner( false );
            yamlscanner.setYamlLibrary( YAML_Libraries.NodeImpl_Library );

            final java.io.StringReader strrdr = new java.io.StringReader( _yamlString );
            return yamlscanner.load( strrdr );

        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( HDR +"Failure to read/write the contents: '" + _yamlString +"'." );
            throw e;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( HDR +"Unknown Internal error re: '" + _yamlString +"'." );
            throw e;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  Generates an empty YAML-compatible Scalar-node, which when output as YAML gives you an empty-string '', and when printed to JSON gives '{}'.
     *  @param _dumperoptions important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     *  @return a new ScalarNode, which when printed to a file/stdout will give you 'empty' content
     */
    public static Node getEmptyYAML( final DumperOptions _dumperoptions ) throws Exception {
        // throw new Exception( CLASSNAME +".getEmptyYAML(): This method is NOT yet implemented! " );
        // new Mark( "startMark", 1, 1, 1, "String buffer", 1)
        // new Mark( "endMark",   1, 1, 1, "String buffer", 2)
        // new MappingNode( Tag.MAP, false, new List<NodeTuple>(), Mark startMark, Mark endMark, DumperOptions.FlowStyle.BLOCK ) ;
        // return new ScalarNode( Tag.NULL, "null", null, null, DumperOptions.ScalarStyle.PLAIN ); // This should be representing an empty YAML.  I hope!
        return new ScalarNode( Tag.NULL, "", null, null, _dumperoptions.getDefaultScalarStyle() ); // This should be representing an empty YAML.  I hope!      DumperOptions.ScalarStyle.PLAIN
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  Will return a simple MappingNode consisting of 2 ScalarNode (one for the Key/LHS and the other for the Scalar-value/RHS)
     *  @param _key the key in 'key: value' KV Pair
     *  @param _val the value in 'key: value' KV pair
     *  @param _dumperoptions important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     *  @return a new MappingNode representing a KV pair
     */
    public static Node getNewSingleMap( String _key, String _val, final DumperOptions _dumperoptions ) {
        if ( _key == null ) _key = "";
        if ( _val == null ) _val = "";
        final ScalarNode keyN = new ScalarNode( Tag.STR, _key, null, null, _dumperoptions.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
        final ScalarNode valN = new ScalarNode( Tag.STR, _val, null, null, _dumperoptions.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
        final NodeTuple tuple = new NodeTuple( keyN, valN );
        final java.util.List<NodeTuple> tuples = new LinkedList<NodeTuple>();
        tuples.add( tuple );
        final MappingNode mapN = new MappingNode ( Tag.MAP, false, tuples, null, null, _dumperoptions.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK
        return mapN;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    // /**
    //  * Will return an 'empty' ScalarNode, which when printed to a file/stdout will give you 'empty' content
    //  * @param _orig Can be null.  But, whatever you pass-in, will __ONLY__ be used to determine the org.yaml.snakeyaml.DumperOptions.ScalarStyle - when creating the new ScalarNode.  Nothing else.
    //  * @return a new ScalarNode
    //  */
    // public static Node getNewSingleNode( final Node _orig ) {
    //     final NodeId nid = ( _orig == null ) ? NodeId.anchor : _orig.getNodeId();
    //     switch ( nid ) {
    //         case scalar: 
    //             final ScalarNode _origScalarNode = (ScalarNode) _orig;
    //             return new ScalarNode( _orig.getTag(), "", null, null, _origScalarNode.getScalarStyle() );
    //         case sequence:
    //         case mapping:
    //         case anchor:
    //         default:
    //             return new ScalarNode( _orig.getTag(), "", null, null, DumperOptions.ScalarStyle.PLAIN );
    //     }
    //     // return null;
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>Given a YAML content (MORE SPECIFICALLY, an instance of org.yaml.snakeyaml.nodes.MappingNode), return the NodeTuple whose 'key/LHS' matches _keyStr.</p>
     *  <p>ATTENTION! This method will only search within the immediate children of _mapnode.  It will NOT do a recursive search.  If you want a recursive deep-dive search, use {@link getNodeTupleRecursive}
     *  @param _mapnode an instance of org.yaml.snakeyaml.nodes.MappingNode (cannot be a simpler Node or SequenceNode)
     *  @param _keyStr the LHS to lookup within the MappingNode
     *  @return eiher null or an instance of org.yaml.snakeyaml.nodes.NodeTuple
     */
    public static NodeTuple getNodeTuple( final MappingNode _mapnode, final String _keyStr ) {
        final String HDR = CLASSNAME +": getNodeTuple(_mapnode,"+_keyStr+") ";
        final java.util.List<NodeTuple> tuples = _mapnode.getValue();
        for( NodeTuple kv: tuples ) {
            final Node keyN = kv.getKeyNode();
            assert( keyN instanceof ScalarNode );
            final ScalarNode scalarKeyN = (ScalarNode) keyN;
            final String keyAsStr = scalarKeyN.getValue();
            assert( keyAsStr != null );
            if ( keyAsStr.equals(_keyStr) )
                return kv;
            // final Node valN = kv.getValueNode();
            // if ( valN instanceof ScalarNode && valN.getNodeId() == NodeId.scalar ) {
            //     final ScalarNode scalarValN = (ScalarNode) valN;
            //     if ( _verbose ) System.out.println( HDR +" found LHS, keyTag & RHS = ["+ keyN + "] !"+ scalarKeyN.getTag().getValue() + " : "+ scalarValN.getValue() + " ;" );
            // } else {
            // 
            // }
        }
        return null;
    }

    /**
     *  <p>Given a YAML content (MORE SPECIFICALLY, an instance of org.yaml.snakeyaml.nodes.MappingNode), return the RHS of the NodeTuple whose 'key/LHS' matches _keyStr.</p>
     *  <p>ATTENTION! This method will only search within the immediate children of _mapnode.  It will NOT do a recursive search.  If you want a recursive deep-dive search, use {@link getNodeTupleRecursive}
     *  @param _mapnode an instance of org.yaml.snakeyaml.nodes.MappingNode (cannot be a simpler Node or SequenceNode)
     *  @param _keyStr the LHS to lookup within the MappingNode
     *  @return eiher null or an instance of org.yaml.snakeyaml.nodes.Node
     */
    public static Node getRHSNode( final MappingNode _mapnode, final String _keyStr ) {
        final NodeTuple nt = getNodeTuple( _mapnode, _keyStr );
        if ( nt != null ) {
            final Node valN = nt.getValueNode();
            return valN;
        } else {
            return null;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** Takes YAML input - as a org.yaml.snakeyaml.nodes.Node instance - and deep-clones it (by writing as a String-YAML and reading it back using {@link GenericYAMLScanner})
     *  @param _orig a org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML library
     *  @return a new org.yaml.snakeyaml.nodes.Node object that has Nothing in common with _orig
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings and back (as part of lintremoval)
     */
    public static final org.yaml.snakeyaml.DumperOptions deepClone( final org.yaml.snakeyaml.DumperOptions _orig ) throws Exception
    {
        final String HDR = CLASSNAME + ": deepClone(DumperOptions): ";
        final org.yaml.snakeyaml.DumperOptions duopt = GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter();
// System.out.println( "_Original dumperoptions = "+ _orig.getDefaultScalarStyle() +" "+ _orig.getDefaultFlowStyle() );
        duopt.setAllowUnicode         ( _orig.isAllowUnicode() );
        duopt.setDefaultScalarStyle   ( _orig.getDefaultScalarStyle() );
        duopt.setIndent               ( _orig.getIndent() );
        duopt.setIndicatorIndent      ( _orig.getIndicatorIndent() );
        duopt.setPrettyFlow           ( _orig.isPrettyFlow() );
        duopt.setWidth                ( _orig.getWidth() );
        duopt.setDefaultFlowStyle     ( _orig.getDefaultFlowStyle() );
// System.out.println( "CLONED dumperoptions = "+ duopt.getDefaultScalarStyle() +" "+ duopt.getDefaultFlowStyle() );
        return duopt;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================


    /** Takes YAML input - as a org.yaml.snakeyaml.nodes.Node instance - and deep-clones it (by writing as a String-YAML and reading it back using {@link GenericYAMLScanner})
     *  @param _orig a org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML library
     *  @return a new org.yaml.snakeyaml.nodes.Node object that has Nothing in common with _orig
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings and back (as part of lintremoval)
     */
    public static final Node deepClone( final Node _orig ) throws Exception // !!!!!!!!!!! ATTENTION !!!!!!!!!!! No need for any DumperOptions parameter here!
    {
        final String HDR = CLASSNAME + ": deepClone(Node): ";

        try {
            final GenericYAMLScanner yamlscanner = new GenericYAMLScanner( false );
            yamlscanner.setYamlLibrary( YAML_Libraries.NodeImpl_Library );
            final GenericYAMLWriter yamlwriter = new GenericYAMLWriter( false );
            yamlwriter.setYamlLibrary( YAML_Libraries.NodeImpl_Library );
            final DumperOptions dumperopts = GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter(); // ATTENTION: This Default DumperOptions is JUST FINE
                                            // Since we are only deep-cloning, the quoteStyle and blockStyle will be automatically represented in STRING FORM.
    
            final java.io.StringWriter strwrtr3 = new java.io.StringWriter();

            yamlwriter.prepare( strwrtr3, dumperopts );
            yamlwriter.write( _orig, dumperopts );
            yamlwriter.close();
// System.out.println( HDR +" created new YAML-String\n" + strwrtr3.toString() +"\n" );

            final java.io.StringReader strrdr = new java.io.StringReader( strwrtr3.toString() );
            return yamlscanner.load( strrdr );

        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( HDR +"Failure to read/write the contents: '" + _orig +"'." );
            throw e;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( HDR +"Unknown Internal error re: '" + _orig +"'." );
            throw e;
        }
    } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** Takes any YAML input - as a LinkedHashmap - and exports it as org.yaml.snakeyaml.nodes.Node (compliant with SnakeYAML library)
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _yamlArr a java.util.ArrayLisg&lt;Object&gt; object, as generated by any source (that ___better be___ YAML compatible semantically)
     *  @param _dumperoptions important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     *  @return a org.yaml.snakeyaml.nodes.Node object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings and back (as part of lintremoval)
     */
    public static org.yaml.snakeyaml.nodes.SequenceNode ArrayList2Node( final boolean _verbose, final java.util.ArrayList<?> _yamlArr, final DumperOptions _dumperoptions ) throws Exception
    {
        final String HDR = CLASSNAME + ": ArrayList2Node(): ";
        final java.util.LinkedList<Node> seqs = new java.util.LinkedList<Node>();
        final SequenceNode seqN = new SequenceNode( Tag.SEQ, false,     seqs,               null, null, _dumperoptions.getDefaultFlowStyle() );

        for ( Object o: _yamlArr ) {
            if ( o instanceof LinkedHashMap ) { // if the array-yaml-element is Not a simple string.
                @SuppressWarnings("unchecked")
                final LinkedHashMap<String, Object> omap = (LinkedHashMap<String, Object>) o;
                seqs.add ( Map2Node( _verbose, omap, _dumperoptions ) );
            } else if ( o instanceof java.lang.String ) {
                final ScalarNode simpleScalar = new ScalarNode( Tag.STR,     o.toString(),                null, null, _dumperoptions.getDefaultScalarStyle() );
                seqs.add ( simpleScalar );
                if ( _verbose ) System.out.println( HDR +": added SIMPLE-node= ["+ simpleScalar +"]" );
            } else {
                throw new Exception( HDR +" incomplete code: failure with an Array of types= '"+ o.getClass().getName() +"'" );
            } // if-Else   o instanceof Map - (WITHIN FOR-LOOP)
        } // for Object o: arr

        return seqN;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /** Takes any YAML input - as a LinkedHashmap - and exports it as org.yaml.snakeyaml.nodes.Node (compliant with SnakeYAML library)
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _yaml a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
     *  @param _dumperoptions important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     *  @return a org.yaml.snakeyaml.nodes.Node object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings and back (as part of lintremoval)
     */
    public static org.yaml.snakeyaml.nodes.Node Map2Node( final boolean _verbose, final LinkedHashMap<String, Object> _yaml, final DumperOptions _dumperoptions ) throws Exception
    {
        final String HDR = CLASSNAME + ": Map2Node(): ";
        final List<NodeTuple> nodetuple = new LinkedList<NodeTuple>();
        //--------------------------
        for (String key : _yaml.keySet()) {

            final ScalarNode keySN = new ScalarNode( Tag.STR,     key,                null, null, _dumperoptions.getDefaultScalarStyle() );
            final Object rhs = _yaml.get(key);
            final String rhsStr = rhs.toString(); // to make verbose logging code simplified

            //--------------------------------------------------------
            // if we are here, we've only a PARTIAL match.
            // So.. we need to keep recursing (specifically for Map & ArrayList YAML elements)
            if ( rhs instanceof LinkedHashMap ) {
                @SuppressWarnings("unchecked")
                final LinkedHashMap<String, Object> omap = (LinkedHashMap<String, Object>) rhs;
                final NodeTuple nnt = new NodeTuple( keySN, Map2Node( _verbose, omap, _dumperoptions ) );
                nodetuple.add ( nnt );
                if ( _verbose ) System.out.println( HDR +": added NodeTuple= ["+ nnt +"]" );

            } else if ( rhs instanceof java.util.ArrayList ) {
                @SuppressWarnings("unchecked")
                final ArrayList<Object> arr = (ArrayList<Object>) rhs;
                final SequenceNode seqN = NodeTools.ArrayList2Node( _verbose, arr, _dumperoptions );
                final NodeTuple nnt = new NodeTuple( keySN, seqN );
                nodetuple.add ( nnt );
                if ( _verbose ) System.out.println( HDR +": added SequenceNode= ["+ seqN +"]" );
        
            } else if ( rhs instanceof java.lang.String ) {
                final ScalarNode valSN = new ScalarNode( Tag.STR,     rhs.toString(),     null, null, _dumperoptions.getDefaultScalarStyle() );
                final NodeTuple nnt = new NodeTuple( keySN, valSN );
                nodetuple.add ( nnt );
                if ( _verbose ) System.out.println( HDR +": added SIMPLE-node= ["+ nnt +"]" );

            } else {
                if ( _verbose ) System.out.println ( "\n"+ HDR +": "+ key +": "+ rhsStr.substring(0,rhsStr.length()>360?360:rhsStr.length()) );
                throw new Exception( HDR +": incomplete code: Unable to handle rhs of type '"+ rhs.getClass().getName() +"'" );
            } // if-else   rhs instanceof   Map/Array/String/.. ..

        } // for loop

        final Node newMN = new MappingNode( Tag.MAP, false,    nodetuple,    null, null, _dumperoptions.getDefaultFlowStyle() );
        return newMN;

    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  Takes a org.yaml.snakeyaml.nodes.Node object and converts it into a LinkedHashMap heirarchy that is compatible with ESO TERIC SOFT WARE'S YAML LIBRARY
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param args an instance of org.yaml.snakeyaml.nodes.Node
     *  @return instance of {@link org.ASUX.common.Output.Object} which will contain the appropriate object.
     *  @throws Exception if any invalid Node object hierarchy, or if unimplemented scenarios.
     */
    public static org.ASUX.common.Output.Object<?> Node2Map( final boolean _verbose, final Node _node ) throws Exception
    {
        final String HDR = CLASSNAME +" Node2Map(): ";
        org.ASUX.common.Output.Object<?> outputObj= null;

        // public enum org.yaml.snakeyaml.nodes.NodeId = scalar, sequence, mapping, anchor
        final NodeId nid = _node.getNodeId(); // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/NodeId.java
        if ( _verbose ) System.out.println( HDR +"@top, node-id = ["+ nid + "]" );

        if ( _node instanceof MappingNode ) {
            // https://yaml.org/spec/1.2/spec.html#id2762107
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/MappingNode.java   
            final MappingNode map = (MappingNode) _node;
            // MappingNode(Tag ignore, boolean resolved, List<NodeTuple> value, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle)
            final java.util.List<NodeTuple> tuples = map.getValue();
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/NodeTuple.java
            if ( _verbose ) System.out.println( HDR +" Mapping-node has value/tuples= ["+ tuples + "]" );

            final LinkedHashMap<String, Object> lhm = new LinkedHashMap<String, Object>();
            for( NodeTuple kv: tuples ) {
                final Node key = kv.getKeyNode();
                assert ( key.getNodeId() == NodeId.scalar ); // if assert fails, what scenario does that represent?
                final ScalarNode scalarKey = (ScalarNode) key;
                String kstr = (scalarKey.getTag().startsWith("!")) ? (scalarKey.getTag()+" ") : "";
                kstr += scalarKey.getValue();

                final Node val = kv.getValueNode();
                if ( _verbose ) System.out.println( HDR +"found LHS: RHS = ["+ key + "] : ["+ val + "]" );

                if ( val.getNodeId() == NodeId.scalar) {
                    final ScalarNode scalarVal = (ScalarNode) val;
                    String v = (scalarVal.getTag().startsWith("!")) ? (scalarVal.getTag()+" ") : "";
                    v += scalarVal.getValue();
                    lhm.put( kstr, v );
                    if ( _verbose ) System.out.println( HDR +">>>>>>>>>>> ADDED SCALAR KV-pair= ["+ kstr + "] = ["+ v + "]" );

                } else {
                    if ( _verbose ) System.out.println( HDR +"recursing.. ..= ["+ val.getNodeId() + "]" );
                    final org.ASUX.common.Output.Object<?> asuxobj = Node2Map( _verbose, val ); // recursion
                    lhm.put( kstr, asuxobj.getJavaObject() );
                }
            } // for
            if ( _verbose ) System.out.println( HDR +"function-returning a LinkedHashMap = ["+ lhm + "]" );
            outputObj = new org.ASUX.common.Output.Object<String>();
            outputObj.setMap( lhm );
            return outputObj;

        } else if ( _node instanceof SequenceNode ) {
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/SequenceNode.java
            final SequenceNode seq = (SequenceNode) _node;
            // SequenceNode(Tag ignore, boolean resolved, List<Node> value, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle)
            if ( _verbose ) System.out.println( HDR +"SEQUENCE-node-id = ["+ seq.getNodeId() + "]" );

            final java.util.List<Node> lst = seq.getValue();
            final ArrayList<Object> arrObj = new ArrayList<Object>();
            final ArrayList<String> arrStr = new ArrayList<String>();
            boolean bNonScalarsDetected = false;
            for( Node val: lst ) {
                if ( val.getNodeId() == NodeId.scalar) {
                    final ScalarNode scalarVal = (ScalarNode) val;
                    String v = (scalarVal.getTag().startsWith("!")) ? (scalarVal.getTag()+" ") : "";
                    v += scalarVal.getValue();
                    arrObj.add( v );
                    arrStr.add( v );
                    if ( _verbose ) System.out.println( HDR +">>>>>>>>>>> ADDED SCALAR into Array = ["+ scalarVal.getValue() + "]" );
                } else {
                    if ( _verbose ) System.out.println( HDR +"recursing.. ..= ["+ val.getNodeId() + "]" );
                    final org.ASUX.common.Output.Object<?> asuxobj = Node2Map( _verbose, val );  // recursion
                    arrObj.add( asuxobj.getJavaObject() );
                    bNonScalarsDetected = true;
                }
            } // for
            if ( bNonScalarsDetected ) {
                final org.ASUX.common.Output.Object<java.lang.Object> o2 = new org.ASUX.common.Output.Object<java.lang.Object>();
                o2.setArray( arrObj );
                outputObj = o2;
            } else {
                final org.ASUX.common.Output.Object<String> o3 = new org.ASUX.common.Output.Object<String>();
                o3.setArray( arrStr );
                outputObj = o3;
            }
            if ( _verbose ) System.out.println( HDR +"function-returning something = ["+ outputObj + "]" );
            return outputObj;

        } else if ( _node instanceof ScalarNode ) {
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/ScalarNode.java
            final ScalarNode scalarVal = (ScalarNode) _node;
            // ScalarNode(Tag ignore, String value, Mark startMark, Mark endMark, DumperOptions.ScalarStyle style)
            String v = (scalarVal.getTag().startsWith("!")) ? (scalarVal.getTag()+" ") : "";
            v += scalarVal.getValue();
            // boolean scalarVal.isPlain()
            // lhm.put( ??What-is-the-Key??? , val );
            if ( _verbose ) System.out.println( HDR +" >>>>>>>>>>> returning a SCALAR !! = ["+ v + "]" );
            final org.ASUX.common.Output.Object<String> o4 = new org.ASUX.common.Output.Object<String>();
            o4.setString( v );
            return o4;

        } else {
            final String erms = HDR +" Unimplemented SnakeYaml Node-type: " + nid +" = ["+ _node.toString() +"]";
            System.err.println( erms );
            throw new Exception( erms );
        } // if-else-if-else
    } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    // /**
    //  *  Takes any STRING-form JSON as input - it better be valid JSON - and reads it back as org.yaml.snakeyaml.nodes.Node (compatible with SnakeYAML Library).
    //  *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
    //  *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
    //  *  @param _jsonString a java.lang.String object
    //  *  @return a org.yaml.snakeyaml.nodes.Node object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
    //  * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
    //  * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
    //  */
    // public org.yaml.snakeyaml.nodes.Node  JSONString2Node( final String  _jsonString )
    //                 throws java.io.IOException, Exception
    // {
    //     final LinkedHashMap<String, Object> map = JSONString2Map(_jsonString);
    //     final org.yaml.snakeyaml.nodes.Node n = NodeTools.Map2Node( this.verbose, map, GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter() );
    //     return n;
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * org.yaml.snakeyaml.DumperOptions (like all SnakeYaml classes) does NOT have a toString() and does NOT implement java.io.Streamable.  Hence this static method to show what's inside a DumperOptions object.
     * @param dumperoptions a non-null object.  Null will cause NullPointerException
     */
    public static final void printDumperOptions( DumperOptions dumperopt ) {
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1 Enums.ScalarStyle="+ org.ASUX.yaml.Enums.ScalarStyle.list(" / ") );
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1 this.dumperoptions.getDefaultScalarStyle()="+ dumperopt.getDefaultScalarStyle() );
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1 this.dumperoptions.getDefaultScalarStyle().getChar()="+ dumperopt.getDefaultScalarStyle().getChar() );
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 2 this.dumperoptions.getDefaultFlowStyle()="+ dumperopt.getDefaultFlowStyle() );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    // /**
    //  *  Takes any STRING-form JSON as input - it better be valid JSON - and reads it back as org.yaml.snakeyaml.nodes.Node.
    //  *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
    //  *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
    //  *  @param _yamlString a java.lang.String object
    //  *  @param _bWrapScalar true or false.  Not relevant to the SnakeYaml library based implementation org.ASUX.YAML.NodeImp;  It is relevant to the other/alternative implementation org.ASUX.YAML.CollectionImpl;
    //  *  @return org.yaml.snakeyaml.nodes.Node object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
    //  * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
    //  * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
    //  */
    // public org.yaml.snakeyaml.nodes.Node  YAMLString2Node( final String  _yamlString, final boolean _bWrapScalar )
    //                 throws java.io.IOException, Exception
    // {
    //     org.yaml.snakeyaml.nodes.Node n = NodeTools.YAMLString2Node( _yamlString );
    //     return n;
    //     // throw new Exception( CLASSNAME + "; NOT IMPLEMENTED: org.yaml.snakeyaml.nodes.Node  YAMLString2Node( final String  _yamlString, final boolean _bWrapScalar ); "   );
    //     // final LinkedHashMap<String, Object> map = YAMLString2Map( _yamlString, false ); // boolean _bWrapScalar is the 2nd parameter
    //     // final org.yaml.snakeyaml.nodes.Node n = Map2Node( map );
    //     // return n;
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public static void main( String[] args ) {
        // try {
        //     final GenericYAMLScanner rdr = new GenericYAMLScanner(true);
        //     rdr.setYamlLibrary( YAML_Libraries.ESOTERICSOFTWARE_Library );
        //     final GenericYAMLWriter wr = new GenericYAMLWriter(true);
        //     wr.setYamlLibrary( YAML_Libraries.ESOTERICSOFTWARE_Library );
        //     final Tools tools = new Tools( true );
        //     // tools.cmdInvoker
        //     // LinkedHashMap<String, Object> map = tools.JSONS tring 2Node????????????????????????????????????????????( args[0] );
        //     // System.out.println("Normal completion of program");
        // // } catch (java.io.IOException e) {
        // //     e.printStackTrace(System.err);
        // //     System.exit(102);
        // } catch (Exception e) {
        //     if ( e.getMessage().contains("but found: scalar" ) ) {
        //         System.out.println("\n\n Just a string!" );
        //     } else {
        //         e.printStackTrace(System.err);
        //         System.exit(103);
        //     }
        // }
        // System.exit(0);
    }

}
