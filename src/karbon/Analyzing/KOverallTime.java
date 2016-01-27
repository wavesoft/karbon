/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Analyzing;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.TAnalyzeTarget;
import KAnalyzer.API.TPresenter;
import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.Utils.TextPresenter;

/**
 *
 * @author Ioannis Charalampidis
 */
public class KOverallTime extends TAnalyzeTarget {

    Double totalTime;
    TextPresenter tp;

    public KOverallTime() {
        tp = new TextPresenter();
        tp.Title = "Overall Time";
    }

    @Override
    public void receiveLine(TLineToken line, IAnalyzeRule byRule) {
        Double time = (Double)line.get("time", 0d);
        totalTime += time;
    }

    @Override
    public void beginAnalysis() {
        totalTime = 0d;
    }

    @Override
    public void completeAnalysis() {
         tp.writeLine("Total time = " + totalTime.toString());
    }

    @Override
    public void reset() {
        totalTime = 0d;
    }

    @Override
    public TPresenter getPresenter() {
        return tp;
    }
    
}
