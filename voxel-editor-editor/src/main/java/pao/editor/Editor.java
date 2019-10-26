package pao.editor;

import pao.core.services.CommandService;
import pao.core.services.MapService;
import pao.core.services.TilesetService;
import pao.editor.views.EditorView;
import pao.editor.views.LoaderView;

import wilds.rune.viewport.IViewportFactory;
import wilds.rune.viewport.jogl.JoglContextManager;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.tooltip.TooltipManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.SwingUtilities;

/**
 * @author Mark van der Wal
 * @since 18/01/18
 * <p>
 * This is the entry point for the editor.
 *
 * todo: find a way to handle hacky hotkeys, not being able to use hotkeys when a modal dialog is open.
 */
public class Editor {

    private final static Logger LOGGER = LogManager.getLogger(Editor.class);

    public static void main(String... args) {
        final IViewportFactory editorViewportFactory = new JoglContextManager();
        final MapService mapService = new MapService();
        final TilesetService tilesetService = new TilesetService();
        final CommandService commandService = new CommandService();

        SwingUtilities.invokeLater(() -> {
            try {
                WebLookAndFeel.install();
                WebLookAndFeel.initializeManagers();
                WebLookAndFeel.setForceSingleEventsThread(true);

                TooltipManager.initialize();
                TooltipManager.setShowHotkeysInTooltips(true);

                HotkeyManager.initialize();

                editorViewportFactory.initialize();
                LoaderView loader = new LoaderView("Loading editor");
                EditorView editor = new EditorView(editorViewportFactory,
                        mapService, tilesetService, commandService);
//                editor.show();
                loader.setOnDoneCallback(editor::show);
                loader.execute();

            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
                System.exit(1);
            }
        });
    }
}
