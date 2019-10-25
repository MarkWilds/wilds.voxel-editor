package pao.editor.models;

/**
 * @author Mark "Wilds" van der Wal
 * @since 19-2-2018
 */
public class MapInformation {
    private String mapName;
    private int mapWidth;
    private int mapHeight;

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }
}
