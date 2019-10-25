package pao.core.io.porygon;

import pao.core.exceptions.EditorIOException;
import pao.core.io.MapReader;
import pao.core.models.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Mark van der Wal
 * @since 15/02/18
 */
public class PorygonMapReader implements MapReader {

    private static final Logger LOGGER = LogManager.getLogger(PorygonMapReader.class);

    @Override
    public Map read(Path path) throws EditorIOException {
        if (path == null) {
            throw new EditorIOException("Specified path is null!");
        }

        if (!Files.exists(path)) {
            throw new EditorIOException(String.format("The map file does not exist: %s", path));
        }

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                String name = reader.readLine();
                int version = Integer.valueOf(reader.readLine());
                int width = Integer.valueOf(reader.readLine());
                int height = Integer.valueOf(reader.readLine());
                int tileSize = Integer.valueOf(reader.readLine());
                String tileDataString = reader.readLine();

                int[] collisionData = Stream.of(tileDataString.split(","))
                        .mapToInt(Integer::valueOf).toArray();

                ByteBuffer collisionBuffer = ByteBuffer.allocate(collisionData.length);
                Arrays.stream(collisionData).forEach(collisionBuffer::putInt);

                Map map = new Map(name, version, width, height);
                map.setTileSize(tileSize);
                map.setCollisionData(collisionBuffer.array());

                return map;
            }
        } catch (NumberFormatException e) {
            throw new EditorIOException(String.format("Could not read property from map file: %s\n%s", path, e.getMessage()));
        } catch (IOException e) {
            throw new EditorIOException(String.format("Could not read map file: %s", path));
        }
    }
}
