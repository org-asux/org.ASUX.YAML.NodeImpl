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

import org.ASUX.yaml.*;
import org.yaml.snakeyaml.nodes.Node;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

// https://yaml.org/spec/1.2/spec.html#id2762107
// import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java

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
 * @see org.ASUX.yaml.CmdLineArgsCommon
 * @see org.ASUX.yaml.CmdLineArgsRegExp
 *
 * @see org.ASUX.YAML.NodeImpl.ReadYamlEntry
 * @see org.ASUX.YAML.NodeImpl.ListYamlEntry
 * @see org.ASUX.YAML.NodeImpl.DeleteYamlEntry
 * @see org.ASUX.YAML.NodeImpl.ReplaceYamlEntry
 */
public class CmdInvoker extends org.ASUX.yaml.CmdInvoker<Node> {

    private static final long serialVersionUID = 312L;

    public static final String CLASSNAME = CmdInvoker.class.getName();

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     *  The constructor exclusively for use by  main() classes anywhere.
     *  @param _cmdLineArgs NotNull instance of the command-line arguments passed in by the user.
     */
    public CmdInvoker( final CmdLineArgsCommon _cmdLineArgs ) {
        this( _cmdLineArgs, null );
    }
    // public CmdInvoker( final boolean _verbose, final boolean _showStats ) {
    //     this( _verbose, _showStats, null );
    // }

