/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer;

import KAnalyzer.Interfaces.IStreamReceiver;
import KAnalyzer.API.TAnalyzeTarget;
import KAnalyzer.API.IAnalyzeRule;
import KAnalyzer.API.TPreprocessingTool;
import KAnalyzer.API.TTokenizer;
import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.Interfaces.TToolBus;
import java.util.ArrayList;

/**
 * Core traffic router
 * 
 * This class routes the incoming data lines to the appropriate
 * analyzers. It uses IAnalyzeRules to do the selection.
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class Router implements IStreamReceiver {

    private ArrayList<TPreprocessingTool> tools;
    private ArrayList<TAnalyzeTarget> targets;
    private ArrayList<IAnalyzeRule> rules;

    private TTokenizer tokenizer;
    
    // Single-instanced variables (for speed-up)
    public TToolBus toolBus;
    
    public Router(TTokenizer tokenizer) {
        // The router is based on a specified tokenizer
        this.tokenizer = tokenizer;
        this.tokenizer.router = this;
        this.targets = new ArrayList<TAnalyzeTarget>();
        this.rules = new ArrayList<IAnalyzeRule>();
        this.tools = new ArrayList<TPreprocessingTool>();
        this.toolBus = new TToolBus();
    }

    public Object queryTools(String parameter, Object[] arguments) {
        Object ans = null;
        for (TPreprocessingTool tool : tools) {
            ans = tool.getInformation(parameter, arguments);
            if (ans != null) return ans;
        }
        return null;
    }

    public void addTarget(TAnalyzeTarget target, IAnalyzeRule rule) {
        target.router = this;
        target.toolBus = this.toolBus;
        targets.add(target);
        rules.add(rule);
    }

    public void removeTarget(TAnalyzeTarget target, IAnalyzeRule rule) {
        target.router = null;
        target.toolBus = null;
        targets.remove(target);
        rules.remove(rule);
    }

    public void addTool(TPreprocessingTool tool) {
        tool.router = this;
        tool.toolBus = this.toolBus;
        tools.add(tool);
    }

    public void removeTool(TPreprocessingTool tool) {
        tool.router = null;
        tool.toolBus = null;
        tools.remove(tool);
    }

    public void receiveTokenizedLine(TLineToken t) {

        // First, forward the line to the pre-rpcoessing tools
        this.toolBus.reset();
        for (int i=0; i<tools.size(); i++) {
            tools.get(i).processLine(t);
        }

        // Then use the target rules to forward the output to the specified target
        IAnalyzeRule rule;
        for (int i=0; i<targets.size(); i++) {
            rule = rules.get(i);
            if (rule.apply(t)) targets.get(i).receiveLine(t, rule);
        }
    }

    @Override
    public void receiveLine(String line) {
        // First, tokenize line
        // Tokenizer with then call receiveTokenizedLine() when
        // we have enough data to process the line
        tokenizer.tokenize(line);
    }

    public void broadcastBeginAnalysis() {
        for (int i=0; i<tools.size(); i++) {
            tools.get(i).beginAnalysis();
        }
        for (TAnalyzeTarget t: targets) {
            t.beginAnalysis();
        }
    }

    public void broadcastCompleteAnalysis() {
        for (int i=0; i<tools.size(); i++) {
            tools.get(i).completeAnalysis();
        }
        for (TAnalyzeTarget t: targets) {
            t.completeAnalysis();
        }
    }

    public void resetToolsAndTargets() {
        tokenizer.reset();
        for (int i=0; i<targets.size(); i++) {
            targets.get(i).reset();
        }
        for (int i=0; i<tools.size(); i++) {
            tools.get(i).reset();
        }
    }

}
