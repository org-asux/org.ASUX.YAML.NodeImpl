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
import org.ASUX.common.Output;

// import java.util.Map;
import java.util.LinkedList;
import java.util.LinkedHashMap;

/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link org.ASUX.yaml.AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see org.ASUX.yaml.AbstractYamlEntryProcessor
 */
public class DeleteYamlEntry extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.DeleteYamlEntry";

    protected final LinkedList< Tuple< String,LinkedHashMap<String, Object> > > keys2bRemoved = new LinkedList<>();

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     */
    public DeleteYamlEntry( final boolean _verbose, final boolean _showStats ) {
        super( _verbose, _showStats );
    }

    private DeleteYamlEntry() {
        super( false, true );
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    
    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch()
     */
    protected boolean onPartialMatch(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths) {

        // Do Nothing for "delete YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch()
     */
    protected boolean onEnd2EndMatch(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths)
    {
        if ( this.verbose )
            System.out.print("onEnd2EndMatch: _end2EndPaths =");
        if ( this.verbose || this.showStats ) {
            _end2EndPaths.forEach( s -> System.out.print(s+", ") );
            System.out.println("");
        }

        this.keys2bRemoved.add( new Tuple< String, LinkedHashMap<String, Object> >(_key, _map) );
        if ( this.verbose ) System.out.println( CLASSNAME +": onE2EMatch: count="+this.keys2bRemoved.size());
        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail()
     */
    protected void onMatchFail(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths) {

        // Do Nothing for "delete YAML-entry command"
    }

    //-------------------------------------
    /** This function will be called when processing has ended.
     * After this function returns, the AbstractYamlEntryProcessor class is done!
     * See details in @see org.ASUX.yaml.AbstractYamlEntryProcessor#oatEndOfInput()
     *
     * You can fuck with the contents of any of the parameters passed, to your heart's content.
     */
    protected void atEndOfInput(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath) {

        if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): count=" + this.keys2bRemoved.size() );
        for (Tuple< String, LinkedHashMap<String, Object> > tpl: this.keys2bRemoved ) {
            final String rhsStr = tpl.val.toString();
            if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): atEndOfInput: "+ tpl.key +": "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));
            tpl.val.remove(tpl.key);
        }
        // java's forEach never works if you are altering anything within the Lambda body
        // this.keys2bRemoved.forEach( tpl -> {tpl.val.remove(tpl.key); });
        if ( this.showStats ) System.out.println( "count="+this.keys2bRemoved.size() );

        // This IF-Statement line below is Not outputting the entire YAML-Path.  So, I'm relying on onEnd2EndMatch() to do the job.
        // Not a squeeky clean design (as summary should be done at end only).. but it avoids having to add additional data structures
        // if ( this.showStats ) this.keys2bRemoved.forEach( tpl -> { System.out.println(tpl.key); } );
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

}
