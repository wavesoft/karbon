/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer;

import KAnalyzer.Utils.ListPresenter;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * Error collector for the entire KAnalyzer subsystem
 *
 * It also provides visual feedback with a ListPresenter.
 * To get an instance to the presenter use ErrorCollector.getPresenter()
 * 
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class ErrorCollector {

    public static ArrayList<String> errors = new ArrayList<String>();
    private static ListPresenter presenter = new ListPresenter();

    private static void logMessage(String message, int level) {
        if (level == 0) {
            errors.add(message);
            presenter.addLine("<html><span style=\"color: #1155ff\"><b>NOTICE</b> "+message+"</span></html>", "Notice: "+message);
        } else if (level == 1) {
            errors.add(message);
            presenter.addLine("<html><span style=\"color: #998800\"><b>WARNING</b> "+message+"</span></html>", "Warning: "+message);
        } else if (level == 2) {
            errors.add(message);
            presenter.addLine("<html><span style=\"color: #ff9933\"><b>ERROR</b> "+message+"</span></html>", "Error: "+message);
        } else if (level == 3) {
            errors.add(message);
            presenter.addLine("<html><span style=\"color: #FF0000\"><b>CRITICAL</b> "+message+"</span></html>", "Critical: "+message);
        }
    }

    public static void store_warning(String message) {
        logMessage(message, 1);
    }

    public static void store_error(String message) {
        logMessage(message, 2);
    }

    public static void store_notice(String error) {
        logMessage(error, 0);
    }

    public static void store_exception(Exception ex, String position, Boolean critical) {
        logMessage("Exception on "+position+": "+ex.toString(), critical ? 3 : 2);
    }

    public static ListPresenter getPresenter() {
        presenter.Title = "Errors / Warnings";
        return presenter;
    }

    public static void display() {
        if (errors.isEmpty()) return;
        
        String message = "";
        for (String s: errors) {
            message += s+"\n";
        }
        JOptionPane.showMessageDialog(null, "Errors occured:\n"+message);
    }

    public static void reset() {
        errors.clear();
        presenter.clear();
    }

}
