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

import java.util.LinkedList;

// https://yaml.org/spec/1.2/spec.html#id2762107
// import org.yaml.snakeyaml.nodes.NodeTuple;
// import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
// import org.yaml.snakeyaml.nodes.ScalarNode;
// import org.yaml.snakeyaml.nodes.MappingNode;
// import org.yaml.snakeyaml.nodes.SequenceNode;
// import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java


/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link org.ASUX.yaml.AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see org.ASUX.yaml.AbstractYamlEntryProcessor
 */
public class ReplaceYamlEntry extends InsertYamlEntry {

    public static final String CLASSNAME = ReplaceYamlEntry.class.getName();

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _do instance of org.yaml.snakeyaml.DumperOptions (typically passed in via {@link CmdInvoker})
     *  @param _r this can be either a java.lang.String or a java.util.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries)
     *  @throws java.lang.Exception - if the _r parameter is not as per above Spec
     */
    public ReplaceYamlEntry( final boolean _verbose, final boolean _showStats, final DumperOptions _d, Object _r ) throws Exception {
        super( _verbose, _showStats, _d, _r);
        // if ( _r == null )
        //     throw new Exception( CLASSNAME + ": _r parameter is Null");
        // // if ( ! (_r instanceof java.lang.String) && ! (_r instanceof java.util.LinkedHashMap ) )
        // //     throw new Exception( CLASSNAME + ": Invalid _r parameter of type:" + _r.getClass().getName() + "'");
        // this.replacementData = _r;

        // final Output output = new Output(this.verbose);
        // final Tools tools = new Tools( this.verbose );
        // final Output.OutputType typ = output.getWrappedObjectType( _r );

        // Object o = output.getTheActualObject( _r ); // perhaps the object is already wrapped (via a prior invocation of output.wrapAnObject_intoLinkedHashMap() )
        // if (this.verbose) System.out.println( CLASSNAME +": constructort(): provided ["+ _r.toString() +"].  I assume the actual object of type=["+ typ.toString() +"]=["+ o.toString() +"]" );

        // // for the following SWITCH-statement, keep an eye on Output.OutputType
        // switch( typ ) {
        //     case Type_ArrayList:
        //     case Type_LinkedList:
        //     case Type_LinkedHashMap:
        //         // Do Nothing
        //         this.replacementData = _r;
        //         break;
        //     case Type_String:
        //         final String s = o.toString();
        //         // Convert Strings into YAML/JSON compatible LinkedHashMap .. incl. converting Key=Value  --> Key: Value
        //         LinkedHashMap<String, Object> map = null; // let's determine if o is to be treated as a LinkedHashMap.. because user provided a JSON or YAML inline to the command line.

        //         // IF-and-ONLY-IF _r is a simple-scalar-String, then this call will wrap the String by calling output.wrapAnObject_intoLinkedHashMap()
        //         try{
        //             // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
        //             // and less likely to see a YAML string inline
        //             map = tools.JSONString2YAML( s );
        //         } catch( Exception e ) {
        //             if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ s +"): FAILED-attempted to PARSE as JSON." );
        //             try {
        //                 // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
        //                 // and less likely to see a YAML string inline
        //                 map = tools.YAMLString2YAML( s, false );  // 2nd parameter is 'bWrapScalar' === false;.  if 's' turns out to be scalar at this point.. I want to go into the catch() block below and handle it there.
        //             } catch(Exception e2) {
        //                 if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ s +"): FAILED-attempted to PARSE as YAML also!  So.. treating it as a SCALAR string." );
        //                 map = null; // The user provided a !!!SCALAR!!! java.lang.String directly - to be used AS-IS
        //             }
        //         } // outer-try-catch

        //         this.replacementData = (map != null)? map : s;
        //         // if ( s.equals( output.getTheActualObject(map).toString() ) )
        //         //     o = s; // IF-and-ONLY-IF _r is a simple-scalar-String, then use it as-is.
        //         break;
        //     case Type_KVPairs:
        //     case Type_KVPair:
        //     case Type_Unknown:
        //         throw new Exception( CLASSNAME + ": constructor(): Invalid _r parameter of type:" + _r.getClass().getName() + "'");
        // }

    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch}
     */
    protected boolean onPartialMatch( final Node _node, final YAMLPath _yamlPath, final String _keyStr, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {   // Do Nothing for "Replace YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch}
     */
    protected boolean onEnd2EndMatch( final YAMLPath _yamlPath, final Object _key, final Node _keyNode, final Node _valNode, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {   return super.onEnd2EndMatch(_yamlPath, _key, _keyNode, _valNode, _parentNode, _end2EndPaths);
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail}
     */
    protected void onMatchFail( final YAMLPath _yamlPath, final Node _parentNode, final Node _nodeNoMatch, final Object _key, final LinkedList<String> _end2EndPaths )
    {   // OVERRIDE  !!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!
        // OVERRIDE what is default for InsertYamlEntry.java
        // OVERRIDE - to Do Nothing for "Replace YAML-entry command"
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This function will be called when processing has ended.
     *  After this function returns, the AbstractYamlEntryProcessor class is done!
     *  See details and warnings in {@link AbstractYamlEntryProcessor#atEndOfInput}
     *
     *  You can fuck with the contents of any of the parameters passed, to your heart's content.
     *  @throws Exception like ClassNotFoundException while trying to serialize and deserialize the input-parameter
     */
    protected void atEndOfInput( final Node _node, final YAMLPath _yamlPath ) throws Exception
    {   super.atEndOfInput( _node, _yamlPath );
        // if ( this.verbose ) System.out.println("count=" + this.keys2bRemoved.size() );
        // for (Tuple< String, LinkedHashMap<String, Object> > tpl: this.keys2bRemoved ) {
        //     final String rhsStr = tpl.val.toString();
        //     if ( this.verbose ) System.out.println("atEndOfInput: "+ tpl.key +": "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));

        //     // tpl.val.remove(tpl.key);

        //     // Now put in a new entry - with the replacement data!
        //     tpl.val.put( tpl.key, org.ASUX.common.Utils.deepClone(this.replacementData) );
        //     // If there are multiple matches.. then without deepClone(), the Eso teric Soft ware
        //     // YAML library, will use "&1" to define your 1st copy (in output) and put "*1" in
        //     // all other locations this replacement text WAS SUPPOSED have been :-(
        // }
        // // java's forEach never works if you are altering anything within the Lambda body
        // // this.keys2bRemoved.forEach( tpl -> {tpl.val.remove(tpl.key); });
        // if ( this.showStats ) System.out.println( "count="+this.keys2bRemoved.size() );

        // // This IF-Statement line below is Not outputting the entire YAML-Path.  So, I'm relying on onEnd2EndMatch() to do the job.
        // // Not a squeeky clean design (as summary should be done at end only).. but it avoids having to add additional data structures
        // // if ( this.showStats ) this.keys2bRemoved.forEach( tpl -> { System.out.println(tpl.key); } );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
