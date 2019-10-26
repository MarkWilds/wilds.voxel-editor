package pao.core.models;

import wilds.rune.geometry.Area;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * @author Mark "Wilds" van der Wal
 * @since 23-2-2018
 */
public class Tileset {
    private String name;
    private BufferedImage image;

    private Area area;
    private int tileSize;

    public Tileset(BufferedImage image, String name, int width, int height, int tileSize) {
        this.name = name;
        this.image = image;

        area = new Area();
        area.grow(0, 0);
        area.grow(width, height);

        this.tileSize = tileSize;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Area getArea() {
        return area;
    }

    public int getWidth() {
        return (int) area.getWidth();
    }

    public int getHeight() {
        return (int) area.getHeight();
    }

    public int getTileSize() {
        return tileSize;
    }

    public void toTilesetCoordinates(Vector2i point) {
        point.x = (int) Math.floor(point.x / tileSize);
        point.y = (int) Math.floor(point.y / tileSize);
    }

    public void toTilesetCoordinates(Vector2f point) {
        point.x = (float) Math.floor(point.x / tileSize);
        point.y = (float) Math.floor(point.y / tileSize);
    }

    public Point toTilesetCoordinates(float x, float y) {
        return new Point((int) Math.floor(x / tileSize), (int) Math.floor(y / tileSize));
    }

    public void toTilesetCoordinates(Area area) {
        toTilesetCoordinates(area.getMin());
        toTilesetCoordinates(area.getMax());
    }

    public void keepAreaWithinBounds(Area area) {
        keepPointWithinBounds(area.getMin());
        keepPointWithinBounds(area.getMax());
    }

    public void keepPointWithinBounds(Vector2f point) {
        point.x = Math.max(0, Math.min(getWidth() - 1, point.x));
        point.y = Math.max(0, Math.min(getHeight() - 1, point.y));
    }
}
