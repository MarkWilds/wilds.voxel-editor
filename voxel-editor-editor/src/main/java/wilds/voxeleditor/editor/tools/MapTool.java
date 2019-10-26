package wilds.voxeleditor.editor.tools;

import wilds.voxeleditor.core.services.CommandService;
import wilds.voxeleditor.core.services.MapService;
import wilds.voxeleditor.editor.models.ToolBarElement;
import wilds.voxeleditor.editor.views.ToolBarView;

import wilds.rune.viewport.IViewport;
import wilds.rune.viewport.handler.ViewportHandlerAdapter;

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
