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

// import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;

import java.util.regex.*;

import static org.junit.Assert.*;

/** <p>This abstract class was written to re-use code to query/traverse a YAML file.</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This abstract class has 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace).</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see org.ASUX.yaml.ReadYamlEntry
 * @see org.ASUX.yaml.ListYamlEntry
 * @see org.ASUX.yaml.DeleteYamlEntry
 * @see org.ASUX.yaml.ReplaceYamlEntry
*/
public abstract class AbstractYamlEntryProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.AbstractYamlEntryProcessor";

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    /** <p>Whether you want a final SHORT SUMMARY onto System.out.</p><p>a summary of how many matches happened, or how many entries were affected or even a short listing of those affected entries.</p>
     */
    public final boolean showStats;

    private YAMLPath yp = null;

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     */
    public AbstractYamlEntryProcessor( final boolean _verbose, final boolean _showStats ) {
        this.verbose = _verbose;
        this.showStats = _showStats || _verbose;
    }
    protected AbstractYamlEntryProcessor(){
        this.verbose = false;
        this.showStats = true;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>This function will be called when a partial match of a YAML path-expression happens.</p>
     * <p>Example: if the YAML-Path-regexp is <code>paths.*.*.responses.200.description</code></p>
     * <p>This function will be called for: <code>paths./pet   paths./pet.put   paths./pet.put.responses paths./pet.put.responses.200</code></p>
     * <p>Note: This function will NOT be invoked for a full/end2end match for <code>paths./pet.put.responses.200.description</code></p>
     * <p>That full/end2end match will trigger the other function "onEnd2EndMatch()".</p>
     *
     * <p>Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.</p>
     *  @param _map This contains the java.utils.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries) containing the YAML SUB-tree (Note: Sub-tree) of the YAML file, as pointed to by "_yamlPath" and "_key".
     *  @param _yamlPath See the class YAMLPath @see org.ASUX.yaml.YAMLPath
     *  @param _key The value (typically a String) is what matched the _yamlPath.  Use it to get the "rhs" of the YAML element pointed to by _key
     *  @param _parentMap A Placeholder to be used in the future.  Right now it's = null
     *  @param _end2EndPaths for _yamlPathStr, this java.util.LinkedList shows the "stack of matches".   Example:  ["paths", "/pet", "get", "responses", "200"]
     *  @return The concrete sub-class can return false, to STOP any further progress on this partial match
     *  @throws Exception To allow for sub-classes (Example: see @see org.ASUX.yaml.TableYamlQuery - which will throw if data-issues while trying to query YAML for a nice 2-D tabular output)
     */
    protected abstract boolean onPartialMatch(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths) throws Exception;

    //-------------------------------------
    /** <p>This function will be called when a full/end2end match of a YAML path-expression happens.</p>
     * <p>Example: if the YAML-Path-regexp is <code>paths.*.*.responses.200.description</code></p>
     * <p>This function will be called ONLY for  <code>paths./pet.put.responses.200.description</code></p>
     * <p>Partial matches (of "parent yaml-elements") will trigger the other function "onPartialMatch()".</p>
     * <p>The words "onFullMatch" * "onCompleteMatch()" are confusing from user/regexp perspective.
     *  Hence the choice of onEnd2EndMath() as the function name.</p>
     *
     * <p>Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.</p>
     *  @param _map This contains the java.utils.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries) containing the "bottom-most" YAML SUB-tree (Note: Sub-tree) of the YAML file, as pointed to by "_yamlPath" and "_key".  This map could be potentially represent a simple YAML element like "name: petid"
     *  @param _yamlPath See the class YAMLPath @see org.ASUX.yaml.YAMLPath
     *  @param _key The value (typically a String) is what matched the _yamlPath.  For "YAML Query", the "rhs" of the YAML element pointed to by _key is what you're looking for.  For "YAML Delete" or "YAML Replace", you do Not care about the "rhs".. just use the _key to remove the entry/replace the "rhs".
     *  @param _parentMap A Placeholder to be used in the future.  Right now it's = null
     *  @param _end2EndPaths for _yamlPathStr, this java.util.LinkedList shows the "stack of matches".   Example:  ["paths", "/pet", "get", "responses", "200"]
     *  @return The concrete sub-class can return false, to STOP any further progress on this partial match
     *  @throws Exception To allow for sub-classes (Example: see @see org.ASUX.yaml.TableYamlQuery - which will throw if data-issues while trying to query YAML for a nice 2-D tabular output)
     */
    protected abstract boolean onEnd2EndMatch(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths) throws Exception;

    //-------------------------------------
    /** <p>This function will be called whenever the YAML path-expression fails to match.</p>
     * <p>This will be called way too often.  It's only interesting if you want a "negative" match scenario (as in show all rows that do Not match)</p>
     *
     * <p>Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.</p>
     * <p>Note: Unlike the other abstract methods of this Abstract class, this does NOT have a return-value.</p>
     *
     *  @param _map This contains the java.utils.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries) containing the YAML SUB-tree (Note: Sub-tree) of the YAML file (as pointed to by "_yamlPath" and "_key") - representing where PathPattern match failed.  This map could be potentially represent a simple YAML element like "name: petid"
     *  @param _yamlPath See the class YAMLPath @see org.ASUX.yaml.YAMLPath
     *  @param _key The value (typically a String) is what *FAILED* to match the _yamlPath.
     *  @param _parentMap A Placeholder to be used in the future.  Right now it's = null
     *  @param _end2EndPaths for _yamlPathStr, this java.util.LinkedList shows the "stack of matches".   Example:  ["paths", "/pet", "get", "responses", "200"]
     *  @throws Exception To allow for sub-classes (Example: see @see org.ASUX.yaml.TableYamlQuery - which will throw if data-issues while trying to query YAML for a nice 2-D tabular output)
     */
    protected abstract void onMatchFail(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths) throws Exception;

    //-------------------------------------
    /** <p>This function will be called when processing has ended.</p>
     *  <p>After this function returns, the AbstractYamlEntryProcessor class is done!</p>
     *
     *  <p>You can fuck with the contents of any of the parameters passed, to your heart's content.</p>
     *
     *  @param _map This contains the java.utils.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries) containing the entire Tree representing the YAML file.
     *  @param _yamlPath See the class YAMLPath @see org.ASUX.yaml.YAMLPath
     *  @throws Exception To allow for sub-classes (Example: see @see org.ASUX.yaml.TableYamlQuery - which will throw if data-issues while trying to query YAML for a nice 2-D tabular output)
     */
    protected abstract void atEndOfInput(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath) throws Exception;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>Internal Note: This is <b>NOT NOT NOT NOT NOT</b> ........ a RECURSIVE-FUNCTION.</p>
     *  <p>This is a simple way to invoke the real-recursive function {@link #recursiveSearch}.</p>
     *  @param _map This contains the java.utils.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries) containing the entire Tree representing the YAML file.
     *  @param _yamlPathStr Example: "<code>paths.*.*.responses.200</code>" - <b>ATTENTION: This is a human readable pattern, NOT a proper RegExp-pattern</b>
     *  @param _delim pass in a value like '.'  '\t'   ','   .. such a character as a string-parameter (being flexible in case delimiters can be more than a single character)
     *  @return true = whether at least one match happened.
     *  @throws YAMLPath.YAMLPathException if Pattern for YAML-Path provided is either semantically empty or is NOT java.util.Pattern compatible.
     *  @throws Exception any errors/troubles noted from within the subclasses, especially TableCmdProcessor.java
     */
    public boolean searchYamlForPattern(LinkedHashMap<String, Object> _map, String _yamlPathStr, final String _delim)
                throws YAMLPath.YAMLPathException, Exception
    {
        final LinkedList<String> end2EndPaths = new LinkedList<>();
        this.yp = new YAMLPath( this.verbose, _yamlPathStr, _delim );
        boolean retval = false;
        if ( YAMLPath.ROOTLEVEL.equals( this.yp.getRaw() ) ) {
            retval = true;
            if ( this.verbose ) System.out.println( CLASSNAME +": searchYamlForPattern("+ _yamlPathStr +"):  Skipping this.recursiveSearch() as the YAML-Path pattern is ROOT-ELEM" );
        } else {
            if ( this.verbose ) System.out.println( CLASSNAME +": searchYamlForPattern("+ _yamlPathStr +"):  invoking this.recursiveSearch().. .." );
            retval = this.recursiveSearch( _map, this.yp, end2EndPaths );
        }
        atEndOfInput( _map, this.yp );
//  ???? What should be done if atEndOfInput returns false.. ??? by the sub-classes?
        return retval;
    }

    /**
     * A convenience function, to cut down on code-size within recursiveSearch() below.. especially avoiding the SuppressWarnings("unchecked")
     * If null, you'll get an empty NEW LinkedList<String> object.
     * If Not null, the LinkedList<String> is SHALLOW-cloned, but obviously, the String objects are shared.  Not an issue as String is immutable.
     * @ param pass in an object of type LinkedList<String>  (null is ok)
     */
    private LinkedList<String> clone( final LinkedList<String> _orig ) {
        if ( _orig == null ) return new LinkedList<String> ();

        @SuppressWarnings("unchecked")
        final LinkedList<String> clone = (LinkedList<String>) _orig.clone();

        return clone;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>This is a RECURSIVE-FUNCTION.  Make sure to pass in the right parameters.</p>
     *  <p><b>Don't tell me I did NOT warn you!</b>  Use the {@link searchYamlForPattern} function instead.</p>
     *  <p>This function returns true, if the invocation (or it's recursion) did find a match (partial or end2end).<br>
     *  For now, I'm Not using the return value ANYWHERE.   Either I will - or - will refactor the return as Void.</p>
     *  @param _map This contains the java.utils.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries) containing the entire Tree representing the YAML file.
     *  @param _yamlPath This is the {@link YAMLPath} class consstructed using example strings like "<code>paths.*.*.responses.200</code>" - <b>ATTENTION: This string is a human readable pattern, NOT a proper RegExp-pattern</b>
     *  @param _end2EndPaths for _yamlPathStr, this java.util.LinkedList shows the "stack of matches".   Example:  ["paths", "/pet", "get", "responses", "200"]
     *  @return true = whether at least one match happened.
     *  @throws java.util.regex.PatternSyntaxException - this is thrown the innocuous String.match(regexp)
     *  @throws Exception any errors/troubles noted from within the subclasses, especially TableCmdProcessor.java
     */
    public boolean recursiveSearch(LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final LinkedList<String> _end2EndPaths )
                    throws java.util.regex.PatternSyntaxException, Exception
    {
        if ( (_map==null) || (_yamlPath==null) ) return true; // returning TRUE helps with a cleaner recursion logic
        if (  ! _yamlPath.isValid ) return false;
        if ( ! _yamlPath.hasNext() ) return true; // YAML path has ended.  So, must be a good thing, as we got this far down the YAML-Path

        //--------------------------
        final String yamlPathElemStr = _yamlPath.get(); // current path-element (a substring of full yamlPath)

        boolean aMatchFound = false;

        //--------------------------
        for (String key : _map.keySet()) {

            final Object rhs = _map.get(key);  // otherwise we'll inefficiently be doing map.get multiple times below.
            final String rhsStr = rhs.toString(); // to make verbose logging code simplified

            if ( this.verbose ) System.out.println ( "\n"+ CLASSNAME +": "+ key +": "+ rhsStr.substring(0,rhsStr.length()>181?180:rhsStr.length()) );

            //-----------------
            boolean hasThisYamlLineMatched = false;
            boolean hasThisYamlLineLiterallyMatched = ! yamlPathElemStr.equals("**");
            if ( yamlPathElemStr.equals("**") ) {
                hasThisYamlLineLiterallyMatched = false; // redundant
                hasThisYamlLineMatched = true;
            } else {
                final Pattern yamlPElemPatt = java.util.regex.Pattern.compile(yamlPathElemStr); // This should Not throw, per precautions in YAMLPath class
                hasThisYamlLineLiterallyMatched = yamlPElemPatt.matcher(key).matches();
                hasThisYamlLineMatched = hasThisYamlLineLiterallyMatched;
            }
            // One more check: If current YamlLine's key did NOT match, but is there a "**" for a "greedy-match"
            if ( ! hasThisYamlLineMatched && _yamlPath.hasWildcardPrefix() ) {
                hasThisYamlLineMatched = true;
                hasThisYamlLineLiterallyMatched = false;
            }

            if ( hasThisYamlLineMatched ) {
                if ( this.verbose ) System.out.println(CLASSNAME + ": @# " + _yamlPath.index() +"\t"+ _yamlPath.getPrefix() +"\t"+ _yamlPath.get() +"\t"+ _yamlPath.getSuffix() + "\t matched("+ hasThisYamlLineLiterallyMatched+ ") '"+ key +"':\t"+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()) +"\t\t of type '"+rhs.getClass().getName() +"'");

                _end2EndPaths.add( key ); // _end2EndPaths keeps the breadcrumbs

                //------------------------------------------------------
                assert (_yamlPath.hasNext()); // why on earth would this assertion fail - see checks @ top of function.

                final YAMLPath lookForwardYAMLPath = YAMLPath.deepClone(_yamlPath); // to keep _yamlPath intact as we recurse in & out of sub-yaml-elements
                if (  hasThisYamlLineLiterallyMatched ||  !  _yamlPath.hasWildcardPrefix() )
                    lookForwardYAMLPath.next(); // _yamlPath.get() should continue to have "**" as previous element.

                final LinkedList<String> cloneOfE2EPaths = this.clone( _end2EndPaths );

                if ( this.verbose ) System.out.println(CLASSNAME + ": @ whether to recurse: deepcloned YamlPath @# " + lookForwardYAMLPath.index() +"\t"+ lookForwardYAMLPath.getPrefix() +"\t"+ lookForwardYAMLPath.get() +"\t"+ lookForwardYAMLPath.getSuffix() +" -- cloneOfYP.hasNext()='"+ lookForwardYAMLPath.hasNext() +"'  _yamlPath.hasWildcardPrefix()='"+ _yamlPath.hasWildcardPrefix() +"'");

                if ( ! lookForwardYAMLPath.hasNext() ) {
                    // NO more recursion feasible!
                    // well! we've matched end2end .. to a "Map" element (instead of String elem)!
                    aMatchFound = true;

                    // let sub-classes determine what to do here
                    final boolean callbkRet3 = onEnd2EndMatch(_map, _yamlPath, key, null, cloneOfE2EPaths); // location #1 for end2end match
                    if ( ! callbkRet3 ) continue; // Pretend as if match failed.
                    _end2EndPaths.removeLast();

                    if ( this.verbose ) System.out.println(CLASSNAME +": End2End Match#1 in YAML-file: "+ _yamlPath.getPrefix() +" "+ key  +":\t"+  rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()) +"\t\t type '"+rhs.getClass().getName() +"'");

                    continue; // outermost for-loop (String key : _map.keySet())
                }
                //else .. continue below.

                //------------------------------------------------------
                // If we're here, it means INCOMPLETE match..

                // let sub-classes determine what to do here
                final boolean callbkRet2 = onPartialMatch(_map, _yamlPath, key, null, _end2EndPaths );
                if ( ! callbkRet2 ) continue; // If so, STOP  any further matching DOWN/BENEATH that partial-match

                if ( this.verbose ) System.out.println(CLASSNAME + ": recursing with YAMLPath @# " + lookForwardYAMLPath.index() +"\t"+ lookForwardYAMLPath.getPrefix() +"\t"+ lookForwardYAMLPath.get() +"\t"+ lookForwardYAMLPath.getSuffix() +": ... @ YAML-file-location: '"+ key +"': "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));

                //--------------------------------------------------------
                // if we are here, we've only a PARTIAL match.
                // So.. we need to keep recursing (specifically for Map & ArrayList YAML elements)
                if ( rhs instanceof LinkedHashMap ) {

                    @SuppressWarnings("unchecked")
                    final LinkedHashMap<String, Object> rhs2 = (LinkedHashMap<String, Object>) rhs;
                    aMatchFound = this.recursiveSearch( rhs2, lookForwardYAMLPath, cloneOfE2EPaths); // recursion call
                    // we do Not know how deep the recursion is.
                    // once recursion call returns, we happily go back to the UNTOUCHED _yamlPath & to _end2EndPaths  - which is still intact for use by the FOR loop.

                } else if ( rhs instanceof java.util.ArrayList ) {

                    // Let's check: whether a wildcard('**'), or we have a '*' for current path-element
                    boolean bMatchAny = false;
                    boolean bWildcard = false;
                    boolean bLetsIterate = false;
                    String upcomingPathElem = null;
                    YAMLPath nonStarLookFwdYAMLPath = null;
                    if (  lookForwardYAMLPath.hasWildcardPrefix() ) {
                        // of course! we should loop thru each element of the array below.
                        // WildCard is so powerful a concept, and like the Greedy-Algorithms of RegExp '*'-matcher.. it will 'match anything'
                        // So, we'll continue 'recursion' in code below - assuming the next non-WildCard element is STILL this WildCard element (that is what 'greedy matching means!)
                        nonStarLookFwdYAMLPath = lookForwardYAMLPath;
                        upcomingPathElem = lookForwardYAMLPath.get(); 
                        bWildcard = true;
                        bLetsIterate = true;
                    } else {
                        if ( lookForwardYAMLPath.get().equals( YAMLPath.MATCHANYSINGLEPATHELEMENT ) ) {
                            // We've a '*'/'.*'.. .. so, let's clone .. to SAFELY see one step ahead.
                            // make 'nonStarLookFwdYAMLPath' point to the YAML-Path-Pattern-element !!!that exists RIGHT AFTER!!! the current '.*'/'*'
                            // After testing successfully, I can say that it's OK to assume that anything following a '*'/'.*' is a NON-Star element ---- especially, if the user entered patterns like 'xyz.abc.*.*.qqq' ??? Hmmmmm.
                            nonStarLookFwdYAMLPath = YAMLPath.deepClone(lookForwardYAMLPath); // to keep _yamlPath intact as we recurse in & out of sub-yaml-elements
                            nonStarLookFwdYAMLPath.next(); // let's see what the next yaml-element is.  We know this will succeed.
                            upcomingPathElem = nonStarLookFwdYAMLPath.get();
                            bMatchAny = true;
                            bLetsIterate = true;
                        } else {
                            if ( lookForwardYAMLPath.get().matches( "\\[?[0-9][0-9]*\\]?" ) ) {
                                // Clearly.. We should definitely check out each item in the array 
                                // Make 'nonStarLookFwdYAMLPath' point to the YAML-Path-Pattern-element !!!that exists RIGHT AFTER!!! the 0
                                nonStarLookFwdYAMLPath = YAMLPath.deepClone(lookForwardYAMLPath); // to keep _yamlPath intact as we recurse in & out of sub-yaml-elements
                                nonStarLookFwdYAMLPath.next(); // let's see what the next yaml-element is.  We know this will succeed.
                                upcomingPathElem = lookForwardYAMLPath.get();
                                bLetsIterate = true;
                            } else {
                                // No point loopoing thru the array.
                                bLetsIterate = false;
                            }
                        }
                    }
                    // by reaching here, we're sure that nonStarLookFwdYAMLPath, upcomingPathElem, bLetsIterate & bMatchAny have valid values.

                    ArrayList arr = (ArrayList) rhs;

                    // ATTENTION: if bLetsIterate === false, we'll NOT be entering this loop.
                    for ( int ix=0;  bLetsIterate && ix < arr.size(); ix ++ ) {

                        final Object o = arr.get(ix); 

                        // if have a '**' or *' for current path-element..  variable 'upcomingPathElem' will point to the next yaml-element within the YAML-Path-PATTERN is.
                        // otherwise variable 'upcomingPathElem' will point to CURRENT yaml-element.
                        if ( this.verbose ) System.out.println(CLASSNAME +": bWildcard="+ bWildcard +" bMatchAny="+ bMatchAny +" upcomingPathElem="+ upcomingPathElem +" ix="+ix );

                        if ( bWildcard || bMatchAny || Integer.valueOf(ix).toString().matches(upcomingPathElem) ) {
                            if (   !   nonStarLookFwdYAMLPath.hasNext() ) {
                                // yeah! We found a !!!full!!! end2end match!  Reason:- No more recursion is feasible.
                                final LinkedList<String> clone222OfE2EPaths = this.clone( cloneOfE2EPaths ); // to keep _yamlPath intact as we ITERATE thru this ARRAY LIST.
                                clone222OfE2EPaths.add("["+ix+"]"); // add the index like [1] into the discovered yaml-path
                                // let sub-classes determine what to do here
                                final boolean callbkRet6 = onEnd2EndMatch(_map, lookForwardYAMLPath, key, null, clone222OfE2EPaths); // location #2 for end2end match
                                // we do Not know how deep the recursion is.
                                // once recursion call returns, we happily go back to the UNTOUCHED _yamlPath & to _end2EndPaths  - which is still intact for use by the FOR loop.
                                if ( this.verbose ) System.out.println(CLASSNAME +": callbkRet6="+callbkRet6+" End2End Match#2 @ YAML-File: "+ key +": "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));
                                if ( ! callbkRet6 ) continue; // Pretend that EVEN IF match failed (per sub-class), continue to next peer YAML element.
                                aMatchFound = true;
                                continue; // for loop over Array
                            } else {
                                // Ok. We're FORCED to "move to" next element of YAML-Path-PATTERN.
                                // Fall thru out of this nested-IF into rest of FOR-Loop body.
                            }
                        } else {
                            continue; // to next Array YAML element.
                        }

                        // We've got an INCOMPLETE-Match: Either via a wildcard('**'), or we have a '*' for current path-element or.. we've a INCORRECT index-match.
                        //-------------------------------

                        if ( o instanceof LinkedHashMap ) { // if the array-yaml-element is Not a simple string.

                            @SuppressWarnings("unchecked")
                            final LinkedHashMap<String, Object> lhmp2 = (LinkedHashMap<String, Object>) o;

                            // Let's prepare for recursion .. clone all modifiaable-variables being passed.
                            final YAMLPath recursionYamlPath = YAMLPath.deepClone( nonStarLookFwdYAMLPath );
                            final LinkedList<String> clone333OfE2EPaths = this.clone( cloneOfE2EPaths ); // to keep _yamlPath intact as we ITERATE thru this ARRAY LIST.
                            clone333OfE2EPaths.add("["+ix+"]"); // add the index like [1] into the discovered yaml-path
                            aMatchFound = this.recursiveSearch( lhmp2, recursionYamlPath, clone333OfE2EPaths); // recursion call
                            // we do Not know how deep the recursion is.
                            // once recursion call returns, we happily go back to the UNTOUCHED _yamlPath/nonStarLookFwdYAMLPath & to _end2EndPaths/cloneOfE2EPaths   - which is still intact for use by the FOR loop.

                        } else if ( o instanceof java.lang.String ) {
                            // can't be a match, as it's Not even in the format   "rhs: lhs"
                            onMatchFail(_map, _yamlPath, key, null, _end2EndPaths); // location #1 for failure-2-match

                        } else {
                            System.err.println(CLASSNAME +": incomplete code: failure w Array-type '"+ o.getClass().getName() +"'");
                            onMatchFail(_map, _yamlPath, key, null, _end2EndPaths); // location #2 for failure-2-match

                        } // if-Else   o instanceof Map - (WITHIN FOR-LOOP)
                    } // for Object o: arr

                } else if ( rhs instanceof java.lang.String ) {

                    if ( _yamlPath.hasNext() ) { // then it's Not an end2end match
                        _end2EndPaths.removeLast();
                        continue;
                    } else {

                        // yeah! We found a full end2end match!  Also, No more recursion is feasible.
                        // let sub-classes determine what to do here
                        final boolean callbkRet5 = onEnd2EndMatch(_map, _yamlPath, key, null, cloneOfE2EPaths); // location #2 for end2end match
                        if ( this.verbose ) System.out.println(CLASSNAME +": callbkRet5="+callbkRet5+" End2End Match#2 @ YAML-File: "+ key +": "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));
                        if ( ! callbkRet5 ) continue; // Pretend as if match failed and continue to next peer YAML element.
                        _end2EndPaths.clear();
                        aMatchFound = true;
                    }

                } else {

                    System.err.println(CLASSNAME +": incomplete code: Unable to handle rhs of type '"+ rhs.getClass().getName() +"'");
                    onMatchFail(_map, _yamlPath, key, null, _end2EndPaths); // location #3 for failure-2-match

                } // if-else   rhs instanceof   Map/Array/String/.. ..

                // As we've had AT-LEAST a PARTIAL-MATCH, in CURRENT-ITERATION (of FOR-LOOP).. ..
                // we need to "undo" that for next iteration (of FOR) for the next-peer YAML-element
                if ( _end2EndPaths.size() > 0 )  _end2EndPaths.removeLast();
                
            } else {
                // false == foundAMatch  -- -- i.e., FAILED to match YAML-Path pattern.
                onMatchFail(_map, _yamlPath, key, null, _end2EndPaths); // location #4 for failure-2-match
                
            }// if-else yamlPElemPatt.matcher()

        } // for loop   key: _map.keySet()

        if (  !   aMatchFound ) {
            // Not a single end2end match.  At best .. we can HOPE THAT we only had partial matches.
            // Specifically, when the YAMLPath is A.B.C.D (4-levels deep) and the YAML itself it < 4-levels deep.. we need to address such a scenario.
            // This above scenario.. in case of InsertYamlProcessor.java.. allows it to do the equivalent of 'mkdir -p'.
            if ( this.verbose ) System.err.println(CLASSNAME +": recursiveSearch(): Not a single match for '"+ _yamlPath.toString() +"'");
            onMatchFail(_map, _yamlPath, _yamlPath.yamlElemArr[ _yamlPath.yamlElemArr.length - 1 ], null, null); // location #2 for failure-2-match
            // final YAMLPath ypNoMatches = YAMLPath.deepClone(_yamlPath); // to keep _yamlPath intact within this function.. as it's passed by reference into this function.
            // ypNoMatches.skip2end();
            // onMatchFail(_map, ypNoMatches, ypNoMatches.yamlElemArr[ ypNoMatches.yamlElemArr.length - 1 ], null, null); // location #2 for failure-2-match
        }

        // Now that we looped thru all keys at current recursion level..
        // .. for now nothing to do here.

        return aMatchFound;
    } // function

}
