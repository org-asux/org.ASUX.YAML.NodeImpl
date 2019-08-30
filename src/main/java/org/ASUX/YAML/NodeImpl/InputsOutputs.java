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

import org.ASUX.common.StringUtils;
import org.ASUX.yaml.MemoryAndContext;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.regex.*;
import java.util.LinkedHashMap;
import java.util.Properties;

// https://yaml.org/spec/1.2/spec.html#id2762107
// import org.yaml.snakeyaml.Yaml;
// import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
// import org.yaml.snakeyaml.nodes.MappingNode;
// import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

import static org.junit.Assert.*;


/** <p>This class only contains a collection of static methods, to help to read and write to YAML files / JSON files as well as handle inline-JSON (in command line)</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see org.ASUX.YAML.NodeImpl.AbstractYamlEntryProcessor
 */
public class InputsOutputs {

    public static final String CLASSNAME = InputsOutputs.class.getName();

    // private boolean verbose;
    // private transient GenericYAMLScanner YAMLScanner;
    // private transient GenericYAMLWriter YAMLWriter;

    // public transient DumperOptions dumperopt = null;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // /**
    //  *  Variation of constructor that allows you to pass-in memory from another previously existing instance of this class.  Useful within {@link BatchCmdProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
    //  *  @param _verbose Whether you want deluge of debug-output onto System.out.
    //  *  @param _memoryAndContext pass in memory from another previously existing instance of this class.  Useful within {@link BatchCmdProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
    //  */
    // public InputsOutpus( final boolean _verbose ) // , final MemoryAndContext _memoryAndContext, final GenericYAMLScanner _YAMLScanner, final GenericYAMLWriter _YAMLWriter )
    // {
    //     th is . erbose = _verbose;
    //     // th is .YAMLScanner = _YAMLScanner;
    //     // th is .YAMLWriter = _YAMLWriter;
    // }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This functon takes a single parameter that is a javalang.String value - and, either detects it to be inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to something saved in {@link MemoryAndContext} within a Batch-file execution (must be prefixed with a '!')
     * @param _src a javalang.String value - either inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to a property within a Batch-file execution (must be prefixed with a '!')
     * @param _memoryAndContext a non-null reference to {@link org.ASUX.yaml.MemoryAndContext}.  CmdInvoker can provide this reference.
     * @param _YAMLScanner a non-null reference to {@link GenericYAMLScanner}.  CmdInvoker can provide this reference.
     * @param _dumperopt a non-null reference to org.yaml.snakeyaml.DumperOptions instance.  CmdInvoker can provide this reference.
     * @param _verbose Whether you want deluge of debug-output onto System.out.
     * @return an object (either any of Node, SequenceNode, MapNode, ScalarNode ..)
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public static final Object getDataFromReference( final String _src,
                                final MemoryAndContext _memoryAndContext, final GenericYAMLScanner _YAMLScanner, final DumperOptions _dumperopt,
                                final boolean _verbose )
                                throws FileNotFoundException, IOException, Exception
    {
        final String HDR = CLASSNAME +" getDataFromReference("+ _src +"): ";
        if ( _src == null )
            return null;
        try {
            Pattern emptyPattern        = Pattern.compile( "^\\s*$" ); // empty line
            Matcher emptyMatcher = emptyPattern.matcher( _src );
            if ( emptyMatcher.matches() ) {
                if ( _verbose ) System.out.println( HDR +" inline String is semantically empty." );
                return NodeTools.getEmptyYAML( _dumperopt );
            } // else _src is NOT an empty String.. so fall thru to code below.
        } catch( PatternSyntaxException e ) {
            e.printStackTrace(System.err); // PatternSyntaxException! too fatal an error (why would it throw!), to allow program/application to continue to run.
            System.err.println( "\n\n"+ HDR +"Unexpected Internal ERROR, while checking for patterns for line= [" + _src +"]. Exception Message: "+ e );
            System.exit(331); // This is a serious internal-failure. Shouldn't be happening.
        }

        if ( _verbose ) System.out.println( HDR +" inline String is NOT empty. checking whether its JSON or YAML or a simple plain string." );

        if ( _src.startsWith("@") ) {
            final boolean isNoFailCommand = _src.charAt(1) == '?'; // example:  @?./perhaps/nonexistent/file.yaml
            final String srcFile = _src.substring( isNoFailCommand ? 2 : 1 ); // get rid of the '@' and any optional '?' a the beginning
            InputStream fs = null;
            try {
                fs = new FileInputStream( srcFile );
            } catch(FileNotFoundException fe) {
                if ( isNoFailCommand)
                    return NodeTools.getEmptyYAML( _dumperopt );
                else
                    throw fe;
            }
            if ( srcFile.endsWith(".json") ) {
                if ( _verbose ) System.out.println( HDR +" detected a JSON-file provided via '@'." );
                // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
                objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                final com.fasterxml.jackson.databind.type.MapType type = objMapper.getTypeFactory().constructMapType( LinkedHashMap.class, String.class, Object.class );
                final LinkedHashMap<String, Object> retMap2 = objMapper.readValue( fs, new com.fasterxml.jackson.core.type.TypeReference< LinkedHashMap<String,Object> >(){}  );
                fs.close();
                if ( _verbose ) System.out.println( HDR +" jsonMap loaded BY OBJECTMAPPER into tempOutputMap =" + retMap2 );
                final Node retNode = NodeTools.Map2Node( _verbose, retMap2, _dumperopt );
                return retNode;

            } else if ( srcFile.endsWith(".yaml") ) {
                if ( _verbose ) System.out.println( HDR +" detected a YAML-file provided via '@'." );
                final java.io.Reader reader1 = new java.io.InputStreamReader( fs  );
                final Node output = _YAMLScanner.load( reader1 );
                reader1.close(); // automatically includes fs.close();
                if ( _verbose ) System.out.println( HDR +" YAML loaded into tempOutputMap =" + output );
                return output;

            } else if ( srcFile.endsWith(".properties") || srcFile.endsWith(".txt") ) {
                final Properties properties = new Properties();
                properties.load( fs );
                return properties;

            } else if ( "/dev/null".equals(srcFile) ) {
                return new Properties(); // an empty Properties file.  /dev/null ==> by definition, we CANNOT TELL if its JSON or YAML.  So, Properties it is!

            } else {
                final String emsg = "Found NEITHER a JSON NOR A YAML (nor a java.util.Properties) file. You provided the file-name: "+ _src;
                System.err.println( "ERROR:\t"+ HDR +emsg );
                throw new Exception( emsg );
            }

        } else if ( _src.startsWith("!") ) {
            if ( _verbose ) System.out.println( HDR +" detecting Recall-from-memory via '!'." );
            final boolean isNoFailCommand = _src.charAt(1) == '?'; // example:  !?perhapsNonexistentLabel
            final String savedMapLabel = _src.substring( isNoFailCommand ? 2 : 1 ); // get rid of the '!' and any optional '?' a the beginning
            // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
            Object recalledContent = (_memoryAndContext != null) ? _memoryAndContext.getDataFromMemory( savedMapLabel ) : null;
            if ( isNoFailCommand && recalledContent == null ) recalledContent = NodeTools.getEmptyYAML( _dumperopt );
            if (_verbose) System.out.println( HDR +"Memory returned =" + ((recalledContent==null)?"null":recalledContent.toString()) );
            return recalledContent;

        } else {
            if ( _verbose ) System.out.println( HDR +"Must be an inline String.  Let me see if it's inline-JSON or inline-YAML." );
            try{
                // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
                // and less likely to see a YAML string inline
                return NodeTools.JSONString2Node( _verbose, _src, _dumperopt );

            } catch( Exception e ) {
                if (_verbose) e.printStackTrace( System.out );
                if (_verbose) System.out.println( HDR +"FAILED-attempted to PARSE as JSON for [" + _src +"]" );
                try {
                    // because SnakeYAML Library will read 'key=value' as a simple ScalarNode (that is, no errors, success).. we need to check if inline-string is KV-pairs first.
                    final Properties props = org.ASUX.common.Utils.parseProperties( _src );
                    if ( _verbose ) System.out.println( HDR +"props="+ props );
                    if ( _verbose && props != null ) props.list(System.err);
                    return props;
                } catch(Exception e2) {
                    if (_verbose) e2.printStackTrace( System.out );
                    try {
                        // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
                        // and less likely to see a YAML string inline
                        final String multilineStr = StringUtils.convertString2MultiLine( _verbose, _src, _memoryAndContext.getAllPropsRef() );
                        Node newnode = NodeTools.YAMLString2Node( multilineStr );
                        if ( _verbose ) System.out.println( HDR +" new Node="+ newnode );
                        if ( newnode instanceof ScalarNode ) {
                            // THen.. rebuild the ScalanNode with the right DumperOptions
                            final ScalarNode sn = (ScalarNode) newnode;
                            newnode = new ScalarNode( Tag.STR, sn.getValue(), null, null, _dumperopt.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.SINGLE_QUOTED
                        }
                        return newnode;
                    } catch(Exception e3) {
                        if (_verbose) e3.printStackTrace( System.out );
                        if (_verbose) System.out.println( HDR +"FAILED-attempted to PARSE as YAML for [" + _src +"] also!  So.. treating it as a SCALAR string." );
                        return _src; // The user provided a !!!SCALAR!!! java.lang.String directly - to be used AS-IS
                        // final ScalarNode newnode = new ScalarNode( Tag.STR, _src, null, null, _dumperopt.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.SINGLE_QUOTED
                        // if ( _verbose ) System.out.println( HDR +" new ScalarNode="+ newnode );
                        // return newnode;
                    }
                }
            } // outer-try-catch
        } // if-else startsWith("@")("!")
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================


    /**
     *  This function saved _input to a reference to a file (_dest parameter must be prefixed with an '@').. or, to a string prefixed with '!' (in which it's saved into Working RAM, Not to disk/file)
     *  @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
     *  @param _input the object to be saved using the reference provided in _dest paramater
     *  @param _memoryAndContext a non-null reference to {@link org.ASUX.yaml.MemoryAndContext}.  CmdInvoker can provide this reference.
     *  @param _yamlImplementation a NotNull reference to {@link org.ASUX.YAML.NodeImpl.NodeTools})
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     *  @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     *  @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public static final void saveDataIntoReference( final String _dest, final Object _input,
                            final MemoryAndContext _memoryAndContext, final NodeTools _yamlImplementation,
                            final boolean _verbose )
                            throws FileNotFoundException, IOException, Exception {
        saveDataIntoReference( _dest, _input, _memoryAndContext, _yamlImplementation.getYAMLWriter(), _yamlImplementation.getDumperOptions(), _verbose );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  This function saved _input to a reference to a file (_dest parameter must be prefixed with an '@').. or, to a string prefixed with '!' (in which it's saved into Working RAM, Not to disk/file)
     *  @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
     *  @param _input the object to be saved using the reference provided in _dest paramater
     *  @param _memoryAndContext a non-null reference to {@link org.ASUX.yaml.MemoryAndContext}.  CmdInvoker can provide this reference.
     *  @param _YAMLWriter a non-null reference to {@link GenericYAMLWriter}.  CmdInvoker can provide this reference.
     *  @param _dumperopt a non-null reference to org.yaml.snakeyaml.DumperOptions instance.  CmdInvoker can provide this reference.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     *  @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     *  @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public static final void saveDataIntoReference( final String _dest, final Object _input,
                            final MemoryAndContext _memoryAndContext, final GenericYAMLWriter _YAMLWriter, final DumperOptions _dumperopt,
                            final boolean _verbose )
                            throws FileNotFoundException, IOException, Exception
    {
        final String HDR = CLASSNAME +" saveDataIntoReference("+ _dest +" , _input): ";
        if ( _dest == null ) {
            System.err.println( HDR +" parameter _dest is Null. Must be INVALID Code invoking this method." );
            return; // do Nothing.
        }

        if ( _dest.startsWith("@") ) {
            if ( _verbose ) System.out.println( HDR +" saveDataIntoReference("+ _dest +"): detected a JSON-file provided via '@'." );
            final String destFile = _dest.substring(1);  // remove '@' as the 1st character in the file-name provided
            if ( destFile.endsWith(".json") ) {
                // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                final com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                // final InputStream fs = new FileInputStream( destFile );
                final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
                @SuppressWarnings("unchecked")
                final Node topNode = (Node) _input;
                final org.ASUX.common.Output.Object<?> inputObj = NodeTools.Node2Map( _verbose, topNode ); // Can't use SnakeYaml Nodes.
                assertTrue( inputObj.getMap() != null );
                objMapper.writeValue( filewr, inputObj.getMap() ); // objMapper only takes a Collection as input, and CANNOT process SnakeYAML Nodes.
                filewr.close();
                // fs.close();
                if ( _verbose ) System.out.println( HDR +" JSON written was =" + _input );
                return;

            } else if ( destFile.endsWith(".yaml") ) {
                if ( _verbose ) System.out.println( HDR +" detected a YAML-file provided via '@'." );
                final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
                _YAMLWriter.prepare( filewr, _dumperopt );
                _YAMLWriter.write( _input, _dumperopt );
                _YAMLWriter.close();
                filewr.close();
                if ( _verbose ) System.out.println( HDR +" YAML written was =" + _input );
                return;

            } else {
                if ( _verbose ) System.out.println( HDR +"  FileNAME's extension is NEITHER a JSON NOR A YAML, as provided via '@'." );
                throw new Exception("The argument passed to 'saveTo' is "+ _dest +".\nIt is NEITHER a YAML nor JSON file-name-extension.\nFYI: Based on file-name-extension, the content is saved appropriately. ");
            }
        } else {
            // Unlike load/read (as done in getDataFromReference()..) whether or not the user uses a !-prefix.. same action taken.
            if ( _verbose ) System.out.println( HDR +" detecting Save-To-memory via '!' (if '!' is not specified, it's implied)." );
            final String saveToMapName = _dest.startsWith("!") ?  _dest.substring(1) : _dest;
            if ( _memoryAndContext != null ) {
                // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
                _memoryAndContext.saveDataIntoMemory( saveToMapName, _input );  // remove '!' as the 1st character in the destination-reference provided
                if (_verbose) System.out.println( HDR +" saved into 'memoryAndContext'=" + _input );
            }
        } // outer if-else
    } // method

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
