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
import org.ASUX.yaml.Enums;
import org.ASUX.yaml.CmdLineArgs;
import org.ASUX.yaml.CmdLineArgsBatchCmd;
import org.ASUX.yaml.CmdLineArgsInsertCmd;
import org.ASUX.yaml.CmdLineArgsMacroCmd;
import org.ASUX.yaml.CmdLineArgsReplaceCmd;
import org.ASUX.yaml.CmdLineArgsTableCmd;
import org.ASUX.yaml.MacroStringProcessor;

import org.ASUX.common.Output; // needed to convert SnakeYAML' YAML Nodes into JSON's LinkedHashMap

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.regex.*;
import java.util.LinkedHashMap;
import java.util.Properties;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
// import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

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
 * @see org.ASUX.YAML.NodeImpl.ReadYamlEntry
 * @see org.ASUX.YAML.NodeImpl.ListYamlEntry
 * @see org.ASUX.YAML.NodeImpl.DeleteYamlEntry
 * @see org.ASUX.YAML.NodeImpl.ReplaceYamlEntry
 */
public class CmdInvoker extends org.ASUX.yaml.CmdInvoker {

    private static final long serialVersionUID = 312L;

    public static final String CLASSNAME = CmdInvoker.class.getName();

    // private static final String TMP FILE = System.getProperty("java.io.tmpdir") +"/org.ASUX.yaml.STDOUT.txt";

    protected transient GenericYAMLScanner YAMLScanner;
    protected transient GenericYAMLWriter YAMLWriter;

    public transient DumperOptions dumperopt = null;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     *  The constructor exclusively for use by  main() classes anywhere.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     */
    public CmdInvoker( final boolean _verbose, final boolean _showStats ) {
        this( _verbose, _showStats, null, null );
    }

    /**
     *  Variation of constructor that allows you to pass-in memory from another previously existing instance of this class.  Useful within {@link BatchCmdProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _memoryAndContext pass in memory from another previously existing instance of this class.  Useful within {@link BatchCmdProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     * @param _dopt a non-null reference to org.yaml.snakeyaml.DumperOptions instance.  CmdInvoker can provide this reference.
     */
    public CmdInvoker( final boolean _verbose, final boolean _showStats, final MemoryAndContext _memoryAndContext, final DumperOptions _dopt ) {
        super(_verbose, _showStats, _memoryAndContext );
        this.dumperopt = _dopt;
        init();
    }

