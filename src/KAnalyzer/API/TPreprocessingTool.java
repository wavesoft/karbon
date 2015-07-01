/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.API;

import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.Interfaces.TToolBus;
import KAnalyzer.Router;

/**
 *
 * Preprocessing Tool abstract class
 * 
 * A Preprocessing Tool is an analyzing structure simmilar to TAnalyzeTarget
 * that is executed before the actual TAnalyzeTargets.
 *
 * It is used to extract additional information from the input stream.
 * Theese extra information are then shared among the TAnalyzeTargets.
 *
 * A TAnalyzeTarget can access the information provided by a preprocessing
 * tool using:
 *   1) The Router.toolBus class  : Which is a set of variables that provide
 *         additional information for the current tokenized line
 *   2) The getInformation() call : Wich provides detailed acess into the
 *         tool's internal variable store.
 *
 * A simple example usage is a classifying tool: It can provide a "class"
 * variable on the toolBus that will carry the current system call's
 * class (ex. network call, memory call etc..)
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public abstract class TPreprocessingTool {

    /*
     * The ToolBus is a set of additional variables
     * acompaning the TLineToken
     */
    public TToolBus toolBus;

    /*
     * The router that created us
     */
    public Router router;

    /*
     * Fetch various information stored on the
     * preprocessing tool.
     */
    public Object getInformation(String parameter) {
        return getInformation(parameter, new Object[]{} );
    }
    public abstract Object getInformation(String parameter, Object[] arguments);

    /*
     * Process the tokenized line
     */
    public abstract void processLine(TLineToken line);

    /*
     * Notification for the beginning of the analysis
     */
    public abstract void beginAnalysis();

    /*
     * Notification for the completion of the analysis
     */
    public abstract void completeAnalysis();

    /*
     * Reset the tool
     */
    public abstract void reset();

    /*
     * If your tool has a front-end, you can fetch it
     * with this system call
     *
     * By default preprocessing tools have no front-end.
     * Override this function to create yours.
     */
    public TPresenter getPresenter() {
        return null;
    }
    
}
