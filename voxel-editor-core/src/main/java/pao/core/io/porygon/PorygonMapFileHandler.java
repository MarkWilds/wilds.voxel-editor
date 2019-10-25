package pao.core.io.porygon;

import pao.core.exceptions.EditorIOException;
import pao.core.io.MapFileIO;
import pao.core.models.Map;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Mark "Wilds" van der Wal
 * @since 17-2-2018
 */
public class PorygonMapFileHandler implements MapFileIO {

    private final PorygonMapReader mapReader;
    private final PorygonMapWriter mapWriter;

    public PorygonMapFileHandler() {
        mapReader = new PorygonMapReader();
        mapWriter = new PorygonMapWriter();
    }

    @Override
    public String getDescription() {
        return "Porygon map file";
    }

    @Override
    public String getExtension() {
        return "pmap";
    }

    @Override
    public Path getWorkingDirectory() {
        return Paths.get("").toAbsolutePath().normalize();
    }

    @Override
    public Path createMapPath(Map map) {
        String fileWithExtension = String.format("%s.%s", map.getName(), getExtension());
        return getWorkingDirectory().resolve(Paths.get(fileWithExtension));
    }

    @Override
    public void write(Path path, Map map) throws EditorIOException {
        mapWriter.write(path, map);
    }

    @Override
    public Map read(Path path) throws EditorIOException {
        return mapReader.read(path);
    }
}
