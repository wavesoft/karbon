/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package karbon.Analyzing;

import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.Interfaces.TLineToken;

/**
 *
 * The filter for the KTimeAnalyzer
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class FTimecallFilter implements IAnalyzeRule {

    public Double timeRequested = 0d;
    public Double timeCosted = 0d;

    @Override
    public boolean apply(TLineToken line) {
        String call = (String) line.get("call", "");
        Long sec, uSec;

        if ("nanosleep".equals(call)) {
            sec = (Long)line.get("arg1", 0l);
            uSec = (Long)line.get("arg2", 0l);
            timeRequested = sec.doubleValue() * ( uSec.doubleValue() / 1000000000 );
            timeCosted = (Double)line.get("time", 0d);
            return true;
        }
        return false;
    }

}
