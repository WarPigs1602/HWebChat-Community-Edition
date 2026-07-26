package net.midiandmore.chat;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static java.awt.Font.BOLD;
import static java.awt.geom.AffineTransform.getRotateInstance;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.max;
import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.util.logging.Level.WARNING;
import static javax.imageio.ImageIO.createImageOutputStream;
import static javax.imageio.ImageIO.getImageWritersByFormatName;
import static javax.imageio.ImageWriteParam.MODE_EXPLICIT;
import static net.midiandmore.chat.ErrorLog.LOG;

/**
 * Ein einfacher Captcha
 * Dies ist Fremdcode damit ich was funktionierendes habe, da ich noch nie mit
 * AWT-Klassen gearbeitet habe. Wird sp&auml;ter gegen eigenen Code ausgetauscht.
 * @author Andreas Pschorn
 */
public class Captcha implements Software {
    private Bootstrap master;

    /**
     * 
     * @param master
     */
    public Captcha(Bootstrap master) {
        setMaster(master);
    }

    /**
     * 
     * @param request
     * @param response
     */
    protected void drawCaptcha(HttpServletRequest request, HttpServletResponse response) {
        var cm = getMaster().getChatManager();
        var imageFormat = "jpeg";


        try {

            // you can pass in fontSize, width, height via the request
            var         backgroundColor = new Color(paramInt("captcha_bgcolor_rr"), paramInt("captcha_bgcolor_gg"), paramInt("captcha_bgcolor_bb"));
            var         borderColor     = new Color(paramInt("captcha_bdcolor_rr"), paramInt("captcha_bdcolor_gg"), paramInt("captcha_bdcolor_bb"));
            var         textColor       = new Color(paramInt("captcha_txcolor_rr"), paramInt("captcha_txcolor_gg"), paramInt("captcha_txcolor_bb"));
            var         circleColor     = new Color(paramInt("captcha_cicolor_rr"), paramInt("captcha_cicolor_gg"), paramInt("captcha_cicolor_bb"));
            var          textFont        = new Font(paramString("captcha_font_type"), BOLD, paramInt("captcha_font_size"));
            var           charsToPrint    = paramInt("captcha_chars_to_print");
            var           width           = paramInt("captcha_width");
            var           height          = paramInt("captcha_height");
            var           circlesToDraw   = paramInt("captcha_circles_to_draw");
            var         horizMargin     = 20.0f;
            var         imageQuality    = 0.95f;    // max is 1.0 (this is for jpeg)
            var        rotationRange   = 0.7;      // this is radians
            var bufferedImage   = new BufferedImage(width, height, TYPE_INT_RGB);
            var    g               = (Graphics2D) bufferedImage.getGraphics();

            g.setColor(backgroundColor);
            g.fillRect(0, 0, width, height);

            // lets make some noisey circles
            g.setColor(circleColor);

            for (var i = 0; i < circlesToDraw; i++) {
                var circleRadius = (int) (random() * height / 2.0);
                var circleX      = (int) (random() * width - circleRadius);
                var circleY      = (int) (random() * height - circleRadius);

                g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
            }

            g.setColor(textColor);
            g.setFont(textFont);

            var fontMetrics = g.getFontMetrics();
            var         maxAdvance  = fontMetrics.getMaxAdvance();
            var         fontHeight  = fontMetrics.getHeight();
            // i removed 1 and l and i because there are confusing to users...
            // Z, z, and N also get confusing when rotated
            // 0, O, and o are also confusing...
            // lowercase G looks a lot like a 9 so i killed it
            // this should ideally be done for every language...
            // i like controlling the characters though because it helps prevent confusion
            var          elegibleChars   = paramString("captcha_chars");
            var          chars           = elegibleChars.toCharArray();
            var           spaceForLetters = -horizMargin * 2 + width;
            var           spacePerChar    = spaceForLetters / (charsToPrint - 1.0f);
            var    finalString     = new StringBuilder();

            for (var i = 0; i < charsToPrint; i++) {
                var randomValue     = random();
                var    randomIndex     = (int) round(randomValue * (chars.length - 1));
                var   characterToShow = chars[randomIndex];

                finalString.append(characterToShow);

                // this is a separate canvas used for the character so that
                // we can rotate it independently
                var           charWidth       = fontMetrics.charWidth(characterToShow);
                var           charDim         = max(maxAdvance, fontHeight);
                var           halfCharDim     = (charDim / 2);
                var charImage       = new BufferedImage(charDim, charDim, TYPE_INT_ARGB);
                var    charGraphics    = charImage.createGraphics();

                charGraphics.translate(halfCharDim, halfCharDim);

                var angle = (random() - 0.5) * rotationRange;

                charGraphics.transform(getRotateInstance(angle));
                charGraphics.translate(-halfCharDim, -halfCharDim);
                charGraphics.setColor(textColor);
                charGraphics.setFont(textFont);

                var charX = (int) (0.5 * charDim - 0.5 * charWidth);

                charGraphics.drawString("" + characterToShow, charX, ((charDim - fontMetrics.getAscent()) / 2 + fontMetrics.getAscent()));

                var x = horizMargin + spacePerChar * (i) - charDim / 2.0f;
                var   y = ((height - charDim) / 2);

//              System.out.println("x=" + x + " height=" + height + " charDim=" + charDim + " y=" + y + " advance=" + maxAdvance + " fontHeight=" + fontHeight + " ascent=" + fontMetrics.getAscent());
                g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);
                charGraphics.dispose();
            }

            // let's do the border
            g.setColor(borderColor);
            g.drawRect(0, 0, width - 1, height - 1);

            // Write the image as a jpg
            Iterator iter = getImageWritersByFormatName(imageFormat);

            if (iter.hasNext()) {
                var     writer = (ImageWriter) iter.next();
                var iwp    = writer.getDefaultWriteParam();

                if (imageFormat.equalsIgnoreCase("jpg") || imageFormat.equalsIgnoreCase("jpeg")) {
                    iwp.setCompressionMode(MODE_EXPLICIT);
                    iwp.setCompressionQuality(imageQuality);
                }

                writer.setOutput(createImageOutputStream(response.getOutputStream()));

                var imageIO = new IIOImage(bufferedImage, null, null);

                response.setContentType("image/" + imageFormat);
                response.setStatus(200);
                writer.write(null, imageIO, iwp);
            } else {
                LOG.log(WARNING, "No encoder found...");
            }

            // let's stick the final string in the session
            cm.updateCaptcha(finalString.toString().toLowerCase(), request.getParameter("cid"));
            g.dispose();
        } catch (IOException ioe) {
            LOG.log(WARNING, "Unable to build image:", ioe);
        }
    }

    /**
     * 
     * @param paramName
     * @return
     */
    protected String paramString(String paramName) {
        return getMaster().getConfig().getString(paramName);
    }

    /**
     * 
     * @param paramName
     * @return
     */
    protected int paramInt(String paramName) {
        return getMaster().getConfig().getInt(paramName);
    }

    private Bootstrap getMaster() {
        return master;
    }

    private void setMaster(Bootstrap master) {
        this.master = master;
    }
}