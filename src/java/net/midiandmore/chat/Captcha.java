package net.midiandmore.chat;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static java.awt.Font.BOLD;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.max;
import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.util.logging.Level.WARNING;
import static javax.imageio.ImageIO.createImageOutputStream;
import static javax.imageio.ImageIO.getImageWritersByFormatName;
import static javax.imageio.ImageWriteParam.MODE_EXPLICIT;
import static net.midiandmore.chat.ErrorLog.LOG;

public class Captcha implements Software {
    private Bootstrap master;

    public Captcha(Bootstrap master) {
        setMaster(master);
    }

    protected void drawCaptcha(HttpServletRequest request, HttpServletResponse response) {
        var cm = getMaster().getChatManager();
        var imageFormat = "jpeg";

        try {
            var backgroundColor = new Color(paramInt("captcha_bgcolor_rr"), paramInt("captcha_bgcolor_gg"), paramInt("captcha_bgcolor_bb"));
            var borderColor = new Color(paramInt("captcha_bdcolor_rr"), paramInt("captcha_bdcolor_gg"), paramInt("captcha_bdcolor_bb"));
            var textColor = new Color(paramInt("captcha_txcolor_rr"), paramInt("captcha_txcolor_gg"), paramInt("captcha_txcolor_bb"));
            var circleColor = new Color(paramInt("captcha_cicolor_rr"), paramInt("captcha_cicolor_gg"), paramInt("captcha_cicolor_bb"));
            var textFont = new Font(paramString("captcha_font_type"), BOLD, paramInt("captcha_font_size"));
            var charsToPrint = paramInt("captcha_chars_to_print");
            var width = paramInt("captcha_width");
            var height = paramInt("captcha_height");
            var circlesToDraw = paramInt("captcha_circles_to_draw");

            var noiseLines = paramInt("captcha_noise_lines", 5);
            var noiseDots = paramInt("captcha_noise_dots", 80);
            var textShadow = paramInt("captcha_text_shadow", 2);

            var bufferedImage = new BufferedImage(width, height, TYPE_INT_RGB);
            var g = (Graphics2D) bufferedImage.getGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            var darkerBg = backgroundColor.darker();

            var gradient = new GradientPaint(0, 0, backgroundColor, width, height, darkerBg);
            g.setPaint(gradient);
            g.fillRect(0, 0, width, height);

            g.setColor(circleColor);
            for (var i = 0; i < circlesToDraw; i++) {
                var circleRadius = (int) (random() * height / 3.0 + height / 10.0);
                var circleX = (int) (random() * width - circleRadius);
                var circleY = (int) (random() * height - circleRadius);
                g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
            }

            g.setColor(new Color(circleColor.getRed(), circleColor.getGreen(), circleColor.getBlue(), 120));
            for (var i = 0; i < noiseLines; i++) {
                var x1 = (int) (random() * width);
                var y1 = (int) (random() * height);
                var x2 = (int) (random() * width);
                var y2 = (int) (random() * height);
                var cx1 = (int) (random() * width);
                var cy1 = (int) (random() * height);
                var cx2 = (int) (random() * width);
                var cy2 = (int) (random() * height);

                var path = new Path2D.Double();
                path.moveTo(x1, y1);
                path.curveTo(cx1, cy1, cx2, cy2, x2, y2);
                g.draw(path);
            }

            g.setColor(new Color(circleColor.getRed(), circleColor.getGreen(), circleColor.getBlue(), 100));
            for (var i = 0; i < noiseLines / 2; i++) {
                g.drawLine((int) (random() * width), (int) (random() * height), (int) (random() * width), (int) (random() * height));
            }

            g.setColor(new Color(circleColor.getRed(), circleColor.getGreen(), circleColor.getBlue(), 180));
            for (var i = 0; i < noiseDots; i++) {
                var dotSize = (int) (random() * 3 + 1);
                var dotX = (int) (random() * width);
                var dotY = (int) (random() * height);
                g.fillOval(dotX, dotY, dotSize, dotSize);
            }

            g.setColor(textColor);
            g.setFont(textFont);

            var fontMetrics = g.getFontMetrics();
            var maxAdvance = fontMetrics.getMaxAdvance();
            var fontHeight = fontMetrics.getHeight();
            var elegibleChars = paramString("captcha_chars");
            var chars = elegibleChars.toCharArray();
            var spaceForLetters = width - 40;
            var spacePerChar = spaceForLetters / (charsToPrint - 1.0f);
            var finalString = new StringBuilder();

            for (var i = 0; i < charsToPrint; i++) {
                var randomIndex = (int) round(random() * (chars.length - 1));
                var characterToShow = chars[randomIndex];
                finalString.append(characterToShow);

                var charWidth = fontMetrics.charWidth(characterToShow);
                var charDim = max(maxAdvance, fontHeight);
                var halfCharDim = (charDim / 2);

                var charImage = new BufferedImage(charDim + textShadow * 2, charDim + textShadow * 2, TYPE_INT_ARGB);
                var charGraphics = charImage.createGraphics();

                charGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                charGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                charGraphics.setFont(textFont);

                var shadowColor = new Color(max(0, textColor.getRed() - 80), max(0, textColor.getGreen() - 80), max(0, textColor.getBlue() - 80));

                if (textShadow > 0) {
                    charGraphics.setColor(shadowColor);
                    for (var sx = -textShadow; sx <= textShadow; sx++) {
                        for (var sy = -textShadow; sy <= textShadow; sy++) {
                            if (sx != 0 || sy != 0) {
                                charGraphics.drawString("" + characterToShow, halfCharDim - charWidth / 2 + sx, halfCharDim - fontMetrics.getAscent() / 2 + fontMetrics.getAscent() + sy);
                            }
                        }
                    }
                }

                charGraphics.setColor(textColor);
                charGraphics.drawString("" + characterToShow, halfCharDim - charWidth / 2, halfCharDim - fontMetrics.getAscent() / 2 + fontMetrics.getAscent());

                var angle = (random() - 0.5) * 1.2;
                var x = 20 + spacePerChar * i - (charDim + textShadow * 2) / 2.0f;
                var y = (height - charDim - textShadow * 2) / 2;

                var transform = AffineTransform.getRotateInstance(angle, (charDim + textShadow * 2) / 2.0, (charDim + textShadow * 2) / 2.0);
                var rotatedImage = new BufferedImage(charDim + textShadow * 2, charDim + textShadow * 2, TYPE_INT_ARGB);
                var rotatedGraphics = rotatedImage.createGraphics();
                rotatedGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                rotatedGraphics.drawImage(charImage, transform, null);
                rotatedGraphics.dispose();

                g.drawImage(rotatedImage, (int) x, (int) y, charDim + textShadow * 2, charDim + textShadow * 2, null, null);
                charGraphics.dispose();
            }

            g.setColor(borderColor);
            g.drawRect(0, 0, width - 1, height - 1);

            Iterator iter = getImageWritersByFormatName(imageFormat);

            if (iter.hasNext()) {
                var writer = (ImageWriter) iter.next();
                var iwp = writer.getDefaultWriteParam();

                if (imageFormat.equalsIgnoreCase("jpg") || imageFormat.equalsIgnoreCase("jpeg")) {
                    iwp.setCompressionMode(MODE_EXPLICIT);
                    iwp.setCompressionQuality(0.95f);
                }

                writer.setOutput(createImageOutputStream(response.getOutputStream()));

                var imageIO = new IIOImage(bufferedImage, null, null);

                response.setContentType("image/" + imageFormat);
                response.setStatus(200);
                writer.write(null, imageIO, iwp);
            } else {
                LOG.log(WARNING, "No encoder found...");
            }

            cm.updateCaptcha(finalString.toString().toLowerCase(), request.getParameter("cid"));
            g.dispose();
        } catch (IOException ioe) {
            LOG.log(WARNING, "Unable to build image:", ioe);
        }
    }

    protected String paramString(String paramName) {
        return getMaster().getConfig().getString(paramName);
    }

    protected int paramInt(String paramName) {
        return getMaster().getConfig().getInt(paramName);
    }

    protected int paramInt(String paramName, int defaultValue) {
        var value = getMaster().getConfig().getString(paramName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Bootstrap getMaster() {
        return master;
    }

    private void setMaster(Bootstrap master) {
        this.master = master;
    }
}
