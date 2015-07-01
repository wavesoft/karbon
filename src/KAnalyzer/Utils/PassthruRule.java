/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.Interfaces.TLineToken;

/**
 *
 * @author Γιάννης
 */
public final class PassthruRule implements IAnalyzeRule {

    @Override
    public boolean apply(TLineToken line) {
        return true;
    }

}
