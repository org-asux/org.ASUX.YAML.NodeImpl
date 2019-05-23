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

import java.util.Properties;

import java.util.regex.*;

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
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.serializer.Serializer;


/** <p>This abstract class was written to re-use code to query/traverse a YAML file.</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This abstract class has 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace).</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project.</p>
 * @see org.ASUX.yaml.ReadYamlEntry
 * @see org.ASUX.yaml.ListYamlEntry
 * @see org.ASUX.yaml.DeleteYamlEntry
 * @see org.ASUX.yaml.ReplaceYamlEntry
 */
public class MacroYamlProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.MacroYamlProcessor";

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    private final boolean verbose;

    /** <p>Whether you want a final SHORT SUMMARY onto System.out.</p><p>a summary of how many matches happened, or how many entries were affected or even a short listing of those affected entries.</p>
     */
	public final boolean showStats;

	private int changesMade = 0;

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     */
    public MacroYamlProcessor(boolean _verbose, final boolean _showStats) {
		this.verbose = _verbose;
		this.showStats = _showStats;
    }
    private MacroYamlProcessor(){
		this.verbose = false;
		this.showStats = true;
    }

    //------------------------------------------------------------------------------
    public static class MacroException extends Exception {
        private static final long serialVersionUID = 2L;
        public MacroException(String _s) { super(_s); }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>This is a RECURSIVE-FUNCTION.  Make sure to pass in the right parameters.</p>
     *  <p>Note: this function expects you to pass in an empty org.yaml.snakeyaml.nodes.Node as the 2nd parameter.  It will be 'filled' when function returns.</p>
     *  <p>This function returns true, if ANY occurance of ${ASUX::__} was detected and evaluated. If false, _input and _outMap will be identical when function returns</p>
     *  @param _input A org.yaml.snakeyaml.nodes.Node (created by SnakeYAML library) containing the entire Tree representing the YAML file.
     *  @param _output Pass in a new 'empty' org.yaml.snakeyaml.nodes.Node.  THis is what this function *RETURNS* after Macros are evalated within _input
     *  @param _props can be null, otherwise an instance of {@link java.util.Properties}
     *  @return true = whether at least one match of ${ASUX::} happened.
	 *  @throws MacroYamlProcessor.MacroException - thrown if any attempt to evaluate MACROs fails within org.ASUX.yaml.Macros.eval() functions
	 *  @throws Exception - forany other run time error (especially involving YAML issues)
     */
    public Node recursiveSearch(
            final Node _input,
			// final Node _output,
			final Properties _props
    ) throws MacroYamlProcessor.MacroException, Exception {

        // if ( (_input == null) || (_output==null) ) return false;
        if ( _input == null ) return null;

        // public enum org.yaml.snakeyaml.nodes.NodeId = scalar, sequence, mapping, anchor
        final NodeId nid = _input.getNodeId(); // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/NodeId.java
        if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): @top, node-id = ["+ nid + "]" );

        //--------------------------
        if ( _input instanceof MappingNode ) {
			final MappingNode mapNode = (MappingNode) _input;

			final java.util.List<NodeTuple> tuples = mapNode.getValue();
			final java.util.List<NodeTuple> newtuples = new java.util.LinkedList<NodeTuple>();

			if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): Mapping-node has value/tuples= ["+ tuples + "]" );

            for( NodeTuple kv: tuples ) {
                final Node key = kv.getKeyNode();
                assert ( key.getNodeId() == NodeId.scalar ); // if assert fails, what scenario does that represent?
                final ScalarNode scalarKey = (ScalarNode) key;
                final String keytag = scalarKey.getTag().getValue();
                if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): found LHS keyTag : RHS = ["+ key + "] !"+ keytag + " : "+ kv.getValueNode() + " ;" );

				final String keyNM = org.ASUX.yaml.Macros.eval( scalarKey.getValue(), _props );
				final String keytagNM = org.ASUX.yaml.Macros.eval( keytag, _props );
				assert( keyNM != null );
				final ScalarNode newkeynode = new ScalarNode( new Tag(keytagNM), keyNM, scalarKey.getStartMark(), scalarKey.getEndMark(), scalarKey.getScalarStyle() );
				// ScalarNode(Tag tag, String value, Mark startMark, Mark endMark, DumperOptions.ScalarStyle style)

                final Node valNode = kv.getValueNode();

                if ( valNode.getNodeId() == NodeId.scalar) {
                    final ScalarNode scalarVal = (ScalarNode) valNode;
					final String valNM = org.ASUX.yaml.Macros.eval( scalarVal.getValue(), _props );
					final String valtag = scalarVal.getTag().getValue();
					final String valtagNM = org.ASUX.yaml.Macros.eval( valtag, _props );
					final ScalarNode newvalnode = new ScalarNode( new Tag(valtagNM), valNM, scalarVal.getStartMark(), scalarVal.getEndMark(), scalarVal.getScalarStyle() );
					// ScalarNode(Tag tag, String value, Mark startMark, Mark endMark, DumperOptions.ScalarStyle style)
                    // String v = (scalarVal.getTag().startsWith("!")) ? (scalarVal.getTag()+" ") : "";
					// v += scalarVal.getValue();
					final NodeTuple newtuple = new NodeTuple( newkeynode, newvalnode );
                    newtuples.add( newtuple );
                    if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): >>>>>>>>>>> ADDED SCALAR KV-pair= "+ newtuple + " " );

                } else {
                    if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): recursing.. ..= ["+ valNode.getNodeId() + "]" );
					final NodeTuple newtuple = new NodeTuple( newkeynode, recursiveSearch( valNode, _props ) );
					newtuples.add( newtuple );
                }
            } // for
			final MappingNode newmap = new MappingNode(  mapNode.getTag(), false, newtuples, mapNode.getStartMark(), mapNode.getEndMark(), mapNode.getFlowStyle() ) ;
            // MappingNode(Tag ignore, boolean resolved, List<NodeTuple> value, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle)
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): function-returning a NEW MappingNODE with Tag = ["+ newmap.getTag() + "] replicating "+ _input.getTag() +" ;" );
            return newmap;

        } else if ( _input instanceof SequenceNode ) {
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/SequenceNode.java
            final SequenceNode seqNode = (SequenceNode) _input;
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): SEQUENCE-node-id = ["+ seqNode.getNodeId() + "]" );

            final java.util.List<Node> seqs = seqNode.getValue();
			final java.util.List<Node> newseqs = new java.util.LinkedList<Node>();
            for( Node valNode: seqs ) {
                if ( valNode.getNodeId() == NodeId.scalar) {
                    final ScalarNode scalarVal = (ScalarNode) valNode;
					final String valNM = org.ASUX.yaml.Macros.eval( scalarVal.getValue(), _props );
					final String valtag = scalarVal.getTag().getValue();
					final String valtagNM = org.ASUX.yaml.Macros.eval( valtag, _props );
					final ScalarNode newvalnode = new ScalarNode( new Tag(valtagNM), valNM, scalarVal.getStartMark(), scalarVal.getEndMark(), scalarVal.getScalarStyle() );
					// ScalarNode(Tag tag, String value, Mark startMark, Mark endMark, DumperOptions.ScalarStyle style)
                    // String v = (scalarVal.getTag().getValue().startsWith("!")) ? (scalarVal.getTag().getValue()+" ") : "";
					// v += scalarVal.getValue();
                    newseqs.add( newvalnode );
                    if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): >>>>>>>>>>> ADDED SCALAR into Array: "+ newvalnode + " " );

                } else {
                    if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): recursing.. ..= ["+ valNode.getNodeId() + "]" );
					newseqs.add( recursiveSearch( valNode, _props ) );
                }
            } // for
			final SequenceNode newseqNode = new SequenceNode(  seqNode.getTag(), false, newseqs, seqNode.getStartMark(), seqNode.getEndMark(), seqNode.getFlowStyle() ) ;
            // SequenceNode(Tag tag, boolean resolved, List<Node> value, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle)
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): function-returning a NEW SequenceNODE with Tag = ["+ newseqNode.getTag() + "] replicating "+ _input.getTag() +" ;" );
            return newseqNode;

        } else if ( _input instanceof ScalarNode ) {
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/ScalarNode.java
            final ScalarNode scalarVal = (ScalarNode) _input;
            final String valNM = org.ASUX.yaml.Macros.eval( scalarVal.getValue(), _props );
            final String valtag = scalarVal.getTag().getValue();
            final String valtagNM = org.ASUX.yaml.Macros.eval( valtag, _props );
            final ScalarNode newvalnode = new ScalarNode( new Tag(valtagNM), valNM, scalarVal.getStartMark(), scalarVal.getEndMark(), scalarVal.getScalarStyle() );
            // ScalarNode(Tag tag, String value, Mark startMark, Mark endMark, DumperOptions.ScalarStyle style)
            // String v = (scalarVal.getTag().startsWith("!")) ? (scalarVal.getTag()+" ") : "";
            // v += scalarVal.getValue();

            // boolean scalarVal.isPlain()
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveSearch(): >>>>>>>>>>> returning a SCALAR !! = ["+ newvalnode + "]" );
            return newvalnode;

        } else {
            final String erms = CLASSNAME + ": main(): Unimplemented SnakeYaml Node-type: " + nid +" = ["+ _input.toString() +"]";
            System.err.println( erms );
            throw new Exception( erms );
        } // if-else-if-else





		// for (Object keyAsIs : _input.keySet()) {


        //     // Note: the lookup within _input .. uses keyAsIs.  Not key.
		// 	final Object rhsObj = _input.get(keyAsIs);  // otherwise we'll inefficiently be doing _input.get multiple times below.

		// 	final String rhsStr = (rhsObj==null)?"null":rhsObj.toString(); // to make verbose logging code simplified
        //     if ( this.verbose ) System.out.println ( "\n"+ CLASSNAME +": recursiveSearch(): recursing @ YAML-file-location: "+ key +"/"+ keyAsIs +" = "+ rhsStr.substring(0,rhsStr.length()>181?180:rhsStr.length()) );

		// 	if ( rhsObj == null ) continue; // perhaps the YAML line is simply key-only like..    Key:
		// 	//--------------------------------------------------------
		// 	// So.. we need to keep recursing (specifically for LinkedHashMap<String, Object> & ArrayList YAML elements)
		// 	if ( rhsObj instanceof LinkedHashMap ) {

		// 		final LinkedHashMap<String,Object> newMap1	= new LinkedHashMap<>(); // create an empty Map
		// 		@SuppressWarnings("unchecked")
		// 		final LinkedHashMap<String, Object> rhs	= (LinkedHashMap<String, Object>) rhsObj;
		// 		bChangesMade = this.recursiveSearch( rhs, newMap1, _props ); // recursion call

		// 		_output.put( key, newMap1 );
		// 		// Why am I not simply doing:-       _output.put( key, newMap1 )
		// 		// Well: If the key != keyAsIs .. then .. the resulting entry in YAML outputfile is something like '"key"' (that is, a single+double-quote problem)
		// 		// tool.addMapEntry( _output, key, newMap1 );

		// 	} else if ( rhsObj instanceof java.util.ArrayList ) {

		// 		final ArrayList arr = (ArrayList) rhsObj;
		// 		final ArrayList<Object> newarr = new ArrayList<>();

		// 		// Loop thru the 'arr' Array
		// 		for ( Object o: arr ) {
		// 			// iterate over each element
		// 			if ( o instanceof LinkedHashMap ) {
		// 				final LinkedHashMap<String,Object> newMap2 = new LinkedHashMap<>();
		// 				@SuppressWarnings("unchecked")
		// 				final LinkedHashMap<String,Object> rhs22 = (LinkedHashMap<String,Object>) o;
		// 				bChangesMade = this.recursiveSearch( rhs22, newMap2, _props ); // recursion call
		// 				newarr.add( newMap2 );
		// 			} else if ( o instanceof java.lang.String ) {
		// 				// by o.toString(), I'm cloning the String object.. .. so both _input and _output do NOT share the same String object
		// 				newarr.add ( org.ASUX.yaml.Macros.eval( o.toString(), _props ) );
		// 			} else {
		// 				System.err.println( CLASSNAME +": recursiveSearch(): incomplete code #1: failure w Array-type '"+ o.getClass().getName() +"'");
		// 				System.exit(92); // This is a serious failure. Shouldn't be happening.
		// 			} // if-Else   o instanceof LinkedHashMap<String, Object> - (WITHIN FOR-LOOP)
		// 		} // for Object o: arr

		// 		_output.put( key, newarr );
		// 		// Well: If the key != keyAsIs .. then .. the resulting entry in YAML outputfile is something like '"key"' (that is, a single+double-quote problem)

		// 	} else if ( rhsObj instanceof java.lang.String ) {
		// 		// by rhsObj.toString(), I'm cloning the String object.. .. so both _input and _output do NOT share the same String object
		// 		final String asis = rhsObj.toString();
		// 		final String news = org.ASUX.yaml.Macros.eval( asis, _props);
		// 		if (   !    asis.equals(news) ) this.changesMade ++;
		// 		_output.put( key, news );
		// 		// Well: If the key != keyAsIs .. then .. the resulting entry in YAML outputfile is something like '"key"' (that is, a single+double-quote problem)

        //     } else {
		// 		System.err.println( CLASSNAME +": recursiveSearch(): incomplete code #2: failure w Type '"+ ((rhsObj==null)?"null":rhsObj.getClass().getName()) +"'");
		// 		System.exit(93); // This is a serious failure. Shouldn't be happening.
        //     }// if-else yamlPElemPatt.matcher()

        // } // for loop   key: _input.keySet()

        // Now that we looped thru all keys at current recursion level..
		// .. for now nothing to do here.
    }

}
