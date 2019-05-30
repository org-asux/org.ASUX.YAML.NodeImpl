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

import org.ASUX.yaml.MemoryAndContext;
import org.ASUX.yaml.BatchFileGrammer;
import org.ASUX.yaml.Macros;
import org.ASUX.yaml.CmdLineArgs;
import org.ASUX.yaml.CmdLineArgsBasic;
import org.ASUX.yaml.CmdLineArgsBatchCmd;
import org.ASUX.yaml.AWSSDK;

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


/**
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation batch-processing of multiple YAML commands (combinations of read, list, delete, replace, macro commands)</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.CmdInviker} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 * @see org.ASUX.yaml.CollectionsImpl.CmdInvoker
 */
public class BatchYamlProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.BatchYamlProcessor";

    public static final String FOREACH_INDEX = "foreach.index"; // which iteration # (Int) are we in within the loop.
    public static final String FOREACH_ITER_KEY = "foreach.iteration.key"; // if 'foreach' ends up iterating over an array of strings, then you can get each string's value this way.
    public static final String FOREACH_ITER_VALUE = "foreach.iteration.value"; // if 'foreach' ends up iterating over an array of strings, then you can get each string's value this way.

    // I prefer a LinkedHashMap over a plain HashMap.. as it can help with future enhancements like Properties#1, #2, ..
    // That is being aware of Sequence in which Property-files are loaded.   Can't do that with HashMap
    private LinkedHashMap<String,Properties> AllProps = new LinkedHashMap<String,Properties>();


    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    private boolean verbose;

    /** <p>Whether you want a final SHORT SUMMARY onto System.out.</p><p>a summary of how many matches happened, or how many entries were affected or even a short listing of those affected entries.</p>
     */
    public final boolean showStats;

    /** To help create NEW (or empty) ScalarNodes, SequenceNodes and MappingNodes, as SnakeYaml creates immutable Nodes.. so, any changes to YAML requires creating new Nodes
    */
    public final DumperOptions dumperoptions;

    private int runcount = 0;
    private java.util.Date startTime = null;
    private java.util.Date endTime = null;

    private MemoryAndContext memoryAndContext = null;

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     */
    public BatchYamlProcessor( final boolean _verbose, final boolean _showStats, final DumperOptions _d ) {
        this.verbose = _verbose;
        this.showStats = _showStats;
        this.dumperoptions = _d;
        this.AllProps.put( BatchFileGrammer.FOREACH_PROPERTIES, new Properties() );
        this.AllProps.put( BatchFileGrammer.GLOBALVARIABLES, new Properties() );
        this.AllProps.put( BatchFileGrammer.SYSTEM_ENV, System.getProperties() );
        if ( this.verbose ) new Debug(this.verbose).printAllProps(" >>> ", this.AllProps);
    }

    private BatchYamlProcessor() { this.verbose = false;    this.showStats = true;  this.dumperoptions = null; } // Do Not use this.

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public void setMemoryAndContext( final MemoryAndContext _mnc ) {
        this.memoryAndContext = _mnc;
    }

    //------------------------------------------------------------------------------
    private static class BatchFileException extends Exception {
        private static final long serialVersionUID = 1L;
        public BatchFileException(String _s) { super(_s); }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    /** This is the entry point for this class, with the appropriate TRY-CATCH taken care of, hiding the innumerable exception types.
     *  @param _batchFileName batchfile full path (ry to avoid relative paths)
     *  @param _node a org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML library, containing the entire Tree representing the YAML file.
     *  @return a BLANK/EMPTY/NON-NULL org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML library and you'll get the final Map output representing all processing done by the batch file
     *  throws any exception while processing the command(s) within the batchfile
     */
    public Node go( final String _batchFileName, final Node _node ) throws Exception {

        if ( _batchFileName == null ) return NodeTools.getEmptyYAML( this.dumperoptions );  // null is treated as  batchfile with ZERO commands.
        this.startTime = new java.util.Date();
        String line = null;

        final BatchFileGrammer batchCmds = new BatchFileGrammer( this.verbose );

        try {
            if ( batchCmds.openFile( _batchFileName, true, true ) ) {
                if ( this.verbose ) System.out.println( CLASSNAME + ": go(): successfully opened _batchFileName [" + _batchFileName +"]" );
                if ( this.showStats ) System.out.println( _batchFileName +" has "+ batchCmds.getCommandCount() );

                final Node  retNode = this.processBatch( false, batchCmds, _node );
                if ( this.verbose ) System.out.println( CLASSNAME +" go():  retNode =" + retNode +"\n\n");

                this.endTime = new java.util.Date();
                if ( this.showStats ) System.out.println( "Ran "+ this.runcount +" commands from "+ this.startTime +" until "+ this.endTime +" = " + (this.endTime.getTime() - this.startTime.getTime()) +" seconds" );
                return retNode;

            } else { // if-else openFile()
                return NodeTools.getEmptyYAML( this.dumperoptions );
            }

        } catch (BatchFileException bfe) {
            bfe.printStackTrace(System.err);
            System.err.println(CLASSNAME + ": go():\n\n" + bfe.getMessage() );
        } catch(java.io.FileNotFoundException fe) {
            fe.printStackTrace(System.err);
            System.err.println(CLASSNAME + ": go():\n\nERROR In "+ batchCmds.getState() +".   See details on error above. ");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": go(): Unknown Serious Internal error.\n\n ERROR while processing "+ batchCmds.getState() +".   See details above");
        }

        return NodeTools.getEmptyYAML( this.dumperoptions );
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /**
     * This function is meant for recursion.  Recursion happens when 'foreach' or 'batch' commands are detected in a batch file.
     * After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.
     * If there is any failure whatsoever then the batch-file processing stops immediately.
     * If there is any failure whatsoever either return value is NULL or an Exception is thrown.
     * @param _bInRecursion true or false, whether this invocation is a recursive call or not.  If true, when the 'end' or <EOF> is detected.. this function returns
     * @param _batchCmds an object of type BatchFileGrammer created by reading a batch-file, or .. .. the contents between 'foreach' and 'end' commands
     * @param _input input YAML as java.utils.LinkedHashMap&lt;String, Object&gt; object.  In case of recursion, this object can be java.lang.String
     *  @return a BLANK/EMPTY/NON-NULL org.yaml.snakeyaml.nodes.Node object, as generated by SnakeYAML library and you'll get the final Map output representing all processing done by the batch file.  If there is any failure, either return value is NULL or an Exception is thrown.
     * @throws BatchFileException if any failure trying to execute any entry in the batch file.  Batch file processing will Not proceed once a problem occurs.
     * @throws FileNotFoundException if the batch file to be loaded does Not exist
     * @throws Exception
     */
    private Node processBatch( final boolean _bInRecursion, final BatchFileGrammer _batchCmds,
                        final Object _input )
        throws BatchFileException, java.io.FileNotFoundException, Exception
    {
        final String HDR = CLASSNAME +": processBatch(recursion="+ _bInRecursion +","+ _batchCmds.getCmdType().toString() +"): ";
        Node inputNode = null;
        Node tempOutput = null; // it's immediately re-initialized within WHILE-Loop below.
        final org.ASUX.yaml.Tools tools = this.memoryAndContext.getContext().getTools();

        if ( this.verbose ) System.out.println( HDR +" @ BEGINNING recursion="+ _bInRecursion +" & _input="+ ((_input!=null)?_input.toString():"null") +"]" );
        final Properties globalVariables = this.AllProps.get( BatchFileGrammer.GLOBALVARIABLES );

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
        } else if ( _input instanceof String ) {
            // WARNING: THIS IS NOT NEGOTIABLE .. I do NOT (can NOT) have an Input-Map (non-Scalar) as parameter !!!!!!!!!!!!!!!!!!!!!
            // so, we start off this function with an EMPTY 'inputNode'
            inputNode = NodeTools.getEmptyYAML( this.dumperoptions );
        } else {
            throw new BatchFileException( HDR +"INTERNAL ERROR: _input is Neither Map nor String:  while processing "+ _batchCmds.getState() +" .. unknown object of type ["+ _input.getClass().getName() +"]" );
        }

        if ( this.verbose ) System.out.println( HDR +" BEFORE STARTING while-loop.. "+ _batchCmds.hasNextLine() +" re: "+ _batchCmds.getState() );
        while ( _batchCmds.hasNextLine() )
        {
            _batchCmds.nextLine(); // we can always get the return value of this statement .. via _batchCmds.getCurrentLine()
            _batchCmds.determineCmdType(); // must be the 2nd thing we do - if there is another line to be read from batch-file
            if ( this.verbose ) System.out.println( HDR +" START of while-loop for "+ _batchCmds.getState() +" .. for input=["+ NodeTools.Node2YAMLString(inputNode) +"]" );
            if ( _batchCmds.isLine2bEchoed() ) System.out.println( "Echo (As-Is): "+ _batchCmds.currentLine() );
            if ( _batchCmds.isLine2bEchoed() ) System.out.println( "Echo (Macro-substituted): "+  Macros.eval( _batchCmds.currentLine(), this.AllProps ) );

            // start each loop, with an 'empty' placeholder Map, to collect output of current batch command
            tempOutput = NodeTools.getEmptyYAML( this.dumperoptions );

            switch( _batchCmds.getCmdType() ) {
                case Cmd_MakeNewRoot:
                    final String newRootElem = Macros.eval( _batchCmds.getMakeNewRoot(), this.AllProps );
                    tempOutput = NodeTools.getNewSingleMap( newRootElem, "", this.dumperoptions  ); // Very simple YAML:-    NewRoot: <blank>
                    this.runcount ++;
                    break;
                case Cmd_Batch:
                    final String bSubBatch = Macros.eval( _batchCmds.getSubBatchFile(), this.AllProps );
                    tempOutput = this.go( bSubBatch, inputNode );
                    // technically, this.go() method is NOT meant to used recursively.  Semantically, this is NOT recursion :-(
                    this.runcount ++;
                    break;
                case Cmd_Properties:
                    tempOutput = this.onPropertyLineCmd( _batchCmds, inputNode, tempOutput );
                    this.runcount ++;
                    break;
                case Cmd_Foreach:
                    if ( this.verbose ) System.out.println( HDR +"\t'foreach'_cmd detected'");
                    if ( this.verbose ) System.out.println( HDR +"InputMap = "+ NodeTools.Node2YAMLString(inputNode) );
                    tempOutput = processFOREACHCmdForObject( _batchCmds, inputNode  );
                    // since we processed the lines !!INSIDE!! the 'foreach' --> 'end' block .. via recursion.. we need to skip all those lines here.
                    skipInnerForeachLoops( _batchCmds, "processBatch(foreach)" );
                    this.runcount ++;
                    break;
                case Cmd_End:
                    if ( this.verbose ) System.out.println( HDR +"found matching 'end' keyword for 'foreach' !!!!!!! \n\n");
                    this.runcount ++;
                    return inputNode;
                    // !!!!!!!!!!!! ATTENTION : Function exits here SUCCESSFULLY / NORMALLY. !!!!!!!!!!!!!!!!
                    // break;
                case Cmd_SaveTo:
                    // Might sound crazy - at first.  inpMap for this 'saveAs' command is the output of prior command.
                    // final String saveTo_AsIs = _batchCmds.getSaveTo();
                    tempOutput = processSaveToLine( _batchCmds, inputNode );
                    // Input map is cloned before saving.. so the and Output Map is different (when returning from this function)
                    this.runcount ++;
                    break;
                case Cmd_UseAsInput:
                    tempOutput = processUseAsInputLine( _batchCmds );
                    this.runcount ++;
                    break;
                case Cmd_SetProperty:
                    final String key = Macros.eval( _batchCmds.getPropertyKV().key, this.AllProps );
                    final String val = Macros.eval( _batchCmds.getPropertyKV().val, this.AllProps );
                    globalVariables.setProperty( key, val );
                    if ( this.verbose ) System.out.println( HDR +" Cmd_SetProperty key=["+ key +"] & val=["+ val +"].");
                    break;
                case Cmd_Print:
                    tempOutput = this.onPrintCmd( _batchCmds, inputNode, tempOutput );
                    this.runcount ++;
                    break;
                case Cmd_YAMLLibrary:
                    if ( this.verbose ) System.out.println( HDR +" Setting YAMLLibrary ="+ _batchCmds.getYAMLLibrary() );
                    this.memoryAndContext.getContext().setYamlLibrary( _batchCmds.getYAMLLibrary() );
                    tempOutput = inputNode; // as nothing changes re: Input and Output Maps.
                    break;
                case Cmd_Verbose:
                    if ( this.verbose ) System.out.println( HDR +" this.verbose = =["+ this.verbose +"] & _batchCmds.getVerbose()=["+ _batchCmds.getVerbose() +"].");
                    this.verbose = _batchCmds.getVerbose();
                    tempOutput = inputNode; // as nothing changes re: Input and Output Maps.
                    break;
                case Cmd_Sleep:
                    System.err.println("\n\tsleeping for (seconds) "+ _batchCmds.getSleepDuration() );
                    Thread.sleep( _batchCmds.getSleepDuration()*1000 );
                    tempOutput = inputNode; // as nothing changes re: Input and Output Maps.
                    break;
                case Cmd_Any:
                    //This MUST ALWAYS be the 2nd last 'case' in this SWITCH statement
                    tempOutput = this.onAnyCmd( _batchCmds, inputNode );
                    this.runcount ++;
                    break;
                default:
                    System.out.println( HDR +"  unknown (new?) Batch-file command." );
                    System.exit(99);
            } // switch

            // this line below must be the very last line in the loop
            inputNode = tempOutput; // because we might be doing ANOTHER iteraton of the While() loop.
            this.verbose = _batchCmds.getVerbose(); // always keep checking the verbose level, which can change 'implicitly' within _batchCmds / BatchFileGrammerr.java

            if ( this.verbose ) System.out.println( HDR +" _________________________ BOTTOM of WHILE-loop: tempOutput =" + NodeTools.Node2YAMLString(tempOutput) +"");
        } // while loop

        if ( this.verbose ) System.out.println( HDR +" ---@END---  tempOutput =" + NodeTools.Node2YAMLString(tempOutput) +"\n\n");
        // reached end of file.
        return tempOutput;
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    /**
     * Based on command type, process the inputNode and produce an output - for that specific command
     * @param _batchCmds
     * @param _node 
     * @return
     * @throws BatchYamlProcessor.BatchFileException
     * @throws Macros.MacroException
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws Exception
     */
    private Node  processFOREACHCmdForObject( final BatchFileGrammer _batchCmds, Node _node )
                throws BatchYamlProcessor.BatchFileException, Macros.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String HDR = CLASSNAME +": processFOREACHCmdForObject(): ";
        if ( _node == null )
            return NodeTools.getEmptyYAML( this.dumperoptions );

        if ( this.verbose ) System.out.println( HDR +" BEFORE STARTING SWITCH-stmt.. re: "+ _batchCmds.getState() +" object of type ["+ _node.getClass().getName() +"] = "+ _node.getNodeId() );

        switch( _node.getNodeId() ) {
            case scalar:
                throw new BatchFileException( " ERROR while processing "+ _batchCmds.getState() +" .. executing a FOREACH-command with just a SINGLE STRING scalar value -- as output from previous command - does Not make AMY sense!" );
                // return this.processBatch( BatchFileGrammer.deepClone(_batchCmds), output.getTheActualObject( _node ).toString() );
                // break;
            case sequence:
                final SequenceNode seqN = (SequenceNode) _node;
                final java.util.List<Node> arr = seqN.getValue();
                /* return */ processFOREACH( _batchCmds, arr );
                break;
            case mapping:
                // throw new BatchFileException( " ERROR while processing "+ _batchCmds.getState() +" .. executing a FOREACH-command over a LinkedHashMap's contents, which contains arbitrary Nested Map-structure does Not make AMY sense!" );
                final MappingNode map = (MappingNode) _node;
                final java.util.List<NodeTuple> tuples = map.getValue();
                final Node outpMap1 = this.processFOREACH( BatchFileGrammer.deepClone(_batchCmds), tuples );
                // return outpMap1;
                break;
            default:
                throw new BatchFileException( " ERROR while processing "+ _batchCmds.getState() +" .. unknown object of Node-type="+ _node.getNodeId() +" className="+ _node.getClass().getName() +" ");
        } // switch

        // ignore the output of processFOREACH()

        return _node; // the __SET__ of commands encapsulated between FOREACH-and-END (inclusive of FOREACH & END) must ___literally___ pass-thru whatever was the YAML-input, that the FOR-command enountered.
    }

    //-------------------------------------------------------------------------
    private Node  processFOREACH( final BatchFileGrammer _batchCmds, final java.util.List<?> coll )
                throws BatchYamlProcessor.BatchFileException, Macros.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String HDR = CLASSNAME +": processFORECH(): ";
        Node tempOutput = NodeTools.getEmptyYAML( this.dumperoptions );

        //-----------------------------------------
        final Properties forLoopProps = this.AllProps.get( BatchFileGrammer.FOREACH_PROPERTIES );
        final String prevForLoopIndex = forLoopProps.getProperty( FOREACH_INDEX );
        final String prevForLoopIterKey = forLoopProps.getProperty( FOREACH_ITER_KEY );
        final String prevForLoopIterValue = forLoopProps.getProperty( FOREACH_ITER_VALUE );

        //-----------------------------------------
        java.util.Iterator<?> itr = coll.iterator();
        for ( int ix=0;  itr.hasNext(); ix ++ ) {
            final Object o = itr.next();

            forLoopProps.setProperty( FOREACH_INDEX, ""+ix ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
            if ( this.verbose ) System.out.println( HDR +" @@@@@@@@@@@@@@@@@ foreach/Array-index #"+ ix +" : Object's type ="+ o.getClass().getName() +" and it's toString()=["+ o.toString() +"]" );

            if ( o instanceof ScalarNode ) {
                final ScalarNode scalarN = (ScalarNode) o;
                if ( this.verbose ) System.out.println( HDR +" itr.next() is of ScalarNode type="+ scalarN  );

                forLoopProps.setProperty( FOREACH_ITER_KEY, scalarN.getValue() ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
                forLoopProps.setProperty( FOREACH_ITER_VALUE, scalarN.getValue() );

                final Node retMap6 = this.processBatch( true, BatchFileGrammer.deepClone(_batchCmds), scalarN.getValue() ); // kind of a recursion (for all commands between a 'foreach' and the matching 'end')
                tempOutput = retMap6;

            } else if ( o instanceof NodeTuple) {
                final NodeTuple tuple = (NodeTuple) o;
                if ( this.verbose ) System.out.println( HDR +" itr.next() is of NodeTuple type"+ tuple );
                final Node keyN = tuple.getKeyNode();
                assert( keyN instanceof ScalarNode && keyN.getNodeId() == NodeId.scalar ); // if assert fails, what scenario does that represent?
                final ScalarNode scalarKeyN = (ScalarNode) keyN;

                forLoopProps.setProperty( FOREACH_ITER_KEY, scalarKeyN.getValue() ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
                forLoopProps.setProperty( FOREACH_ITER_VALUE, NodeTools.Node2YAMLString( tuple.getValueNode() ) );

                final Node retMap7 = this.processBatch( true, BatchFileGrammer.deepClone(_batchCmds), tuple.getValueNode() ); // kind of a recursion (for all commands between a 'foreach' and the matching 'end')
                tempOutput = retMap7;

            } else if ( o instanceof Node ) {
                final Node node = (Node) o;
                if ( this.verbose ) System.out.println( HDR +" itr.next() is of generic-SnakeYamlNode type = "+ node );

                forLoopProps.setProperty( FOREACH_ITER_KEY, node.toString() ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
                forLoopProps.setProperty( FOREACH_ITER_VALUE, NodeTools.Node2YAMLString( node ) );

                final Node retMap8 = this.processBatch( true, BatchFileGrammer.deepClone(_batchCmds), node ); // kind of a recursion (for all commands between a 'foreach' and the matching 'end')
                tempOutput = retMap8;

            } else {
                    throw new BatchFileException( HDR +" ERROR: Un-implemented logic.  Not sure what this means: Array of Arrays! In "+ _batchCmds.getState() +" .. trying to iterate over object ["+ o.toString() +"]");
            } // end if-else-if-else
    
        } // for arr.size()

        //-----------------------------------------
        if ( prevForLoopIndex != null ) // if there was an outer FOREACH within the batch file, restore it's index.
            forLoopProps.setProperty( FOREACH_INDEX, prevForLoopIndex );
        else
            forLoopProps.remove( FOREACH_INDEX ); // trying NOT to clutter the Properties space (once the iteration of FOREACH command is over)

        if ( prevForLoopIterKey != null ) // if there was an outer FOREACH within the batch file, restore it's iteration_key.
            forLoopProps.setProperty( FOREACH_ITER_KEY, prevForLoopIterKey );
        else
            forLoopProps.remove( FOREACH_ITER_KEY ); // trying NOT to clutter the Properties space (once the iteration of FOREACH command is over)

        if ( prevForLoopIterValue != null ) // if there was an outer FOREACH within the batch file, restore it's index.
            forLoopProps.setProperty( FOREACH_ITER_VALUE, prevForLoopIterValue );
        else
            forLoopProps.remove( FOREACH_ITER_VALUE ); // trying NOT to clutter the Properties space (once the iteration of FOREACH command is over)

        return tempOutput;
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    /**
     * When this function returns, the "pointer" within _batchCmds (.currentLine & .getLineNum()) ..
     *   should be pointing to the command AFTER the 'end' command.
     * This function basically keeps track of any inner foreachs .. and that's how it knows when the matching 'end' was detected.
     * @param _batchCmds pass-by-reference, so we can alter it's state and move it to the line AFTER matching 'end' commamd
     * @param _sInvoker for use in debugging output only (as there is tons of recursion-loops within these classes)
     * @throws BatchFileException
     * @throws Exception
     */
    private void skipInnerForeachLoops( final BatchFileGrammer _batchCmds, final String _sInvoker )
                                                throws BatchFileException, Exception
    {
        final String HDR = CLASSNAME +": skipInnerForeachLoops("+_sInvoker+"): ";
        final int bookmark = _batchCmds.getLineNum();
        boolean bFoundMatchingENDCmd = false;
        int recursionLevel = 0;
        while ( _batchCmds.hasNextLine() ) {
            /* final String line22 = */ _batchCmds.nextLineOrNull(); // we do Not care what the line is about.
            _batchCmds.determineCmdType(); // must be the 2nd thing we do - if there is another line to be read from batch-file
            if ( this.verbose ) System.out.println( HDR +" skipping cmd "+ _batchCmds.getState() );

            final boolean bForEach22 = _batchCmds.isForEachLine();
            if ( bForEach22 ) recursionLevel ++;

            final boolean bEnd22 = _batchCmds.isEndLine();
            if ( bEnd22 ) {
                recursionLevel --;
                if ( recursionLevel < 0 ) {
                    bFoundMatchingENDCmd = true;
                    break; // we're done completely SKIPPING all the lines between 'foreach' --> 'end'
                } else
                    continue; // while _batchCmds.hasNextLine()
            } // if bEnd22
        }
        if (  !  bFoundMatchingENDCmd ) // sanity check.  These exceptions will get thrown if logic in 100 lines above isn't water-tight
            throw new BatchFileException( HDR +" ERROR In "+ _batchCmds.getState() +"] !!STARTING!! from line# "+ bookmark +".. do NOT see a MATCHING 'end' keyword following the  'foreach'.");
    }

    //======================================================================
    private Node processSaveToLine( final BatchFileGrammer _batchCmds, final Node _node )
                                    throws Macros.MacroException,  java.io.IOException, Exception
    {
        final String HDR = CLASSNAME +": processSaveToLine(): ";
        final String saveTo_AsIs = _batchCmds.getSaveTo();
        if ( saveTo_AsIs != null ) {
            final String saveTo = Macros.eval( saveTo_AsIs, this.AllProps );
            if ( this.memoryAndContext == null || this.memoryAndContext.getContext() == null )
                throw new BatchFileException( HDR +" ERROR In "+ _batchCmds.getState() +".. This program currently has NO/Zero memory from one line of the batch file to the next.  And a SaveTo line was encountered for ["+ saveTo +"]" );
            else {
                final Node newnode = NodeTools.deepClone( _node );
                this.memoryAndContext.getContext().saveDataIntoReference( saveTo, newnode );
                return newnode;
            }
        } else 
            throw new BatchFileException( HDR +" ERROR In "+ _batchCmds.getState() +".. Missing or empty label for SaveTo line was encountered = ["+ saveTo_AsIs +"]" );
    }

    //======================================================================
    private Node processUseAsInputLine( final BatchFileGrammer _batchCmds )
                                throws java.io.FileNotFoundException, java.io.IOException, Exception,
                                Macros.MacroException, BatchFileException
    {
        final String HDR = CLASSNAME +": processUseAsInputLine(): ";
        final String inputFrom_AsIs = _batchCmds.getUseAsInput();
        String inputFrom = Macros.eval( inputFrom_AsIs, this.AllProps );
        inputFrom = new org.ASUX.common.StringUtils(this.verbose).removeBeginEndQuotes( inputFrom );

        if ( this.memoryAndContext == null || this.memoryAndContext.getContext() == null ) {
            throw new BatchFileException( HDR +"ERROR In "+ _batchCmds.getState() +".. This program currently has NO/Zero memory to carry it from one line of the batch file to the next.  And a useAsInput line was encountered for ["+ inputFrom +"]" );
        } else {
            final Object o = this.memoryAndContext.getContext().getDataFromReference( inputFrom );
            if ( o instanceof Node ) {
                @SuppressWarnings("unchecked")
                final Node retMap3 = (Node) o;
                return retMap3;
            } else {
                final String es = (o==null) ? "Nothing in memory under that label." : ("We have type="+ o.getClass().getName()  +" = ["+ o.toString() +"]");
                throw new BatchFileException( HDR +"ERROR In "+ _batchCmds.getState() +".. Failed to read YAML/JSON from ["+ inputFrom_AsIs +"].  "+ es );
            }
        }
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    private Node onPropertyLineCmd( final BatchFileGrammer _batchCmds, final Node inputNode, final Node tempOutput )
                            throws Macros.MacroException, java.io.FileNotFoundException, java.io.IOException
    {
        final Tuple<String,String> kv = _batchCmds.getPropertyKV(); // could be null, implying NOT a kvpair
        if ( kv != null) {
            final String kwom = Macros.eval( kv.key, this.AllProps );
            final String fnwom = Macros.eval( kv.val, this.AllProps );
            final Properties props = new Properties();
            props.load( new java.io.FileInputStream( fnwom ) );
            this.AllProps.put( kwom, props ); // This line is the action taken by this 'PropertyFile' line of the batchfile
        }
        return inputNode; // as nothing changes re: Input and Output Maps.
    }


    private Node onPrintCmd(
        final BatchFileGrammer _batchCmds, final Node inputNode, final Node tempOutput )
        throws Macros.MacroException, Exception
    {
        final String printExpression = _batchCmds.getPrintExpr();
        if ( this.verbose ) System.out.print( ">>>>>>>>>>>>> print line is ["+printExpression +"]" );
        if ( (printExpression != null) && (  !  printExpression.equals("-")) )  {
            String str2output = Macros.eval( printExpression, this.AllProps );
            if ( str2output.trim().endsWith("\\n") ) {
                str2output = str2output.substring(0, str2output.length()-2); // chop out the 2-characters '\n'
                if ( str2output.trim().length() > 0 ) {
                    // the print command has text other than the \n character
                    final Object o = this.memoryAndContext.getDataFromMemory( str2output.trim() );
                    if ( o != null ) {
                        if ( o instanceof Node )
                            System.out.println( NodeTools.Node2YAMLString( (Node)o ) );
                        else
                            System.out.println( o ); // println (end-of-line character outputted)
                    } else
                        System.out.println( str2output ); // println (end-of-line character outputted)
                } else { // if length() <= 0 .. which prints all we have is a simple 'print \n'
                    System.out.println(); // OK. just print a new line, as the print command is a simple 'print \n'
                }
            } else {
                final Object o = this.memoryAndContext.getDataFromMemory( str2output.trim() );
                if ( o != null ) {
                    if ( o instanceof Node )
                        System.out.print( NodeTools.Node2YAMLString( (Node)o ) ); // Note: print only.  NO EOL character outputted.
                    else
                        System.out.print( o ); // Note: print only.  NO EOL character outputted.
                } else
                    System.out.print( str2output +" " ); // print only.  NO EOL character outputted.
            }
            // ATTENTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // DO NOT COMMENT THIS ABOVE.  Do NOT ADD AN IF CONDITION to this.  This is by design.
            System.out.flush();
        } else {
            // if the command/line is just the word 'print' .. print the inputNode
            System.out.println( NodeTools.Node2YAMLString(inputNode) );
        }
        return inputNode; // as nothing changes re: Input and Output Maps.
    }

    //==============================================================================
    private Node onAnyCmd( final BatchFileGrammer _batchCmds, final Node input )
                    throws BatchFileException, Macros.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String cmd_AsIs = _batchCmds.getCommand();
        if ( cmd_AsIs != null ) {
            if ( cmd_AsIs.equals("yaml") ) {
                return processAnyCommand( new YAMLCmdType(), _batchCmds, input );
            } else if ( cmd_AsIs.equals("aws.sdk") ) {
                final AWSCmdType awscmd = new AWSCmdType();
                return processAnyCommand( awscmd, _batchCmds, input );
            } else {
                throw new BatchFileException( CLASSNAME +" onANYCmd(): Unknown Batchfile command ["+ cmd_AsIs +"]  in "+ _batchCmds.getState() );
            }
        } // if BatchCmd
        return input; // as nothing changes re: Input and Output Maps.
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    private static abstract class IWhichCMDType {
        protected String [] cmdLineArgsStrArr = null;
        public abstract Object go( final boolean _verbose, final Node _input, final MemoryAndContext _memoryAndContext, final DumperOptions _dumperoptions )
                        throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception;

        public void convStr2Array( final boolean _verbose, final String _cmdStr, final LinkedHashMap<String,Properties> _allProps )
                                throws Macros.MacroException, java.io.IOException
        {
            if (_verbose) System.out.println( CLASSNAME +".IWhichCMDType.convStr2Array(): _cmdStr="+ _cmdStr );
            String cmdStrCompacted = _cmdStr.replaceAll("\\s\\s*", " "); // replace multiple spaces with a single space.
            // cmdStrCompacted = cmdStrCompacted.trim(); // no need.  The _batchCmds already took care of it.
            final String cmdStrNoMacros = Macros.eval( cmdStrCompacted, _allProps ).trim();
            if (_verbose) System.out.println( CLASSNAME +".IWhichCMDType.convStr2Array(): cmdStrCompacted = "+ cmdStrCompacted );

            // https://mvnrepository.com/artifact/com.opencsv/opencsv
            final java.io.StringReader reader = new java.io.StringReader( cmdStrNoMacros );
            final com.opencsv.CSVParser parser = new com.opencsv.CSVParserBuilder().withSeparator(' ').withQuoteChar('\'').withIgnoreQuotations(false).build();
            final com.opencsv.CSVReader cmdLineParser = new com.opencsv.CSVReaderBuilder( reader ).withSkipLines(0).withCSVParser( parser ).build();
            this.cmdLineArgsStrArr = cmdLineParser.readNext(); // pretend we're reading the 1st line ONLY of a CSV file.
            if (_verbose) { System.out.print( CLASSNAME +".IWhichCMDType.convStr2Array(): cmdLineArgsStrArr = ");  for( String s: cmdLineArgsStrArr) System.out.println(s+"\t"); System.out.println(); }
            // some of the strings in this.cmdLineArgsStrArr may still have a starting and ending single/double-quote
            this.cmdLineArgsStrArr = new org.ASUX.common.StringUtils(_verbose).removeBeginEndQuotes( this.cmdLineArgsStrArr );
            if (_verbose) { System.out.print( CLASSNAME +".IWhichCMDType.convStr2Array(): cmdLineArgsStrArr(REMOVEDALLQUOTES) = ");  for( String s: cmdLineArgsStrArr) System.out.println(s+"\t"); System.out.println(); }
        }
    }
    //----------------------
    private static class YAMLCmdType extends IWhichCMDType {
         // technically override -.. but, actually extend!
        public void convStr2Array( final boolean _verbose, final String _cmdStr, final LinkedHashMap<String,Properties> _allProps ) throws Macros.MacroException, java.io.IOException {
            final String cmdStrWIO = _cmdStr + " -i - -o -";
            super.convStr2Array( _verbose, cmdStrWIO, _allProps ); // this will set  this.cmdLineArgsStrArr
            this.cmdLineArgsStrArr = java.util.Arrays.copyOfRange( this.cmdLineArgsStrArr, 1, this.cmdLineArgsStrArr.length ); // get rid of the 'yaml' word at the beginning
        }
        public Object go( final boolean _verbose, final Node _input, final MemoryAndContext _memoryAndContext, final DumperOptions _dumperoptions )
                throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception
        {
            final CmdLineArgsBasic cmdlineArgsBasic = new CmdLineArgsBasic( this.cmdLineArgsStrArr );
            if (_verbose) System.out.println( CLASSNAME +".YAMLCmdType.go(): cmdlineArgsBasic = "+ cmdlineArgsBasic.toString() );
            final CmdLineArgs cmdLineArgs = cmdlineArgsBasic.getSpecificCmd();
            cmdLineArgs.verbose = _verbose; // pass on whatever this user specified on cmdline re: --verbose or not.
            if (_verbose) System.out.println( CLASSNAME +".YAMLCmdType.go(): cmdLineArgs="+ cmdLineArgs.toString() );
            final org.ASUX.YAML.NodeImpl.CmdInvoker newCmdinvoker = org.ASUX.YAML.NodeImpl.CmdInvoker.deepClone( (org.ASUX.YAML.NodeImpl.CmdInvoker) _memoryAndContext.getContext() );
            final Object output = newCmdinvoker.processCommand( cmdLineArgs, _input );
            return output;
        }
    }
    //----------------------
    private static class AWSCmdType extends IWhichCMDType {
        private static AWSSDK awssdk = null;
        //----------------------
        public Object go( final boolean _verbose, final Node _input, final MemoryAndContext _memoryAndContext, final DumperOptions _dumperoptions )
                throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception
        {
            // for( String s: this.cmdLineArgsStrArr) System.out.print( "\t"+s );   System.out.println("\n\n");
            // aws.sdk ----list-regions us-east-2
            // aws.sdk ----list-AZs     us-east-2
            if ( this.cmdLineArgsStrArr.length < 2 )
                throw new BatchFileException( CLASSNAME +": AWSCmdType.go(AWSCmdType): AWS.SDK command is NOT of sufficient # of parameters ["+ this.cmdLineArgsStrArr +"]");

            if ( AWSCmdType.awssdk == null )
                AWSCmdType.awssdk = AWSSDK.AWSCmdline( _verbose );
            // final org.ASUX.YAML.NodeImpl.Tools tools9 = new org.ASUX.YAML.NodeImpl.Tools(_verbose);

            // skip the 1st word (in this.cmdLineArgsStrArr)  which is fixed at 'AWS.SDK'
            final String awscmdStr = this.cmdLineArgsStrArr[1];
            final String[] awscmdlineArgs = java.util.Arrays.copyOfRange( this.cmdLineArgsStrArr, 2, this.cmdLineArgsStrArr.length ); // last parameter: the final index of the range to be copied, exclusive
            if ( awscmdStr.equals("--list-regions")) {
                final ArrayList<String> regionsList = awssdk.getRegions( );
                final SequenceNode seqN = NodeTools.ArrayList2Node( _verbose, regionsList, _dumperoptions );
                return seqN;
            }
            if ( awscmdStr.equals("--list-AZs")) {
                if ( this.cmdLineArgsStrArr.length < 3 )
                    throw new BatchFileException( CLASSNAME +": AWSCmdType.go(AWSCmdType): AWS.SDK --list-AZs command: INSUFFICIENT # of parameters ["+ this.cmdLineArgsStrArr +"]");
                final ArrayList<String> AZList = awssdk.getAZs( awscmdlineArgs[0] ); // ATTENTION: Pay attention to index# of awscmdlineArgs
                final SequenceNode seqN = NodeTools.ArrayList2Node( _verbose, AZList, _dumperoptions );
                return seqN;
            }
            if ( awscmdStr.equals("--describe-AZs")) {
                if ( this.cmdLineArgsStrArr.length < 3 )
                    throw new BatchFileException( CLASSNAME +": AWSCmdType.go(AWSCmdType): AWS.SDK --list-AZs command: INSUFFICIENT # of parameters ["+ this.cmdLineArgsStrArr +"]");
                final ArrayList< LinkedHashMap<String,Object> > AZList = awssdk.describeAZs( awscmdlineArgs[0] ); // ATTENTION: Pay attention to index# of awscmdlineArgs
                final SequenceNode seqN = NodeTools.ArrayList2Node( _verbose, AZList, _dumperoptions );
                return seqN;
            }
            return null;
        } // go() function
    } // end of AWSCmdType class

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    private Node processAnyCommand( final IWhichCMDType _cmdType, final BatchFileGrammer _batchCmds, final Node _input )
                                throws BatchFileException, Macros.MacroException, java.io.IOException, Exception
    {
        final String HDR = CLASSNAME + ": processAnyCommand(): ";
        final String cmd_AsIs = _batchCmds.getCommand();
        final String cmdStr2 = Macros.eval( cmd_AsIs, this.AllProps ).trim();
        if ( cmdStr2 == null )
            return null;

        _cmdType.convStr2Array( this.verbose, _batchCmds.currentLine(), this.AllProps );
        // if ( this.verbose ) for( String s: cmdLineArgsStrArr ) System.out.println( "\t"+ s );

        try {
            final Object outpObj = _cmdType.go( this.verbose, _input, this.memoryAndContext, this.dumperoptions );
            if ( outpObj instanceof Node ) {
                final Node outpN = (Node) outpObj;
                return outpN;
            } else {
                System.err.println( HDR +" ERROR: Output of batchcommand ["+ _batchCmds.getState() +"] = "+ outpObj );
                throw new Exception( HDR +"Serious INTERNAL ERROR: Output of BatchCommand is Not of type org.yaml.snakeyaml.nodes.Node; Instead it is "+ outpObj.getClass().getName() );
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            final String estr = "ERROR In "+ _batchCmds.getState() +".. Failed to run the command in current line.";
            System.err.println( CLASSNAME + estr );
            throw new BatchFileException( estr );
        }
}

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    // For unit-testing purposes only
    public static void main(String[] args) {
        try {
            final BatchYamlProcessor o = new BatchYamlProcessor(true, true, GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter() );
            Node inpMap = null;
            Node outpMap = o.go( args[0], inpMap );
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
