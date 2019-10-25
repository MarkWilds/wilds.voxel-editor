package pao.editor.views.dockable;

import pao.core.models.Tileset;
import pao.core.services.TilesetService;
import pao.editor.models.DockableData;
import pao.editor.models.ToolBarElement;
import pao.editor.views.ToolBarView;
import pao.editor.views.components.TilesetPanel;
import pao.editor.views.tablelayout.swing.Table;

import com.wildrune.rune.geometry.Area;

import com.alee.api.data.CompassDirection;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.style.StyleId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Mark "Wilds" van der Wal
 * @since 20-2-2018
 * <p>
 * todo add support for correct tileset handling with panes
 * todo fix tilesetPanel, moving out of the panel with the mouse should not cancel dragging.
 * todo fix tilesetPanel, pressing outside of tileset area inside the panel should not do anything, now it selects a tile.
 */
public class TilesetView extends WebPanel implements IDockable {

    private static final Logger LOGGER = LogManager.getLogger(TilesetView.class);

    private final TilesetService tilesetService;

    private final ToolBarView toolBarView;
    private final DockableData dockableData;
    private final WebTabbedPane tilesetPane;

    private WebFileChooser fileChooser;

    public TilesetView(TilesetService tilesetService) {
        this.tilesetService = tilesetService;
        dockableData = new DockableData("TilesetView", "Tilesets", CompassDirection.east, this);
        toolBarView = new ToolBarView(StyleId.toolbarUndecorated, WebToolBar.HORIZONTAL);
        tilesetPane = new WebTabbedPane(WebTabbedPane.RIGHT);

        fileChooser = new WebFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        setLayout();
    }

    public void setLayout() {
        toolBarView.initialize(16, Arrays.asList(ToolBarElement.NewTileset, ToolBarElement.EditTileset, ToolBarElement.RemoveTileset));
        toolBarView.setToolbarListener(this::onHandleToolbar);

        Table table = new Table();
        table.setPreferredSize(new Dimension(352, 192));

        table.addCell(tilesetPane).expand().fill();
        table.row();
        table.addCell(toolBarView).maxHeight(32).fill();

        add(table);
    }

    private void onHandleToolbar(ToolBarElement element) {
        switch (element) {
            case NewTileset:
                createNewTileset();
                break;
        }
    }

    private void createNewTileset() {
        FileFilter filter = new FileNameExtensionFilter("Image(.png)",
                ".png");
        fileChooser.setFileFilter(filter);

        Path currentDirectoy = Paths.get("").toAbsolutePath().normalize();
        fileChooser.setCurrentDirectory(currentDirectoy.toFile());

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == WebFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                Tileset tileset = tilesetService.loadTileset(file, 32);
                addTab(tileset);

            } catch (IOException ex) {

            }
        }
    }

    private void tilesetSelected(Tileset tileset, Area area) {
        LOGGER.info(String.format("Area (%s,%s,%s,%s) selected for tileset: %s", area.getX(), area.getY(),
                area.getMax().x, area.getMax().y, tileset.getName()));
    }

    private void addTab(Tileset tileset) {
        TilesetPanel tilesetPanel = new TilesetPanel(tileset);
        tilesetPanel.setOnTileSelectedHandler(this::tilesetSelected);
        WebScrollPane tilesetScrollPane = new WebScrollPane(StyleId.scrollpaneUndecoratedButtonless, tilesetPanel);
        tilesetScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        tilesetPane.addTab(tileset.getName(), tilesetScrollPane);
    }

    @Override
    public DockableData getData() {
        return dockableData;
    }
}
