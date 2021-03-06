package wilds.voxeleditor.core.io;

import wilds.voxeleditor.core.exceptions.EditorIOException;
import wilds.voxeleditor.core.models.Map;

import java.nio.file.Path;

/**
 * @author Mark van der Wal
 * @since 15/02/18
 */
public interface MapWriter {

    void write(Path path, Map map) throws EditorIOException;
}
