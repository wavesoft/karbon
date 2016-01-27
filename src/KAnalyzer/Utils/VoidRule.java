/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.Interfaces.TLineToken;

/**
 *
 * @author Ioannis Charalampidis
 */
public class VoidRule implements IAnalyzeRule {

    @Override
    public boolean apply(TLineToken line) {
        return false;
    }

}
