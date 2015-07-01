/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KAnalyzer.Utils;

import java.io.File;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class ExtensionFilter extends  javax.swing.filechooser.FileFilter {

    private String extensions[];
    private String description;

    public ExtensionFilter(String description, String extension) {
      this(description, new String[] { extension });
    }

    public ExtensionFilter(String description, String extensions[]) {
      this.description = description;
      this.extensions = (String[]) extensions.clone();
    }

    @Override
    public boolean accept(File file) {
      if (file.isDirectory()) {
        return true;
      }
      int count = extensions.length;
      String path = file.getAbsolutePath();
      for (int i = 0; i < count; i++) {
        String ext = extensions[i];
        if (path.endsWith(ext)
            && (path.charAt(path.length() - ext.length()) == '.')) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
}