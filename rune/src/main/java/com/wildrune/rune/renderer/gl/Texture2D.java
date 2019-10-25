package com.wildrune.rune.renderer.gl;

import com.wildrune.rune.renderer.IBindable;
import com.wildrune.rune.renderer.IDisposable;
import com.wildrune.rune.renderer.gl.states.SamplerState;
import com.wildrune.rune.renderer.jogl.JoglEs2Context;
import com.wildrune.rune.util.Textures;

import com.jogamp.opengl.GL2ES2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * @author Mark "Wilds" van der Wal
 * @since 5-3-2018
 */
public class Texture2D extends JoglEs2Context implements IBindable, IDisposable {

    private static final Logger LOGGER = LogManager.getLogger(Texture2D.class);

    private int id;
    private int width;
    private int height;
    private SamplerState state;

    public Texture2D(int id, int width, int height, SamplerState state) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.state = state;
    }

    public static Texture2D create2D(GL2ES2 gl, BufferedImage image, SamplerState state) {
        if (image == null) {
            LOGGER.error(String.format("Image was null! Texture could not be created for %s", image));
            return null;
        }

        int[] temp = {0};
        int target = gl.GL_TEXTURE_2D;
        int dstFormat = gl.GL_RGBA;
        int srcFormat = image.getColorModel().hasAlpha() ? gl.GL_RGBA : gl.GL_RGB;

        gl.glGenTextures(1, temp, 0);

        if (temp[0] <= 0) {
            LOGGER.error(String.format("Could not created OpenGL resource for: %s", image));
        }

        ByteBuffer imageBuffer = Textures.convertImage(image);

        Texture2D texture2D = new Texture2D(temp[0], image.getWidth(), image.getHeight(), state);
        texture2D.setGL(gl);
        texture2D.bind(0);

        SamplerState.activateState(gl, state);

        gl.glTexImage2D(target, 0, dstFormat, image.getWidth(), image.getHeight(),
                0, srcFormat, gl.GL_UNSIGNED_BYTE, imageBuffer);

        texture2D.unbind();


        return texture2D;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void bind() {
        gl.glBindTexture(gl.GL_TEXTURE_2D, id);
    }

    public void bind(int slot) {
        gl.glActiveTexture(gl.GL_TEXTURE0 + slot);
        bind();
    }

    @Override
    public void unbind() {
        gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
    }

    @Override
    public void dispose() {
        int[] temp = {id};
        gl.glDeleteTextures(1, temp, 0);
    }
}
