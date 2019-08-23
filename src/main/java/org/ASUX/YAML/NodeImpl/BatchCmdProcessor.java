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

import org.ASUX.common.Macros;
import org.ASUX.common.Tuple;
import org.ASUX.common.Output;
import org.ASUX.common.Debug;

import org.ASUX.yaml.MemoryAndContext;
import org.ASUX.yaml.BatchFileGrammer;
import org.ASUX.yaml.Enums;
import org.ASUX.yaml.CmdLineArgs;
import org.ASUX.yaml.CmdLineArgsBasic;
import org.ASUX.yaml.CmdLineArgsBatchCmd;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import java.util.Properties;
import java.util.Set;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

import static org.junit.Assert.*;


/**
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation batch-processing of multiple YAML commands (combinations of read, list, delete, replace, macro commands)</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.CmdInvoker} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 * @see org.ASUX.YAML.NodeImpl.CmdInvoker
 */
public class BatchCmdProcessor extends org.ASUX.yaml.BatchCmdProcessor<Node> {

    public static final String CLASSNAME = BatchCmdProcessor.class.getName();

    /** To help create NEW (or empty) ScalarNodes, SequenceNodes and MappingNodes, as SnakeYaml creates immutable Nodes.. so, any changes to YAML requires creating new Nodes
    */
    public final DumperOptions dumperoptions;

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _offline true if we pretent no internet-access is available, and we use 'cached' AWS-SDK responses - if available.
     *  @param _quoteType one the values as defined in {@link org.ASUX.yaml.Enums} Enummeration
     *  @param _d instance of org.yaml.snakeyaml.DumperOptions (typically passed in via {@link CmdInvoker})
     */
    public BatchCmdProcessor( final boolean _verbose, final boolean _showStats, final boolean _offline, final Enums.ScalarStyle _quoteType, final DumperOptions _d ) {
        super( _verbose, _showStats, _offline, _quoteType );
        this.dumperoptions = _d;
    }

    // private BatchCmdProcessor() { this.verbose = false;    this.showStats = true;  this.dumperoptions = null; } // Do Not use this.

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * Because this class is a Generic&lt;T&gt;, compiler (for good reason) will Not allow me to type 'o instanceof T'.  Hence I am delegating this simple condition-check to the sub-classes.
     * @return true if 'o instanceof T' else false.
     */
    @Override
    protected boolean instanceof_YAMLImplClass( Object o ) {
        return o instanceof Node;
    }

    /**
     *  For SnakeYAML Library based subclass of this, simply return 'NodeTools.Node2YAMLString(tempOutput)'.. or .. for EsotericSoftware.com-based LinkedHashMap-based library, simply return 'tools.Map2YAMLString(tempOutputMap)'
     *  @param _o either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     *  @return a string Not-Null
     */
    @Override
    protected String toStringDebug( Object _o ) throws Exception
    {   assertTrue ( _o == null || _o instanceof Node );
        @SuppressWarnings("unchecked")
        final Node node = (Node) _o;
        return  NodeTools.Node2YAMLString( node );
    }

    /**
     *  For SnakeYAML Library based subclass of this, simply return 'NodeTools.getEmptyYAML( this.dumperoptions )' .. or .. for EsotericSoftware.com-based LinkedHashMap-based library, simply return 'new LinkedHashMap&lt;&gt;()'
     *  @return either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     */
    @Override
    protected Node getEmptyYAML() {
        return NodeTools.getEmptyYAML( this.dumperoptions );
    }

    /**
     *  If any of the Read/List/Replace/Table/Batch commands returned "Empty YAML" (assuming the code retured {@link #getEmptyYAML()}), this is your SIMPLEST way of checking if the YAML is empty.
     *  @param _n Nullable value
     *  @return true if the YAML is empty (specifically, if it is the same as what's returned by {@link #getEmptyYAML()})
     */
    @Override
    protected boolean isEmptyYAML( final Node _n ) {
        return NodeTools.isEmptyNodeYAML( _n );
    }

    /**
     *  For SnakeYAML Library based subclass of this, simply return 'NodeTools.getNewSingleMap( newRootElem, "", this.dumperoptions )' .. or .. for EsotericSoftware.com-based LinkedHashMap-based library, simply return 'new LinkedHashMap&lt;&gt;.put( newRootElem, "" )'
     *  @param _newRootElemStr the string representing 'lhs' in "lhs: rhs" single YAML entry
     *  @param _valElemStr the string representing 'rhs' in "lhs: rhs" single YAML entry
     *  @param _quoteType an enum value - see {@link Enums.ScalarStyle}
     *  @return either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     *  @throws Exception internal implementation (deepCloning) can potentially throw an Exception
     */
    @Override
    protected Node getNewSingleYAMLEntry( final String _newRootElemStr, final String _valElemStr, final Enums.ScalarStyle _quoteType ) throws Exception {
        if ( _quoteType != Enums.ScalarStyle.UNDEFINED ) {
            final DumperOptions tempOptions = NodeTools.deepClone( this.dumperoptions ); // so that we do NOT impact the existing settings.
            NodeTools.updateDumperOptions( tempOptions, _quoteType );
            return NodeTools.getNewSingleMap( _newRootElemStr, "", tempOptions );
        } else {
            return NodeTools.getNewSingleMap( _newRootElemStr, "", this.dumperoptions );
        }
    }

