/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.API;

import KAnalyzer.Router;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public abstract class TTokenizer {
    public Router router = null;
    public abstract void tokenize(String line);
    public abstract void reset();
}
