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
 * This is the base class for all the ananlyzers
 * 
 * This class provides an interface to the tokenized line and 
 * the additional toolBus information in order to create
 * a custom analysis with theese info.
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public abstract class TAnalyzeTarget {
    public Router router;
    public TToolBus toolBus;
    public abstract void receiveLine(TLineToken line, IAnalyzeRule byRule);
    public abstract void beginAnalysis();
    public abstract void completeAnalysis();
    public abstract void reset();
    public abstract TPresenter getPresenter();
}