    /**
     * For SnakeYAML-based subclass of this, simply return 'NodeTools.deepClone( _node )' .. or .. for EsotericSoftware.com-based LinkedHashMap-based library, return ''
     * @param _node A Not-Null instance of either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     * @return full deep-clone (Not-Null)
     */
    @Override
    protected Node deepClone( Node _node ) throws Exception {
        return NodeTools.deepClone( _node );
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /**
     * This function is meant for recursion.  Recursion happens when 'foreach' or 'batch' commands are detected in a batch file.
     * After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.
     * If there is any failure whatsoever then the batch-file processing stops immediately.
     * If there is any failure whatsoever either return value is NULL or an Exception is thrown.
     * @param _bInRecursion true or false, whether this invocation is a recursive call or not.  If true, when the 'end' or [EOF] is detected.. this function returns
     * @param _batchCmds an object of type BatchFileGrammer created by reading a batch-file, or .. .. the contents between 'foreach' and 'end' commands
     * @param _input input YAML as java.utils.LinkedHashMap&lt;String, Object&gt; object.  In case of recursion, this object can be java.lang.String
     *  @return a BLANK/EMPTY/NON-NULL org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML library and you'll get the final Map output representing all processing done by the batch file.  If there is any failure, either return value is NULL or an Exception is thrown.
     * @throws BatchFileException if any failure trying to execute any entry in the batch file.  Batch file processing will Not proceed once a problem occurs.
     * @throws java.io.FileNotFoundException if the batch file to be loaded does Not exist
     * @throws Exception when any of the commands within a batch-file are processed
     */
    @Override
    protected Node processBatch( final boolean _bInRecursion, final BatchFileGrammer _batchCmds, final Node _input )
                        throws BatchFileException, java.io.FileNotFoundException, Exception
    {
        assertTrue( _batchCmds != null );
        assertTrue( _input != null );
        final String HDR = CLASSNAME +": processBatch(recursion="+ _bInRecursion +","+ _batchCmds.getCmdType().toString() +"): ";
        Node inputNode = null;

        if ( _input == null ) { // if the user specified /dev/null as --inputfile via command line, then _input===null
            inputNode = NodeTools.getEmptyYAML( this.dumperoptions );
        } else if ( _input instanceof MappingNode ) {
            @SuppressWarnings("unchecked")
            final MappingNode map = (MappingNode) _input;
            inputNode = map;
        } else if ( _input instanceof ScalarNode ) {
            @SuppressWarnings("unchecked")
            final ScalarNode scaN = (ScalarNode) _input;
            inputNode = scaN;
        // } else if ( _input instanceof String ) {
        //     // WARNING: THIS IS NOT NEGOTIABLE .. I do NOT (can NOT) have an Input-Map (non-Scalar) as parameter !!!!!!!!!!!!!!!!!!!!!
        } else {
            throw new BatchFileException( HDR +"INTERNAL ERROR: _input is Neither Map nor String:  while processing "+ _batchCmds.getState() +" .. unknown object of type ["+ _input.getClass().getName() +"]" );
        }

        return super.processBatch( _bInRecursion, _batchCmds, inputNode );
    }

    //=============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=============================================================================

    /**
     *  Based on command type, process the inputNode and produce an output - for that specific command
     *  @param _batchCmds Non-Null instance of {@link BatchFileGrammer}
     *  @param _node Non-null instance of either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     *  @return a BLANK/EMPTY/NON-NULL org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML/CollectionsImpl library and you'll get the final YAML output representing all processing done by the batch file.  If there is any failure, either an Exception is thrown.
     *  @throws BatchCmdProcessor.BatchFileException if there is any issue with the command in the batchfile
     *  @throws Macros.MacroException if there is any issues with evaluating Macros.  This is extremely rare, and indicates a software bug.
     *  @throws java.io.FileNotFoundException specifically thrown by the SnakeYAML-library subclass of this
     *  @throws java.io.IOException Any issues reading or writing to PropertyFiles or to JSON/YAML files
     *  @throws Exception Any other unexpected error
     */
    protected Node processFOREACHCmd_Step1( final BatchFileGrammer _batchCmds, Node _node )
                throws BatchCmdProcessor.BatchFileException, Macros.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String HDR = CLASSNAME +": processFOREACHCmd_Step1(): ";
        assertTrue( _batchCmds != null );
        assertTrue( _node != null );
        // if ( _node == null ) return NodeTools.getEmptyYAML( this.dumperoptions );

        //-----------------------------------------
        if ( this.verbose ) System.out.println( HDR +" BEFORE STARTING SWITCH-stmt.. re: "+ _batchCmds.getState() +" object of type ["+ _node.getClass().getName() +"] = "+ _node.getNodeId() );

        //-----------------------------------------
        switch( _node.getNodeId() ) {
            case scalar:
                throw new BatchFileException( " ERROR while processing "+ _batchCmds.getState() +" .. executing a FOREACH-command with just a SINGLE STRING scalar value -- as output from previous command - does Not make AMY sense!" );
                // return this.processBatch( BatchFileGrammer.deepClone(_batchCmds), output.getTheActualObject( _node ).toString() );
                // break;
            case sequence:
                final SequenceNode seqN = (SequenceNode) _node;
                final java.util.List<Node> arr = seqN.getValue();
                /* return */ processFOREACH_Step2( _batchCmds, arr ); // ignore the return value 
                break;
            case mapping:
                // throw new BatchFileException( " ERROR while processing "+ _batchCmds.getState() +" .. executing a FOREACH-command over a LinkedHashMap's contents, which contains arbitrary Nested Map-structure does Not make AMY sense!" );
                final MappingNode map = (MappingNode) _node;
                final java.util.List<NodeTuple> tuples = map.getValue();
                /* final Node outpMap1 = */ this.processFOREACH_Step2( _batchCmds, tuples ); // ignore the return value
                break;
            default:
                throw new BatchFileException( " ERROR while processing "+ _batchCmds.getState() +" .. unknown object of Node-type="+ _node.getNodeId() +" className="+ _node.getClass().getName() +" ");
        } // switch

        //-----------------------------------------
        // ignore the output of processFOREACH_Step2()
        return _node; // the __SET__ of commands encapsulated between FOREACH-and-END (inclusive of FOREACH & END) must ___literally___ pass-thru whatever was the YAML-input, that the FOR-command enountered.
    }

