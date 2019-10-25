package pao.editor.views;

import pao.core.renderer.TileMapRenderer;
import pao.core.services.CommandService;
import pao.core.services.MapService;
import pao.core.services.TilesetService;
import pao.editor.models.ToolBarElement;
import pao.editor.models.ToolBarIcon;
import pao.editor.tools.MapTool;
import pao.editor.tools.PaintTileTool;
import pao.editor.views.dockable.TilesetView;
import pao.editor.views.tablelayout.swing.Table;

import com.wildrune.rune.renderer.Color;
import com.wildrune.rune.renderer.IRenderer;
import com.wildrune.rune.viewport.IViewport;
import com.wildrune.rune.viewport.IViewportFactory;
import com.wildrune.rune.viewport.exceptions.ViewportException;
import com.wildrune.rune.viewport.handler.IViewportInputHandler;
import com.wildrune.rune.viewport.handler.IViewportLifeycleHandler;

import com.alee.laf.button.WebToggleButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.style.StyleId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * @author Mark van der Wal
 * @since 18/01/18
 */
public class MapView extends WebPanel implements IViewportLifeycleHandler, IViewportInputHandler {

    private final static Logger LOGGER = LogManager.getLogger(MapView.class);

    private final Table contentTable;
    private final ToolBarView toolBarView;
    private final TilesetView tilesetView;

    private final TileMapRenderer tileMapRenderer;
    private final Color viewportColor = Color.fromRGBA(64, 64, 64, 255);

    private IViewport mapViewport;
    private MapTool currentTool;
    private MapTool paintTool;

    public MapView(IViewportFactory viewportFactory, MapService mapService, TilesetService tilesetService,
                   CommandService commandService) {
        contentTable = new Table();
        toolBarView = new ToolBarView(StyleId.toolbarUndecorated, WebToolBar.VERTICAL);
        tilesetView = new TilesetView(tilesetService);

        try {
            mapViewport = viewportFactory.createViewport();
            mapViewport.setLifecycleHandler(this);
            mapViewport.setInputHandler(this);
        } catch (ViewportException e) {
            LOGGER.error(e.getMessage());
        }

        // create tabpane
        JTabbedPane toolsPane = new JTabbedPane();
        toolsPane.addTab("Tiles", tilesetView);
        toolsPane.addTab("Terrain", new JApplet());
        toolsPane.addTab("Objects", new JPanel());
        toolsPane.addTab("Collision", new JPanel());

        contentTable.addCell(mapViewport.getRoot()).expand().fill();
        contentTable.addCell(toolBarView).expandY().fill();
        contentTable.addCell(toolsPane).expandY().fill();
        add(contentTable);

        // initialize renderers
        tileMapRenderer = new TileMapRenderer(mapViewport, mapService);
        tileMapRenderer.toggleGrid();

        // initialize tools
        paintTool = new PaintTileTool(mapViewport, mapService, commandService);
    }

    @Override
    public void create() {
        tileMapRenderer.create();
        paintTool.create();

        setupTool(paintTool);
    }

    private void setupTool(MapTool tool) {
        if (currentTool != null) {
            currentTool.teardown();
        }
        currentTool = tool;

        int defaultIconSize = ToolBarView.DEFAULT_ICON_SIZE;

        WebToggleButton gridButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.Grid.getIcon(defaultIconSize));
        gridButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.Grid));
        gridButton.setSelected(true);

        toolBarView.removeAll();
        toolBarView.add(gridButton);
        toolBarView.addSeparator();

        tool.setup(toolBarView);
    }

    public void onToolbarAction(ToolBarElement element) {
        switch (element) {
            case Grid:
                tileMapRenderer.toggleGrid();
                break;
        }
    }

    @Override
    public void dispose() {
        currentTool.teardown();

        paintTool.dispose();
        tileMapRenderer.dispose();
    }

    @Override
    public void update(float deltaTime) {
        currentTool.update(deltaTime);
    }

    @Override
    public void render() {
        IRenderer renderer = mapViewport.getRenderer();
        renderer.setClearColor(viewportColor);
        renderer.clear();

        tileMapRenderer.render();
        currentTool.render();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        Component component = mapViewport.getRoot();
        component.setFocusable(true);
        component.requestFocusInWindow();

        currentTool.mouseExited(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        Component component = mapViewport.getRoot();
        component.setFocusable(false);

        currentTool.mouseEntered(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        currentTool.mouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        currentTool.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        currentTool.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentTool.mouseDragged(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        currentTool.mouseWheelMoved(e);
    }

    @Override
    public void keyDown(KeyEvent e) {
        currentTool.keyDown(e);
    }

    @Override
    public void keyUp(KeyEvent e) {
        currentTool.keyUp(e);
    }
}
