package pao.core.services;

import pao.core.models.Tileset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * @author Mark van der Wal
 * @since 14/04/18
 */
public class TilesetService {

    private HashMap<String, Tileset> tilesetHashMap;

    public TilesetService() {
        tilesetHashMap = new HashMap<>();
    }

    public Tileset loadTileset(File file, int tileSize) throws IOException {
        Path filePath = file.toPath();
        BufferedImage image = ImageIO.read(file);

        return new Tileset(image, filePath.getFileName().toString(), image.getWidth(), image.getHeight(), tileSize);
    }
}