    //-------------------------------------------------------------------------
    private Node  processFOREACH_Step2( final BatchFileGrammer _batchCmds, final java.util.List<?> coll )
                throws BatchCmdProcessor.BatchFileException, Macros.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String HDR = CLASSNAME +": processFOREACH_Step2(): ";
        assertTrue( _batchCmds != null );
        assertTrue( coll != null );

        Node tempOutput = NodeTools.getEmptyYAML( this.dumperoptions );

        //-----------------------------------------
        final Properties forLoopProps = super.allProps.get( FOREACH_PROPERTIES );
        final String prevForLoopIndex = forLoopProps.getProperty( FOREACH_INDEX );
        final String prevForLoopIndexPlus1 = forLoopProps.getProperty( FOREACH_INDEX_PLUS1 );
        final String prevForLoopIterKey = forLoopProps.getProperty( FOREACH_ITER_KEY );
        final String prevForLoopIterValue = forLoopProps.getProperty( FOREACH_ITER_VALUE );

        //----------------------------------------------------------------------------------------------------------------------
        java.util.Iterator<?> itr = coll.iterator();
        for ( int ix=0;  itr.hasNext(); ix ++ ) {
            final Object o = itr.next();

            forLoopProps.setProperty( FOREACH_INDEX, Integer.toString(ix) ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
            forLoopProps.setProperty( FOREACH_INDEX_PLUS1, Integer.toString(ix+1) ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile

            if ( this.verbose ) System.out.println( HDR +" @@@@@@@@@@@@@@@@@ foreach/Array-index #"+ ix +" : Object's type ="+ o.getClass().getName() +" and it's toString()=["+ o.toString() +"]" );

            //----------------------------------------------------------------------------------------------------------------------
            if ( o instanceof ScalarNode ) {
                final ScalarNode scalarN = (ScalarNode) o;
                if ( this.verbose ) System.out.println( HDR +" itr.next() is of ScalarNode type="+ scalarN  );

                forLoopProps.setProperty( FOREACH_ITER_KEY, scalarN.getValue() ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
                forLoopProps.setProperty( FOREACH_ITER_VALUE, scalarN.getValue() );

                //----------------------------
                // !!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!
                // Warning: Do NOT call .hasNextLine() on either _batchCmds or it's clone UNTIL.. .. FOREACH_INDEX, FOREACH_ITER_KEY, FOREACH_ITER_VALUE are set above!!
                // Otherwise.. any commands that utilize them WILL END NOT getting macro-evaluated AT ALL!
                final BatchFileGrammer clone = BatchFileGrammer.deepClone( _batchCmds );

                final Node retMap6 = this.processBatch( true, clone, scalarN ); // kind of a recursion (for all commands between a 'foreach' and the matching 'end')
                tempOutput = retMap6;

            //----------------------------------------------------------------------------------------------------------------------
            } else if ( o instanceof NodeTuple) {
                final NodeTuple tuple = (NodeTuple) o;
                if ( this.verbose ) System.out.println( HDR +" itr.next() is of NodeTuple type"+ tuple );
                final Node keyN = tuple.getKeyNode();
                assertTrue( keyN instanceof ScalarNode && keyN.getNodeId() == NodeId.scalar ); // if assertTrue fails, what scenario does that represent?
                final ScalarNode scalarKeyN = (ScalarNode) keyN;

                forLoopProps.setProperty( FOREACH_ITER_KEY, scalarKeyN.getValue() ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
                forLoopProps.setProperty( FOREACH_ITER_VALUE, NodeTools.Node2YAMLString( tuple.getValueNode() ) );

                //----------------------------
                // !!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!
                // Warning: Do NOT call .hasNextLine() on either _batchCmds or it's clone UNTIL.. .. FOREACH_INDEX, FOREACH_ITER_KEY, FOREACH_ITER_VALUE are set above!!
                // Otherwise.. any commands that utilize them WILL END NOT getting macro-evaluated AT ALL!
                final BatchFileGrammer clone = BatchFileGrammer.deepClone( _batchCmds );

                final Node retMap7 = this.processBatch( true, clone, tuple.getValueNode() ); // kind of a recursion (for all commands between a 'foreach' and the matching 'end')
                tempOutput = retMap7;

            //----------------------------------------------------------------------------------------------------------------------
            } else if ( o instanceof Node ) {
                final Node node = (Node) o;
                if ( this.verbose ) System.out.println( HDR +" itr.next() is of generic-SnakeYamlNode type = "+ node );

                forLoopProps.setProperty( FOREACH_ITER_KEY, node.toString() ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
                forLoopProps.setProperty( FOREACH_ITER_VALUE, NodeTools.Node2YAMLString( node ) );

                //----------------------------
                // !!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!
                // Warning: Do NOT call .hasNextLine() on either _batchCmds or it's clone UNTIL.. .. FOREACH_INDEX, FOREACH_ITER_KEY, FOREACH_ITER_VALUE are set above!!
                // Otherwise.. any commands that utilize them WILL END NOT getting macro-evaluated AT ALL!
                final BatchFileGrammer clone = BatchFileGrammer.deepClone( _batchCmds );

                final Node retMap8 = this.processBatch( true, clone, node ); // kind of a recursion (for all commands between a 'foreach' and the matching 'end')
                tempOutput = retMap8;

            //----------------------------------------------------------------------------------------------------------------------
            } else {
                    throw new BatchFileException( HDR +" ERROR: Un-implemented logic.  Not sure what this means: Array of Arrays! In "+ _batchCmds.getState() +" .. trying to iterate over object ["+ o.toString() +"]");
            } // end if-else-if-else
    
        } // for arr.size()

        //-----------------------------------------
        if ( prevForLoopIndex != null ) { // if there was an outer FOREACH within the batch file, restore it's index.
                forLoopProps.setProperty( FOREACH_INDEX, prevForLoopIndex );
                forLoopProps.setProperty( FOREACH_INDEX_PLUS1, prevForLoopIndexPlus1 );
        } else {
                forLoopProps.remove( FOREACH_INDEX ); // trying NOT to clutter the Properties space (once the iteration of FOREACH command is over)
                forLoopProps.remove( FOREACH_INDEX_PLUS1 ); // trying NOT to clutter the Properties space (once the iteration of FOREACH command is over)
        }

        if ( prevForLoopIterKey != null ) // if there was an outer FOREACH within the batch file, restore it's iteration_key.
                forLoopProps.setProperty( FOREACH_ITER_KEY, prevForLoopIterKey );
        else
                forLoopProps.remove( FOREACH_ITER_KEY ); // trying NOT to clutter the Properties space (once the iteration of FOREACH command is over)

        if ( prevForLoopIterValue != null ) // if there was an outer FOREACH within the batch file, restore it's index.
                forLoopProps.setProperty( FOREACH_ITER_VALUE, prevForLoopIterValue );
        else
                forLoopProps.remove( FOREACH_ITER_VALUE ); // trying NOT to clutter the Properties space (once the iteration of FOREACH command is over)

        //-----------------
        return tempOutput;
    }

    //=============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=============================================================================

    // For unit-testing purposes only
    public static void main(String[] args) {
        try {
            final BatchCmdProcessor o = new BatchCmdProcessor(true, true, true, Enums.ScalarStyle.PLAIN, GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter() );
            Node inpMap = null;
            Node outpMap = o.go( args[0], inpMap );
        } catch (Exception e) {
            e.printStackTrace(System.err); // main() method for unit-testing
        }
    }

}
