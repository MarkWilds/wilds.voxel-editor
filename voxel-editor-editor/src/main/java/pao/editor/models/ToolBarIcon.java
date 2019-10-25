package pao.editor.models;

import static java.awt.Image.SCALE_SMOOTH;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark "Wilds" van der Wal
 * @since 21-2-2018
 */
public enum ToolBarIcon {

    New,
    Open,
    Save,
    SaveAs,

    Undo,
    Redo,

    Copy,
    Cut,
    Paste,

    Settings,
    ToolWindow,

    TopDownView,
    GameView,
    FreeView,

    Bin,
    Select,
    Pencil,
    Rect,
    Bucket,
    Eraser,
    Edit,

    Cube,
    Quad,
    Grid,

    Translate,
    Rotate;

    private static final Logger LOGGER = LogManager.getLogger(ToolBarIcon.class);
    private static final String iconsPathFormat = "/icons/toolbars/%s.png";

    private Map<Integer, ImageIcon> cachedIcons;

    ToolBarIcon() {
        cachedIcons = new HashMap<>();
    }

    public ImageIcon getIcon(int iconDimension) {
        if (cachedIcons.containsKey(iconDimension)) {
            return cachedIcons.get(iconDimension);
        }

        ImageIcon icon = loadIcon(iconDimension);
        cachedIcons.put(iconDimension, icon);

        return icon;
    }

    private ImageIcon loadIcon(int iconDimension) {
        String path = String.format(iconsPathFormat, name());
        ImageIcon imageIcon = null;
        try {
            Image image = ImageIO.read(getClass().getResource(path));
            image = image.getScaledInstance(iconDimension, iconDimension, SCALE_SMOOTH);

            imageIcon = new ImageIcon(image);
        } catch (Exception ex) {
            String cn = getClass().getCanonicalName();
            LOGGER.error("Unable to find icon \"" + path + "\" near class: " + cn);
        }

        return imageIcon;
    }
}
