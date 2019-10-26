package pao.editor.views;

import pao.core.exceptions.EditorIOException;
import pao.core.io.MapFileIO;
import pao.core.models.events.CommandEvent;
import pao.core.models.events.MapEvent;
import pao.core.services.CommandService;
import pao.core.services.MapService;
import pao.core.services.TilesetService;
import pao.editor.models.DockableData;
import pao.editor.models.ToolBarElement;
import pao.editor.views.dialogs.MapInformationDialog;
import pao.editor.views.dockable.IDockable;
import pao.editor.views.dockable.LoggingView;

import wilds.rune.viewport.IViewportFactory;

import com.alee.extended.dock.SidebarVisibility;
import com.alee.extended.dock.WebDockableFrame;
import com.alee.extended.dock.WebDockablePane;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.window.WebFrame;
import com.alee.managers.hotkey.HotkeyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark "Wilds" van der Wal
 * @since 12/08/17
 */
public class EditorView extends AbstractWindowView {

    private final static Logger LOGGER = LogManager.getLogger(EditorView.class);

    private final static int MINIMUM_WIDTH = 1280;
    private final static int MINIMUM_HEIGHT = 720;
    private static final String version = "0.15";
    private static final String titleText = "Porygon v%s";
    private static final String editorTitle;

    static {
        editorTitle = String.format(titleText, version);
    }

    private final JFrame editorFrame;
    private final WebDockablePane dockablePane;
    private final IViewportFactory viewportFactory;
    private final MapService mapService;
    private final TilesetService tilesetService;
    private final CommandService commandService;

    private final StatusBarView statusBarView;
    private final ToolBarView toolbarView;
    private MapInformationDialog mapInformationDialog;
    private WebFileChooser fileChooser;

    private LoggingView loggingView;
    private MapView mapView;

    public EditorView(IViewportFactory _viewportFactory, MapService _mapService, TilesetService _tilesetService,
                      CommandService _commandService) {
        editorFrame = new WebFrame();
        statusBarView = new StatusBarView();
        toolbarView = new ToolBarView();
        dockablePane = new WebDockablePane();

        fileChooser = new WebFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        viewportFactory = _viewportFactory;
        commandService = _commandService;
        tilesetService = _tilesetService;
        mapService = _mapService;

        initializeEditor();
    }

    public void initializeEditor() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/icons/editor/icon.png"));
        editorFrame.setLayout(new BorderLayout());
        editorFrame.setIconImage(icon.getImage());
        editorFrame.setExtendedState(WebFrame.MAXIMIZED_BOTH);
        editorFrame.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        editorFrame.setLocationRelativeTo(null);
        editorFrame.setTitle(editorTitle);
        editorFrame.addWindowListener(this);

        Collection<ToolBarElement> toolbarButtons = Arrays.asList(ToolBarElement.New,
                ToolBarElement.Open, ToolBarElement.Save, ToolBarElement.SaveAs,
                ToolBarElement.Separator,
                ToolBarElement.Undo, ToolBarElement.Redo,
                ToolBarElement.Separator,
                ToolBarElement.Edit,
                ToolBarElement.ToolWindow, ToolBarElement.Settings);

        toolbarView.initialize(toolbarButtons);

        dockablePane.setSidebarVisibility(SidebarVisibility.never);
        editorFrame.add(dockablePane, BorderLayout.CENTER);

        editorFrame.add(statusBarView, BorderLayout.PAGE_END);
        editorFrame.add(toolbarView, BorderLayout.PAGE_START);

        mapView = new MapView(viewportFactory, mapService, tilesetService, commandService);

        setDefaultGuiState();

//        loggingView = new LoggingView();
//        LogReceiverAppender.addReceiver(loggingView);
//
//        addDockable(loggingView);

        setWindowListener(this::onWindowEvent);
        toolbarView.setToolbarListener(this::onToolBarAction);
        mapService.onMapChanged(this::onMapChanged);
        commandService.onCommand(this::onCommand);

