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

import org.ASUX.yaml.YAMLPath;

import java.util.regex.*;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

import static org.junit.Assert.*;


/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class (org.ASUX.yaml.AbstractYamlEntryProcessor).</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 */
public class TableYamlQuery extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = TableYamlQuery.class.getName();

    protected String[] tableColumns = new String[]{"UNinitialized", "TableColumns"};
    private String delimiter = "UNINITIALIZED DELIMITER";

    protected int count;
    protected LinkedList< ArrayList<String> > output;
    protected SequenceNode outputAsNode;

    //------------------------------------------------------------------------------
    public static class TableCmdException extends Exception {
        private static final long serialVersionUID = 12L;
        public TableCmdException(String _s) { super(_s); }
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _d instance of org.yaml.snakeyaml.DumperOptions (typically passed in via {@link CmdInvoker})
     *  @param _tableColumns a delimiter-separated list of "columns" (think SQL table columns).  The output of this table-yaml command is true 2-D table (2-D Array to be precise)
     *  @param _delim This delimiter should be used to separate the 'column-names' within the _tableColumns parameter.  pass in a value like '.'  '\t'   ','   .. pass in such a character as a string-parameter (being flexible in case delimiters can be more than a single character)
     *  @throws Exception if Pattern provided for YAML-Path is either semantically empty, Not a viable variable-name (see BatchFileGrammer.java's REGEXP_NAME constant) or is NOT java.util.Pattern compatible.
     */
    public TableYamlQuery( final boolean _verbose, final boolean _showStats, final DumperOptions _d, String _tableColumns, final String _delim )
                        throws Exception
    {
        super( _verbose, _showStats, _d );
        this.delimiter = _delim;
        this.count = 0;
        this.output = new LinkedList<>();
        this.outputAsNode = new SequenceNode( Tag.SEQ, false, new LinkedList<Node>(),  null, null, this.dumperoptions.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK

        // Sanity check of "_delim"
        try {
            Pattern p = Pattern.compile(_delim);
        }catch(PatternSyntaxException e){
            if ( _verbose ) e.printStackTrace(System.err);
            System.err.println( CLASSNAME +" Constructor: Invalid delimiter-pattern '"+ _delim +"' provided to constructor "+ e );
            throw e; // Should this method .. instead return for an invalid YAMLPath object.. .. and Let "this.isValid" stay as false ??
        }

        _tableColumns = _tableColumns.trim(); // strip leading and trailing whitesapce (Java11 user strip(), Java<11, use trim()
        if ( _tableColumns.length() <= 0 ) {
            throw new Exception( CLASSNAME +" Constructor: semantically EMPTY list of Table-columns provided to Table-query Command." );
        }

        if (this.verbose) System.out.println( CLASSNAME + ": about to split '"+ _tableColumns +"' with delimiter '"+ _delim +"'");
        this.tableColumns = _tableColumns.split( _delim );
        for(int ix=0; ix < this.tableColumns.length; ix++ ) {
            final String col = this.tableColumns[ix];
            final String errMsg = CLASSNAME +" Constructor: Invalid column # "+ ix +" '"+ col +"' provided to Table-query Command.";
            try {
                Pattern p = Pattern.compile( org.ASUX.yaml.BatchFileGrammer.REGEXP_NAME );
                if ( p.matcher( col ).matches() )
                    continue;
                else
                    throw new Exception( errMsg );
            }catch(PatternSyntaxException e){
                if ( _verbose ) e.printStackTrace(System.err);
                throw new Exception( errMsg );
            }
        }
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch()
     */
    protected boolean onPartialMatch( final Node _node, final YAMLPath _yamlPath, final String _keyStr, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {    
        // Do Nothing for "Table YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch()
     */
    protected boolean onEnd2EndMatch( final YAMLPath _yamlPath, final Object _key, final Node _keyNode, final Node _valNode, final Node _parentNode, final LinkedList<String> _end2EndPaths ) throws Exception
    {
        final String HDR = CLASSNAME +": onEnd2EndMatch(): ";
        if ( this.verbose ) {
            System.out.print( CLASSNAME +": onEnd2EndMatch(): _end2EndPaths =");
            _end2EndPaths.forEach( s -> System.out.print(s+_yamlPath.delimiter) );
            System.out.println("onEnd2EndMatch: _key = ["+ _key +"] _valNode = ["+ _valNode +"]");
        }

        String errmsg = HDR +" For the pattern provided on cmdline for YAML-Path "+ _yamlPath.toString() +" we found [";
        for( String s: _end2EndPaths )
            errmsg += s+this.delimiter;

        //-------------------------------------
        // local Class - so I can create a passive "local function"
        class PullTableElemsFromMap {
            public void go( final boolean _verbose, final MappingNode _mapnode, final String[] _tableColumns, final String _errmsg ) throws TableCmdException {
                final String HDR2 = HDR +": PullTableElemsFromMap.go() ";
                final ArrayList<String> tablerow = new ArrayList<>();
                final java.util.List<Node> rowAsNode = new LinkedList<Node>();
                final java.util.List<NodeTuple> tuples = _mapnode.getValue();
                // !!!!!!!! ATTENTION !!!!!!! The outermost for-loop must be over _tableColumns.  That is the ONLY way we can throw an Exception stating: canNOT find column
                for (int ix=0; ix < _tableColumns.length; ix++ ) {
                    final String col = _tableColumns[ix];
                    if ( _verbose ) System.out.println( HDR2 +": Going thru Column # "+ ix +" for key="+ col +" = "+ _mapnode  );
                    boolean bFound = false;
                    INNERFORLOOP:
                    for( NodeTuple kv: tuples ) {
                        final Node keyN = kv.getKeyNode();
                        assertTrue( keyN instanceof ScalarNode );
                        final ScalarNode scalarKeyN = (ScalarNode) keyN;
                        final String keyAsStr = scalarKeyN.getValue();
                        assertTrue( keyAsStr != null );
                        final Node valN = kv.getValueNode();
                        if ( _verbose ) System.out.println( HDR2 +" checking on [LHS] !keyTag : RHS = ["+ keyN + "] !"+ scalarKeyN.getTag().getValue() + " : "+ valN + " ;" );

                        if ( valN instanceof ScalarNode && valN.getNodeId() == NodeId.scalar ) {
                            final ScalarNode scalarValN = (ScalarNode) valN;
                            if ( col.equals(keyAsStr) ) {
                                if ( _verbose ) System.out.println( HDR2 +" found LHS, keyTag & RHS = ["+ keyN + "] !"+ scalarKeyN.getTag().getValue() + " : "+ scalarValN.getValue() + " ;" );
                                tablerow.add( scalarValN.getValue() );
                                final ScalarNode newSN = new ScalarNode( Tag.STR,     scalarValN.getValue(),     null, null, dumperoptions.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.SINGLE_QUOTED
                                rowAsNode.add( newSN );
                                bFound = true;
                                break INNERFORLOOP; // break inner for-loop
                            } else {
                            }
                        } else {
                            final String s = _errmsg + "].  At that location canNOT find SIMPLE-PAIR-of-SCALAR @ key= "+ keyAsStr +". Instead found '"+ valN +"' provided to Table-query Command.";
                            if ( _verbose ) System.out.println( HDR + s );
    // ?????????????? Need a cmdline-flag that gives user the option of gracefully returning KVPairs, instead of throwing Exception (next line)
                            throw new TableCmdException( _errmsg + "].  At that location canNOT find SIMPLE-PAIR-of-SCALAR @ key= "+ keyAsStr +". Instead found '"+ valN +"' provided to Table-query Command." );
                        }
                    } // inner for loop

                    if (  !  bFound )
                        throw new TableCmdException( _errmsg + "].  At that location canNOT find column # "+ ix +" '"+ col +"' provided to Table-query Command." );
                } // outer for loop

                // if we are here, ALL the 'columns' of the tabular-output were found (if not, an exception is thrown)
                count ++;

                // this.output wont work within this inline-class
                output.add( tablerow ); // could be a string or a java.util.LinkedHashMap&lt;String, Object&gt;

                final SequenceNode tableRowAsNode = new SequenceNode( Tag.SEQ, false, rowAsNode,  null, null, dumperoptions.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK
                final java.util.List<Node> seqs = outputAsNode.getValue();   // 'this.outputAsNode' wont compile within this inline-class
                seqs.add( tableRowAsNode );
            } // go()
        } // local class PullTableElemsFromMap
        //-------------------------------------
        final ArrayList<String> tablerow = new ArrayList<>();
        final java.util.List<Node> rowAsNode = new LinkedList<Node>();
        if ( _valNode instanceof MappingNode ) {
            final MappingNode mapN = (MappingNode) _valNode;
            new PullTableElemsFromMap().go( this.verbose, mapN, this.tableColumns, errmsg );
        // } else if ( _valNode instanceof Node ) {
        //      = (Node) _valNode;
        } else if ( _valNode instanceof SequenceNode ) {
            final SequenceNode seqN = (SequenceNode) _valNode;
            final java.util.List<Node> listOfNodes = seqN.getValue();
            for( Node node: listOfNodes ) {
                if ( node instanceof MappingNode && node.getNodeId() == NodeId.mapping ) {
                    final MappingNode map2N = (MappingNode) node;
                    new PullTableElemsFromMap().go( this.verbose, map2N, this.tableColumns, errmsg );
                } else {
                    throw new TableCmdException( errmsg +"].  At that location canNOT find ANY subelements!  Instead it's of type="+ _valNode.getNodeId() );
                }
            } // for
        } else {
            throw new Exception( errmsg +"]. Searching to a TABULAR content, but found type ["+ _valNode.getClass().getName() +"]  with value = ["+ _valNode +"]");
        }

        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail()
     */
    protected void onMatchFail( final YAMLPath _yamlPath, final Node _parentNode, final Node _nodeNoMatch, final Object _key, final LinkedList<String> _end2EndPaths )
    {    
            // Do Nothing for "Table YAML-entry command"
    }

    //-------------------------------------
    /** This function will be called when processing has ended.
     * After this function returns, the AbstractYamlEntryProcessor class is done!
     * See details in @see org.ASUX.yaml.AbstractYamlEntryProcessor#oatEndOfInput()
     *
     * You can fuck with the contents of any of the parameters passed, to your heart's content.
     */
    protected void atEndOfInput( final Node _node, final YAMLPath _yamlPath ) throws Exception
    {
        if ( this.showStats ) System.out.println("Total=" + this.count );

        if ( this.verbose ) {
            // for( ArrayList<String> arr: this.output ) {
            //     arr.forEach( s -> System.out.print(s+"\t") );
            //     System.out.println();
            // } // for
            // The above commented code will produce same output as the following for-loop - ONLY if Scalar 2D-array output.  In case the 'cells' in the 'table' are NOT ScalarNodes, the code below does a far better job of producing human-readable debugging-output.
            final java.util.List<Node> seqs = this.outputAsNode.getValue();
            for ( int ix=0;  ix < seqs.size(); ix ++ ) {
                final Node seqItemNode = seqs.get(ix); 
                if ( seqItemNode.getNodeId() == NodeId.scalar && seqItemNode instanceof ScalarNode ) {
                    final ScalarNode scN = (ScalarNode) seqItemNode;
                    System.out.print( scN +"\t" );
                } else {
                    final String s = NodeTools.Node2YAMLString(seqItemNode);
                    System.out.println( s );
                }
            } // for loop
            System.out.println();
        } // if
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     * @return the count of how many matches happened.  This value is also = this.getOutput.size()
     */
    public int getCount() {
        return this.count;
    }

    /**
     * @return the output as an LinkedList of objects (either Strings or java.util.LinkedHashMap&lt;String, Object&gt; objects).  This is because the 'rhs' of an 
     */
    public LinkedList< ArrayList<String> > getOutputAsJavaCollection() {
        return this.output;
    }

    /**
     * @return the output as an LinkedList of objects (either Strings or java.util.LinkedHashMap&lt;String, Object&gt; objects).  This is because the 'rhs' of an 
     */
    public Node getOutput() {
        final java.util.List<Node> seqs = this.outputAsNode.getValue();
        if ( seqs.size() == 1 ) // if it's a single 'item' that we found.. provide the user a more humanly-meaningful format. .. .. just provide that single element/Node.
            return seqs.get(0);
        else
            return this.outputAsNode;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

}
