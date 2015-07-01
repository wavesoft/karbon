/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GridPresenter.java
 *
 * Created on 20 Αυγ 2010, 5:39:54 μμ
 */

package KAnalyzer.Utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import KAnalyzer.Utils.ReportTools.ReportFormat;

/**
 *
 * @author Ioannis Charalampidis <johnys2@gmail.com>
 */
public class GridPresenterEx extends KAnalyzer.API.TPresenter {

    private SortableTableModel tableModel;
    private SortButtonRenderer headerRenderer;
    private Object[][] columnInfo;

    /*
     * Set the column information as a double-vector format:
     * {
     *   { <String: Name>, <class: Data class>, <Integer: Width>
     * }
     */
    public void setColumnInfo(Object[][] Info) {
        // Create new
        for (int c = 0; c < Info.length; c++) {
            if (Info[c].length == 0) {
                Info[c] = new Object[]{ "", String.class, 400 };
            } else if (Info[c].length == 1) {
                Info[c] = new Object[]{ Info[c][0], String.class, 400 };
            } else if (Info[c].length == 2) {
                Info[c] = new Object[]{ Info[c][0], Info[c][1], 400 };
            }
            tableModel.addColumn((String)Info[c][0]);
        }
        columnInfo = Info;

        // Update info
        TableColumnModel model = jGridData.getColumnModel();
        int n = model.getColumnCount();
        for (int i = 0; i < n; i++) {
            model.getColumn(i).setHeaderRenderer(headerRenderer);
            model.getColumn(i).setPreferredWidth((Integer)Info[i][2]);
        }

    }

    /** Creates new form GridPresenter */
    public GridPresenterEx() {
        initComponents();

        this.columnInfo = new Object[][]{{}};

        tableModel = new SortableTableModel() {

            @Override
              public Class getColumnClass(int col) {
                try {
                    return (Class)columnInfo[col][1];
                } catch (Exception e) {
                    return String.class;
                }
              }

            @Override
              public boolean isCellEditable(int row, int col) {
                return false;
              }

            @Override
              public void setValueAt(Object obj, int row, int col) {
                return;
              }
          };

        jGridData.setModel(tableModel);
        //table.setShowGrid(false);
        //jGridData.setShowVerticalLines(true);
        //jGridData.setShowHorizontalLines(false);

        headerRenderer = new SortButtonRenderer();

        /*
        TableColumnModel model = jGridData.getColumnModel();
        int n = headerStr.length;
        for (int i = 0; i < n; i++) {
          model.getColumn(i).setHeaderRenderer(renderer);
          model.getColumn(i).setPreferredWidth(columnWidth[i]);
        }
         */

        // Bind the button renderer
        JTableHeader header = jGridData.getTableHeader();
        header.addMouseListener(new HeaderListener(header, headerRenderer));
    }

    @Override
    public void reset() {
        tableModel.setNumRows(0);
    }

