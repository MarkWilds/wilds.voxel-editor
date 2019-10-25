package pao.editor.tools;

import pao.core.services.CommandService;
import pao.core.services.MapService;
import pao.editor.models.ToolBarElement;
import pao.editor.views.ToolBarView;

import com.wildrune.rune.viewport.IViewport;
import com.wildrune.rune.viewport.handler.ViewportHandlerAdapter;

import java.awt.event.MouseEvent;

/**
 * @author Mark "Wilds" van der Wal
 * @since 19-3-2018
 */
public abstract class MapTool extends ViewportHandlerAdapter {

    protected IViewport viewport;
    protected MapService mapService;
    protected CommandService commandService;

    public MapTool(IViewport viewport, MapService mapService, CommandService commandService) {
        this.viewport = viewport;
        this.mapService = mapService;
        this.commandService = commandService;
    }

    public void setup(ToolBarView toolBar) {
    }

    public void teardown() {
    }

    public void onToolbarAction(ToolBarElement element) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
