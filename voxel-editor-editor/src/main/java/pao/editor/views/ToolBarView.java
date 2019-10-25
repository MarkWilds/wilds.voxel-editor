package pao.editor.views;

import pao.editor.models.ToolBarElement;
import pao.editor.models.ToolBarElementPosition;
import pao.editor.models.ToolBarIcon;

import com.alee.extended.button.WebSplitButton;
import com.alee.laf.button.WebButton;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Mark "Wilds" van der Wal
 * @since 5-1-2018
 */
public class ToolBarView extends WebToolBar {

    private static final Logger LOGGER = LogManager.getLogger(ToolBarView.class);

    public static final int DEFAULT_ICON_SIZE = 24;

    private Consumer<ToolBarElement> toolbarListener;
    private Collection<ToolBarElement> toolbarElements;
    private Map<String, JComponent> JComponentMap;

    private int iconSize = 0;

    public ToolBarView() {
        this(StyleId.auto, WebToolBar.HORIZONTAL);
    }

    public ToolBarView(StyleId styleId, int orientation) {
        super(styleId, orientation);
        JComponentMap = new HashMap<>();
    }

    public void initialize(Collection<ToolBarElement> toolbarElements) {
        this.iconSize = DEFAULT_ICON_SIZE;
        this.toolbarElements = toolbarElements;
        setLayout();
    }

    public void initialize(int iconSize, Collection<ToolBarElement> toolbarElements) {
        this.iconSize = iconSize;
        this.toolbarElements = toolbarElements;
        setLayout();
    }

    public void setLayout() {
        if (toolbarElements == null) {
            LOGGER.warn("Toolbar elements are not set yet!");
        } else {
            setFloatable(false);
            for (ToolBarElement enumeration : toolbarElements) {
                if (ToolBarElement.Separator.equals(enumeration)) {
                    addSeparator();
                    continue;
                }

                // create element type
                JButton element;
                if (ToolBarElement.ToolWindow.equals(enumeration)) {
                    ToolBarIcon toolBarIcon = enumeration.getToolBarIcon();
                    if (toolBarIcon == null) {
                        element = new WebSplitButton(StyleId.buttonIconHover, enumeration.getDescription());
                    } else {
                        element = new WebSplitButton(StyleId.buttonIconHover, toolBarIcon.getIcon(iconSize));
                    }
                } else {
                    ToolBarIcon toolBarIcon = enumeration.getToolBarIcon();
                    if (toolBarIcon == null) {
                        element = new WebButton(StyleId.buttonIconHover, enumeration.getDescription());
                    } else {
                        element = new WebButton(StyleId.buttonIconHover, toolBarIcon.getIcon(iconSize));
                    }
                }

                element.addActionListener(actionEvent -> onToolbarPressed(element, enumeration));

                if (enumeration.getHotkeyData() != null) {
                    HotkeyManager.registerHotkey(element, enumeration.getHotkeyData(), keyEvent -> {
                        onToolbarPressed(element, enumeration);
                    });
                }

                add(enumeration.name(), element, enumeration.getToolBarElementPosition(), enumeration.getDescription());
            }
        }
    }

    public void add(String name, JComponent element, ToolBarElementPosition position, String description) {
        TooltipManager.setTooltip(element, description);

        switch (position) {
            case Start:
                add(element);
                break;
            case Middle:
                addToMiddle(element);
                break;
            case End:
                addToEnd(element);
                break;
        }

        JComponentMap.put(name, element);
    }

    private void onToolbarPressed(JButton button, ToolBarElement buttonId) {
        if (button.isEnabled() && toolbarListener != null) {
            toolbarListener.accept(buttonId);
        }
    }

    public JComponent getElement(ToolBarElement element) throws Exception {
        if (!JComponentMap.containsKey(element.name())) {
            throw new Exception("Could not find specified toolbar button!");
        }

        return JComponentMap.get(element.name());
    }

    public void setToolbarListener(final Consumer<ToolBarElement> listener) {
        toolbarListener = listener;
    }
}
