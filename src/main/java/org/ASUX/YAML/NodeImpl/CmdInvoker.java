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
import org.ASUX.yaml.YAML_Libraries;
import org.ASUX.yaml.MemoryAndContext;
import org.ASUX.yaml.CmdLineArgs;
import org.ASUX.yaml.CmdLineArgsBatchCmd;
import org.ASUX.yaml.CmdLineArgsInsertCmd;
import org.ASUX.yaml.CmdLineArgsMacroCmd;
import org.ASUX.yaml.CmdLineArgsReplaceCmd;
import org.ASUX.yaml.CmdLineArgsTableCmd;

import org.ASUX.common.Output.OutputType;
import org.ASUX.common.Output;
import org.ASUX.common.Debug;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.FileNotFoundException;
import java.io.IOException;

// import java.util.LinkedList;
// import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <p>
 * This org.ASUX.yaml GitHub.com project and the
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a>
 * GitHub.com projects.
 * </p>
 * <p>
 * This class is the "wrapper-processor" for the various "YAML-commands" (which
 * traverse a YAML file to do what you want).
 * </p>
 * <p>
 * The 4 YAML-COMMANDS are: <b>read/query, list, delete</b> and <b>replace</b>.
 * </p>
 * <p>
 * See full details of how to use these commands - in this GitHub project's wiki
 * - or - in
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a>
 * GitHub.com project and its wiki.
 * </p>
 *
 * <p>
 * Example:
 * <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml  --double-quote</code><br>
 * Example: <b><code>java org.ASUX.yaml.Cmd</code></b> will show all command
 * line options supported.
 * </p>
 * 
 * @see org.ASUX.yaml.YAMLPath
 * @see org.ASUX.yaml.CmdLineArgs
 *
 * @see org.ASUX.yaml.ReadYamlEntry
 * @see org.ASUX.yaml.ListYamlEntry
 * @see org.ASUX.yaml.DeleteYamlEntry
 * @see org.ASUX.yaml.ReplaceYamlEntry
 */
public class CmdInvoker extends org.ASUX.yaml.CmdInvoker {

    private static final long serialVersionUID = 312L;

    public static final String CLASSNAME = CmdInvoker.class.getName();

    // private static final String TMP FILE = System.getProperty("java.io.tmpdir") +"/org.ASUX.yaml.STDOUT.txt";

    private transient GenericYAMLScanner YAMLScanner;
    private transient GenericYAMLWriter YAMLWriter;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     *  The constructor exclusively for use by  main() classes anywhere.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     */
    public CmdInvoker( final boolean _verbose, final boolean _showStats ) {
        this( _verbose, _showStats, null, new Tools(_verbose ) );
    }

    /**
     *  Variation of constructor that allows you to pass-in memory from another previously existing instance of this class.  Useful within {@link BatchYamlProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _memoryAndContext pass in memory from another previously existing instance of this class.  Useful within {@link BatchYamlProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     *  @param _tools reference to an instance of org.ASUX.yaml.Tools class or it's subclasses org.ASUX.yaml.CollectionsImpl.Tools or org.ASUX.yaml.NodeImpl.Tools
     */
    public CmdInvoker( final boolean _verbose, final boolean _showStats, final MemoryAndContext _memoryAndContext, final org.ASUX.yaml.Tools _tools ) {
        super(_verbose, _showStats, _memoryAndContext, _tools );
        init();
    }

