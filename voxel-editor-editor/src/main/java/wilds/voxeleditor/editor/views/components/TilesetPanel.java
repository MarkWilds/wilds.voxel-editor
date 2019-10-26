package wilds.voxeleditor.editor.views.components;

import wilds.voxeleditor.core.models.Tileset;

import wilds.rune.geometry.Area;

import com.alee.laf.panel.WebPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

/**
 * @author Mark "Wilds" van der Wal
 * @since 21-2-2018
 */
public class TilesetPanel extends WebPanel {

    private static final Logger LOGGER = LogManager.getLogger(TilesetPanel.class);

    private final Color disabledColor = Color.GRAY;
    private final Color checkerColorA = Color.WHITE;
    private final Color checkerColorB = Color.LIGHT_GRAY;
    private final Color cursorColor = Color.red;

    private final int selectorThickness = 6;
    private final float hotAreaAlpha = 0.5f;

    private TileSelectionHandler selectionHandler;
    private Tileset tileset;
    private boolean showHotArea = false;

    private BiConsumer<Tileset, Area> onTileSelected;

    public TilesetPanel(Tileset tileset) {
        this.selectionHandler = new TileSelectionHandler();
        this.tileset = tileset;

        setMinimumWidth(tileset.getWidth());
        setMinimumHeight(tileset.getHeight());

        addMouseListener(selectionHandler);
        addMouseMotionListener(selectionHandler);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        drawBackground(graphics2D);

        graphics2D.drawImage(tileset.getImage(), 0, 0, null);

        if (showHotArea) {
            drawCursor(graphics2D);
        }
        drawSelector(graphics2D);
    }

    private void drawBackground(Graphics2D graphics) {
        int tileSize = tileset.getTileSize();
        int checkerSize = tileSize / 2;
        int checkerWidth = (int) Math.ceil(tileset.getWidth() / checkerSize);
        int checkerHeight = (int) Math.ceil(tileset.getHeight() / checkerSize);

        for (int y = 0; y < checkerHeight; y++) {
            for (int x = 0; x < checkerWidth; x++) {

                Color currentColor = checkerColorA;
                if (x % 2 == y % 2) {
                    currentColor = checkerColorB;
                }

                graphics.setColor(currentColor);
                graphics.fillRect(x * checkerSize, y * checkerSize, checkerSize, checkerSize);
            }
        }
    }

    private void drawCursor(Graphics2D graphics) {
        Composite originalComposite = graphics.getComposite();

        Area area = selectionHandler.getHotArea();
        int tileSize = tileset.getTileSize();
        Vector2f min = area.getMin();
        Vector2f max = area.getMax();

        int startX = (int) (min.x * tileSize);
        int startY = (int) (min.y * tileSize);
        int areaTileX = (int) ((max.x - min.x + 1) * tileSize);
        int areaTileY = (int) ((max.y - min.y + 1) * tileSize);

        graphics.setColor(cursorColor);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hotAreaAlpha));
        graphics.fillRect(startX, startY, areaTileX, areaTileY);
        graphics.setComposite(originalComposite);
    }

    private void drawSelector(Graphics2D graphics) {
        Area area = selectionHandler.getSelectedArea();
        int tileSize = tileset.getTileSize();

        Vector2f min = area.getMin();
        Vector2f max = area.getMax();

        int startX = (int) (min.x * tileSize);
        int startY = (int) (min.y * tileSize);

        int endX = (int) ((max.x + 1) * tileSize);
        int endY = (int) ((max.y + 1) * tileSize);

        int areaTileX = endX - startX;
        int areaTileY = endY - startY;

        // top, bottom, left & right bars
        graphics.setColor(Color.WHITE);
        graphics.fillRect(startX, startY, areaTileX, selectorThickness);
        graphics.fillRect(startX, endY - selectorThickness, areaTileX, selectorThickness);
        graphics.fillRect(startX, startY, selectorThickness, areaTileY);
        graphics.fillRect(endX - selectorThickness, startY, selectorThickness, areaTileY);

        // borders
        int startThickness = selectorThickness - 1;
        int endThickness = selectorThickness * 2 - 1;

        graphics.setColor(Color.BLACK);
        graphics.drawRect(startX, startY, areaTileX - 1, areaTileY - 1);
        graphics.drawRect(startX + startThickness, startY + startThickness,
                areaTileX - endThickness, areaTileY - endThickness);
    }

    public void setOnTileSelectedHandler(BiConsumer<Tileset, Area> tileSelecedHandler) {
        onTileSelected = tileSelecedHandler;
    }

    private void setArea(Area area, float... points) {
        area.regenerate(points);
        tileset.keepAreaWithinBounds(area);
        tileset.toTilesetCoordinates(area);
    }

    private class TileSelectionHandler extends MouseAdapter {

        private Point startMousePosition;
        private Area hotArea;
        private Area selectedArea;

        private TileSelectionHandler() {
            startMousePosition = new Point();
            hotArea = new Area(0, 0);
            selectedArea = new Area(0, 0);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            startMousePosition.setLocation(e.getX(), e.getY());
            setArea(hotArea, startMousePosition.x, startMousePosition.y);

            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            setArea(hotArea, startMousePosition.x, startMousePosition.y, e.getX(), e.getY());

            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            setArea(hotArea, e.getX(), e.getY());
            setArea(selectedArea, startMousePosition.x, startMousePosition.y, e.getX(), e.getY());

            showHotArea = tileset.getArea().intersects(e.getX(), e.getY());

            if (onTileSelected != null) {
                onTileSelected.accept(tileset, selectedArea);
            }

            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            setArea(hotArea, e.getX(), e.getY());

            showHotArea = tileset.getArea().intersects(e.getX(), e.getY());

            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            showHotArea = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            showHotArea = false;
            repaint();
        }

        private Area getHotArea() {
            return hotArea;
        }

        private Area getSelectedArea() {
            return selectedArea;
        }
    }
}
