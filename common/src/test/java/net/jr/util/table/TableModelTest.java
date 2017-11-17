package net.jr.util.table;

import org.junit.Assert;
import org.junit.Test;

public class TableModelTest {

    @Test
    public void testStyles() {
        TableModel<String> tableModel = new TableModel<>();
        tableModel.addStyleOnColumn(0, "font-weight:bold");
        tableModel.addStyleOnRow(0, "font-weight:bold");
        tableModel.addStyleOnRow(1, "font-weight:bold");
        tableModel.addStyleOnRow(1, "text-align:center");
        tableModel.removeStyleFromRow(1, "background:blue");
        tableModel.removeStyleFromColumn(2, "text-decoration:none");
        tableModel.addStyleHint(1, 1, 100, 100, "background:pink");

        Assert.assertEquals("font-weight:bold", tableModel.getStyle(0, 0));
        Assert.assertEquals("font-weight:bold", tableModel.getStyle(20, 0));
        Assert.assertEquals("font-weight:bold", tableModel.getStyle(0, 30));
        Assert.assertEquals("font-weight:bold;\ntext-align:center", tableModel.getStyle(0, 1));
        Assert.assertEquals("background:pink", tableModel.getStyle(10, 10));
    }
}

