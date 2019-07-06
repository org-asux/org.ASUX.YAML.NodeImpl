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


/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor
 */
public class ReplaceYamlEntry extends InsertYamlEntry {

    public static final String CLASSNAME = ReplaceYamlEntry.class.getName();

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _d instance of org.yaml.snakeyaml.DumperOptions (typically passed in via {@link CmdInvoker})
     *  @param _r this can be either a java.lang.String or a java.util.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries)
     *  @throws java.lang.Exception - if the _r parameter is not as per above Spec
     */
    public ReplaceYamlEntry( final boolean _verbose, final boolean _showStats, final DumperOptions _d, Object _r ) throws Exception {
        super( _verbose, _showStats, _d, _r);
        // this.reset(); InsertYamlEntry/super-class already invokes ths.
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    // /**
    //  * In case you'd like to re-use this subclass within other java-code, we absolutely need to reset working instance-variables.
    //  */
    // @Override
    // public void reset() {
    //     // Do Nothing
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor#onPartialMatch}
     */
    protected boolean onPartialMatch( final Node _node, final YAMLPath _yamlPath, final String _keyStr, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {   // Do Nothing for "Replace YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor#onEnd2EndMatch}
     */
    protected boolean onEnd2EndMatch( final YAMLPath _yamlPath, final Object _key, final Node _keyNode, final Node _valNode, final Node _parentNode, final LinkedList<String> _end2EndPaths )
    {   return super.onEnd2EndMatch(_yamlPath, _key, _keyNode, _valNode, _parentNode, _end2EndPaths);
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in {@link org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor#onMatchFail}
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
    protected void atEndOfInput( final Node _topmostNode, final YAMLPath _yamlPath ) throws Exception
    {
        final String HDR = CLASSNAME + ": atEndOfInput(): ";
        //------------------------------------------------
        this.output = _topmostNode;
        // UNLIKE InsertCommand, we do NOT allow '/' as the YAML path - for a REPLACE command.
        // Why don't you use 'useAsInput' Batch-command instead?

        // Step 1: In case '/' is the YAML-Path-RegExp .. do it first .. before ANY CHecks (steps 2 & beyond)
        if ( YAMLPath.ROOTLEVEL.equals( _yamlPath.getRaw() ) ) // '/' is exactly the entire YAML-Path pattern provided by the user on the cmd line
            throw new Exception( "Replace command does NOT support "+ YAMLPath.ROOTLEVEL +" as YAML-Path-RegularExpression.  use 'useAsInput' Batch-command instead."  );

        //-------------------------------------------------
        // Now that the AbstractYamlProcessor.java has recursively parsed the entire YAML.. .. we need to find out if there were ANY matches.
        // Next Step: Check to see if the YAML-Path-RegExp did match any rows.
        // We should NOT be silently returning the input-YAML AS-IS
        if ( this.existingPathsForInsertion.size() <= 0 ) {
            throw new Exception( " No matches were found For RegExp="+ super.getYAMLPath() +" and for the input-YAML :-\n"+ NodeTools.Node2YAMLString( _topmostNode )  );
            // Unlike Insert COMMAND, if there are NO Matches, we will NOT create paths (like 'mkdir -p');   Hence this exception MUST be thrown.
            // if user does NOT want exception, then they MUST list-Command to see if there was any rows returned before calling this Replace-Command
        } else {
            // that is ( this.existingPathsForInsertion.size() >= 1 )
            insertNewContentAtExistingNodes( true, _topmostNode, _yamlPath );
            if ( this.showStats ) System.out.println( "count="+ this.existingPathsForInsertion.size() );
            if ( this.showStats ) this.existingPathsForInsertion.forEach( tpl -> { System.out.println(tpl.key); } );
        }

    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
