/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Analyzing;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.TAnalyzeTarget;
import KAnalyzer.API.TPresenter;
import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.Utils.GridPresenterEx;
import KAnalyzer.Utils.ReportTools.ReportFormat;

/**
 *
 * A special modules that measures the performance of
 * the time delaying system calls:
 *   - sleep()
 *   - usleep()
 *   - nanosleep()
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class KTimeAnalyzer extends TAnalyzeTarget {

    GridPresenterEx presenter;

    public KTimeAnalyzer() {
        presenter = new GridPresenterEx();
        presenter.Title = "Time analysis";
        presenter.setColumnInfo(new Object[][] {
            { "Requested delay", Double.class, 100 },
            { "Caused delay", Double.class, 100 },
            { "Requested delay", Double.class, 100 },
        });
    }

    @Override
    public void reset() {
        presenter.reset();
    }

    @Override
    public void receiveLine(TLineToken line, IAnalyzeRule byRule) {
        FTimecallFilter filter = (FTimecallFilter) byRule;
        
    }

    @Override
    public void beginAnalysis() {
    }

    @Override
    public void completeAnalysis() {
    }

    @Override
    public TPresenter getPresenter() {
        return presenter;
    }

}
