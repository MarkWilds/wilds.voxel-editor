package pao.editor.utils;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.*;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * @author Mark "Wilds" van der Wal
 * @since 7-1-2018
 */
@Plugin(name = "LogReceiverAppender", category = "Core", elementType = "appender", printObject = true)
public class LogReceiverAppender extends AbstractAppender {
    private static volatile List<ILogReceiver> logReceivers;

    protected LogReceiverAppender(String name, Filter filter,
                                  Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);

        logReceivers = new ArrayList<>();
    }

    @PluginFactory
    public static AbstractAppender createAppender(@PluginAttribute("name") String name,
                                                  @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                  @PluginElement("Filter") final Filter filter,
                                                  @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            LOGGER.error("No name provided for LogReceiverAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new LogReceiverAppender(name, filter, layout, true);

    }

    public static void addReceiver(final ILogReceiver receiver) {
        logReceivers.add(receiver);
    }

    @Override
    public void append(LogEvent logEvent) {
        byte[] layoutBytes = getLayout().toByteArray(logEvent);
        final String message = new String(layoutBytes);

        SwingUtilities.invokeLater(() -> {
            logReceivers.stream().forEach(logReceiver -> {
                logReceiver.receiveLog(message);
            });
        });
    }
}
