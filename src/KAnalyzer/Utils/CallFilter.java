/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;
import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.Interfaces.TLineToken;
import java.util.ArrayList;

/**
 *
 * Basic Analyzing Rule - Classify by calls
 * 
 * You can use this abstract method to implement your own analyzer rule.
 * Just implement the abstract method getCalls() and return an array
 * with all the system calls you want to handle.
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public abstract class CallFilter implements IAnalyzeRule {

    /*
     * Protected function to get the list of the calls to filter
     */
    abstract protected String[] getCalls();

    /*
     * The name of the call that is currently being handled
     */
    public String callName;

    /*
     * The name of the call that is currently being handled
     */
    public Integer callIndex;

    /*
     * A local mirror of getCalls response as an ArrayList<String>
     */
    public ArrayList<String> calls;

    /*
     * Initialize calls array list
     */
    public CallFilter() {
        calls = new ArrayList<String>();
        String[] myCalls = this.getCalls();
        for (int i=0; i<myCalls.length; i++) {
            calls.add(myCalls[i]);
        }
    }

    /*
     * Apply the basic filter on the calls
     */
    public boolean apply(TLineToken line) {
        String call = (String) line.get("call");
        this.callName = call;
        this.callIndex = this.calls.indexOf(call);
        return this.calls.contains(call);
    }

}
