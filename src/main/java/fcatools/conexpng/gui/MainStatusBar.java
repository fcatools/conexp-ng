package fcatools.conexpng.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingWorker;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;
import fcatools.conexpng.Conf;
import fcatools.conexpng.Conf.StatusBarMessage;

@SuppressWarnings("serial")
public class MainStatusBar extends WebPanel implements PropertyChangeListener {

    public MainStatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(100, 20));
    }

    private class StatusBar extends SwingWorker<Void, Void> {

        private WebLabel label = null;

        @Override
        protected Void doInBackground() throws Exception {
            WebPanel panel = new WebPanel(new BorderLayout());
            label = new WebLabel("");
            panel.add(label, BorderLayout.CENTER);
            WebProgressBar progressbar = new WebProgressBar();
            progressbar.setIndeterminate(true);
            panel.add(progressbar, BorderLayout.EAST);
            removeAll();
            MainStatusBar.this.add(panel, BorderLayout.EAST);
            revalidate();
            getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            while (!isCancelled()) {
                label.setText(text);
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            MainStatusBar.this.removeAll();
            MainStatusBar.this.add(new WebLabel(""));
            revalidate();

        }

    }

    private StatusBar bar;
    private String text = "";

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof StatusBarMessage) {
            StatusBarMessage status = (StatusBarMessage) evt;
            if ((int) status.getNewValue() == Conf.START) {
                if (bar == null || bar.isDone()) {
                    bar = new StatusBar();
                    bar.execute();
                }
                if (text.isEmpty()) {
                    text = status.getPropertyName();
                } else {
                    if (!text.contains(status.toString()))
                        ;
                    text += ", " + status.getPropertyName();
                }
            } else if ((int) status.getNewValue() == Conf.STOP) {

                text = text.replace(status.getPropertyName(), "");
                text = text.replace(", , ", ", ");
                if (text.endsWith(", "))
                    text = text.substring(0, text.lastIndexOf(','));
                if (text.startsWith(", "))
                    text = text.substring(2);
                if (text.trim().isEmpty() && bar != null) {
                    bar.cancel(true);
                }
            }

        }
    }
}