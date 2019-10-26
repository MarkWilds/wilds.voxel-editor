package wilds.voxeleditor.editor.views;

import wilds.voxeleditor.editor.views.tablelayout.swing.Table;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.JWindow;
import javax.swing.SwingWorker;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Mark van der Wal
 * @since 20/01/18
 * <p>
 * todo: try to get rid of magic numbers
 */
public class LoaderView extends JWindow {

    private final static Logger LOGGER = LogManager.getLogger(LoaderView.class);

    private LoaderPanel loader;
    private WebProgressBar progressBar;

    private EditorLoaderController controller;
    private Runnable onWorkerDone;

    public LoaderView(String startingMessage) {
        controller = new EditorLoaderController(this);
        loader = new LoaderPanel();
        loader.setLayout(new BorderLayout());
        progressBar = new WebProgressBar();

        setBackground(new Color(0, 0, 0, 0));
        setMinimumSize(new Dimension(512, 264));
        setLocationRelativeTo(null);

        progressBar.setBoldFont(true);
        progressBar.setStringPainted(true);
        progressBar.setString(startingMessage);

        Table table = new Table();
        table.addCell(progressBar)
                .expand()
                .padTop(206)
                .width(300)
                .top();

        loader.add(table, BorderLayout.CENTER);
        add(loader);

        pack();
        setVisible(true);
    }

    public void setOnDoneCallback(Runnable onDone) {
        onWorkerDone = onDone;
    }

    public void execute() {
        controller.execute();
    }

    private void setProgress(String message, int progress) {
        progressBar.setString(message);
        progressBar.setValue(progress);
    }

    private class LoaderPanel extends WebPanel {

        private BufferedImage loaderImage;

        public LoaderPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());

            try {
                loaderImage = ImageIO.read(LoaderPanel.class.getResource("/images/loader_image.png"));
            } catch (IOException ex) {
                LOGGER.error("Could not load loader_image.png");
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(loaderImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private class EditorLoaderController extends SwingWorker<Void, Integer> {

        private static final int TEST_TIME = 500;
        private static final String LOADING_MESSAGE = "Loading... %s";
        private LoaderView loaderView;

        public EditorLoaderController(LoaderView loaderView) {
            this.loaderView = loaderView;
        }

        @Override
        protected void process(List<Integer> chunks) {
            int latestChunk = chunks.get(chunks.size() - 1);

            loaderView.setProgress(String.format(LOADING_MESSAGE, latestChunk), latestChunk);
        }

        @Override
        protected void done() {
            onWorkerDone.run();
            loaderView.setVisible(false);
            loaderView.dispose();
        }

        @Override
        protected Void doInBackground() {
            final int maxRange = 100;
            final int chunkTime = TEST_TIME / maxRange;
            IntStream.rangeClosed(0, maxRange).forEach(i -> {
                publish(i);
                try {
                    Thread.sleep(chunkTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            return null;
        }
    }
}