    // *  @param _verbose Whether you want deluge of debug-output onto System.out.
    // *  @param _showStats Whether you want a final summary onto console / System.out
    // public CmdInvoker( final boolean _verbose, final boolean _showStats, final MemoryAndContext _memoryAndContext ) {
    /**
     *  Variation of constructor that allows you to pass-in memory from another previously existing instance of this class.  Useful within org.ASUX.YAML.NodeImp.BatchYamlProcessor which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     *  @param _cmdLineArgs NotNull instance of the command-line arguments passed in by the user.
     *  @param _memoryAndContext pass in memory from another previously existing instance of this class.  Useful within org.ASUX.YAML.CollectionImpl.BatchYamlProcessor which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     */
    public CmdInvoker( final CmdLineArgsCommon _cmdLineArgs, final MemoryAndContext _memoryAndContext ) {
        super( _cmdLineArgs, _memoryAndContext );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  This function is meant to be used by Cmd.main() and by BatchProcessor.java.  Read the code *FIRST*, to see if you can use this function too.
     *  @param _clArgs Everything passed as commandline arguments to the Java program {@link org.ASUX.yaml.CmdLineArgsCommon}
     *  @param _inputData _the YAML inputData that is the input to pretty much all commands (a org.yaml.snakeyaml.nodes.Node object).
     *  @return either a String or org.yaml.snakeyaml.nodes.Node
     *  @throws YAMLPath.YAMLPathException if Pattern for YAML-Path provided is either semantically empty or is NOT java.util.Pattern compatible.
     *  @throws FileNotFoundException if the filenames within _clArgs do NOT exist
     *  @throws IOException if the filenames within _clArgs give any sort of read/write troubles
     *  @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    @Override
    public Object processCommand ( org.ASUX.yaml.CmdLineArgsCommon _clArgs, final Object _inputData )
                throws FileNotFoundException, IOException, Exception,
                YAMLPath.YAMLPathException
    {
        final String HDR = CLASSNAME + ": processCommand("+ _clArgs.cmdType +",_inputData): "; // NOTE !!!!!! _clArgs/CmdLineArgsCommon .. does NOT have 'cmdType' instance-variable

        final NodeTools nodetools = (NodeTools) super.getYAMLImplementation();
        assertNotNull( nodetools );
        NodeTools.updateDumperOptions( nodetools.getDumperOptions(), _clArgs.quoteType ); // Important <<---------- <<---------- <<-----------

        assertTrue( _inputData instanceof Node );
        // why didn't we just make 2nd parameter of this method to be Node?
        // Well. This is ONE YAML-Library implementation (using SnakeYAML).
        // org.ASUX.YAML project has a 2nd YAML-Library Implementation.  Take a look at org.ASUX.yaml.YAML_Libraries
        // @SuppressWarnings("unchecked")
        final Node _inputNode = (Node) _inputData;

        switch ( _clArgs.cmdType ) {
        case READ:
            final org.ASUX.yaml.CmdLineArgsReadCmd claRead = (org.ASUX.yaml.CmdLineArgsReadCmd) _clArgs;
            ReadYamlEntry readcmd = new ReadYamlEntry( claRead, nodetools.getDumperOptions() );
            readcmd.searchYamlForPattern( _inputNode, claRead.yamlRegExpStr, claRead.yamlPatternDelimiter );
            final Node outputStr = readcmd.getOutput();
            return outputStr;

        case LIST:
            final org.ASUX.yaml.CmdLineArgsRegExp claList = (org.ASUX.yaml.CmdLineArgsRegExp) _clArgs;
            ListYamlEntry listcmd = new ListYamlEntry( claList.verbose, claList.showStats, nodetools.getDumperOptions(), " , " );
            listcmd.searchYamlForPattern( _inputNode, claList.yamlRegExpStr, claList.yamlPatternDelimiter );
            final Node outputStr2 = listcmd.getOutput();
            return outputStr2;

        case DELETE:
            final org.ASUX.yaml.CmdLineArgsRegExp claDel = (org.ASUX.yaml.CmdLineArgsRegExp) _clArgs;
            if ( claDel.verbose ) System.out.println( HDR +" about to start DELETE command");
            DeleteYamlEntry delcmd = new DeleteYamlEntry( claDel.verbose, claDel.showStats, nodetools.getDumperOptions() );
            delcmd.searchYamlForPattern( _inputNode, claDel.yamlRegExpStr, claDel.yamlPatternDelimiter );
            return _inputNode;

        case TABLE:
            final CmdLineArgsTableCmd claTbl = (CmdLineArgsTableCmd) _clArgs;
            if (claTbl.verbose) System.out.println( HDR +" claTbl.yamlRegExpStr="+ claTbl.yamlRegExpStr +" & tableColumns=[" + claTbl.tableColumns +"]" );
            TableYamlQuery tblcmd = new TableYamlQuery( claTbl, nodetools.getDumperOptions() );
            tblcmd.searchYamlForPattern( _inputNode, claTbl.yamlRegExpStr, claTbl.yamlPatternDelimiter );
            final Node output = tblcmd.getOutput();
            return output;

        case INSERT:
            final org.ASUX.yaml.CmdLineArgsInsertCmd claIns = (org.ASUX.yaml.CmdLineArgsInsertCmd) _clArgs;
            if (claIns.verbose) System.out.println( HDR +" claIns.yamlRegExpStr="+ claIns.yamlRegExpStr +" & loading @Insert-file: " + claIns.insertFilePath);
            final Object newContent = this.getDataFromReference( claIns.insertFilePath );
            if (claIns.verbose) System.out.println( HDR +" about to start INSERT command using: [" + newContent.toString() + "]");
            InsertYamlEntry inscmd = new InsertYamlEntry( claIns.verbose, claIns.showStats, nodetools.getDumperOptions(), newContent );
            inscmd.searchYamlForPattern( _inputNode, claIns.yamlRegExpStr, claIns.yamlPatternDelimiter );
            final Node output3 = inscmd.getOutput();
            return output3;

        case REPLACE:
            final CmdLineArgsReplaceCmd claRepl = (CmdLineArgsReplaceCmd) _clArgs;
            if (claRepl.verbose) System.out.println( HDR +" loading @Replace-file: " + claRepl.replaceFilePath);
            final Object replContent = this.getDataFromReference( claRepl.replaceFilePath );
            if (claRepl.verbose) System.out.println( HDR +" about to start CHANGE/REPLACE command using: [" + replContent.toString() + "]");
            ReplaceYamlEntry replcmd = new ReplaceYamlEntry( claRepl.verbose, claRepl.showStats, nodetools.getDumperOptions(), replContent );
            replcmd.searchYamlForPattern( _inputNode, claRepl.yamlRegExpStr, claRepl.yamlPatternDelimiter );
            final Node output5 = replcmd.getOutput();
            return output5;

        case BATCH:
            final CmdLineArgsBatchCmd claBatch = (CmdLineArgsBatchCmd) _clArgs;
            if (claBatch.verbose) System.out.println( HDR +" about to start BATCH command using: BATCH file [" + claBatch.batchFilePath + "]");
            final Enums.ScalarStyle quoteStyle = ( claBatch.quoteType == Enums.ScalarStyle.UNDEFINED ) ? Enums.ScalarStyle.PLAIN : claBatch.quoteType;

            final BatchCmdProcessor batcher = new BatchCmdProcessor( claBatch, nodetools.getDumperOptions() );
            batcher.setMemoryAndContext( this.memoryAndContext );
            final Node outpData2 = batcher.go( claBatch.batchFilePath, _inputNode );
            if ( this.cmdLineArgs.verbose ) System.out.println( HDR +" outpData2 =" + outpData2 +"\n\n");
            return outpData2;

        case MACROYAML:
        case MACRO:
            // @SuppressWarnings("unchecked")
            final CmdLineArgsMacroCmd claMacro = (CmdLineArgsMacroCmd) _clArgs;
            if (claMacro.verbose) System.out.println( HDR +" case MACRO: loading Props file [" + claMacro.propertiesFilePath + "]");
            assertNotNull( claMacro.propertiesFilePath );

            MacroYamlProcessor macroYamlPr = null;

            switch ( _clArgs.cmdType ) {
                case MACRO:     // fail(); // we can't get here with '_input' ..  _WITHOUT_ it being a _VALID_ YAML content.   So, so might as well as use 'MacroYamlProcessor'
                                // break;
                case MACROYAML: macroYamlPr = new MacroYamlProcessor( claMacro.verbose, claMacro.showStats ); // does NOT use 'nodetools.getDumperOptions()'
                                break;
                default: fail(); // should not be here.
            }

            Properties properties = null;
            if ( "!AllProperties".equals( claMacro.propertiesFilePath ) || (claMacro.propertiesFilePath == null) || "null".equals(claMacro.propertiesFilePath) || (claMacro.propertiesFilePath.trim().length()<=0)  )   {
                // do Nothing.   properties will remain set to 'null'
                if (claMacro.verbose) System.out.println( HDR +" case MACRO: All Properties remain set to NULL!");
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
            switch ( _clArgs.cmdType ) {
                case MACRO:
                case MACROYAML: outpData = macroYamlPr.recursiveSearch( _inputNode, properties, this.memoryAndContext.getAllPropsRef() );
                                break;
                default:        fail(); // should not be here.
            }

            return outpData;

        default:
            final String es = HDR +" Unimplemented command: " + _clArgs.toString();
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
    {
        final NodeTools nodetools = (NodeTools) this.getYAMLImplementation();
        return InputsOutputs.getDataFromReference( _src, this.memoryAndContext, nodetools.getYAMLScanner(), nodetools.getDumperOptions(), this.cmdLineArgs.verbose );
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
    {
        final NodeTools nodetools = (NodeTools) this.getYAMLImplementation();
        InputsOutputs.saveDataIntoReference( _dest, _input, this.memoryAndContext, nodetools.getYAMLWriter(), nodetools.getDumperOptions(), this.cmdLineArgs.verbose );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>This method needs to supplement org.ASUX.YAML.CmdInvoker.deepClone() as this subclass (org.ASUX.YAML.NodeImpl.CmdInvoker) has it's own transient instance-fields/variables.</p>
     *  <p>Such Transients are made Transients for only ONE-SINGLE REASON - they are NOT serializable).</p>
     *  <p>!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!</p>
     *  <p>So, after a deepClone() of CmdInvoker.class .. you'll need to call: </p>
     *  <p> <code> clone.dumperopt = origObj.dumperopt; </code> <br>
     *  @param origObj the non-null original to clone
     *  @return a properly cloned and re-initiated clone of the original (that works around instance-variables that are NOT serializable)
     *  @throws Exception when org.ASUX.common.Utils.deepClone clones the core of this class-instance 
     */
    public static org.ASUX.YAML.NodeImpl.CmdInvoker deepClone( final org.ASUX.YAML.NodeImpl.CmdInvoker origObj ) throws Exception {
        // final org.ASUX.yaml.CmdInvoker<Node> newCmdInvk = org.ASUX.yaml.CmdInvoker<Node>.deepClone( origObj );
        final org.ASUX.yaml.CmdInvoker<Node> newCmdInvk = origObj.deepClone( origObj );
        final org.ASUX.YAML.NodeImpl.CmdInvoker newCmdinvoker = (org.ASUX.YAML.NodeImpl.CmdInvoker) newCmdInvk;
        // NOTE: Parent takes care of deep-cloning the YAML-implemenatation.  So, no need to deepclone the DumperOptions.
        // newCmdinvoker.getYAMLImplementation().setLibraryOptionsObject( NodeTools.deepClone( origObj.getYAMLImplementation().getLibraryOptionsObject() ) );
        // newCmdinvoker.getYAMLImplementation().setLibraryOptionsObject( NodeTools.deepClone( (org.yaml.snakeyaml.DumperOptions) origObj.getYAMLImplementation().getLibraryOptionsObject() ) );
        return newCmdinvoker;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
