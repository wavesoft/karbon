/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.API;

import KAnalyzer.Utils.ReportTools.ReportFormat;

/**
 *
 * This interface provides basic reporting features
 * on a class.
 * 
 * These reports are usually logged to files.
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public interface IReportable {
    /**
     * Return the report in the specified format
     * @return the string buffer
     */
    public String getReport( ReportFormat format );

    /*
     * Return the title of the report
     * @return The title of the report
     */
    public String getTitle();

    /*
     * Return TRUE if this report should be included
     * in a collective report.
     */
    public boolean includeToReport();
}
