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

import org.ASUX.yaml.Enums;
import org.ASUX.yaml.JSONTools;
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
public class NodeTools extends org.ASUX.yaml.YAMLImplementation<Node>
{
    public static final String CLASSNAME = NodeTools.class.getName();

    protected transient GenericYAMLScanner YAMLScanner;
    protected transient GenericYAMLWriter YAMLWriter;

    public transient DumperOptions dumperopt = null;

    //--------------------------
    private static DumperOptions default_dumperopts = null;
    // Following static-code-block is to initialize 'this.default_dumperopts'
    static {
        final String HDR = CLASSNAME + ": STATIC_INIT_CODE for NodeTools.default_dumperopts: ";
        try {
            GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter();
        } catch( Exception e ){
            e.printStackTrace(System.err); // No 'verbose' variable present in Node2YAMLString(). printStackTrace() happens even if user did NOT ask for --verbose
            System.err.println( HDR +"!!!!!!! SERIOUS INTERNAL FAILURE !!!!!!!!" );
            System.exit(490);
        }
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public NodeTools( final boolean _verbose ) {
		super( _verbose, YAML_Libraries.NodeImpl_Library );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * see {@link org.ASUX.yaml.YAMLImplementation#instanceof_YAMLImplClass}
     */
    @Override
    public boolean instanceof_YAMLImplClass( Object o ) {
        return (o != null) && (o instanceof Node);
    }

    /**
     * see {@link org.ASUX.yaml.YAMLImplementation#toStringDebug}
     */
    @Override
    public String toStringDebug( Object _o ) throws Exception {
        if ( this.instanceof_YAMLImplClass(_o) )
            return NodeTools.Node2YAMLString( (Node) _o );
        else
            throw new RuntimeException( CLASSNAME +": !!! Serious Internal Error!!! toStringDebug(): received an object of type: "+ _o );
    }

    /**
     * see {@link org.ASUX.yaml.YAMLImplementation#getEmptyYAML}
     */
    @Override
    public Node getEmptyYAML() {
        // final DumperOptions dumperopts = NodeTools.getDefault DumperOptions();
        return NodeTools.getEmptyYAML( this.getDumperOptions() );
    }

    /**
     * see {@link org.ASUX.yaml.YAMLImplementation#isEmptyYAML}
     */
    @Override
    public boolean isEmptyYAML( final Node _n ) {
        return NodeTools.isEmptyNodeYAML( _n );
    }

    /**
     * see {@link org.ASUX.yaml.YAMLImplementation#getNewSingleYAMLEntry}
     */
    @Override
    public ScalarNode getNewScalarEntry( final String _val ) {
        final ScalarNode sn = new ScalarNode( Tag.STR, _val, null, null, this.getDumperOptions().getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
        // final ScalarNode sn = new ScalarNode( Tag.STR, _val, null, null, NodeTools.getDefault DumperOptions().getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
        return sn;
    }

    //==================================================================================

    /**
     * A utility to help "map" the values of the enum {@link org.ASUX.yaml.Enums.ScalarStyle}, into the "language" of SnakeYAML implementation (see <a>https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java</a>)
     * @param _dopt a NotNull org.yaml.snakeyaml.DumperOptions object-reference
     * @param _orgASUXQuoteType an enum value - see {@link org.ASUX.yaml.Enums.ScalarStyle}
     */
    public static void updateDumperOptions( final DumperOptions _dopt, final Enums.ScalarStyle _orgASUXQuoteType ) {
        switch( _orgASUXQuoteType ) {
            case DOUBLE_QUOTED: _dopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.DOUBLE_QUOTED );  break;
            case SINGLE_QUOTED: _dopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.SINGLE_QUOTED );  break;
            case LITERAL:       _dopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.LITERAL );        break;
            case FOLDED:        _dopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.FOLDED );         break;
            case PLAIN:         _dopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN );          break;
            default:            _dopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.FOLDED );         break;
        }
    }
    /**
     * see {@link org.ASUX.yaml.YAMLImplementation#getNewSingleYAMLEntry}
     */
    @Override
    public Node getNewSingleYAMLEntry( final String _newRootElemStr, final String _valElemStr ) {
        // final DumperOptions dumperopts = NodeTools.getDefault DumperOptions();
        return NodeTools.getNewSingleMap( _newRootElemStr, _valElemStr, this.getDumperOptions() );
    }

    /**
     * see {@link org.ASUX.yaml.YAMLImplementation#getScalarContent}
     */
    @Override
    public String getScalarContent( final Node _n ) throws Exception {
        final String HDR = CLASSNAME + ": getScalarContent(_n): ";
        if ( this.verbose ) System.out.println( HDR +" provided argument =\n" + NodeTools.Node2YAMLString( _n ) + "\n");
        assertTrue( _n != null );
        if ( _n instanceof ScalarNode ) {
            final ScalarNode scalar = (ScalarNode) _n;
            return scalar.getValue();
        } else if ( _n instanceof SequenceNode ) {
            final SequenceNode seqNode = (SequenceNode) _n;
            final java.util.List<Node> seqs = seqNode.getValue();
            if( seqs.size() < 1 )
                return null;
            assertTrue( seqs.get(0) instanceof ScalarNode );
            final ScalarNode scalar = (ScalarNode) seqs.get(0);
            return scalar.getValue();
        } else {
            throw new Exception( "Invalid Node of type: "+ _n.getNodeId() +"\nInstead, you provided:\n"+ NodeTools.Node2YAMLString( _n ) );
        }
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>The SnakeYAML implementation relies on <code>g.yaml.snakeyaml.DumperOptions</code> for allowing us to customize how YAML is outputted.</p>
     *  @param _d important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     */
    public static void setDefaultDumperOptions( final DumperOptions _d ) {
        NodeTools.default_dumperopts = _d;
        assertTrue( NodeTools.default_dumperopts != null );
    }

    /**
     *  <p>The SnakeYAML implementation relies on <code>g.yaml.snakeyaml.DumperOptions</code> for allowing us to customize how YAML is outputted.</p>
     *  @return A non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     */
    public static DumperOptions getDefaultDumperOptions() { return NodeTools.default_dumperopts; }

    //---------------------------------------------------
    /**
     *  <p>The SnakeYAML implementation relies on <code>g.yaml.snakeyaml.DumperOptions</code> for allowing us to customize how YAML is outputted.</p>
     *  @param _d important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     */
    public void setDumperOptions( final DumperOptions _d ) {
        this.dumperopt = _d;
        assertTrue( this.dumperopt != null );
    }

    /**
     *  <p>The SnakeYAML implementation relies on <code>g.yaml.snakeyaml.DumperOptions</code> for allowing us to customize how YAML is outputted.</p>
     *  @return A non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     */
    public DumperOptions getDumperOptions() {
        if ( this.dumperopt != null )
            return this.dumperopt;
        else
            return NodeTools.default_dumperopts;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     *  <p>Example: For SnakeYAML-library based subclass of this, this should return DumperOptions.class</p>
     *  <p>This is to be used primarily within org.ASUX.yaml.BatchCmdProcessor#onAnyCmd().</p>
     *  @return name of class of the object that subclasses of {@link CmdInvoker} use, to configure YAML-Output (example: SnakeYAML uses DumperOptions)
     */
    public Class<?> getLibraryOptionsClass() { return DumperOptions.class; }

    /**
     *  <p>Example: For SnakeYAML-library based subclass of this, this should return the reference to the instance of the class DumperOption</p>
     *  <p>This is to be used primarily within org.ASUX.yaml.BatchCmdProcessor#onAnyCmd().</p>
     * @return instance/object that subclasses of {@link CmdInvoker} use, to configure YAML-Output (example: SnakeYAML uses DumperOptions objects)
     */
    public Object getLibraryOptionsObject() { return NodeTools.default_dumperopts; }

    /**
     *  <p>Example: For SnakeYAML-library based subclass of this, this should return the reference to the instance of the class DumperOption</p>
     *  <p>This is to be used primarily within org.ASUX.yaml.BatchCmdProcessor#onAnyCmd().</p>
     * @param _o A NotNull instance/object NotNull reference, to configure YAML-Output (example: SnakeYAML uses DumperOptions objects)
     */
    public void setLibraryOptionsObject( final Object _o ) {
        final String HDR = CLASSNAME + ": setLibraryOptionsObject(): ";
        // System.err.println( HDR + "Method not implemented !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        // throw new RuntimeException( HDR + "Method Not Implemeted" );
        assertTrue( _o != null );
        if (this.verbose) System.out.println( HDR +" about to convert object of type "+ _o.getClass().getName() +" into org.yaml.snakeyaml.DumperOptions" );
        final DumperOptions dopt = (DumperOptions) _o;
        this.setDumperOptions( dopt );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Reference to the implementation of the YAML read/parsing ONLY
     * @return a reference to the YAML Library in use.
     */
    public GenericYAMLScanner getYAMLScanner() {
        return this.YAMLScanner;
    }

    /**
     *  @param _sc NotNull reference to {@link org.ASUX.YAML.NodeImpl.GenericYAMLScanner}
     */
    public void setYAMLScanner( final GenericYAMLScanner _sc ) {
        this.YAMLScanner = _sc;
    }

    /**
     * Reference to the implementation of the YAML read/parsing ONLY
     * @return a reference to the YAML Library in use.
     */
    public GenericYAMLWriter getYAMLWriter() {
        return this.YAMLWriter;
    }

    /**
     *  @param _wr NotNull reference to {@link org.ASUX.YAML.NodeImpl.GenericYAMLWriter}
     */
    public void setYAMLWriter( final GenericYAMLWriter _wr ) {
        this.YAMLWriter = _wr;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This method will use the SnakeYAML-Library and load the YAML content (pointed to by the _inreader paramater).
     * @param _inreader either a StringReader or a FileReader
     * @return NotNull instance of T.  Even Empty-YAML will come back as NotNull.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public Node load( final java.io.Reader _inreader ) throws Exception {
        return this.getYAMLScanner().load( _inreader );
    }

    /**
     *  This method takes the java.io.Writer (whether StringWriter or FileWriter) and prepares the YAML library to write to it.
     *  @param _javawriter StringWriter or FileWriter (cannot be null)
     * @param _output the content you want written out as a YAML file.
     *  @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void write( final java.io.Writer _javawriter, final Object _output ) throws Exception {
        // this.getYAMLWriter().prepare( _javawriter, this.getDefault DumperOptions() );
        // this.getYAMLWriter().write( _output, this.getDefault DumperOptions() );
        this.write( _javawriter, _output, this.getDumperOptions() );
    }

    /**
     *  This method takes the java.io.Writer (whether StringWriter or FileWriter) and prepares the YAML library to write to it.
     *  @param _javawriter StringWriter or FileWriter (cannot be null)
     *  @param _output the content you want written out as a YAML file.
     *  @param _dumperoptions important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     *  @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void write( final java.io.Writer _javawriter, final Object _output, final DumperOptions _dumperoptions ) throws Exception {
        this.getYAMLWriter().prepare( _javawriter, _dumperoptions );
        this.getYAMLWriter().write( _output, _dumperoptions );
    }

    /**
     * Call this in exactly the way you'd close a file after writing to it.  This method should be called ONLY after {@link #write} will no longer be invoked.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void close() throws Exception {
        this.getYAMLWriter().close();
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

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
        final LinkedHashMap<String, Object> map = JSONTools.JSONString2Map( _verbose, _jsonString );
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
     *  @param _node a org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML library
     *  @return a new java.lang.String object - it will NOT BE NULL.  Instead you'll get an exception.
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings
     */
    public static final String Node2YAMLString(final Node _node) throws Exception // !!!!!!!!!! ATTENTION !!!!!!!!! This is used for dumping YAML-content SPECIFICALLY for ____DEBUGGING___ purposes.
    {                                               // !!!!!! Hence, No need for a DumperOptions parameter
        final String HDR = CLASSNAME + ": Node2YAMLString(): ";
        if ( _node == null )
            return "";

        try {
            final GenericYAMLWriter yamlwriter = new GenericYAMLWriter( false );
            final DumperOptions dumperopts = NodeTools.getDefaultDumperOptions();
            yamlwriter.setYAMLLibrary( YAML_Libraries.SNAKEYAML_Library );
            final java.io.StringWriter strwrtr3 = new java.io.StringWriter();

            yamlwriter.prepare( strwrtr3, dumperopts );
            yamlwriter.write( _node, dumperopts );
            yamlwriter.close();

            return strwrtr3.toString();

        } catch (java.io.IOException e) {
            e.printStackTrace(System.err); // No 'verbose' variable present in Node2YAMLString(). printStackTrace() happens even if user did NOT ask for --verbose
            System.err.println( HDR +"Failure to read/write the contents: '" + _node +"'." );
            throw e;
        } catch (Exception e) {
            e.printStackTrace(System.err); // No 'verbose' variable present in Node2YAMLString(). printStackTrace() happens even if user did NOT ask for --verbose
            System.err.println( HDR +"Unknown Internal error re: '" + _node +"'." );
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
            yamlscanner.setYAMLLibrary( YAML_Libraries.SNAKEYAML_Library );

            final java.io.StringReader strrdr = new java.io.StringReader( _yamlString );
            return yamlscanner.load( strrdr );

        } catch (java.io.IOException e) {
            e.printStackTrace(System.err); // No 'verbose' variable present in YAMLString2Node(). printStackTrace() happens even if user did NOT ask for --verbose
            System.err.println( HDR +"Failure to read/write the contents: '" + _yamlString +"'." );
            throw e;
        } catch (Exception e) {
            e.printStackTrace(System.err); // No 'verbose' variable present in YAMLString2Node(). printStackTrace() happens even if user did NOT ask for --verbose
            System.err.println( HDR +"Unknown Internal error re: '" + _yamlString +"'." );
            throw e;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public static ScalarNode defaultEmptyYAML = null;

    private static String defaultEmptyYAMLAsString = null;
    // private static final String defaultEmptyYAMLAsString = defaultEmptyYAML.toString();
    private static boolean isDefaultEmptyYAMLInitialized = false;

    /* Private implementation code */
    private static void initDefaultEmptyYAML() {
        final String HDR = CLASSNAME +": initDefaultEmptyYAML(): ";
        if (   !    NodeTools.isDefaultEmptyYAMLInitialized ) {
            try {
                NodeTools.defaultEmptyYAML = new ScalarNode( Tag.STR, "", null, null, NodeTools.getDefaultDumperOptions().getDefaultScalarStyle() ); // This should be representing an empty YAML.  I hope!      DumperOptions.ScalarStyle.PLAIN
                NodeTools.defaultEmptyYAMLAsString = NodeTools.Node2YAMLString( defaultEmptyYAML );
                NodeTools.isDefaultEmptyYAMLInitialized = true;
            } catch( Exception e ) {
                e.printStackTrace( System.err );
                System.err.println( HDR + "Unexpected Serious Internal Error" );
                System.exit(99);
            }
        }
    }

    /**
     *  Generates an empty YAML-compatible Scalar-node, which when output as YAML gives you an empty-string '', and when printed to JSON gives '{}'.
     *  @param _dumperoptions important to pass in a non-null object.  This option is most valuable when you'll EVER save this new MappingNode into a file (or dump it to Stdout)
     *  @return a new ScalarNode, which when printed to a file/stdout will give you 'empty' content
     */
    public static Node getEmptyYAML( final DumperOptions _dumperoptions ) {
        // throw new Exception( CLASSNAME +".getEmptyYAML(): This method is NOT yet implemented! " );
        // new Mark( "startMark", 1, 1, 1, "String buffer", 1)
        // new Mark( "endMark",   1, 1, 1, "String buffer", 2)
        // new MappingNode( Tag.MAP, false, new List<NodeTuple>(), Mark startMark, Mark endMark, DumperOptions.FlowStyle.BLOCK ) ;
        // return new ScalarNode( Tag.NULL, "null", null, null, DumperOptions.ScalarStyle.PLAIN ); // This should be representing an empty YAML.  I hope!
        NodeTools.initDefaultEmptyYAML();
        return NodeTools.defaultEmptyYAML;
    }

    // *  @param _bIgnoreContentCheck true if you want to IGNORE the CONTENT of the 1st argument (perhaps it's SEMANTICALLY 'empty', but this argument is set to true, no attempt will be made to check content)
    /**
     *  If any of the Read/List/Replace/Table/Batch commands returned "Empty YAML" (assuming the code retured {@link #getEmptyYAML()}), this is your SIMPLEST way of checking if the YAML is empty.
     *  @param _n Nullable value
     *  @return true if the YAML is empty (specifically, if it is the same as what's returned by {@link #getEmptyYAML()})
     */
    protected static boolean isEmptyNodeYAML( final Node _n ) {
        final String HDR = CLASSNAME +": isEmptyYAML(_n): ";
        if ( _n == null )
            return true;

        NodeTools.initDefaultEmptyYAML();
        if ( _n == NodeTools.defaultEmptyYAML ) // as in, the memory addresses are the same!
            return true;

        // if ( _bIgnoreContentCheck )
        //     return false;

            // if someone else created the empty YAML, let's do a String-level check.
        // final String s = _n.toString(); // will Not be null.
        try {
            final String s = NodeTools.Node2YAMLString( _n );
            return ( defaultEmptyYAMLAsString.equals( s ) );
        } catch( Exception e ) {
            e.printStackTrace( System.err );
            System.err.println( HDR + "Unexpected Internal Error" );
            return false;
        }
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
    //  * @param _node Can be null.  But, whatever you pass-in, will __ONLY__ be used to determine the org.yaml.snakeyaml.DumperOptions.ScalarStyle - when creating the new ScalarNode.  Nothing else.
    //  * @return a new ScalarNode
    //  */
    // public static Node getNewSingleNode( final Node _node ) {
    //     final NodeId nid = ( _node == null ) ? NodeId.anchor : _node.getNodeId();
    //     switch ( nid ) {
    //         case scalar: 
    //             final ScalarNode _origScalarNode = (ScalarNode) _node;
    //             return new ScalarNode( _node.getTag(), "", null, null, _origScalarNode.getScalarStyle() );
    //         case sequence:
    //         case mapping:
    //         case anchor:
    //         default:
    //             return new ScalarNode( _node.getTag(), "", null, null, DumperOptions.ScalarStyle.PLAIN );
    //     }
    //     // return null;
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>Given a YAML content (MORE SPECIFICALLY, an instance of org.yaml.snakeyaml.nodes.MappingNode), return the NodeTuple whose 'key/LHS' matches _keyStr.</p>
     *  <p>ATTENTION! This method will only search within the immediate children of _mapnode.  It will NOT do a recursive search.
     *  @param _mapnode an instance of org.yaml.snakeyaml.nodes.MappingNode (cannot be a simpler Node or SequenceNode)
     *  @param _keyStr the LHS to lookup within the MappingNode
     *  @return eiher null or an instance of org.yaml.snakeyaml.nodes.NodeTuple
     */
    public static NodeTuple getNodeTuple( final MappingNode _mapnode, final String _keyStr ) {
        final String HDR = CLASSNAME +": getNodeTuple(_mapnode,"+_keyStr+") ";
        final java.util.List<NodeTuple> tuples = _mapnode.getValue();
        for( NodeTuple kv: tuples ) {
            final Node keyN = kv.getKeyNode();
            assertTrue( keyN instanceof ScalarNode );
            final ScalarNode scalarKeyN = (ScalarNode) keyN;
            final String keyAsStr = scalarKeyN.getValue();
            assertTrue( keyAsStr != null );
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
     *  <p>ATTENTION! This method will only search within the immediate children of _mapnode.  It will NOT do a recursive search.
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

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public NodeTools deepClone() throws Exception {
        return NodeTools.deepClone( this );
    }

    /**
     *  <p>This method needs to supplement org.ASUX.YAML.CmdInvoker.deepClone() as this subclass (org.ASUX.YAML.NodeImpl.CmdInvoker) has it's own transient instance-fields/variables.</p>
     *  <p>Such Transients are made Transients for only ONE-SINGLE REASON - they are NOT serializable).</p>
     *  <p>!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!</p>
     *  <p>So, after a deepClone() of CmdInvoker.java .. since 'org.yaml.snakeyaml.DumperOptions' is __NOT__ serializable/clonable, you'll need to call: 
     *       NodeTools.class' static method <code> deepClone( final org.yaml.snakeyaml.DumperOptions _orig )</code> <br>
     *  @param origObj the non-null original to clone
     *  @return a properly cloned and re-initiated clone of the original (that works around instance-variables that are NOT serializable)
     *  @throws Exception when org.ASUX.common.Utils.deepClone clones the core of this class-instance 
     */
    public static NodeTools deepClone( final NodeTools origObj ) throws Exception {
        final NodeTools newobj = org.ASUX.common.Utils.deepClone( origObj );

        newobj.YAMLScanner = new GenericYAMLScanner( origObj.verbose );
        newobj.YAMLScanner.setYAMLLibrary( origObj.getYAMLScanner().getYAMLLibrary() );

        newobj.YAMLWriter = new GenericYAMLWriter( origObj.verbose );
        newobj.YAMLWriter.setYAMLLibrary( origObj.getYAMLWriter().getYAMLLibrary() );

        newobj.setDumperOptions( NodeTools.deepClone( origObj.getDumperOptions() ) );
        return newobj;
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
        final org.yaml.snakeyaml.DumperOptions duopt = new org.yaml.snakeyaml.DumperOptions();
// System.out.println( "_Original dumperoptions = "+ _orig.getDefaultScalarStyle() +" "+ _orig.getDefaultFlowStyle() );
        duopt.setAllowUnicode         ( _orig.isAllowUnicode() );
        duopt.setDefaultScalarStyle   ( _orig.getDefaultScalarStyle() );    // this is the Quote-config: no-quote, single-quote, double-quote
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
            yamlscanner.setYAMLLibrary( YAML_Libraries.SNAKEYAML_Library );
            final GenericYAMLWriter yamlwriter = new GenericYAMLWriter( false );
            yamlwriter.setYAMLLibrary( YAML_Libraries.SNAKEYAML_Library );
            // final DumperOptions dumperopts = NodeTools.getDefault DumperOptions(); // ATTENTION: This Default DumperOptions is JUST FINE
                                            // Since we are only deep-cloning, the quoteStyle and blockStyle will be automatically represented in STRING FORM.
    
            final java.io.StringWriter strwrtr3 = new java.io.StringWriter();

            yamlwriter.prepare( strwrtr3, NodeTools.getDefaultDumperOptions() );
            yamlwriter.write  ( _orig,    NodeTools.getDefaultDumperOptions() );
            yamlwriter.close();
// System.out.println( HDR +" created new YAML-String\n" + strwrtr3.toString() +"\n" );

            final java.io.StringReader strrdr = new java.io.StringReader( strwrtr3.toString() );
            return yamlscanner.load( strrdr );

        } catch (java.io.IOException e) {
            e.printStackTrace(System.err); // No 'verbose' variable present in deepClone(Node). printStackTrace() happens even if user did NOT ask for --verbose
            System.err.println( HDR +"Failure to read/write the contents: '" + _orig +"'." );
            throw e;
        } catch (Exception e) {
            e.printStackTrace(System.err); // No 'verbose' variable present in deepClone(Node). printStackTrace() happens even if user did NOT ask for --verbose
            System.err.println( HDR +"Unknown Internal error re: '" + _orig +"'." );
            throw e;
        }
    } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

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

            } else if ( (rhs instanceof java.lang.String) || (rhs instanceof java.lang.Number) || (rhs instanceof java.lang.Boolean) ) {
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
     *  @param _node an instance of org.yaml.snakeyaml.nodes.Node
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
                assertTrue( key.getNodeId() == NodeId.scalar ); // if this ass-ert fails, what scenario does that represent?
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
    //     final org.yaml.snakeyaml.nodes.Node n = NodeTools.Map2Node( this.verbose, map, NodeTools.getDefaultDumperOptions() );
    //     return n;
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * org.yaml.snakeyaml.DumperOptions (like all SnakeYaml classes) does NOT have a toString() and does NOT implement java.io.Streamable.  Hence this static method to show what's inside a DumperOptions object.
     * @param _dumperopt a non-null object.  Null will cause NullPointerException
     */
    public static final void printDumperOptions( DumperOptions _dumperopt ) {
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1 Enums.ScalarStyle="+ org.ASUX.yaml.Enums.ScalarStyle.list(" / ") );
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1 this.dumperoptions.getDefaultScalarStyle()="+ _dumperopt.getDefaultScalarStyle() );
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1 this.dumperoptions.getDefaultScalarStyle().getChar()="+ _dumperopt.getDefaultScalarStyle().getChar() );
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 2 this.dumperoptions.getDefaultFlowStyle()="+ _dumperopt.getDefaultFlowStyle() );
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
        //     rdr.setYamlLibrary( YAML_Libraries.SNAKEYAML_Library );
        //     final GenericYAMLWriter wr = new GenericYAMLWriter(true);
        //     wr.setYamlLibrary( YAML_Libraries.SNAKEYAML_Library );
        //     final Tools tools = new Tools( true );
        //     // tools.cmdInvoker
        //     // LinkedHashMap<String, Object> map = tools.JSONS tring 2Node????????????????????????????????????????????( args[0] );
        //     // System.out.println("Normal completion of program");
        // // } catch (java.io.IOException e) {
        // //     e.printStackTrace(System.err); //main() for unit testing
        // //     System.exit(102);
        // } catch (Exception e) {
        //     if ( e.getMessage().contains("but found: scalar" ) ) {
        //         System.out.println("\n\n Just a string!" );
        //     } else {
        //         e.printStackTrace(System.err); // main for unit testing
        //         System.exit(103);
        //     }
        // }
        // System.exit(0);
    }

}
