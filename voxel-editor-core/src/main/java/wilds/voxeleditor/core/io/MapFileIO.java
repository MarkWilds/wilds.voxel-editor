package wilds.voxeleditor.core.io;

import wilds.voxeleditor.core.models.Map;

import java.nio.file.Path;

/**
 * @author Mark "Wilds" van der Wal
 * @since 17-2-2018
 */
public interface MapFileIO extends MapReader, MapWriter {

    String getDescription();

    String getExtension();

    Path getWorkingDirectory();

    Path createMapPath(Map map);
}