    protected void init() {
        this.YAMLScanner = new GenericYAMLScanner( this.verbose );
        this.YAMLWriter = new GenericYAMLWriter( this.verbose );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * know which YAML-parsing/emitting library was chosen by user.  Ideally used within a Batch-Yaml script / BatchCmdProcessor.java
     * @return the YAML-library in use. See {@link YAML_Libraries} for legal values to this parameter
     */
    @Override
    public YAML_Libraries getYamlLibrary() {
        // why make this check below with ass-ert()?
        // Why shouln't users use one library to read YAML and another to write YAML?
        final YAML_Libraries sclib = this.YAMLScanner.getYamlLibrary();
        // String s = sclib.toString();
        // s = (s==null) ? "null" : s;
        // assertTrue( s.equals( this.YAMLWriter.getYamlLibrary() ) );
        assertTrue( sclib == this.YAMLWriter.getYamlLibrary() );
        return sclib;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link YAML_Libraries} for legal values to this parameter
     */
    @Override
    public void setYamlLibrary( final YAML_Libraries _l ) {
        if ( this.YAMLScanner == null || this.YAMLWriter == null )
            this.init();
        this.YAMLScanner.setYamlLibrary(_l);
        this.YAMLWriter.setYamlLibrary(_l);
    }

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

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     *  <p>Example: For SnakeYAML-library based subclass of this, this should return DumperOptions.class</p>
     *  <p>This is to be used primarily within BatchCmdProcessor.onAnyCmd().</p>
     *  @return name of class of the object that subclasses of {@link CmdInvoker} use, to configure YAML-Output (example: SnakeYAML uses DumperOptions)
     */
    @Override
    public Class<?> getLibraryOptionsClass() {
        return DumperOptions.class;
    }

    /**
     *  <p>Example: For SnakeYAML-library based subclass of this, this should return the reference to the instance of the class DumperOption</p>
     *  <p>This is to be used primarily within BatchCmdProcessor.onAnyCmd().</p>
     * @return instance/object that subclasses of {@link CmdInvoker} use, to configure YAML-Output (example: SnakeYAML uses DumperOptions objects)
     */
    @Override
    public Object getLibraryOptionsObject() {
        return this.dumperopt;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  This function is meant to be used by Cmd.main() and by BatchProcessor.java.  Read the code *FIRST*, to see if you can use this function too.
     *  @param _cmdLA Everything passed as commandline arguments to the Java program {@link org.ASUX.yaml.CmdLineArgsCommon}
     *  @param _inputData _the YAML inputData that is the input to pretty much all commands (a org.yaml.snakeyaml.nodes.Node object).
     *  @return either a String or org.yaml.snakeyaml.nodes.Node
     *  @throws YAMLPath.YAMLPathException if Pattern for YAML-Path provided is either semantically empty or is NOT java.util.Pattern compatible.
     *  @throws FileNotFoundException if the filenames within _cmdLA do NOT exist
     *  @throws IOException if the filenames within _cmdLA give any sort of read/write troubles
     *  @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object processCommand ( org.ASUX.yaml.CmdLineArgsCommon _cmdLA, final Object _inputData )
                throws FileNotFoundException, IOException, Exception,
                YAMLPath.YAMLPathException
    {
        assertTrue( _cmdLA instanceof org.ASUX.yaml.CmdLineArgs );
        final org.ASUX.yaml.CmdLineArgs cmdLineArgs = (org.ASUX.yaml.CmdLineArgs) _cmdLA;
        final String HDR = CLASSNAME + ": processCommand("+ cmdLineArgs.cmdType +"): ";

        assertTrue( _inputData instanceof Node );
        // why didn't we just make 2nd parameter of this method to be Node?
        // Well. This is ONE YAML-Library implementation (using SnakeYAML).
        // org.ASUX.YAML project has a 2nd YAML-Library Implementation.  Take a look at org.ASUX.yaml.YAML_Libraries
        @SuppressWarnings("unchecked")
        final Node _inputNode = (Node) _inputData;

        // This entire CollectionsImpl library clearly is chained to the YAML.org SNAKEYAML library.
        // So, let's make that explicit
        this.getYamlScanner().setYamlLibrary( YAML_Libraries.SNAKEYAML_Library );
        this.getYamlWriter().setYamlLibrary( YAML_Libraries.SNAKEYAML_Library );

        if ( this.dumperopt == null ) { // this won't be null, if this object was created within BatchCmdProcessor.java
            this.dumperopt = GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter();
        }
        switch( cmdLineArgs.getQuoteType() ) {
            case DOUBLE_QUOTED: dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.DOUBLE_QUOTED );  break;
            case SINGLE_QUOTED: dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.SINGLE_QUOTED );  break;
            case LITERAL:       dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.LITERAL );        break;
            case FOLDED:        dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.FOLDED );         break;
            case PLAIN:         dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN );          break;
            default:            dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.FOLDED );         break;
        }

        switch ( cmdLineArgs.cmdType ) {
        case READ:
            ReadYamlEntry readcmd = new ReadYamlEntry( cmdLineArgs.verbose, cmdLineArgs.showStats, dumperopt );
            readcmd.searchYamlForPattern( _inputNode, cmdLineArgs.yamlRegExpStr, cmdLineArgs.yamlPatternDelimiter );
            final Node outputStr = readcmd.getOutput();
            return outputStr;

        case LIST:
            ListYamlEntry listcmd = new ListYamlEntry( cmdLineArgs.verbose, cmdLineArgs.showStats, dumperopt, " , " );
            listcmd.searchYamlForPattern( _inputNode, cmdLineArgs.yamlRegExpStr, cmdLineArgs.yamlPatternDelimiter );
            final Node outputStr2 = listcmd.getOutput();
            return outputStr2;

        case DELETE:
            if ( cmdLineArgs.verbose ) System.out.println( HDR +" about to start DELETE command");
            DeleteYamlEntry delcmd = new DeleteYamlEntry( cmdLineArgs.verbose, cmdLineArgs.showStats, dumperopt );
            delcmd.searchYamlForPattern( _inputNode, cmdLineArgs.yamlRegExpStr, cmdLineArgs.yamlPatternDelimiter );
            return _inputNode;

        case TABLE:
            final CmdLineArgsTableCmd claTbl = (CmdLineArgsTableCmd) cmdLineArgs;
            if (claTbl.verbose) System.out.println( HDR +" claTbl.yamlRegExpStr="+ claTbl.yamlRegExpStr +" & tableColumns=[" + claTbl.tableColumns +"]" );
            TableYamlQuery tblcmd = new TableYamlQuery( claTbl.verbose, claTbl.showStats, dumperopt, claTbl.tableColumns, claTbl.yamlPatternDelimiter );
            tblcmd.searchYamlForPattern( _inputNode, claTbl.yamlRegExpStr, claTbl.yamlPatternDelimiter );
            final Node output = tblcmd.getOutput();
            return output;

        case INSERT:
            final CmdLineArgsInsertCmd claIns = (CmdLineArgsInsertCmd) cmdLineArgs;
            if (claIns.verbose) System.out.println( HDR +" claIns.yamlRegExpStr="+ claIns.yamlRegExpStr +" & loading @Insert-file: " + claIns.insertFilePath);
            final Object newContent = this.getDataFromReference( claIns.insertFilePath );
            if (claIns.verbose) System.out.println( HDR +" about to start INSERT command using: [" + newContent.toString() + "]");
            InsertYamlEntry inscmd = new InsertYamlEntry( claIns.verbose, claIns.showStats, dumperopt, newContent );
            inscmd.searchYamlForPattern( _inputNode, claIns.yamlRegExpStr, claIns.yamlPatternDelimiter );
            final Node output3 = inscmd.getOutput();
            return output3;

        case REPLACE:
            final CmdLineArgsReplaceCmd claRepl = (CmdLineArgsReplaceCmd) cmdLineArgs;
            if (claRepl.verbose) System.out.println( HDR +" loading @Replace-file: " + claRepl.replaceFilePath);
            final Object replContent = this.getDataFromReference( claRepl.replaceFilePath );
            if (claRepl.verbose) System.out.println( HDR +" about to start CHANGE/REPLACE command using: [" + replContent.toString() + "]");
            ReplaceYamlEntry replcmd = new ReplaceYamlEntry( claRepl.verbose, claRepl.showStats, dumperopt, replContent );
            replcmd.searchYamlForPattern( _inputNode, claRepl.yamlRegExpStr, claRepl.yamlPatternDelimiter );
            final Node output5 = replcmd.getOutput();
            return output5;

        case BATCH:
            final CmdLineArgsBatchCmd claBatch = (CmdLineArgsBatchCmd) cmdLineArgs;
            if (claBatch.verbose) System.out.println( HDR +" about to start BATCH command using: BATCH file [" + claBatch.batchFilePath + "]");
            final Enums.ScalarStyle quoteStyle = ( claBatch.getQuoteType() == Enums.ScalarStyle.UNDEFINED ) ? Enums.ScalarStyle.PLAIN : claBatch.getQuoteType();

            final BatchCmdProcessor batcher = new BatchCmdProcessor( claBatch.verbose, claBatch.showStats, claBatch.isOffline(), quoteStyle, dumperopt );
            batcher.setMemoryAndContext( this.memoryAndContext );
            final Node outpData2 = batcher.go( claBatch.batchFilePath, _inputNode );
            if ( this.verbose ) System.out.println( HDR +" outpData2 =" + outpData2 +"\n\n");
            return outpData2;

        case MACROYAML:
        case MACRO:
            final CmdLineArgsMacroCmd claMacro = (CmdLineArgsMacroCmd) cmdLineArgs;
            if (claMacro.verbose) System.out.println( HDR +" loading Props file [" + claMacro.propertiesFilePath + "]");
            assertTrue( claMacro.propertiesFilePath != null );

            MacroYamlProcessor macroYamlPr = null;
            // MacroStringProcessor macroStrPr = null;

            switch ( cmdLineArgs.cmdType ) {
                case MACRO:     assertTrue( false ); // we can't get here with '_input' ..  _WITHOUT_ it being a _VALID_ YAML content.   So, so might as well as use 'MacroYamlProcessor'
                                // macroStrPr = new MacroStringProcessor( claMacro.verbose, claMacro.showStats ); // does NOT use 'dumperopt'
                                break;
                case MACROYAML: macroYamlPr = new MacroYamlProcessor( claMacro.verbose, claMacro.showStats ); // does NOT use 'dumperopt'
                                break;
                default: assertTrue( false ); // should not be here.
            }

            Properties properties = null;
            if ( "!AllProperties".equals( claMacro.propertiesFilePath ) || (claMacro.propertiesFilePath == null) || "null".equals(claMacro.propertiesFilePath) || (claMacro.propertiesFilePath.trim().length()<=0)  )   {
                // do Nothing.   properties will remain set to 'null'
            } else {
                final Object content = this.getDataFromReference( claMacro.propertiesFilePath );
                if (content instanceof Properties) {
                    properties = (Properties) content;
                }else {
                    throw new Exception( claMacro.propertiesFilePath +" is Not a java properties file, with the extension '.properties' .. or, it's contents (of type'"+ content.getClass().getName() +"')are Not compatible with java.util.Properties" );
                }
            }

            if (claMacro.verbose) System.out.println( HDR +" about to start MACRO command using: [Props file [" + claMacro.propertiesFilePath + "]");
            Node outpData = null;
            switch ( cmdLineArgs.cmdType ) {
                case MACRO:     assertTrue( false ); // we can't get here with '_input' ..  _WITHOUT_ it being a _VALID_ YAML content.   So, so might as well as use 'MacroYamlProcessor'
                                // outpData = macroStrPr.searchNReplace( raw-java.lang.String-from-where??, properties, this.memoryAndContext.getAllPropsRef() );
                                break;
                case MACROYAML: outpData = macroYamlPr.recursiveSearch( _inputNode, properties, this.memoryAndContext.getAllPropsRef() );
                                break;
                default: assertTrue( false ); // should not be here.
            }

            return outpData;

        default:
            final String es = HDR +" Unimplemented command: " + cmdLineArgs.toString();
            System.err.println( es );
            throw new Exception( es );
        }
        // return null; // should Not reach here!
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    /**
     * This is a simpler facade/interface to {@link InputsOutputs#getDataFromReference}, for use by {@link BatchCmdProcessor}
     * @param _src a javalang.String value - either inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to a property within a Batch-file execution (must be prefixed with a '!')
     * @return an object (either any of Node, SequenceNode, MapNode, ScalarNode ..)
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object getDataFromReference( final String _src )
                                throws FileNotFoundException, IOException, Exception
    {   return InputsOutputs.getDataFromReference( _src, this.memoryAndContext, this.getYamlScanner(), this.dumperopt, this.verbose );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * This is a simpler facade/interface to {@link InputsOutputs#saveDataIntoReference}, for use by {@link BatchCmdProcessor}
     * @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
     * @param _input the object to be saved using the reference provided in _dest paramater
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public void saveDataIntoReference( final String _dest, final Object _input )
                            throws FileNotFoundException, IOException, Exception
    {   InputsOutputs.saveDataIntoReference( _dest, _input, this.memoryAndContext, this.getYamlWriter(), this.dumperopt, this.verbose );
    }
                        
    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>This method needs to supplement org.ASUX.YAML.CmdInvoker.deepClone() as this subclass (org.ASUX.YAML.NodeImpl.CmdInvoker) has it's own transient instance-fields/variables.</p>
     *  <p>Such Transients are made Transients for only ONE-SINGLE REASON - they are NOT serializable).</p>
     *  <p>!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!</p>
     *  <p>So, after a deepClone() of CmdInvoker.java .. you'll need to call: </p>
     *  <p> <code> clone.dumperopt = origObj.dumperopt; </code> <br>
     *  @param origObj the non-null original to clone
     *  @return a properly cloned and re-initiated clone of the original (that works around instance-variables that are NOT serializable)
     *  @throws Exception when org.ASUX.common.Utils.deepClone clones the core of this class-instance 
     */
    public static org.ASUX.YAML.NodeImpl.CmdInvoker deepClone( final org.ASUX.YAML.NodeImpl.CmdInvoker origObj ) throws Exception {
        final org.ASUX.yaml.CmdInvoker newCmdInvk = org.ASUX.yaml.CmdInvoker.deepClone( origObj );
        final org.ASUX.YAML.NodeImpl.CmdInvoker newCmdinvoker = (org.ASUX.YAML.NodeImpl.CmdInvoker) newCmdInvk;
        newCmdinvoker.dumperopt = NodeTools.deepClone( origObj.dumperopt );
        return newCmdinvoker;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