    public void addColumn(String Name) {
        tableModel.addColumn(Name);
        TableColumnModel model = jGridData.getColumnModel();
        int n = model.getColumnCount();
        for (int i = 0; i < n; i++) {
            model.getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }

    public void addColumn(String Name, Integer Width) {
        tableModel.addColumn(Name);
        TableColumnModel model = jGridData.getColumnModel();
        int n = model.getColumnCount();
        for (int i = 0; i < n; i++) {
            model.getColumn(i).setHeaderRenderer(headerRenderer);
            if (i == n-1) {
                model.getColumn(i).setPreferredWidth(Width);
            }
        }
    }

    public void addRow(Object[] Data) {
        tableModel.addRow(Data);
    }

    public void resetColumns() {
        tableModel.setColumnCount(0);
    }

  private static DateFormat dateFormat = DateFormat.getDateInstance(
      DateFormat.SHORT, Locale.JAPAN);

  private static Date getDate(String dateString) {
    Date date = null;
    try {
      date = dateFormat.parse(dateString);
    } catch (ParseException ex) {
      date = new Date();
    }
    return date;
  }

    @Override
    public boolean includeToReport() {
        return jToggleButton1.isSelected();
    }

  class HeaderListener extends MouseAdapter {
    JTableHeader header;

    SortButtonRenderer renderer;

    HeaderListener(JTableHeader header, SortButtonRenderer renderer) {
      this.header = header;
      this.renderer = renderer;
    }

        @Override
    public void mousePressed(MouseEvent e) {
      int col = header.columnAtPoint(e.getPoint());
      int sortCol = header.getTable().convertColumnIndexToModel(col);
      renderer.setPressedColumn(col);
      renderer.setSelectedColumn(col);
      header.repaint();

      if (header.getTable().isEditing()) {
        header.getTable().getCellEditor().stopCellEditing();
      }

      boolean isAscent;
      if (SortButtonRenderer.DOWN == renderer.getState(col)) {
        isAscent = true;
      } else {
        isAscent = false;
      }
      ((SortableTableModel) header.getTable().getModel()).sortByColumn(
          sortCol, isAscent);
    }

        @Override
    public void mouseReleased(MouseEvent e) {
      int col = header.columnAtPoint(e.getPoint());
      renderer.setPressedColumn(-1); // clear
      header.repaint();
    }
  }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jGridData = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();

        jGridData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jGridData);

        jButton1.setText("Save to file");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jToggleButton1.setText("Include to report");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jToggleButton1)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ReportTools.saveReportDialog(this);
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    public javax.swing.JTable jGridData;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables

    private String padToLength(String src, Integer length) {
        return String.format("%1$-" + length + "s", src);
    }

    @Override
    public String getReport(ReportFormat format) {
        String buffer = "", cellStr = "";
        Integer cols = tableModel.getColumnCount();
        Integer rows = tableModel.getRowCount();
        Integer totalSize = 0;
        Integer[] colSizes = new Integer[] { };

        // Header and initializations
        if (format == ReportFormat.Text) {

            // Find out the maximum size of the contents per column
            colSizes = new Integer[cols];
            for (int x=0; x<cols; x++) {
                colSizes[x] = tableModel.getColumnName(x).length();
                for (int y=0; y<rows; y++) {
                    cellStr = tableModel.getValueAt(y, x).toString();
                    if (cellStr.length() > colSizes[x]) colSizes[x] = cellStr.length();
                }
                totalSize += colSizes[x]+1;
            }

            buffer += " ==[ " + Title + " ]==========================\n\n";
        } else if (format == ReportFormat.HTML) {
            buffer += "<h2>" + Title + "</h2>\n<table border=\"1\"><tr>\n";
        } else if (format == ReportFormat.CSV) {
            buffer += Title + "\n";
        }

        // Column headers
        String colName, dataRow = "";
        dataRow = "";
        for (int i=0; i<cols; i++) {
            colName = tableModel.getColumnName(i);
            if (format == ReportFormat.Text) {
                dataRow += " "+padToLength(colName, colSizes[i]);
            } else if (format == ReportFormat.HTML) {
                dataRow += "   <th>"+colName+"</th>";
            } else if (format == ReportFormat.CSV) {
                if (!dataRow.equals("")) dataRow += ",";
                dataRow += colName;
            }
        }
        buffer += dataRow + "\n";

        // Finalize headers
        if (format == ReportFormat.HTML) {
            buffer += "</tr>\n";
        }

        // Content processing
        for (int r=0; r<rows; r++) {
            dataRow = "";

            // Initialize data row
            if (format == ReportFormat.Text) {
            } else if (format == ReportFormat.HTML) {
                dataRow += "<tr>\n";
            } else if (format == ReportFormat.CSV) {
            }

            // Create data row
            for (int c=0; c<cols; c++) {
                cellStr = tableModel.getValueAt(r, c).toString();
                if (format == ReportFormat.Text) {
                    dataRow += " "+padToLength(cellStr, colSizes[c]);
                } else if (format == ReportFormat.HTML) {
                    dataRow += "   <td>"+cellStr+"</td>";
                } else if (format == ReportFormat.CSV) {
                    if (!dataRow.equals("")) dataRow += ",";
                    dataRow += cellStr;
                }
            }

            // Finalize data row
            if (format == ReportFormat.Text) {
                dataRow += "\n";
            } else if (format == ReportFormat.HTML) {
                dataRow += "</tr>\n";
            } else if (format == ReportFormat.CSV) {
                dataRow += "\n";
            }

            // Insert data row
            buffer += dataRow;
        }

        // Footer
        if (format == ReportFormat.Text) {
            buffer += "\n";
        } else if (format == ReportFormat.HTML) {
            buffer += "</table>";
        } else if (format == ReportFormat.CSV) {
            buffer += "\n";
        }

        return buffer;
    }
        
}

