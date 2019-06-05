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

import java.util.LinkedList;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java


/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor
 */
public class DeleteYamlEntry extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = DeleteYamlEntry.class.getName();

    protected final LinkedList< Tuple< Node, Object> > keys2bRemoved = new LinkedList<>();

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _d instance of org.yaml.snakeyaml.DumperOptions (typically passed in via {@link CmdInvoker})
     */
    public DeleteYamlEntry( final boolean _verbose, final boolean _showStats, final DumperOptions _d ) {
        super( _verbose, _showStats, _d );
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    
    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch()
     */
    protected boolean onPartialMatch( final Node _node, final YAMLPath _yamlPath, final String _keyStr, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {    
        // Do Nothing for "delete YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch()
     */
    protected boolean onEnd2EndMatch( final YAMLPath _yamlPath, final Object _key, final Node _keyNode, final Node _valNode, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {
        if ( this.verbose )
            System.out.print("onEnd2EndMatch: _end2EndPaths =");
        if ( this.verbose || this.showStats ) {
            _end2EndPaths.forEach( s -> System.out.print(s+", ") );
            System.out.println("");
        }

        this.keys2bRemoved.add( new Tuple< Node, Object>( _parentNode, _key ) );
        if ( this.verbose ) System.out.println( CLASSNAME +": onE2EMatch: count="+this.keys2bRemoved.size());
        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail()
     */
    protected void onMatchFail( final YAMLPath _yamlPath, final Node _parentNode, final Node _nodeNoMatch, final Object _key, final LinkedList<String> _end2EndPaths )
    {    
        // Do Nothing for "delete YAML-entry command"
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
        if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): count=" + this.keys2bRemoved.size() );
        for ( Tuple< Node, Object> tpl: this.keys2bRemoved ) {
            final Node parentN = tpl.key;
            // final String rhsStr = tpl.val.toString();
            if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): item["+ tpl.val.getClass().getName() +"]= "+ tpl.val +"  within parentN["+ parentN.getClass().getName() +"]="+ tpl.key +" " );
            // if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): atEndOfInput: parentN="+ tpl.key +": item["+ tpl.val.getClass().getName() +"]= "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));
            if ( tpl.val instanceof String && parentN instanceof MappingNode ) {
                assert( tpl.val instanceof String ); 
                final String key2Search = (String) tpl.val;

                final MappingNode mapN = (MappingNode) parentN;
                final java.util.List<NodeTuple> tuples = mapN.getValue();
                // iterate thru tuples and find 'key2Search' as a ScalarNode for Key/LHS.
                int ix = -1;
                boolean bFound = false;
                for( NodeTuple kv: tuples ) {
                    ix ++;
                    final Node keyN = kv.getKeyNode();
                    assert( keyN instanceof ScalarNode );
                    assert( keyN.getNodeId() == NodeId.scalar ); // if assert fails, what scenario does that represent?
                    // @SuppressWarnings("unchecked")
                    final ScalarNode scalarN = (ScalarNode) keyN;
                    final String keyAsStr = scalarN.getValue();
                    assert( keyAsStr != null );
                    if ( this.verbose ) System.out.println( CLASSNAME +" atEndOfInput(): found LHS, keyTag & RHS = ["+ keyN + "] !"+ scalarN.getTag().getValue() + " : "+ kv.getValueNode() + " ;" );
                    if ( keyAsStr.equals(key2Search) ) {
                        bFound = true;
                        break;
                    }
                } // for loop
                if ( bFound )
                    tuples.remove( ix );

            } else if ( tpl.val instanceof Integer && parentN instanceof SequenceNode ) {
                assert( tpl.val instanceof Integer ); 
                final Integer ix = (Integer) tpl.val;

                final SequenceNode seqN = (SequenceNode) tpl.key;
                final java.util.List<Node> seqs = seqN.getValue();
                seqs.remove( ix.intValue() );
            } else {
                throw new Exception( CLASSNAME +": atEndOfInput(): UNEXPECTED item["+ tpl.val.getClass().getName() +"]= "+ tpl.val +"  within parentN["+ parentN.getClass().getName() +"]="+ tpl.key +" " );
            }
        } // for

        // java's forEach never works if you are altering anything within the Lambda body
        // this.keys2bRemoved.forEach( tpl -> {tpl.val.remove(tpl.key); });
        if ( this.showStats ) System.out.println( "count="+this.keys2bRemoved.size() );

        // This IF-Statement line below is Not outputting the entire YAML-Path.  So, I'm relying on onEnd2EndMatch() to do the job.
        // Not a squeeky clean design (as summary should be done at end only).. but it avoids having to add additional data structures
        // if ( this.showStats ) this.keys2bRemoved.forEach( tpl -> { System.out.println(tpl.key); } );
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

}
