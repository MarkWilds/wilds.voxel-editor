package wilds.voxeleditor.editor.views.dockable;

import wilds.voxeleditor.editor.models.DockableData;
import wilds.voxeleditor.editor.models.ToolBarElement;
import wilds.voxeleditor.editor.utils.ILogReceiver;
import wilds.voxeleditor.editor.views.ToolBarView;
import wilds.voxeleditor.editor.views.tablelayout.swing.Table;

import com.alee.api.data.CompassDirection;
import com.alee.laf.list.WebList;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.style.StyleId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultListModel;
import java.awt.Dimension;
import java.util.Arrays;

/**
 * @author Mark "Wilds" van der Wal
 * @since 4-1-2018
 */
public class LoggingView extends WebPanel implements IDockable, ILogReceiver {

    private final static Logger LOGGER = LogManager.getLogger(LoggingView.class);

    private final ToolBarView toolBarView;
    private final WebList logList;
    private final DefaultListModel<String> logModel;

    private final DockableData dockableData;

    public LoggingView() {
        dockableData = new DockableData("LogView", "Logging", CompassDirection.south, this);
        logModel = new DefaultListModel<>();
        logList = new WebList(StyleId.list, logModel);
        toolBarView = new ToolBarView(StyleId.toolbarUndecorated, WebToolBar.HORIZONTAL);

        setLayout();
    }

    public void setLayout() {
        toolBarView.initialize(16, Arrays.asList(ToolBarElement.ClearLog));
        toolBarView.setToolbarListener(event -> clearLog());

        // setup layout
        Table table = new Table();
        table.setPreferredSize(new Dimension(256, 196));

        WebScrollPane logListScroll = new WebScrollPane(StyleId.scrollpaneUndecoratedButtonless, logList);
        table.addCell(logListScroll).expand().fill();
        table.row();
        table.addCell(toolBarView).maxHeight(32).fill();

        add(table);
    }

    @Override
    public void receiveLog(final String log) {
        logModel.addElement(log);

        int lastIndex = logModel.getSize() - 1;
        logList.ensureIndexIsVisible(lastIndex);
    }

    private void clearLog() {
        logModel.clear();
    }

    @Override
    public DockableData getData() {
        return dockableData;
    }
}
