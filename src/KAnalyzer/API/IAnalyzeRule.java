/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.API;

import KAnalyzer.Interfaces.TLineToken;

/**
 * Target selecting rule
 *
 * This interface is used to implement input selection
 * to a specific TAnalyzeTarget.
 * 
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public interface IAnalyzeRule {

    /*
     * Apply the rule to the specified line and return
     * the result.
     *
     * If the result is TRUE the line will be forwarded
     * to the linked IAnalyzeRule class.
     */
    public boolean apply(TLineToken line);
}
