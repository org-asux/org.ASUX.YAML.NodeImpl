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


/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 *  @see org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor
 */
public class ReadYamlEntry extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = ReadYamlEntry.class.getName();

    protected org.ASUX.yaml.CmdLineArgsReadCmd cmdLineArgs;
    private int count;
    private SequenceNode output;

     // *  @param _verbose Whether you want deluge of debug-output onto System.out
     // *  @param _showStats Whether you want a final summary onto console / System.out
    /** The only Constructor.
     * @param _claRead NotNull object, that is created when the comamnd-line (or a line in batch-file) is parsed by the ANTLR4 or other parser
     *  @param _d instance of org.yaml.snakeyaml.DumperOptions (typically passed in via {@link CmdInvoker})
     */
    public ReadYamlEntry( final org.ASUX.yaml.CmdLineArgsReadCmd _claRead, final DumperOptions _d ) {
        super( _claRead.verbose, _claRead.showStats, _d );
        this.cmdLineArgs = _claRead;
        this.reset();
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     * In case you'd like to re-use this subclass within other java-code, we absolutely need to reset working instance-variables.
     */
    @Override
    public void reset() {
        this.count = 0;
        this.output = new SequenceNode( Tag.SEQ, false, new java.util.LinkedList<Node>(),  null, null, this.dumperoptions.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    
    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch()
     */
    protected boolean onPartialMatch( final Node _node, final YAMLPath _yamlPath, final String _keyStr, final Node _parentNode, final java.util.LinkedList<String> _end2EndPaths )
    {
        // Do Nothing for "read YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch()
     */
    protected boolean onEnd2EndMatch( final YAMLPath _yamlPath, final Object _key, final Node _keyNode, final Node _valNode, final Node _parentNode, final LinkedList<String> _end2EndPaths ) throws Exception
    {   final String HDR = CLASSNAME +": onEnd2EndMatch(): ";

        this.count ++; // keep count of # of matches

        if ( this.cmdLineArgs.verbose ) {
            System.out.print( HDR +"_end2EndPaths =");
            _end2EndPaths.forEach( s -> System.out.print(s+"\t") );
            System.out.println("onEnd2EndMatch: _key/LHS = ["+ _key +"] RHS = ["+ NodeTools.Node2YAMLString(_valNode) + "\n under parent = "+ _parentNode );
        }

        if ( this.cmdLineArgs.projectionPath == null ) {
            this.output.getValue().add( NodeTools.deepClone( _valNode ) ); // could be a string or a complex-Node;
            return true; // <<--------------- !!
        }

        // Otherwise, if this.cmdLineArgs.projectionPath is NOT empty/null
        final YAMLPath yp = new YAMLPath( this.cmdLineArgs.verbose, this.cmdLineArgs.projectionPath, this.cmdLineArgs.yamlPatternDelimiter );
        final ReadYamlEntry readYE = new ReadYamlEntry( this.cmdLineArgs, this.dumperoptions );

        if ( yp.yamlElemArr.length == 1 && "..".equals( yp.yamlElemArr[0] ) ) {
            // very simple this.cmdLineArgs.projectionPath.  Get the parent node!
            this.output.getValue().add( _parentNode );
            return true; // <<--------------- !!
        }

        // So, the this.cmdLineArgs.projectionPath is COMPLICATED enough for is to TRAVERSE some more..
        if ( yp.yamlElemArr.length > 1 && "..".equals(yp.yamlElemArr[0]) ) {
            // strip out the initial '..' in the this.cmdLineArgs.projectionPath
            // Then, Lookup up within parentNode (by invoking readYE.searchYamlForPattern())
            yp.hasNext();
            if ( this.cmdLineArgs.verbose ) System.out.println( HDR +" yp='"+ yp +"' and yp.getSuffix()="+ yp.getSuffix() );

            readYE.searchYamlForPattern( _parentNode, yp.getSuffix(), this.cmdLineArgs.yamlPatternDelimiter );
            // Hope people do NOT do crazy things - by providing a RegExp in this.cmdLineArgs.projectionPath
            // Ideally, readYE.getOutput() should - in typical semantic usage - return a singleton (Scalar, Mapping or Sequence Nodes).

        } else {
            // the complex this.cmdLineArgs.projectionPath does _NOT_ begin with '..'
            readYE.recursiveSearch( _valNode, yp, null, new LinkedList<>());
        }

        final SequenceNode seqN1 = readYE.getOutput();
        if ( this.cmdLineArgs.verbose ) System.out.println( "seqN2 = "+ NodeTools.Node2YAMLString( seqN1 ) + "\n" );

        if ( seqN1.getValue().size() < 1 ) {
            StringBuilder errMsgBuf = new StringBuilder( "ERROR: " );
            if ( this.cmdLineArgs.verbose ) errMsgBuf.append( HDR );
            errMsgBuf.append( "For the pattern provided on cmdline as YAML-Path " + _yamlPath.yamlPathStr + " .. .. we found [" );
            for( String s: _end2EndPaths )
                errMsgBuf.append(s).append( this.cmdLineArgs.yamlPatternDelimiter );
            final String errMsg = errMsgBuf.toString();
            throw new org.ASUX.yaml.InvalidCmdLineArgumentException( errMsg + "].\n\tAt that location canNOT find anything at '"+ this.cmdLineArgs.projectionPath +"' provided via the --projection cmd-line argument." );
        } else {
            for ( Node n:  seqN1.getValue() ) {
                final Node newN = NodeTools.deepClone( n );
                this.output.getValue().add( newN );
            } // for-loop
        } // if-else (above 11 lines)

        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail()
     */
    protected void onMatchFail( final YAMLPath _yamlPath, final Node _parentNode, final Node _nodeNoMatch, final Object _key, final LinkedList<String> _end2EndPaths )
    {
            // Do Nothing for "read YAML-entry command"
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
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     * @return the count of how many matches happened.  This value is also = this.getOutput.size()
     */
    public int getCount() {
        return this.count;
    }

    /**
     * @return the output as an Node/YAML of content (either Strings or sub-Node).  This is because the 'rhs' of a YAML-line can be either of the two
     */
    public SequenceNode getOutput() {
        return this.output;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

}