        LOGGER.info(String.format("Initialized %s", editorTitle));
    }

    private void setDefaultGuiState() {
        try {
            JComponent undoButton = toolbarView.getElement(ToolBarElement.Undo);
            JComponent redoButton = toolbarView.getElement(ToolBarElement.Redo);
            JComponent saveButton = toolbarView.getElement(ToolBarElement.Save);
            JComponent saveAsButton = toolbarView.getElement(ToolBarElement.SaveAs);

            saveButton.setEnabled(false);
            saveAsButton.setEnabled(false);
            undoButton.setEnabled(false);
            redoButton.setEnabled(false);
        } catch (Exception e) {
            LOGGER.warn("Some requested button does not exist");
        }
    }

    public void show() {
        editorFrame.setVisible(true);
    }

    private void addDockable(IDockable dockable) {
        DockableData dockableData = dockable.getData();
        WebDockableFrame frame = new WebDockableFrame(dockableData.getId(), dockableData.getTitle());
        frame.setMaximizable(false);
        frame.setPosition(dockableData.getDirection());
        frame.add(dockableData.getComponent());

        dockablePane.addFrame(frame);
    }

    private void onCommand(CommandEvent event) {
        try {
            JComponent undoButton = toolbarView.getElement(ToolBarElement.Undo);
            JComponent redoButton = toolbarView.getElement(ToolBarElement.Redo);

            undoButton.setEnabled(event.hasUndoCommands());
            redoButton.setEnabled(event.hasRedoCommands());
        } catch (Exception e) {
            LOGGER.warn("Undo or redo button does not exist");
        }
    }

    private void onMapChanged(MapEvent event) {
        try {
            JComponent saveButton = toolbarView.getElement(ToolBarElement.Save);
            JComponent saveAsButton = toolbarView.getElement(ToolBarElement.SaveAs);

            saveButton.setEnabled(event.isMapDirty());
            saveAsButton.setEnabled(!event.isMapClosed());

            // show map component when map is created or opened
            if (event.isMapFresh()) {
                dockablePane.setContent(mapView);
                commandService.clear();
            }

            // update window title
            if (event.isMapClosed()) {
                editorFrame.setTitle(String.format("%s", editorTitle));
                dockablePane.setContent(null);
                commandService.clear();
            } else {
                String titleFormat = "%s - <%s>";
                if (event.isMapDirty()) {
                    titleFormat = "%s - <%s*>";
                }

                editorFrame.setTitle(String.format(titleFormat, editorTitle, mapService.getMap().getName()));
            }

        } catch (Exception e) {
            LOGGER.warn(String.format("Save or saveAs button does not exist"));
        }
    }

    private void onWindowEvent(WindowEvent windowEvent) {
        switch (windowEvent.getNewState()) {
            case WindowEvent.WINDOW_CLOSED:
                System.exit(0);
                break;
        }
    }

    private void onToolBarAction(ToolBarElement buttonIdentifier) {
        switch (buttonIdentifier) {
            case New:
                newMap();
                break;
            case Save:
                saveMap();
                break;
            case SaveAs:
                saveAsMap();
                break;
            case Open:
                openMap();
                break;
            case Undo:
                commandService.undo();
                break;
            case Redo:
                commandService.redo();
                break;
        }
    }

    private void newMap() {
        if (mapInformationDialog == null) {
            HotkeyManager.disableHotkeys();

            mapInformationDialog = new MapInformationDialog(editorFrame);
            mapInformationDialog.setTitle("New map");
            mapInformationDialog.setActionButtonText("Create map");
            mapInformationDialog.setOnActionHandler(information -> {
                mapService.newMap(information.getMapName(), information.getMapWidth(), information.getMapHeight());
            });
            mapInformationDialog.setVisible(true);

            mapInformationDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent event) {

                    mapInformationDialog = null;
                    HotkeyManager.enableHotkeys();
                }
            });
        }
    }

    private void setupFileChooser(boolean setSelectedFile) {
        MapFileIO mapIOHandler = mapService.getMapIOHandler();

        FileFilter filter = new FileNameExtensionFilter(mapIOHandler.getDescription(),
                mapIOHandler.getExtension());
        fileChooser.setFileFilter(filter);

        if (setSelectedFile) {
            Path path = mapIOHandler.createMapPath(mapService.getMap());
            fileChooser.setSelectedFile(path.toFile());
        } else {
            fileChooser.setCurrentDirectory(mapIOHandler.getWorkingDirectory().toFile());
        }
    }

    private void openMap() {
        setupFileChooser(false);

        try {
            int returnValue = fileChooser.showOpenDialog(editorFrame);
            if (returnValue == WebFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                mapService.loadMap(file.toPath());
            }
        } catch (EditorIOException e) {
            LOGGER.error(e.getMessage());
        }

    }

    private void saveMap() {
        try {
            if (mapService.hasMapPath()) {
                mapService.saveMap(mapService.getMapPath());
            } else {
                saveAsMap();
            }
        } catch (EditorIOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void saveAsMap() {
        setupFileChooser(true);

        try {
            int returnValue = fileChooser.showSaveDialog(editorFrame);
            if (returnValue == WebFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                mapService.saveMap(file.toPath());
            }
        } catch (EditorIOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
