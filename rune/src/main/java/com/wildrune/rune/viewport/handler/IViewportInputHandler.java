package com.wildrune.rune.viewport.handler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * @author Mark "Wilds" van der Wal
 * @since 23-1-2018
 */
public interface IViewportInputHandler {

    void mousePressed(MouseEvent e);

    void mouseReleased(MouseEvent e);

    void mouseEntered(MouseEvent e);

    void mouseExited(MouseEvent e);

    void mouseWheelMoved(MouseWheelEvent e);

    void mouseDragged(MouseEvent e);

    void mouseMoved(MouseEvent e);

    void keyDown(KeyEvent keyEvent);

    void keyUp(KeyEvent keyEvent);
}