// <editor-fold defaultstate="collapsed" desc="SortableTableModel">

// <editor-fold defaultstate="collapsed" desc="TableSorter">
class SortableTableModel extends DefaultTableModel {

    int[] indexes;
    TableSorter sorter;

    public SortableTableModel() {
    }

    @Override
    public void setNumRows(int rowCount) {
        if (rowCount == 0) {
            // Reset
            indexes = null;
        }
        super.setNumRows(rowCount);
    }

    @Override
    public Object getValueAt(int row, int col) {
        try {
            int rowIndex = row;
            if (indexes != null) {
                rowIndex = indexes[row];
            }
            return super.getValueAt(rowIndex, col);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        try {
            int rowIndex = row;
            if (indexes != null) {
                rowIndex = indexes[row];
            }
            super.setValueAt(value, rowIndex, col);
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
    }

    public void sortByColumn(int column, boolean isAscent) {
        if (sorter == null) {
            sorter = new TableSorter(this);
        }
        sorter.sort(column, isAscent);
        fireTableDataChanged();
    }

    public int[] getIndexes() {
        int n = getRowCount();
        if (indexes != null) {
            if (indexes.length == n) {
                return indexes;
            }
        }
        indexes = new int[n];
        for (int i = 0; i < n; i++) {
            indexes[i] = i;
        }
        return indexes;
    }
}// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="TableSorter">
class TableSorter {

    SortableTableModel model;

    public TableSorter(SortableTableModel model) {
        this.model = model;
    }

    //n2 selection
    public void sort(int column, boolean isAscent) {
        int n = model.getRowCount();
        int[] indexes = model.getIndexes();

        for (int i = 0; i < n - 1; i++) {
            int k = i;
            for (int j = i + 1; j < n; j++) {
                if (isAscent) {
                    if (compare(column, j, k) < 0) {
                        k = j;
                    }
                } else {
                    if (compare(column, j, k) > 0) {
                        k = j;
                    }
                }
            }
            int tmp = indexes[i];
            indexes[i] = indexes[k];
            indexes[k] = tmp;
        }
    }

    // comparaters
    public int compare(int column, int row1, int row2) {
        Object o1 = model.getValueAt(row1, column);
        Object o2 = model.getValueAt(row2, column);
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else {
            Class type = model.getColumnClass(column);
            if (type.getSuperclass() == Number.class) {
                return compare((Number) o1, (Number) o2);
            } else if (type == String.class) {
                return ((String) o1).compareTo((String) o2);
            } else if (type == Date.class) {
                return compare((Date) o1, (Date) o2);
            } else if (type == Boolean.class) {
                return compare((Boolean) o1, (Boolean) o2);
            } else {
                return ((String) o1).compareTo((String) o2);
            }
        }
    }

    public int compare(Number o1, Number o2) {
        double n1 = o1.doubleValue();
        double n2 = o2.doubleValue();
        if (n1 < n2) {
            return -1;
        } else if (n1 > n2) {
            return 1;
        } else {
            return 0;
        }
    }

    public int compare(Date o1, Date o2) {
        long n1 = o1.getTime();
        long n2 = o2.getTime();
        if (n1 < n2) {
            return -1;
        } else if (n1 > n2) {
            return 1;
        } else {
            return 0;
        }
    }

    public int compare(Boolean o1, Boolean o2) {
        boolean b1 = o1.booleanValue();
        boolean b2 = o2.booleanValue();
        if (b1 == b2) {
            return 0;
        } else if (b1) {
            return 1;
        } else {
            return -1;
        }
    }
}// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="SortButtonRenderer">
class SortButtonRenderer extends JButton implements TableCellRenderer {

    public static final int NONE = 0;
    public static final int DOWN = 1;
    public static final int UP = 2;
    int pushedColumn;
    Hashtable state;
    JButton downButton, upButton;

    public SortButtonRenderer() {
        pushedColumn = -1;
        state = new Hashtable();

        setMargin(new Insets(0, 0, 0, 0));
        setHorizontalTextPosition(LEFT);
        setIcon(new BlankIcon());

        // perplexed
        // ArrowIcon(SwingConstants.SOUTH, true)
        // BevelArrowIcon (int direction, boolean isRaisedView, boolean
        // isPressedView)

        downButton = new JButton();
        downButton.setMargin(new Insets(0, 0, 0, 0));
        downButton.setHorizontalTextPosition(LEFT);
        downButton.setIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false));
        downButton.setPressedIcon(new BevelArrowIcon(BevelArrowIcon.DOWN,
                false, true));

        upButton = new JButton();
        upButton.setMargin(new Insets(0, 0, 0, 0));
        upButton.setHorizontalTextPosition(LEFT);
        upButton.setIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, false));
        upButton.setPressedIcon(new BevelArrowIcon(BevelArrowIcon.UP, false,
                true));

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        JButton button = this;
        Object obj = state.get(new Integer(column));
        if (obj != null) {
            if (((Integer) obj).intValue() == DOWN) {
                button = downButton;
            } else {
                button = upButton;
            }
        }
        button.setText((value == null) ? "" : value.toString());
        boolean isPressed = (column == pushedColumn);
        button.getModel().setPressed(isPressed);
        button.getModel().setArmed(isPressed);
        return button;
    }

    public void setPressedColumn(int col) {
        pushedColumn = col;
    }

    public void setSelectedColumn(int col) {
        if (col < 0) {
            return;
            
        }
        Integer value = null;
        Object obj = state.get(new Integer(col));
        if (obj == null) {
            value = new Integer(DOWN);
        } else {
            if (((Integer) obj).intValue() == DOWN) {
                value = new Integer(UP);
            } else {
                value = new Integer(DOWN);
            }
        }
        state.clear();
        state.put(new Integer(col), value);
    }

    public int getState(int col) {
        int retValue;
        Object obj = state.get(new Integer(col));
        if (obj == null) {
            retValue = NONE;
        } else {
            if (((Integer) obj).intValue() == DOWN) {
                retValue = DOWN;
            } else {
                retValue = UP;
            }
        }
        return retValue;
    }
}// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="BevelArrowIcon">
class BevelArrowIcon implements Icon {

