package app.components;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

public final class ColumnResizer {
    private ColumnResizer() {}

    /** Lock column to the width of its longest text (+pad). */
    public static void lockToContent(TableView<?> tv, double pad) {
        int n = tv.getColumns().size();

        for (int i = 0; i < n - 1 ; i++) {
            TableColumn<?, ?> col = tv.getColumns().get(i);
            double w = contentWidth(col) + pad;
            col.setResizable(false);
            col.setMinWidth(w);
            col.setPrefWidth(w);
            col.setMaxWidth(w);
        }
    }

    /** Header + all visible cell text widths; returns the max. */
    private static double contentWidth(TableColumn<?, ?> col) {
        double max = 0;
        var tv = col.getTableView();

        if (tv != null) {
            int n = tv.getItems().size();
            for (int i = 0; i < n; i++) {
                Object v = col.getCellData(i);
                if (v != null) max = Math.max(max, textW(String.valueOf(v))); // approx char width
            }
        }
        return Math.ceil(max);
    }

    // cut string's text width in pixels
    private static double textW(String s) {
        return new Text(s == null ? "" : s).getLayoutBounds().getWidth() + 20;
    }
}
