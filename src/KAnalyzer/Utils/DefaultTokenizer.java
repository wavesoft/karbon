/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import KAnalyzer.API.TTokenizer;
import KAnalyzer.Interfaces.TLineToken;
import KAnalyzer.ErrorCollector;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.*;

/**
 * Basic Tokenizer Class
 *
 * This class tokenizes any tracing line into a hashtable
 * using a sophisticated regular expressions.
 * 
 * This class provides the following line tokens:
 *
 *  - "pid"         = The process ID in case of multithreading analysis
 *  - "exe"         = The image name in case of detailed multithreading analysis
 *  - "timestamp"   = The timestamp as given by the tracer
 *  - "call"        = The name of the system call
 *  - "args"        = The raw argument string
 *  - "return"      = The return value
 *  - "return_desc" = Additional comments on the return value
 *  - "time"        = The time spent on the call
 *
 * It also supports line sparsing (created by strace when tracking multi-
 * threaded applications)
 * 
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class DefaultTokenizer extends TTokenizer {

    private Hashtable<Long, String> incomplete;

    private Pattern rFull;
    private Pattern rArgsRegex;
    private Pattern rSimple;
    private Pattern rHashSplitter;
    private Pattern rSparse;
    private Pattern rIntegers;
    private Pattern rDecimals;

    private Matcher rmFull;
    private Matcher rmArgsRegex;
    private Matcher rmSimple;
    private Matcher rmHashSplitter;
    private Matcher rmSparse;
    private Matcher rmIntegers;
    private Matcher rmDecimals;
    
    private Integer lineNo;
    private TLineToken lineTokenSingleton;

    private class ByRefInfo {
        public Matcher args;
        public String line;
        public TLineToken token;
        public Integer argid;
        public ArrayList<Object> arglist;
        public Hashtable<String, Object> argmap;

        public ByRefInfo(TLineToken token, String line) {
            this.args = null;
            this.line = line;
            this.token = token;
            this.argid = 0;
            this.arglist = new ArrayList<Object>();
            this.argmap = new Hashtable<String, Object>();
        }
    }

    public DefaultTokenizer() {

        // Pre-build all the regex expressions we are about to use:

        // v1 Regex: ^(\d+)?(?:\s*\(([\w-_+\.]+)\))?\s*(\d+\.\d+)?\s*:?\s*(.*)
        // v2 Regex: ^(\d\d*)\s.*
        // Usage: Use this to detect and get the PID of a line
	rSimple = Pattern.compile("^(\\d\\d*)\\s.*",
		Pattern.CANON_EQ);
        rmSimple = rSimple.matcher("");
        rmSimple.reset();

        // v1 Regex: ^(\d+)?(?:\s*\(([\w-_+\.]+)\))?\s*(\d+\.\d+)?\s*:?\s*(\w+)\((.*)\)\s*=\s*([\w\d-]+)\s*([\w\s\(\){}\[\]=|,_-]+)<(.*?)>
        // v2 Regex: ^(\d+)?(?:\s*+\(([\w-_+\.]+)\))?\s*+(\d+\.\d+)?\s*+:?\s*+(\w+)\((.*)\)\s*+=\s*+([\w\d-]+)\s*+([\w\s\(\){}\[\]=|,_-]*+)(?:<(.*)>)?
        // v3 Regex: ^(\d\d+)?\s*+(?:\((\w+)\)\s*)?+(\d\d*\.\d+\s*)?+:?(\w+)\((.*)\)\s+=\s+(.+)\s?([^<]*+)(?:<([^>]+)>)?
        // v4 Regex: ^(\d\d+)?\s*+(?:\((\w+)\)\s*)?+(\d\d*\.\d+\s*)?+:?(\w+)\((.*)\)\s+=\s+([\da-fx\.\-\x3F]+)([^<$]*)(?:<([\d\.]+)>)?
        // v5 Regex: ?^(\d\d*)?(?:\s*+\(([\w\.-]+)\))?(?:\s*+(\d+\.\d+))?\s*+:?\s*+(\w+)\((.*)\)\s+=\s+([\da-fx\.\-\x3F]+)([^<$]*)(?:<([\d\.]+)>)?
        // Usage: Use this to get the pid/exename/timestamp/callname/arguments/retun value/misc output/execution time on various trace formats
        rFull = Pattern.compile("^(\\d\\d*)?(?:\\s*+\\(([\\w\\.-]+)\\))?(?:\\s*+(\\d+\\.\\d+))?\\s*+:?\\s*+(\\w+)\\((.*)\\)\\s+=\\s+([\\da-fx\\.\\-\\x3F]+)([^<$]*)(?:<([\\d\\.]+)>)?",
		Pattern.CANON_EQ);
        rmFull = rFull.matcher("");
        rmFull.reset();

        // Regex: (".*?(?:(?<!\\)"|"\.\.\.)|'.*?(?:(?<!\\)'|'\.\.\.)|[0-9xa-f-\.]+|\[.*?\](?=\s*(?:,|$))|\{.*?\}(?=\s*(?:,|$))|[A-Z][A-Z|_0-9]+)
        // Usage: Use this to fetch the arguments of a system call
	rArgsRegex = Pattern.compile("(\".*?(?:(?<!\\\\)\"|\"\\.\\.\\.)|'.*?(?:(?<!\\\\)'|'\\.\\.\\.)|[0-9xa-f-\\.]+|\\[.*?\\](?=\\s*(?:,|$))|\\{.*?\\}(?=\\s*(?:,|$))|[A-Z][A-Z|_0-9]+)",
		Pattern.CANON_EQ);
        rmArgsRegex = rArgsRegex.matcher("");
        rmArgsRegex.reset();

        // Regex: (\w+)=(".*?(?<!\\)"|'.*?(?<!\\)'|[0-9xa-f-\.]+|\[.*?\](?=\s*(?:,|$))|\{.*?\}(?=\s*(?:,|$))|[A-Z][A-Z|_0-9]+)
        // Usage: Use this to split the hash parameter->values: {parm=value, parm=[value] ..}
        rHashSplitter = Pattern.compile("(\\w+)=(\".*?(?<!\\\\)\"|'.*?(?<!\\\\)'|[0-9xa-f-\\.]+|\\[.*?\\](?=\\s*(?:,|$))|\\{.*?\\}(?=\\s*(?:,|$))|[A-Z][A-Z|_0-9]+)",
		Pattern.CANON_EQ);
        rmHashSplitter = rHashSplitter.matcher("");
        rmHashSplitter.reset();

        // Regex: ^.*<.*?resumed>(.*)
        // Usage: Use this to check if a line matches a resume state
        rSparse = Pattern.compile("^.*<.*?resumed>(.*)",
		Pattern.CANON_EQ);
        rmSparse = rSparse.matcher("");
        rmSparse.reset();

        // Regex: ^.*<.*?resumed>(.*)
        // Usage: Use this to check if a line matches a resume state
        rIntegers = Pattern.compile("^-?\\d+$",
		Pattern.CANON_EQ);
        rmIntegers = rIntegers.matcher("");
        rmIntegers.reset();

        // Regex: ^.*<.*?resumed>(.*)
        // Usage: Use this to check if a line matches a resume state
        rDecimals = Pattern.compile("^-?[0-9]+\\.[0-9]+$",
		Pattern.CANON_EQ);
        rmDecimals = rDecimals.matcher("");
        rmDecimals.reset();

        // Prepare hashtable
        incomplete = new Hashtable<Long, String>();

        // Initialize singletons
        lineTokenSingleton = new TLineToken();
    }

    private void process_curly_brackets(ByRefInfo r) {
        r.line = r.line.substring(1,r.line.length()-1);
        //Matcher vars = rHashSplitter.matcher(r.line);
        Object arg = null;
        String[] vars;
        ArrayList<String> matches = split_arguments(r.line);
        for (String match: matches) {
            
            match = match.trim();
            if ("...".equals(match)) continue;

            vars = match.split("=",2);

            if (vars.length > 1) {
                if (vars[1].startsWith("[")) {
                    r.line = vars[1];
                    this.process_square_brackets(r);
                } else if (vars[1].startsWith("{")) {
                    r.line = vars[1];
                    this.process_curly_brackets(r);
                } else {
                    // Store the argument
                    r.argid++;
                    arg = this.getFormatedArgument(vars[1]);
                    r.arglist.add(arg);
                    r.token.set("arg" + r.argid.toString(), arg);
                    r.token.set("=" + vars[0], arg);
                }
            } else {
                // Store argument with no prefix
                r.argid++;
                arg = this.getFormatedArgument(match);
                r.arglist.add(arg);
                r.token.set("arg" + r.argid.toString(), arg);
            }
        }
    }

    private void process_square_brackets(ByRefInfo r) {
        r.line = r.line.substring(1,r.line.length()-1);
        process_argmatch(r);
    }

    private Object getFormatedArgument(String arg) {
        Object o = "";
        rmIntegers.reset(arg);
        rmDecimals.reset(arg);
        try {
            if (arg.startsWith("0x")) {
                if (arg.length() > 2) {
                    o = Long.parseLong(arg.substring(2),16);
                } else {
                    o = 0L;
                }
            } else if (arg.startsWith("'")) {
                if (arg.length() < 3) {
                    o = "";
                } else {
                    o = arg.substring(1, arg.length()-1);
                }
            } else if (arg.startsWith("\"")) {
                if (arg.length() < 3) {
                    o = "";
                } else {
                    o = arg.substring(1, arg.length()-1);
                }
            } else if (rmDecimals.matches()) {
                o = Double.parseDouble(arg);
            } else if (rmIntegers.matches()) {
                o = Long.parseLong(arg);
            } else {
                o = arg;
            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "DefaultTokenizer.getFormatedArgument", false);
        }
        return o;
    }

    /*
     * This is faster than the regex :P
     * 
     * It groups { .. }, [ .. ] and " .. " together
     */
    private ArrayList<String> split_arguments(String line ) {
        String split_pattern = ", ";
        String[] src_tokens = line.split(split_pattern);
        String cur_token = "";
        String token = "";
        char in_token = ' ';
        ArrayList<String> dst_tokens = new ArrayList<String>();
        int i;
        for (i=0; i<src_tokens.length; i++)  {
            token = src_tokens[i].replace(" ", "");
            if (token.startsWith("{") && !token.endsWith("}") && (in_token == ' ')) {
                in_token = '{';
                cur_token = token;
            } else if (token.startsWith("[") && !token.endsWith("]") && (in_token == ' ')) {
                in_token = '[';
                cur_token = token;
            } else if (token.startsWith("\"") && !token.endsWith("\"") && (in_token == ' ')) {
                in_token = '"';
                cur_token = token;
            } else if (!token.startsWith("{") && token.endsWith("}") && (in_token == '{')) {
                in_token = ' ';
                dst_tokens.add(cur_token + split_pattern + token);
            } else if (!token.startsWith("[") && token.endsWith("]") && (in_token == '[')) {
                in_token = ' ';
                dst_tokens.add(cur_token + split_pattern + token);
            } else if (!token.startsWith("\"") && token.endsWith("\"") && (in_token == '"')) {
                in_token = ' ';
                dst_tokens.add(cur_token + split_pattern + token);
            } else if (in_token != ' ') {
                cur_token += split_pattern+token;
            } else {
                dst_tokens.add(token);
            }
        }
        return dst_tokens;
    }

    private void process_argmatch(ByRefInfo r) {
        Object arg = null;
        ArrayList<String> arguments = split_arguments(r.line);
        for (String strarg: arguments) {
            arg = this.getFormatedArgument(strarg);

            if (strarg.startsWith("[")) {
                r.line = strarg;
                this.process_square_brackets(r);
            } else if (strarg.startsWith("{")) {
                r.line = strarg;
                this.process_curly_brackets(r);
            } else {

                // Store the argument
                r.argid++;
                r.arglist.add(arg);
                r.token.set("arg" + r.argid.toString(), arg);

            }
        }
    }

    /*
     * Check if the string contains data in
     * the  "parm=value, .." format
     */
    private Boolean isParametrized(String src) {
        if (src.startsWith("{")) return false;
        if (src.startsWith("[")) return false;
        String parts[] = src.split(" ",2);
        if (parts[0].contains("=")) return true;
        return false;
    }

    @Override
    public void reset() {
        lineNo = 0;
    }

    @Override
    public void tokenize(String line) {
      try {
        lineNo++;
        Long pid = 0L;
        
        // Ignore signal lines, they will only
        // create junk error messages
        if (line.endsWith(" ---")) return;

        // First of all, make sure we don't have sparse lines
        // (Occures with strace when it cannot follow the speed of calls
        //  on multithreaded tracing)
        // <editor-fold defaultstate="collapsed" desc="Sparse line handler">
        if (line.endsWith("<unfinished ...>")) {
            Matcher pidinfo = rmSimple.reset(line);

            // rSimple regex returns:
            //   1 = PID
            //   2 = Line

            if (pidinfo.matches()) {
                // We have multithreading information
                pid = Long.parseLong(pidinfo.group(1));
                incomplete.put(pid, line.substring(0, line.length()-17));
            } else {
                // We don't have multithreading information
                // (this means we start from the call line directly)
                incomplete.put(pid, line.substring(0, line.length()-17));
            }

            // Exit...
            return;
        } else {
            rmSparse.reset(line);
            if (rmSparse.matches()) {
                // We cannot have more than one call on the same process, so
                // we don't really need to track the system call name but only
                // the PID. That's why the incomplete[] has uses integer indexing
                Matcher pidinfo = rmSimple.reset(line);

                // rSimple regex returns:
                //   1 = PID
                //   2 = Line

                if (pidinfo.matches()) {
                    // We have multithreading information
                    pid = Long.parseLong(pidinfo.group(1));

                    // Rebuild the splitted line
                    line = incomplete.get(pid) + rmSparse.group(1);

                } else {
                    // We don't have multithreading information
                    // (this means we start from the call line directly)

                    // Rebuild the splitted line
                    line = incomplete.get(pid) + rmSparse.group(1);
                }
            }
        }// </editor-fold>

        // Level one parsing
        lineTokenSingleton.reset(line, lineNo);
        Matcher matches = rmFull.reset(line);
        if (!matches.matches()) {
            ErrorCollector.store_warning("Full Line match not found while parsing line: "+line);
            return;
        }
        try {
            pid = Long.parseLong(matches.group(1));
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "DefaultTokenizer.tokenize.parseInt (PID)", false);
            return;
        }
        
        Double time = 0D;
        try {
            if (matches.group(8) != null && !matches.group(8).equals("")) {
                time = Double.parseDouble(matches.group(8));
            }
        } catch (Exception e) {
            ErrorCollector.store_exception(e, "DefaultTokenizer.tokenize.parseDouble (Time)", false);
        }

        // Store the obvious stuff
        String args = matches.group(5);
        lineTokenSingleton.set("pid", pid);
        lineTokenSingleton.set("exe", matches.group(2));
        if (matches.group(3) == null || matches.group(3).equals("")) {
            lineTokenSingleton.set("timestamp", 0.00);
        } else {
            lineTokenSingleton.set("timestamp", Double.parseDouble(matches.group(3)));
        }
        lineTokenSingleton.set("call", matches.group(4).trim());
        lineTokenSingleton.set("args", args);
        lineTokenSingleton.set("return", this.getFormatedArgument(matches.group(6)));
        lineTokenSingleton.set("return_desc", matches.group(7));
        lineTokenSingleton.set("time", time);

        // Parse the detailed stuff
        ByRefInfo info = new ByRefInfo(lineTokenSingleton, args);
        if (isParametrized(args)) {
            process_curly_brackets(info);
        } else {
            process_argmatch(info);
        }

        // Forward the token to the router
        this.router.receiveTokenizedLine(lineTokenSingleton);
      } catch (NullPointerException e ) {
          ErrorCollector.store_exception(e, "DefaultTokenizer.tokenize",false);
      }
    }
    
}
