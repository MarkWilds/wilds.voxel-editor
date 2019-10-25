package pao.core.services;

import pao.core.logic.commands.ICommand;
import pao.core.models.events.CommandEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Mark "Wilds" van der Wal
 * @since 31-1-2018
 *
 * todo: should implement a circular buffer for undo and redo stacks with max size to save memory.
 */
public class CommandService {

    private final static Logger LOGGER = LogManager.getLogger(CommandService.class);

    private Deque<ICommand> undoStack;
    private Deque<ICommand> redoStack;

    private BehaviorSubject<CommandEvent> onCommand;

    public CommandService() {
        undoStack = new ArrayDeque<>();
        redoStack = new ArrayDeque<>();

        onCommand = BehaviorSubject.create();
    }

    public Subscription onCommand(Action1<CommandEvent> action) {
        return onCommand.subscribe(action);
    }

    private void notifyObservers(CommandEvent.Type type, String text, int undoCount, int redoCount) {
        CommandEvent event = new CommandEvent(type, text, undoCount, redoCount);

        LOGGER.debug(text);

        onCommand.onNext(event);
    }

    public void executeCommand(ICommand command) {
        if (command.execute()) {
            undoStack.push(command);

            notifyObservers(CommandEvent.Type.Execute, command.getDescription(), undoStack.size(), redoStack.size());
        }
    }

    public void clear() {
        if (undoStack.size() > 0 || redoStack.size() > 0) {
            undoStack.clear();
            redoStack.clear();

            notifyObservers(CommandEvent.Type.Cleared, "Cleared command history", 0, 0);
        }
    }

    public void undo() {
        if (undoStack.size() > 0) {
            ICommand command = undoStack.pop();
            command.undo();
            redoStack.push(command);

            notifyObservers(CommandEvent.Type.Undo, command.getDescription(), undoStack.size(), redoStack.size());
        } else {
            LOGGER.debug("The undo stack is empty");
        }
    }

    public void redo() {
        if (redoStack.size() > 0) {
            ICommand command = redoStack.pop();
            command.execute();
            undoStack.push(command);

            notifyObservers(CommandEvent.Type.Redo, command.getDescription(), undoStack.size(), redoStack.size());
        } else {
            LOGGER.debug("The redo stack is empty");
        }
    }
}
