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

import org.ASUX.common.Tuple;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;


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


/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link org.ASUX.yaml.AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see org.ASUX.yaml.AbstractYamlEntryProcessor
 */
public class InsertYamlEntry extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = InsertYamlEntry.class.getName();

    // Note: We need to remove the "old" - exactly as DeleteYamlEntry.java does.  Then we insert new value.
    protected final ArrayList< Tuple<Object, Node> > existingPathsForInsertion = new ArrayList<>();
    protected final ArrayList< Tuple< YAMLPath, Node> > newPaths2bCreated = new ArrayList<>();

    protected final Object newData2bInserted;

    protected Node output;

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _do instance of org.yaml.snakeyaml.DumperOptions (typically passed in via {@link CmdInvoker})
     *  @param _nob this can be either a java.lang.String or a org.yaml.snakeyaml.nodes.Node object
     *  @throws java.lang.Exception - if the _nob parameter is not as per above Spec
     */
    public InsertYamlEntry( final boolean _verbose, final boolean _showStats, final DumperOptions _d, Object _nob ) throws Exception
    {
        super( _verbose, _showStats, _d );

        final String HDR = CLASSNAME +": Constructor: ";
        if ( _nob == null )
            throw new Exception( HDR +"_nob parameter is Null" );

        this.newData2bInserted = _nob;
        if ( this.verbose ) System.out.println(HDR + " _nob="+ _nob );

        assert( _nob instanceof String || _nob instanceof Node );
    } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * <p>If the _nob (the new content provided as the ONLY parameter to the INSERT/REPLACE YAML command) is a Node.. then, do Nothing</p>
     * <p>If the _nob (the new content provided as the ONLY parameter to the INSERT/REPLACE YAML command) is a java.lang.String.. then, convert it into a org.yaml.snakeyaml.nodes.ScalarNode.</p>
     * <p>Otherwise throw an exception</p>
     * @param _nob the new content provided as the ONLY parameter to the INSERT/REPLACE YAML command
     *  @throws java.lang.Exception - if the _nob parameter is not as per above Spec
     */
    public final Node validateNewContent( final Object _nob ) throws Exception
    {
        final String HDR = CLASSNAME +": validateNewContent(): ";
        Node tmpO = null;
        if ( this.verbose ) System.out.println( HDR +"  _nob type= "+ _nob.getClass().getName() +" (_nob instanceof Node)= "+ (_nob instanceof Node) );
        if ( _nob instanceof Node ) {
            tmpO = (Node) _nob;
        } else if ( _nob instanceof String ) {
            final ScalarNode newnode = new ScalarNode( Tag.STR, _nob.toString(), null, null, this.dumperoptions.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.SINGLE_QUOTED
            if ( this.verbose ) System.out.println( HDR +" new ScalarNode="+ newnode );
            tmpO = newnode;
        } else {
            throw new Exception( HDR +": Serious ERROR: Your new content of type ["+ _nob.getClass().getName() +"]  with value = ["+ _nob.toString() +"]");
        }
        return tmpO;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch}
     */
    protected boolean onPartialMatch( final Node _node, final YAMLPath _yamlPath, final String _keyStr, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {   // Do Nothing for "Insert YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch}
     */
    protected boolean onEnd2EndMatch( final YAMLPath _yamlPath, final Object _key, final Node _keyNode, final Node _valNode, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {
        final String HDR = CLASSNAME +": onEnd2EndMatch(): ";
        if ( this.verbose )
            System.out.print( HDR +"_end2EndPaths=");
        if ( this.verbose || this.showStats ) {
            _end2EndPaths.forEach( s -> System.out.print(s+", ") );
            System.out.println("");
        }
        this.existingPathsForInsertion.add( new Tuple<Object, Node>( _key, _parentNode ) );
        if ( this.verbose ) System.out.println( HDR +"count="+this.existingPathsForInsertion.size() +" _parentNode="+ _parentNode );
        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail}
     */
    protected void onMatchFail( final YAMLPath _yamlPath, final Node _parentNode, final Node _nodeNoMatch, final Object _key, final LinkedList<String> _end2EndPaths )
    {
        final String HDR = CLASSNAME +": onMatchFail(): ";
        if ( this.verbose ) System.out.println( HDR +">>>>>>>>>>>> _yamlPath="+ _yamlPath );
        if ( this.verbose ) System.out.println( HDR +">>>>>>>>>>>> _parentNode="+ _parentNode );
        // we are going to have TONS and TONS of entries within this.newPaths2bCreated !!!
        // Especially for large YAML files - let's say - 1000 lines, then.. you could see a couple of hundred entries
        // Also, we'll have SO MANY DUPLICATES!
        if ( _yamlPath == null || _parentNode == null )
            return;
        final Tuple< YAMLPath, Node > tuple = new Tuple<>( YAMLPath.deepClone(_yamlPath), _parentNode );
        if ( this.verbose ) System.out.println( HDR +">>>>>>>>>>>> tuple="+ tuple );
        this.newPaths2bCreated.add( tuple );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** See details and warnings in {@link AbstractYamlEntryProcessor#atEndOfInput}
     * You can fuck with the contents of any of the parameters passed, to your heart's content.
     */
    // *  @param _topmostNode the topmost Node in the input YAML
    // *  @throws Exception like ClassNotFoundException while trying to serialize and deserialize the input-parameter
    protected void atEndOfInput( final Node _topmostNode, final YAMLPath _yamlPath ) throws Exception
    {
        final String HDR = CLASSNAME + ": atEndOfInput(): ";

        this.output = _topmostNode; // this should handle all scenarios - except when '/' is the YAML path.

        final Node newNode2bInserted = validateNewContent( this.newData2bInserted );
        if ( YAMLPath.ROOTLEVEL.equals( _yamlPath.getRaw() ) ) // '/' is exactly the entire YAML-Path pattern provided by the user on the cmd line
        {
                if ( _topmostNode instanceof MappingNode && _topmostNode.getNodeId() == NodeId.mapping ) {
                final MappingNode topmostMapN = (MappingNode) _topmostNode;
                final java.util.List<NodeTuple> topmostTuples = topmostMapN.getValue();
                if ( newNode2bInserted instanceof MappingNode ) {
                    final MappingNode newMapN = (MappingNode) this.newData2bInserted;
                    final java.util.List<NodeTuple> newTuples = newMapN.getValue();
                    topmostTuples.addAll( newTuples );
                } else {
                    throw new Exception( "Invalid combination of new content and --input.  You provided new-content for "+ YAMLPath.ROOTLEVEL +" .. .. but provided new-content is NOT a proper 'Map' YAML. Instead new-content is of type ["+ this.newData2bInserted.getClass().getName() +"]  with value = ["+ this.newData2bInserted.toString() +"]");
                }
            } else if ( _topmostNode instanceof SequenceNode && _topmostNode.getNodeId() == NodeId.sequence ) {
                final SequenceNode seqN = (SequenceNode) _topmostNode;
                final java.util.List<Node> seqs = seqN.getValue();
                seqs.add( newNode2bInserted );
            } else if ( _topmostNode instanceof ScalarNode && _topmostNode.getNodeId() == NodeId.scalar ) {
                final ScalarNode scaN = (ScalarNode) _topmostNode;
                throw new Exception( "Invalid use of '/' for YAML-Path-RegExp. You provided new content for "+ YAMLPath.ROOTLEVEL +" .. .. but the YAML provided via --input cmdlime optiom is a SIMPLE SCALAR Node containing the string-value ["+ scaN.getValue() +"]  .. full Node details = ["+ scaN +"]");
            } else {
                throw new Exception( HDR +": Serious ERROR B: You provided new content for "+ YAMLPath.ROOTLEVEL +" .. .. but the YAML provided via --input cmdlime optiom is is of __UNKNOWN__ type ["+ _topmostNode.getClass().getName() +"]  with value = ["+ _topmostNode +"]");
            }
            // if ( newNode2bInserted instanceof MappingNode ) {
            //     this.output = (MappingNode) this.newData2bInserted;
            // } else if ( newNode2bInserted instanceof SequenceNode ) {
            //     this.output = (SequenceNode) this.newData2bInserted;
            // } else if ( newNode2bInserted instanceof Node ) {
            //     this.output = (Node) this.newData2bInserted;
            // } else if ( this.newData2bInserted instanceof String ) {
            //     final ScalarNode newnode = new ScalarNode( Tag.STR, this.newData2bInserted.toString(), null, null, this.dumperoptions.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.SINGLE_QUOTED
            //     this.output = newnode; // !!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!! this.output is Not set to the 'default' == whatever was provided as input YAML for INSERT-Cmd
            // } else {
            //     throw new Exception( HDR +": Serious ERROR: You wanted to insert new content at / .. .. but provided content that is of type ["+ this.newData2bInserted.getClass().getName() +"]  with value = ["+ this.newData2bInserted.toString() +"]");
            // }
            return; // !!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!! This function returns here.
        }
        // !!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!
        // If you want to use INSERT-CMD using '/' to add a ___2nd___ YAML document to an existing-YAML-file.. it will NOT WORK!!!
        // Instead, use bash-shell-level commands to concatenate files;.

        //-------------------------------------------------
        // first loop goes over Paths that already exist, in the sense the leaf-element exists, and we'll add a new Child element to that.
        OUTERFORLOOP:
        for ( Tuple<Object, Node> tpl: this.existingPathsForInsertion ) {
            final String rhsStr = tpl.val.toString();
            if ( this.verbose ) System.out.println( HDR +": key=["+ tpl.key +"], while map-in-context="+ rhsStr.substring(0,rhsStr.length()>241?240:rhsStr.length()) );
            // tpl.val.remove(tpl.key);

            if ( tpl.key instanceof String && tpl.val instanceof MappingNode ) {
                final String key2Search = (String) tpl.key; // could be null, even if code is not buggy

                final MappingNode mapN = (MappingNode) tpl.val;
                final java.util.List<NodeTuple> tuples = mapN.getValue();
                // iterate thru tuples and find 'key2Search' as a ScalarNode for Key/LHS.
                int ix = -1;
                boolean bFound = false;
                NodeTuple newTuple = null;
                for( NodeTuple kv: tuples ) {
                    ix ++;
                    final Node keyN = kv.getKeyNode();
                    assert( keyN instanceof ScalarNode );
                    assert( keyN.getNodeId() == NodeId.scalar ); // if assert fails, what scenario does that represent?
                    final ScalarNode scalarN = (ScalarNode) keyN;
                    final String keyAsStr = scalarN.getValue();
                    assert( keyAsStr != null );
                    if ( this.verbose ) System.out.println( CLASSNAME +" atEndOfInput(): found LHS, keyTag & RHS = ["+ keyN + "] !"+ scalarN.getTag().getValue() + " : "+ kv.getValueNode() + " ;" );

                    if ( keyAsStr.equals(key2Search) ) {
                        bFound = true;
                        // Now put in a new entry - with the replacement data!  This is because NodeTuple is immutable, so it needs to be replaced (within tuples) with a new instance.
                        // newTuple = new NodeTuple( keyN, NodeTools.deepClone( newNode2bInserted ) );
                        doInsertBasedOnNodeType( mapN, key2Search, NodeTools.deepClone( newNode2bInserted ) ); // This command will CHANGE the 'tuples' iterator used for the INNER FOR LOOP!!!!!
                        // If there are multiple matches.. then without deepclone, the YAML implementation libraries (like Eso teric Soft ware)
                        // library, will use "&1" to define your 1st copy (in output) and put "*1" in
                        // all other locations this replacement text WAS SUPPOSED have been :-(
                        if ( this.verbose ) System.out.println( HDR +": key=["+ tpl.key +"], it's new value="+ this.newData2bInserted );

                        // break out of INNER for loop ( NodeTuple kv: tuples )
                        continue OUTERFORLOOP;
                    } // if
                } // INNER for loop
                assert( true ); // we should Not be here.  the bFound must be true WITHIN the Inner-For-Loop.
                // Semantically, it makes NO sense that we have an entry in 'this.existingPathsForInsertion' that is NOT in 'mapN'

            } else if ( tpl.key instanceof Integer && tpl.val instanceof SequenceNode ) {
                final Integer ix = (Integer) tpl.key;

                final SequenceNode seqN = (SequenceNode) tpl.val;
                final java.util.List<Node> seqs = seqN.getValue();
                seqs.add( ix.intValue(), newNode2bInserted );

            } else {
                throw new Exception( CLASSNAME +": atEndOfInput(): UNEXPECTED Node/Tpl2["+ tpl.key.getClass().getName() +"]="+ tpl.key +" and the Key/Ref/Tpl1["+ tpl.val.getClass().getName() +"]= "+ tpl.val +" " );
            }
        } // OUTER for loop

        if ( this.existingPathsForInsertion.size() >= 1 ) {
            if ( this.showStats ) System.out.println( "count="+ this.existingPathsForInsertion.size() );
            if ( this.showStats ) this.existingPathsForInsertion.forEach( tpl -> { System.out.println(tpl.key); } );
            return; // !!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!! This function returns here.
        }
        // else continue below.

        //------------------------------------------------
        // if we are here, it means the YAMLPathRegExpr provided by user (via command-line) did Not match ANY EXACT YAML-contents.   That is, this.existingPathsForInsertion is 'empty'
        // By implication, it means there are ____MEANINGFUL_____ entries in this.newPaths2bCreated

        //------------------------------------------------
        // IMPORTANT: See the comments inside onMatchFail
        // We need to 'cull' the entries within this.newPaths2bCreated
        int longestDepth = -1;
        for ( Tuple< YAMLPath, Node > tpl : this.newPaths2bCreated ) {
            final YAMLPath yp = tpl.key;
            if ( this.verbose ) System.out.println( HDR +": newPaths2bCreated tpl.key="+ tpl.key +"  tpl.value="+ tpl.val +" ");
            if ( yp.index()> longestDepth ) longestDepth = yp.index();
        }

        if ( this.verbose ) System.out.println( HDR +": longestDepth ="+ longestDepth +"]" );
        final ArrayList< Tuple< YAMLPath, Node > > deepestNewPaths2bCreated = new ArrayList<>();

        outerloop:
        for ( Tuple< YAMLPath, Node > tpl : this.newPaths2bCreated ) {
            final YAMLPath ypNew = tpl.key;
            if ( this.verbose ) System.out.println( HDR +": ypNew ="+ ypNew +"]" );
            if ( ypNew.index() >= longestDepth ) {
                // let's check .. have we added it already?
                for ( Tuple< YAMLPath, Node > tpl2 : deepestNewPaths2bCreated ) {
                    final YAMLPath ypE = tpl2.key;
                    if ( YAMLPath.areEquivalent( ypNew, ypE ) )
                        continue outerloop;
                } // inner for-loop
                deepestNewPaths2bCreated.add ( tpl );
                if ( this.verbose ) System.out.println( HDR +": added new entry "+tpl.key +" making deepestNewPaths2bCreated's size ="+ deepestNewPaths2bCreated.size() +"] for the newContent=/"+ tpl.val.toString() +"/" );
            }
        } // outer for-loop
        // going forward.. ignore this.newPaths2bCreated
        // instead use deepestNewPaths2bCreated (local variable).  Both are exactly the same class-type.

        //------------------------------------------------
        // This 2nd loop is going to deal with WITH MISSING 'paths' to the missing leaf-element.
        // similar to how 'mkdir -p' works on Linux.
        if ( this.verbose ) System.out.println( HDR +": deepestNewPaths2bCreated.size()="+ deepestNewPaths2bCreated.size() +"]" );
        for ( Tuple< YAMLPath, Node > tpl : deepestNewPaths2bCreated ) {
            final YAMLPath yp = tpl.key;
            final Node lowestExistingNode = tpl.val;
            final String prefix = yp.getPrefix();
            final String suffix = yp.getSuffix();
            if ( this.verbose ) System.out.println( HDR +": about to.. add the NEW path ["+ suffix +"]" );
            Node prevchildelem;
            if ( this.newData2bInserted instanceof MappingNode ) {
                prevchildelem = (MappingNode) this.newData2bInserted;
            } else if ( this.newData2bInserted instanceof SequenceNode ) {
                prevchildelem = (SequenceNode) this.newData2bInserted;
            } else if ( this.newData2bInserted instanceof Node ) {
                prevchildelem = (Node) this.newData2bInserted;
            } else if ( this.newData2bInserted instanceof String ) {
                prevchildelem = new ScalarNode( Tag.STR, this.newData2bInserted.toString(), null, null, this.dumperoptions.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.SINGLE_QUOTED
            } else {
                throw new Exception( HDR +": Serious ERROR #2: You wanted to insert new content at / .. .. but provided content that is of type ["+ this.newData2bInserted.getClass().getName() +"]  with value = ["+ this.newData2bInserted +"]");
            }

            for( int ix=yp.yamlElemArr.length - 1;   ix > yp.index() ; ix-- ) {
                // !!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!! This iterator / for-loop counts DOWN.
                final ScalarNode keySN = new ScalarNode( Tag.STR,     yp.yamlElemArr[ix],     null, null, this.dumperoptions.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.SINGLE_QUOTED
                final List<NodeTuple> nt = new LinkedList<NodeTuple>();
                nt.add ( new NodeTuple( keySN, prevchildelem ) );
                final Node newMN = new MappingNode( Tag.MAP, false,    nt,    null, null, this.dumperoptions.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK
                if ( this.verbose ) System.out.println( HDR +": added the NEW path @ "+ ix +" yp.yamlElemArr[ix]="+ yp.yamlElemArr[ix] +"  newMN= ["+ newMN +"]" );
                prevchildelem = newMN;
            }

            // we created an ENTIRE Node-Hierarchy in the FOR loop above.
            // Now .. Let's 'attach' it to the exiting --input YAML provided by the user
            final String existingKeyStr = yp.yamlElemArr[ yp.index() ];
            if ( this.verbose ) System.out.println( HDR +": Adding the final MISSING Path-elem @ ["+ yp.index() +"] = ["+ existingKeyStr +"]" );
            if ( this.verbose ) System.out.println( HDR +": Existing lowestExistingNode, which is now the parent Map = ["+ lowestExistingNode.toString() +"]" );
            doInsertBasedOnNodeType( lowestExistingNode, existingKeyStr, prevchildelem );
        }

        // java's forEach never works if you are altering anything within the Lambda body
        // this.existingPathsForInsertion.forEach( tpl -> {tpl.val.remove(tpl.key); });
        if ( this.showStats ) System.out.println( "count="+ deepestNewPaths2bCreated.size() );
        if ( this.showStats ) this.newPaths2bCreated.forEach( tpl -> { System.out.println(tpl.key); } );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    private void doInsertBasedOnNodeType( final Node lowestExistingNode, final String existingKeyStr, final Node prevchildelem ) throws Exception
    {
        final String HDR = CLASSNAME +": doInsertBasedOnNodeType(lowestExistingNode["+ lowestExistingNode.getNodeId() +"],"+existingKeyStr+",prevchildelem) ";
        if ( lowestExistingNode.getNodeId() == NodeId.mapping && lowestExistingNode instanceof MappingNode ) {
            final MappingNode existingMapNode = (MappingNode) lowestExistingNode;
            final java.util.List<NodeTuple> tuples = existingMapNode.getValue();
            // since this is all NEW content and even NEW Parent Node-heirarchy.. we'll assume a simple 'ad()' into the tuples List<> object will work without any issues.
            final NodeTuple kv = NodeTools.getNodeTuple( existingMapNode, existingKeyStr );
assert( kv != null );  // if we look up the existingMapNode.. why on earth should this be null?
            if ( kv == null ) {
                if ( this.verbose ) System.out.println( HDR +" getNodeTuple( existingMapNode, existingKeyStr="+ existingKeyStr +" ) == null. So.. adding new NodeTuple @ that location to 'tuples'." );
                final ScalarNode newKeySN = new ScalarNode( Tag.STR,     existingKeyStr,     null, null, this.dumperoptions.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.SINGLE_QUOTED
                tuples.add( new NodeTuple( newKeySN,  prevchildelem) );
            } else {
                final Node keyN = kv.getKeyNode();
                final ScalarNode scalarKeyN = (ScalarNode) keyN;
                final String keyAsStr = scalarKeyN.getValue();
                final Node valN = kv.getValueNode();
                if ( this.verbose ) System.out.println( HDR +": lowestExistingNode already has a NodeTuple @ keyStr="+ existingKeyStr +" with RHSNode's Type="+ valN.getNodeId() +" whose RHSValue="+ valN +" " );
                if ( valN.getNodeId() == NodeId.mapping && valN instanceof MappingNode ) {
                    final MappingNode mapN = (MappingNode) valN;
                    final java.util.List<NodeTuple> rhsTuples = mapN.getValue();
                    rhsTuples.add( new NodeTuple( scalarKeyN,  prevchildelem) );
                } else if ( valN.getNodeId() == NodeId.scalar && valN instanceof ScalarNode ) {
                    final ScalarNode scalarValN = (ScalarNode) valN;
                    // Since this method is common to both InsertYamlEntry.java and ReplaceYamlEntry.java (which is a subclass of InsertYamlEntry.java) .. we need to distinguish.
                    final boolean bIsReplaceCmd = (this instanceof ReplaceYamlEntry );
                    if ( bIsReplaceCmd || scalarValN.getValue().matches("\\s*") ) {
                        if ( this.verbose ) System.out.println( HDR +": REPLACING the RHS for lowestExistingNode @ keyStr="+ existingKeyStr +" with RHS='"+ scalarValN.getValue() +"' " );
                        final NodeTuple newkv = new NodeTuple( keyN, prevchildelem );
                         // since NodeTuples are immutable.. we remove the old entry and add the new entry.
                        final int ix = tuples.indexOf( kv ); // ix === location of existing NodeTuple within 'tuples'
                        tuples.add( ix, newkv); // insert BEFORE the EXISTING-NodeTuple 'kv'
                        tuples.remove( ix + 1 ); // Now remove the EXISTING-NodeTuple, which got pushes to index-location (ix+1)
                    } else {
                        // System.err.println( HDR +" " );
                        throw new Exception( "The existing node @ LHS="+ keyAsStr +" has an RHS with non-empty String/Scalar value of '"+ scalarValN.getValue() +"'. For insertCommand, that is unacceptable.  RHS should be either blank/'' or an org.yaml.snakeyaml.nodes.MappingNode!  " );
                    }
                } else if ( valN.getNodeId() == NodeId.sequence && valN instanceof SequenceNode ) {
                    final SequenceNode seqN = (SequenceNode) valN;
                    final java.util.List<Node> listOfNodes = seqN.getValue();
                    if ( this.verbose ) System.out.println( HDR +": lowestExistingNode @ keyStr="+ existingKeyStr +" is a SequenceNode ="+ seqN +" " );
                    listOfNodes.add ( prevchildelem );
                } else { // valN is neither MappingNode nor a ScalarNode
                    throw new Exception( "The existing node @ LHS="+ keyAsStr +" has an RHS is of type="+ valN.getNodeId() +" and value='"+ valN +"'. For insert /Replace Command, Not sure how to handle this!  " );
                }
            }
        } else {
            throw new Exception( "Serious ERROR #3: You wanted to insert new content at "+ existingKeyStr +" .. .. but the Node at that LOCATION .. is of type ["+ lowestExistingNode.getClass().getName() +"]  with value = ["+ lowestExistingNode +"]");
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * @return the output as an LinkedList of objects (either Strings or Node.  This is because the 'rhs' of a YAML-line can be either of the two
     */
    public Node getOutput() {
        return this.output;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

}
