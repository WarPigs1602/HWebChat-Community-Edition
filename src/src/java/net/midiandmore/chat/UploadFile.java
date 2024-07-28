/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.midiandmore.chat;

import java.io.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import static net.midiandmore.chat.Bootstrap.boot;

/**
 *
 * @author windo
 */
@MultipartConfig
public class UploadFile extends HttpServlet {

    @Override
    public void init() {
    }

    @Override
    public void doPost(jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response)
            throws ServletException, java.io.IOException {
        var partAttr = request.getPart("file");
        var is = partAttr.getInputStream();
        var sb = new StringBuilder();
        var co = boot.getConfig();
        sb.append(co.getUh());
        sb.append(co.getFs());
        sb.append(".homewebcom");
        sb.append(co.getFs());
        sb.append("temp");
        sb.append(co.getFs());
        var files = new File(sb.toString() + partAttr.getSubmittedFileName());
        var contentType = partAttr.getContentType();
        response.setContentType("text/html; charset=" + co.getString("charset"));
        var out = response.getWriter();
        var cs = boot.getChatServices();
        var map2 = request.getParameterMap();
        Map<String, String> map = new HashMap<>();
        for (var key : map2.keySet()) {
            var value = map2.get(key);
            map.put(key, value[0]);
        }
        try {

            var db = co.getDb();
            var nick = (String) request.getSession().getAttribute("nick");
            var community = request.getSession().getAttribute("community") != null;
            if (!contentType.startsWith("image/")) {
                if (community) {
                    cs.printTemplate("picture_not_picture_com", request, response, map);
                } else {
                    cs.printTemplate("picture_not_picture", request, response, map);
                }
                return;
            }
            if (nick != null && db.isRegistered(nick)) {
                var outputStream = new FileOutputStream(files);
                var read = 0;
                var bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                db.updatePicture(nick, partAttr.getInputStream(), contentType);
                if (community) {
                    cs.printTemplate("picture_success_com", request, response, map);
                } else {
                    cs.printTemplate("picture_success", request, response, map);
                }
            } else {
                if (community) {
                    cs.printTemplate("picture_not_registered_com", request, response, map);
                } else {
                    cs.printTemplate("picture_not_registered", request, response, map);
                }
            }

        } catch (IOException ex) {
            var err = cs.getTemplate("picture_error", request, map);
            err = err.replace("%message%", ex.getLocalizedMessage());
            out.println(err);
        }
    }

    @Override
    public void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, java.io.IOException {

        throw new ServletException("GET method used with "
                + getClass().getName() + ": POST method required.");
    }

}