    public static final int UP = 0; // direction
    public static final int DOWN = 1;
    private static final int DEFAULT_SIZE = 11;
    private Color edge1;
    private Color edge2;
    private Color fill;
    private int size;
    private int direction;

    public BevelArrowIcon(int direction, boolean isRaisedView,
            boolean isPressedView) {
        if (isRaisedView) {
            if (isPressedView) {
                init(UIManager.getColor("controlLtHighlight"), UIManager.getColor("controlDkShadow"), UIManager.getColor("controlShadow"), DEFAULT_SIZE, direction);
            } else {
                init(UIManager.getColor("controlHighlight"), UIManager.getColor("controlShadow"), UIManager.getColor("control"), DEFAULT_SIZE, direction);
            }
        } else {
            if (isPressedView) {
                init(UIManager.getColor("controlDkShadow"), UIManager.getColor("controlLtHighlight"), UIManager.getColor("controlShadow"), DEFAULT_SIZE, direction);
            } else {
                init(UIManager.getColor("controlShadow"), UIManager.getColor("controlHighlight"), UIManager.getColor("control"), DEFAULT_SIZE, direction);
            }
        }
    }

    public BevelArrowIcon(Color edge1, Color edge2, Color fill, int size,
            int direction) {
        init(edge1, edge2, fill, size, direction);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        switch (direction) {
            case DOWN:
                drawDownArrow(g, x, y);
                break;
            case UP:
                drawUpArrow(g, x, y);
                break;
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    private void init(Color edge1, Color edge2, Color fill, int size,
            int direction) {
        this.edge1 = edge1;
        this.edge2 = edge2;
        this.fill = fill;
        this.size = size;
        this.direction = direction;
    }

    private void drawDownArrow(Graphics g, int xo, int yo) {
        g.setColor(edge1);
        g.drawLine(xo, yo, xo + size - 1, yo);
        g.drawLine(xo, yo + 1, xo + size - 3, yo + 1);
        g.setColor(edge2);
        g.drawLine(xo + size - 2, yo + 1, xo + size - 1, yo + 1);
        int x = xo + 1;
        int y = yo + 2;
        int dx = size - 6;
        while (y + 1 < yo + size) {
            g.setColor(edge1);
            g.drawLine(x, y, x + 1, y);
            g.drawLine(x, y + 1, x + 1, y + 1);
            if (0 < dx) {
                g.setColor(fill);
                g.drawLine(x + 2, y, x + 1 + dx, y);
                g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
            }
            g.setColor(edge2);
            g.drawLine(x + dx + 2, y, x + dx + 3, y);
            g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
            x += 1;
            y += 2;
            dx -= 2;
        }
        g.setColor(edge1);
        g.drawLine(xo + (size / 2), yo + size - 1, xo + (size / 2), yo + size
                - 1);
    }

    private void drawUpArrow(Graphics g, int xo, int yo) {
        g.setColor(edge1);
        int x = xo + (size / 2);
        g.drawLine(x, yo, x, yo);
        x--;
        int y = yo + 1;
        int dx = 0;
        while (y + 3 < yo + size) {
            g.setColor(edge1);
            g.drawLine(x, y, x + 1, y);
            g.drawLine(x, y + 1, x + 1, y + 1);
            if (0 < dx) {
                g.setColor(fill);
                g.drawLine(x + 2, y, x + 1 + dx, y);
                g.drawLine(x + 2, y + 1, x + 1 + dx, y + 1);
            }
            g.setColor(edge2);
            g.drawLine(x + dx + 2, y, x + dx + 3, y);
            g.drawLine(x + dx + 2, y + 1, x + dx + 3, y + 1);
            x -= 1;
            y += 2;
            dx += 2;
        }
        g.setColor(edge1);
        g.drawLine(xo, yo + size - 3, xo + 1, yo + size - 3);
        g.setColor(edge2);
        g.drawLine(xo + 2, yo + size - 2, xo + size - 1, yo + size - 2);
        g.drawLine(xo, yo + size - 1, xo + size, yo + size - 1);
    }
}// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Blank Icon">
class BlankIcon implements Icon {

    private Color fillColor;
    private int size;

    public BlankIcon() {
        this(null, 11);
    }

    public BlankIcon(Color color, int size) {
        //UIManager.getColor("control")
        //UIManager.getColor("controlShadow")
        fillColor = color;

        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (fillColor != null) {
            g.setColor(fillColor);
            g.drawRect(x, y, size - 1, size - 1);
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}// </editor-fold>

