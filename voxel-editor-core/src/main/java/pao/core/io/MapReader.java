package pao.core.io;

import pao.core.exceptions.EditorIOException;
import pao.core.models.Map;

import java.nio.file.Path;

/**
 * @author Mark van der Wal
 * @since 15/02/18
 */
public interface MapReader {

    Map read(Path path) throws EditorIOException;
}
