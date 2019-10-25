package pao.core.logic.commands;

/**
 * @author Mark "Wilds" van der Wal
 * @since 1-2-2018
 */
public interface ICommand {
    String getDescription();

    /**
     * Execute a command
     *
     * @return true if command was executed
     */
    boolean execute();

    void undo();
}
