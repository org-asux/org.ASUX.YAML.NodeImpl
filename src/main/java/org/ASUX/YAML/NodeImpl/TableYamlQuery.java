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

import java.util.regex.*;

import java.util.LinkedList;
import java.util.ArrayList;
// import java.util.LinkedHashMap;

// https://yaml.org/spec/1.2/spec.html#id2762107
// import org.yaml.snakeyaml.Yaml;
// import org.yaml.snakeyaml.composer.Composer;
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

    protected org.ASUX.yaml.CmdLineArgsTableCmd cmdLineArgs;
    // private String delimiter; //  = "UNINITIALIZED DELIMITER";

    /** How many 'row/lines' matches happened.  This value should be identical to this.getOutput.size(). Value obtained using {@link #getCount()}*/
    protected int count;
    /** By design, this class is supposed to collect a SIMPLE TABULAR arrangement of Strings */
    protected LinkedList< ArrayList<String> > output;
    /** The _EXACT_ same content as in 'this.output' .. but, this time in SnakeYAML data-model. */
    protected SequenceNode outputAsNode;

    //------------------------------------------------------------------------------
    public static class TableCmdException extends Exception {
        private static final long serialVersionUID = 12L;
        public TableCmdException(String _s) { super(_s); }
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

     // *  @param _delim This delimiter should be used to separate the 'column-names' within the _tableColumns parameter.  pass in a value like '.'  '\t'   ','   .. pass in such a character as a string-parameter (being flexible in case delimiters can be more than a single character)
     // *  @param _tableColumns a delimiter-separated list of "columns" (think SQL table columns).  The output of this table-yaml command is true 2-D table (2-D Array to be precise)
    /** The only Constructor.
     *  @param _claTbl NotNull object, that is created when the comamnd-line (or a line in batch-file) is parsed by the ANTLR4 or other parser.
     *  @param _d instance of org.yaml.snakeyaml.DumperOptions (typically passed in via {@link CmdInvoker})
     *  @throws Exception if Pattern provided for YAML-Path is either semantically empty, Not a viable variable-name (see BatchFileGrammer.java's REGEXP_NAME constant) or is NOT java.util.Pattern compatible.
     */
    public TableYamlQuery(final org.ASUX.yaml.CmdLineArgsTableCmd _claTbl, final DumperOptions _d )
                        throws Exception
    {
        super( _claTbl.verbose, _claTbl.showStats, _d );
        this.cmdLineArgs = _claTbl;

        // Sanity check of "_delim"
        try {
            /* Pattern p = */ Pattern.compile( this.cmdLineArgs.yamlPatternDelimiter );
        }catch(PatternSyntaxException e){
            if ( this.cmdLineArgs.verbose ) e.printStackTrace(System.err);
            System.err.println( CLASSNAME +" Constructor: Invalid delimiter-pattern '"+ this.cmdLineArgs.yamlPatternDelimiter +"' provided to constructor "+ e );
            throw e; // Should this method .. instead return for an invalid YAMLPath object.. .. and Let "this.isValid" stay as false ??
        }

        final String tablColumnsStr = this.cmdLineArgs.tableColumns.trim(); // strip leading and trailing whitesapce (Java11 user strip(), Java<11, use trim()
        if ( this.cmdLineArgs.tableColumns.length() <= 0 ) {
            throw new org.ASUX.yaml.InvalidCmdLineArgumentException( CLASSNAME +" Constructor: semantically EMPTY list of Table-columns provided to Table-query Command." );
        }

        if (this.verbose) System.out.println( CLASSNAME + ": about to split '"+ tablColumnsStr +"' with delimiter '"+ this.cmdLineArgs.yamlPatternDelimiter +"'");
        final String[] tableColumns = tablColumnsStr.split( this.cmdLineArgs.yamlPatternDelimiter );
        for(int ix=0; ix < tableColumns.length; ix++ ) {
            final String col = tableColumns[ix];
            final String errMsg = CLASSNAME +" Constructor: Invalid column # "+ ix +" '"+ col +"' provided to Table-query Command.";
            try {
                Pattern p = Pattern.compile( org.ASUX.yaml.BatchFileGrammer.REGEXP_NAME );
                if (  !  p.matcher( col ).matches() ) {
                    throw new org.ASUX.yaml.InvalidCmdLineArgumentException( errMsg );
                }
            }catch(PatternSyntaxException e){
                if ( this.cmdLineArgs.verbose ) e.printStackTrace(System.err);
                throw new Exception( errMsg );
            }
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     * In case you'd like to re-use this subclass within other java-code, we absolutely need to reset working instance-variables.
     */
    @Override
    public void reset() {
        this.count = 0;
        this.output = new LinkedList<>();
        this.outputAsNode = new SequenceNode( Tag.SEQ, false, new LinkedList<>(),  null, null, this.dumperoptions.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK
        // this.tableColumns <-- can ONLY be changed via Constructor, as it's NOT publicly accesible instance-variable, and currently NO setter() exists.
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch()
     */
    protected boolean onPartialMatch( final Node _node, final org.ASUX.yaml.YAMLPath _yamlPath, final String _keyStr, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {    
        // Do Nothing for "Table YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch()
     */
    protected boolean onEnd2EndMatch( final org.ASUX.yaml.YAMLPath _yamlPath, final Object _key, final Node _keyNode, final Node _valNode, final Node _parentNode, final LinkedList<String> _end2EndPaths ) throws Exception
    {
        final String HDR = CLASSNAME +": onEnd2EndMatch(): ";
        if ( this.verbose ) {
            System.out.print( CLASSNAME +": onEnd2EndMatch(): _end2EndPaths =");
            _end2EndPaths.forEach( s -> System.out.print(s+_yamlPath.delimiter) );
            System.out.println("onEnd2EndMatch: _key = ["+ _key +"] _valNode = ["+ _valNode +"]");
        }

        StringBuilder errMsgBuf = new StringBuilder( "ERROR: " );
        if ( this.cmdLineArgs.verbose ) errMsgBuf.append( HDR );
        errMsgBuf.append( "For the pattern provided on cmdline as YAML-Path " + _yamlPath.yamlPathStr + " .. .. we found [" );
        for( String s: _end2EndPaths )
            errMsgBuf.append(s).append( this.cmdLineArgs.yamlPatternDelimiter );
        final String errMsg = errMsgBuf.toString();

        //-------------------------------------
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        //-------------------------------------

        // local Class - so I can create a "local function-method", without passing too many variables to the function-method
        class PullTableElemsFromMap {
            public void go( final MappingNode _mapnode, final String[] _tableColumns, final String _errmsg ) throws Exception {
                final String HDR2 = HDR +": PullTableElemsFromMap.go() ";
                final ArrayList<String> tableRow = new ArrayList<>();
                final java.util.List<Node> tableRowNodeObjs = new LinkedList<>();
                final java.util.List<NodeTuple> tuples = _mapnode.getValue();
                // !!!!!!!! ATTENTION !!!!!!! The outermost for-loop must be over _tableColumns.  That is the ONLY way we can throw an Exception stating: canNOT find column
                // OUTER FOR LOOP:
                for (int ix=0; ix < _tableColumns.length; ix++ ) {

                    final String col = _tableColumns[ix];
                    if ( verbose ) System.out.println( HDR2 +": Going thru Column # "+ ix +" for key="+ col +" = "+ _mapnode  );

                    final String subDelimiter = "/";
                    if ( verbose ) System.out.println( HDR2 +" col='"+ col +"' and contains="+ col.contains( subDelimiter ) );
                    if ( col.contains( subDelimiter ) ) {
                        // It means.. We have a sophisticated 'path' and NOT just a column-name as the value for 'col'
                        if ( verbose ) System.out.println( NodeTools.Node2YAMLString(_mapnode));
// ATTENTION: the delimiter to split-up 'col' into substrings is hardcoded as '/'
// ATTENTION: In contrast, 'col' was parsed out of '_tableColumns' using 'delimiter' variable.  OBVIOUSLY, we can't use the same 'delimiter' variable.

                        final org.ASUX.yaml.YAMLPath yp = new org.ASUX.yaml.YAMLPath( verbose, col, subDelimiter ); // convert something like '../sibling' into a YAMLPath object
                        final org.ASUX.yaml.CmdLineArgsReadCmd claRead = new org.ASUX.yaml.CmdLineArgsReadCmd();
                        claRead.verbose = verbose;   claRead.showStats = false;   claRead.cmdType = org.ASUX.yaml.Enums.CmdEnum.READ;
                        claRead.projectionPath = null;  claRead.cmdAsStr = "Created-by-code within "+CLASSNAME; // claRead.YAMLLibrary = this.claTable.YAMLLibrary;
                        final ReadYamlEntry readYE = new ReadYamlEntry( claRead, dumperoptions );

                        if ( yp.yamlElemArr.length == 1 && "..".equals( yp.yamlElemArr[0] ) ) {
                            // "_parentNode" won't fit into the expected data-types within the "output" instance-variable of the main-class of this file
                            throw new org.ASUX.yaml.InvalidCmdLineArgumentException( _errmsg + "].\n\t(#1) At that location canNOT give you a scalar at '"+ col +"' provided to Table-query Command." );

                        } else if ( yp.yamlElemArr.length > 1 && "..".equals(yp.yamlElemArr[0]) ) {
                            // throw new org.ASUX.yaml.InvalidCmdLineArgumentException( _errmsg + "].  (#2) Unimplemented support for '..' PREFIX within YAML-Path provided '"+ col +"." );

                            // strip out the initial '..' in the YAML-path denoted by 'col'
                            // Lookup up within parentNode
                            yp.hasNext();
                            if ( verbose ) System.out.println( HDR2 +" yp='"+ yp +"' and yp.getSuffix()="+ yp.getSuffix() );

                            readYE.searchYamlForPattern( _parentNode, yp.getSuffix(), subDelimiter );
                            final SequenceNode seqN1 = readYE.getOutput();
                            // The lookup @ 'col' a.k.a better be a ScalarNode only.  Obviously, by definition, a single ScalarMode only.
                            if ( seqN1.getValue().size() != 1 ||   !  (seqN1.getValue().get(0) instanceof ScalarNode)   ) {
                                throw new org.ASUX.yaml.InvalidCmdLineArgumentException( _errmsg + "].\n\t(#2) At that location canNOT pick a SINGLE scalar at '"+ col +"' provided to Table-query Command." );
                            } else {
                                final ScalarNode sn1 = (ScalarNode) seqN1.getValue().get(0);
                                tableRow.add( sn1.getValue() );
                                final ScalarNode newSN = new ScalarNode( Tag.STR,     sn1.getValue(),     null, null, dumperoptions.getDefaultScalarStyle() );
                                tableRowNodeObjs.add( newSN );
                            } // if-else (above 5 lines)

                        } else {
                            // the YAML-path denoted by 'col' does _NOT_ begin with '..'
                            if ( verbose ) System.out.println( "_mapnode = "+ NodeTools.Node2YAMLString( _mapnode ) + "\n" );
                            readYE.recursiveSearch( _mapnode,   yp,  null,  new LinkedList<>() );
                            final SequenceNode seqN2 = readYE.getOutput();
                            if ( verbose ) System.out.println( "yp = '"+ yp +"'\nseqN2 = "+ NodeTools.Node2YAMLString( seqN2 ) + "\n" );
                            if ( seqN2 == null || seqN2.getValue().size() != 1 ||    !  (seqN2.getValue().get(0) instanceof ScalarNode)   ) {
                                throw new org.ASUX.yaml.InvalidCmdLineArgumentException( _errmsg + "].\n\t(#3) At that location canNOT pick a SINGLE scalar at '"+ col +"' provided to Table-query Command." );
                            } else {
                                final ScalarNode sn2 = (ScalarNode) seqN2.getValue().get(0);
                                tableRow.add( sn2.getValue() );
                                // final ScalarNode newSN = new ScalarNode( Tag.STR,     sn2.getValue(),     null, null, dumperoptions.getDefaultScalarStyle() );
                                // tableRowNodeObjs.add( newSN );
                            }
                        }

                        continue; // OUTER FOR LOOP;
                    } // if col.contains(subDelimiter)

                    boolean bFoundColumn = false;
                    // INNER FOR LOOP:
                    for( NodeTuple kv: tuples ) {
                        final Node keyN = kv.getKeyNode();
                        assertTrue( keyN instanceof ScalarNode );
                        final ScalarNode scalarKeyN = (ScalarNode) keyN;
                        final String keyAsStr = scalarKeyN.getValue();
                        assertNotNull( keyAsStr );
                        final Node valN = kv.getValueNode();
                        if ( verbose ) System.out.println( HDR2 +" checking on [LHS] !keyTag : RHS = ["+ keyN + "] !"+ scalarKeyN.getTag().getValue() + " : "+ valN + " ;" );

                        // Check if 'col' matches one of the keys under parentNode
                        if ( col.equals(keyAsStr) ) {
                            if ( valN instanceof ScalarNode && valN.getNodeId() == NodeId.scalar ) {
                                final ScalarNode scalarValN = (ScalarNode) valN;
                                if ( verbose ) System.out.println( HDR2 +" found LHS, keyTag & RHS = ["+ keyN + "] !"+ scalarKeyN.getTag().getValue() + " : "+ scalarValN.getValue() + " ;" );
                                tableRow.add( scalarValN.getValue() );
                                final ScalarNode newSN = new ScalarNode( Tag.STR,     scalarValN.getValue(),     null, null, dumperoptions.getDefaultScalarStyle() );
                                tableRowNodeObjs.add( newSN );
                                bFoundColumn = true;
                                break; // INNER FOR LOOP; // break inner for-loop
                            } else {
                                final String s = _errmsg + "].  (#4) At that location canNOT find SIMPLE-PAIR-of-SCALAR @ key= "+ keyAsStr +". Instead found '"+ valN +"' provided to Table-query Command.";
                                if ( verbose ) System.out.println( HDR + s );
                                // TO-DO: Need a cmdline-flag that gives user the option of gracefully returning KVPairs, instead of throwing Exception (next line)
                                // throw new org.ASUX.yaml.InvalidCmdLineArgumentException( _errmsg + "].  (#4) At that location canNOT find SIMPLE-PAIR-of-SCALAR @ key= "+ keyAsStr +". Instead found '"+ valN +"' provided to Table-query Command." );
                                throw new org.ASUX.yaml.InvalidCmdLineArgumentException( s );
                            } // if-else whether a scalarNode
                        } // IF the column-name matched one of the columns/keys

                    } // inner for-loop - over each kv: tuples

                    if (  !  bFoundColumn ) {
                        throw new org.ASUX.yaml.InvalidCmdLineArgumentException( _errmsg + "].  (#5) At that location canNOT find column # "+ ix +" '"+ col +"' provided to Table-query Command." );
                    }

                } // outer for-loop (over each column-name that the user wants outputted)

                // if we are here, ALL the 'columns' of the tabular-output were found (if not, an exception is thrown)
                count ++;

                // ATTENTION !!!! "this.output" won't work within an INLINE-class.  So, hope you realize "output" refers to the Java-class that this file is about.
                output.add( tableRow ); // could be a string or a java.util.LinkedHashMap&lt;String, Object&gt;

                final SequenceNode tableRowAsNode = new SequenceNode( Tag.SEQ, false, tableRowNodeObjs,  null, null, dumperoptions.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK
                // NOTE: cannot write 'this.outputAsNode' - in line below.  As this line of code is within a temporary inner-class
                final java.util.List<Node> seqs = outputAsNode.getValue();   // FYI: 'this.outputAsNode' wont compile within this inline-class
                seqs.add( tableRowAsNode );

            } // go() function
        } // local class PullTableElemsFromMap

        //-------------------------------------
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        //-------------------------------------

        // final ArrayList<String> tablerow = new ArrayList<>();
        // final java.util.List<Node> row As Node = new LinkedList<Node>();
        final String tablColumnsStr = this.cmdLineArgs.tableColumns.trim(); // strip leading and trailing whitesapce (Java11 user strip(), Java<11, use trim()
        final String[] tableColumns = tablColumnsStr.split( this.cmdLineArgs.yamlPatternDelimiter );

        if ( _valNode instanceof MappingNode ) {
            final MappingNode mapN = (MappingNode) _valNode;
            new PullTableElemsFromMap().go( mapN, tableColumns, errMsg ); // see inner class definition above
        // } else if ( _valNode instanceof Node ) {
        //      = (Node) _valNode;
        } else if ( _valNode instanceof SequenceNode ) {
            final SequenceNode seqN = (SequenceNode) _valNode;
            final java.util.List<Node> listOfNodes = seqN.getValue();
            for( Node node: listOfNodes ) {
                if ( node instanceof MappingNode && node.getNodeId() == NodeId.mapping ) {
                    final MappingNode map2N = (MappingNode) node;
                    new PullTableElemsFromMap().go( map2N, tableColumns, errMsg );
                } else {
                    throw new org.ASUX.yaml.InvalidCmdLineArgumentException( errMsg +"].  (#6) At that location canNOT find ANY subelements!  Instead it's of type="+ _valNode.getNodeId() );
                }
            } // for
        } else {
            throw new org.ASUX.yaml.InvalidCmdLineArgumentException( errMsg +"]. Searching to a TABULAR content, but found type ["+ _valNode.getClass().getName() +"]  with value = ["+ _valNode +"]");
        }

        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail()
     */
    protected void onMatchFail( final org.ASUX.yaml.YAMLPath _yamlPath, final Node _parentNode, final Node _nodeNoMatch, final Object _key, final LinkedList<String> _end2EndPaths )
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
    protected void atEndOfInput( final Node _node, final org.ASUX.yaml.YAMLPath _yamlPath ) throws Exception
    {
        if ( super.showStats ) System.out.println("Total=" + this.count );

        if ( this.verbose ) {
            // for( ArrayList<String> arr: this.output ) {
            //     arr.forEach( s -> System.out.print(s+"\t") );
            //     System.out.println();
            // } // for
            // The above commented code will produce same output as the following for-loop - ONLY if Scalar 2D-array output.  In case the 'cells' in the 'table' are NOT ScalarNodes, the code below does a far better job of producing human-readable debugging-output.
            final java.util.List<Node> seqs = this.outputAsNode.getValue();
            for (final Node seqItemNode : seqs) {
                if (seqItemNode.getNodeId() == NodeId.scalar && seqItemNode instanceof ScalarNode) {
                    final ScalarNode scN = (ScalarNode) seqItemNode;
                    System.out.print(scN + "\t");
                } else {
                    final String s = NodeTools.Node2YAMLString(seqItemNode);
                    System.out.println(s);
                }
            } // for loop
            System.out.println();
        } // if
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     * @return the count of how many 'row/lines' matches happened.  This value is also = this.getOutput.size()
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
        return this.outputAsNode;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

}
