package net.midiandmore.chat;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.MessageDigest;


public class BasicAuthenticationFilter {
    /** Logger */
    private static final Logger logger = Logger.getLogger(BasicAuthenticationFilter.class.getName());

    private String username = "";

    private String password = "";

    private String realm = "Protected";
    
    protected boolean error = false;
    
    protected String message = "";

    public void init(Bootstrap boot) throws ServletException {
        username = boot.getConfig().getP().getProperty("admin_username");
        password = boot.getConfig().getP().getProperty("admin_password");
        String paramRealm = boot.getConfig().getP().getProperty("admin_realm");
        if (paramRealm != null && paramRealm.length() > 0) {
            realm = paramRealm;
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.regionMatches(true, 0, "Basic ", 0, 6)
                || username == null || username.isBlank() || password == null || password.isEmpty()) {
            unauthorized(response);
            return;
        }
        try {
            var credentials = new String(Base64.getDecoder().decode(authHeader.substring(6).trim()), StandardCharsets.UTF_8);
            var separator = credentials.indexOf(':');
            if (separator < 0 || !MessageDigest.isEqual(username.getBytes(StandardCharsets.UTF_8),
                    credentials.substring(0, separator).getBytes(StandardCharsets.UTF_8))
                    || !MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
                    credentials.substring(separator + 1).getBytes(StandardCharsets.UTF_8))) {
                unauthorized(response, "Bad credentials");
            }
        } catch (IllegalArgumentException ex) {
            unauthorized(response, "Invalid authentication token");
        }

    }

    public void destroy() {
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\", charset=\"UTF-8\"");
        this.message = message;
        error = true;
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }

}
