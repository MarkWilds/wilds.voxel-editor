package com.wildrune.rune.util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

/**
 * @author Mark "Wilds" van der Wal
 * @since 5-3-2018
 */
public final class Textures {
    private static final ColorModel alphaColorModel;
    private static final ColorModel opaqueColorModel;

    static {
        alphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[]{8, 8, 8, 8},
                true,
                false,
                ComponentColorModel.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        opaqueColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[]{8, 8, 8, 0},
                false,
                false,
                ComponentColorModel.OPAQUE,
                DataBuffer.TYPE_BYTE);
    }

    public static BufferedImage loadImage(String path) throws IOException {
        BufferedInputStream imageStream = new BufferedInputStream(Textures.class.getResourceAsStream(path));
        return ImageIO.read(imageStream);
    }

    public static ByteBuffer convertImage(BufferedImage image) {
        BufferedImage convertedImage;
        WritableRaster imageRaster;
        ColorModel colorModel = opaqueColorModel;
        int bands = 3;

        if (image.getColorModel().hasAlpha()) {
            bands = 4;
            colorModel = alphaColorModel;
        }

        imageRaster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(),
                bands, null);
        convertedImage = new BufferedImage(colorModel, imageRaster, false, new Hashtable<>());

        // draw the image in the correct format
        Graphics graphics = convertedImage.getGraphics();
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        // create bytebuffer from converted image
        byte[] imageBytes = ((DataBufferByte) convertedImage.getRaster().getDataBuffer()).getData();
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(imageBytes.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(imageBytes, 0, imageBytes.length);
        imageBuffer.flip();

        return imageBuffer;
    }
}