    private void init() {
        this.YAMLScanner = new GenericYAMLScanner( this.verbose );
        this.YAMLWriter = new GenericYAMLWriter( this.verbose );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Reference to the implementation of the YAML read/parsing ONLY
     * @return a reference to the YAML Library in use.
     */
    public GenericYAMLScanner getYamlScanner() {
        return this.YAMLScanner;
    }

    /**
     * Reference to the implementation of the YAML read/parsing ONLY
     * @return a reference to the YAML Library in use.
     */
    public GenericYAMLWriter getYamlWriter() {
        return this.YAMLWriter;
    }

    /**
     * know which YAML-parsing/emitting library was chosen by user.  Ideally used within a Batch-Yaml script / BatchYamlProcessor.java
     * @return the YAML-library in use. See {@link YAML_Libraries} for legal values to this parameter
     */
    public YAML_Libraries getYamlLibrary() {
        // why make this check below with assert()?
        // Why shouln't users use one library to read YAML and another to write YAML?
        final YAML_Libraries sclib = this.YAMLScanner.getYamlLibrary();
        // String s = sclib.toString();
        // s = (s==null) ? "null" : s;
        // assert( s.equals( this.YAMLWriter.getYamlLibrary() ) );
        assert( sclib == this.YAMLWriter.getYamlLibrary() );
        return sclib;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link YAML_Libraries} for legal values to this parameter
     */
    public void setYamlLibrary( final YAML_Libraries _l ) {
        if ( this.YAMLScanner == null || this.YAMLWriter == null )
            this.init();
        this.YAMLScanner.setYamlLibrary(_l);
        this.YAMLWriter.setYamlLibrary(_l);
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  This function is meant to be used by Cmd.main() and by BatchProcessor.java.  Read the code *FIRST*, to see if you can use this function too.
     *  @param _cmdLineArgs yes, everything passed as commandline arguments to the Java program / org.ASUX.yaml.Cmd
     *  @param _inputData _the YAML inputData that is the input to pretty much all commands (a org.yaml.snakeyaml.nodes.Node object).
     *  @return either a String or org.yaml.snakeyaml.nodes.Node
     *  @throws YAMLPath.YAMLPathException if Pattern for YAML-Path provided is either semantically empty or is NOT java.util.Pattern compatible.
     *  @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     *  @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     *  @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object processCommand ( CmdLineArgs _cmdLineArgs, final Object _inputData )
                throws FileNotFoundException, IOException, Exception,
                YAMLPath.YAMLPathException
    {
        // final Output output = new Output(this.verbose);
        // final org.ASUX.yaml.Tools tools = this.getTools();
        assert( _inputData instanceof Node );
        @SuppressWarnings("unchecked")
        final Node _inputNode = (Node) _inputData;

        // This entire CollectionsImpl library clearly is chained to the YAML.org SNAKEYAML library.
        // So, let's make that explicit
        this.getYamlScanner().setYamlLibrary( YAML_Libraries.SNAKEYAML_Library );
        this.getYamlWriter().setYamlLibrary( YAML_Libraries.SNAKEYAML_Library );

        switch ( _cmdLineArgs.cmdType ) {
        case READ:
            ReadYamlEntry readcmd = new ReadYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            // readcmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final Node outputStr = null; // ???????????????????????????????? readcmd.getOutput();
            return outputStr;

        case LIST:
            ListYamlEntry listcmd = new ListYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats, YAMLPath.DEFAULTPRINTDELIMITER );
            // listcmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final Node outputStr2 = null; // ???????????????????????????????? listcmd.getOutput();
            return outputStr2;

        case DELETE:
            if ( _cmdLineArgs.verbose ) System.out.println(CLASSNAME + ": processCommand(isDelCmd): about to start DELETE command");
            DeleteYamlEntry delcmd = new DeleteYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            // delcmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            return _inputData;

        case TABLE:
            final CmdLineArgsTableCmd claTbl = (CmdLineArgsTableCmd) _cmdLineArgs;
            if (claTbl.verbose) System.out.println(CLASSNAME + ": processCommand(isTableCmd):  claTbl.yamlRegExpStr="+ claTbl.yamlRegExpStr +" & tableColumns=[" + claTbl.tableColumns +"]" );
            TableYamlQuery tblcmd = new TableYamlQuery( claTbl.verbose, claTbl.showStats, claTbl.tableColumns, claTbl.yamlPatternDelimiter );
            // tblcmd.searchYamlForPattern( _inputData, claTbl.yamlRegExpStr, claTbl.yamlPatternDelimiter );
            final Node output = null; // ???????????????????????????????? tblcmd.getOutput();
            return output;

        case INSERT:
            final CmdLineArgsInsertCmd claIns = (CmdLineArgsInsertCmd) _cmdLineArgs;
            if (claIns.verbose) System.out.println(CLASSNAME + ": processCommand(isInsertCmd):  claIns.yamlRegExpStr="+ claIns.yamlRegExpStr +" & loading @Insert-file: " + claIns.insertFilePath);
            final Object newContent = this.getDataFromReference( claIns.insertFilePath );
            if (claIns.verbose) System.out.println( CLASSNAME + ": processCommand(isInsertCmd): about to start INSERT command using: [" + newContent.toString() + "]");
            // Within a Batch-YAML context, the output of the previous line does NOT have to be YAML.
            // In such a case, an ArrayList or LinkedList object is converted into one -- by Tools.wrapAnObject().
            // So, we will use the inverse-function Tools.getTheActualObject() to undo that.
            InsertYamlEntry inscmd = new InsertYamlEntry( claIns.verbose, claIns.showStats, new Output(this.verbose).getTheActualObject( newContent ) );
            // inscmd.searchYamlForPattern( _inputData, claIns.yamlRegExpStr, claIns.yamlPatternDelimiter );
            return _inputData;

        case REPLACE:
            final CmdLineArgsReplaceCmd claRepl = (CmdLineArgsReplaceCmd) _cmdLineArgs;
            if (claRepl.verbose) System.out.println(CLASSNAME + ": processCommand(isReplaceCmd): loading @Replace-file: " + claRepl.replaceFilePath);
            final Object replContent = this.getDataFromReference( claRepl.replaceFilePath );
            if (claRepl.verbose) System.out.println( CLASSNAME + ": processCommand(isReplaceCmd): about to start CHANGE/REPLACE command using: [" + replContent.toString() + "]");
            ReplaceYamlEntry replcmd = new ReplaceYamlEntry( claRepl.verbose, claRepl.showStats, replContent );
            // replcmd.searchYamlForPattern( _inputData, claRepl.yamlRegExpStr, claRepl.yamlPatternDelimiter );
            return _inputData;

        case MACRO:
            final CmdLineArgsMacroCmd claMacro = (CmdLineArgsMacroCmd) _cmdLineArgs;
            if (claMacro.verbose) System.out.println( CLASSNAME + ": processCommand(isMacroCmd): loading Props file [" + claMacro.propertiesFilePath + "]");
            final Properties properties = new Properties();
            assert( claMacro.propertiesFilePath != null );
            if ( claMacro.propertiesFilePath.startsWith("@") ) {
                final java.io.InputStream istrm = new java.io.FileInputStream( claMacro.propertiesFilePath.substring(1) );
                properties.load( istrm );
            } else {
                final java.io.StringReader sr = new java.io.StringReader( claMacro.propertiesFilePath );
                properties.load( sr );
            }
            if (claMacro.verbose) System.out.println( CLASSNAME + ": processCommand(isMacroCmd): about to start MACRO command using: [Props file [" + claMacro.propertiesFilePath + "]");
            MacroYamlProcessor macro = new MacroYamlProcessor( claMacro.verbose, claMacro.showStats );
            assert( _inputData instanceof Node );
            @SuppressWarnings("unchecked")
            final Node topNode = (Node) _inputData;
            final Node outpData = macro.recursiveSearch( topNode, properties );  // this.getTools().YAMLString2Node("",false); // this.getYamlScanner().getEmptyYAML()
            return outpData;

        case BATCH:
            final CmdLineArgsBatchCmd claBatch = (CmdLineArgsBatchCmd) _cmdLineArgs;
            if (claBatch.verbose) System.out.println( CLASSNAME + ": processCommand(isBatchCmd): about to start BATCH command using: BATCH file [" + claBatch.batchFilePath + "]");
            BatchYamlProcessor batcher = new BatchYamlProcessor( claBatch.verbose, claBatch.showStats );
            batcher.setMemoryAndContext( this.memoryAndContext );
            final Node outpData2 = this.getTools().YAMLString2Node("",false); // this.getYamlScanner().getEmptyYAML()
            // batcher.go( claBatch.batchFilePath, _inputData, outpData2 );
            if ( this.verbose ) System.out.println( CLASSNAME +" processCommand(isBatchCmd):  outpData2 =" + outpData2 +"\n\n");
            return outpData2;

        default:
            final String es = CLASSNAME + ": processCommand(): Unimplemented command: " + _cmdLineArgs.toString();
            System.err.println( es );
            throw new Exception( es );
        }
        // return null; // should Not reach here!
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * This functon takes a single parameter that is a javalang.String value - and, either detects it to be inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to something saved in {@link MemoryAndContext} within a Batch-file execution (must be prefixed with a '!')
     * @param _src a javalang.String value - either inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to a property within a Batch-file execution (must be prefixed with a '!')
     * @return an object (either any of Node, SequenceNode, MapNode, ScalarNode ..)
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object getDataFromReference( final String _src  )
                throws FileNotFoundException, IOException, Exception
    {
        if ( _src == null || _src.trim().length() <= 0 )
            return null;
        final org.ASUX.yaml.Tools tools = this.getTools();

        if ( _src.startsWith("@") ) {
            final String srcFile = _src.substring(1);
            final InputStream fs = new FileInputStream( srcFile );
            if ( srcFile.endsWith(".json") ) {
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detected a JSON-file provided via '@'." );
                // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
                objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    com.fasterxml.jackson.databind.type.MapType type = objMapper.getTypeFactory().constructMapType( LinkedHashMap.class, String.class, Object.class );
                final LinkedHashMap<String, Object> retMap2 = objMapper.readValue( fs, new com.fasterxml.jackson.core.type.TypeReference< LinkedHashMap<String,Object> >(){}  );
                fs.close();
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): jsonMap loaded BY OBJECTMAPPER into tempOutputMap =" + retMap2 );
                final Node retNode = tools.Map2Node( retMap2 );
                return retNode;
            } else if ( srcFile.endsWith(".yaml") ) {
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detected a YAML-file provided via '@'." );
                final java.io.Reader reader1 = new java.io.InputStreamReader( fs  );
                final Node output = this.getYamlScanner().load( reader1 );
                reader1.close(); // automatically includes fs.close();
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): YAML loaded into tempOutputMap =" + output );
                return output;
            } else {
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detecting NEITHER a JSON NOR A YAML file provided via '@'." );
                return null;
            }

        } else if ( _src.startsWith("!") ) {
            if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detecting Recall-from-memory via '!'." );
            final String savedMapName = _src.startsWith("!") ?  _src.substring(1) : _src;
            // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
            final Object recalledContent = (this.memoryAndContext != null) ?  this.memoryAndContext.getDataFromMemory( savedMapName ) : null;
            if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): Memory returned =" + ((recalledContent==null)?"null":recalledContent.toString()) );
            return recalledContent;

        } else {
            if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): Must be an inline String.  Let me see if it's inline-JSON or inline-YAML." );
            try{
                // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
                // and less likely to see a YAML string inline
                return tools.JSONString2Node( _src );
            } catch( Exception e ) {
                if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): FAILED-attempted to PARSE as JSON for [" + _src +"]" );
                try {
                    // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
                    // and less likely to see a YAML string inline
                    return tools.YAMLString2Node( _src, false );
                } catch(Exception e2) {
                    if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): FAILED-attempted to PARSE as YAML for [" + _src +"] also!  So.. treating it as a SCALAR string." );
                    return _src; // The user provided a !!!SCALAR!!! java.lang.String directly - to be used AS-IS
                }
            } // outer-try-catch
        } // if-else startsWith("@")("!")
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * This function saved _input to a reference to a file (_dest parameter must be prefixed with an '@').. or, to a string prefixed with '!' (in which it's saved into Working RAM, Not to disk/file)
     * @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
     * @param _input the object to be saved using the reference provided in _dest paramater
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public void saveDataIntoReference( final String _dest, final Object _input )
                throws FileNotFoundException, IOException, Exception
    {
        final org.ASUX.yaml.Tools tools = this.getTools();
        if ( _dest != null ) {
            if ( _dest.startsWith("@") ) {
                if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detected a JSON-file provided via '@'." );
                final String destFile = _dest.substring(1);  // remove '@' as the 1st character in the file-name provided
                if ( destFile.endsWith(".json") ) {
                    // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                    final com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    // final InputStream fs = new FileInputStream( destFile );
                    final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
                    @SuppressWarnings("unchecked")
                    final Node topNode = (Node) _input;
                    final org.ASUX.common.Output.Object<?> inputObj = this.getTools().Node2Map( topNode );
                    assert( inputObj.getMap() != null );
                    objMapper.writeValue( filewr, inputObj.getMap() );
                    filewr.close();
                    // fs.close();
                    if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): JSON written was =" + _input );
                    return;
                } else if ( destFile.endsWith(".yaml") ) {
                    if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detected a YAML-file provided via '@'." );
                    final GenericYAMLWriter yamlwriter = this.getYamlWriter();
                    final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
                    yamlwriter.prepare( filewr );
                    yamlwriter.write( _input );
                    yamlwriter.close();
                    filewr.close();
                    if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): YAML written was =" + _input );
                    return;
                } else {
                    if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detecting NEITHER a JSON NOR A YAML file provided via '@'." );
                    throw new Exception("The saveTo @____ is NEITHER a YAML nor JSON file-name-extension.  Based on file-name-extension, the output is saved appropriately. ");
                }
            } else {
                // Unlike load/read (as done in getDataFromReference()..) whether or not the user uses a !-prefix.. same action taken.
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _dest +"): detecting Save-To-memory via '!' (if '!' is not specified, it's implied)." );
                final String saveToMapName = _dest.startsWith("!") ?  _dest.substring(1) : _dest;
                if ( this.memoryAndContext != null ) {
                    // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
                    this.memoryAndContext.saveDataIntoMemory( saveToMapName, _input );  // remove '!' as the 1st character in the destination-reference provided
                    if (this.verbose) System.out.println( CLASSNAME +": saveDataIntoReference("+ _dest +"): saved into 'memoryAndContext'=" + _input );
                }
            }
        } else {
            return; // do Nothing.
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
