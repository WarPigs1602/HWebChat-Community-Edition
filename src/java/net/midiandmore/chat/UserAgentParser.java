/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.midiandmore.chat;

import static java.lang.Float.valueOf;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;

public class UserAgentParser {

    private final String userAgentString;
    private String browserName;
    private String browserVersion;
    private String browserOperatingSystem;
    private final List<UserAgentDetails> parsedBrowsers = new ArrayList<>();

    private static final Pattern pattern = compile(
            "([^/\\s]*)(/([^\\s]*))?(\\s*\\[[a-zA-Z][a-zA-Z]\\])?"
            + "\\s*(\\((([^()]|(\\([^()]*\\)))*)\\))?\\s*");

    /**
     * Parses the incoming user agent string into useful data about the browser
     * and its operating system.
     *
     * @param userAgentString the user agent header from the browser.
     */
    public UserAgentParser(String userAgentString) {
        this.userAgentString = userAgentString;
        var matcher = pattern.matcher(userAgentString);

        while (matcher.find()) {
            /*
            for(int i=0; i< matcher.groupCount(); i++) {
                System.err.println(i +": " + matcher.group(i));
            }
             */
            var nextBrowserName = matcher.group(1);
            var nextBrowserVersion = matcher.group(3);
            String nextBrowserComments = null;
            if (matcher.groupCount() >= 6) {
                nextBrowserComments = matcher.group(6);
            }
            parsedBrowsers.add(new UserAgentDetails(nextBrowserName,
                    nextBrowserVersion, nextBrowserComments));

        }

        if (!parsedBrowsers.isEmpty()) {
            processBrowserDetails();
        } else {

        }

    }

    /**
     * Wraps the process of extracting browser name, version, and operating
     * sytem.
     */
    private void processBrowserDetails() {

        var browserNameAndVersion = extractBrowserNameAndVersion();
        browserName = browserNameAndVersion[0];
        browserVersion = browserNameAndVersion[1];

        browserOperatingSystem = extractOperatingSystem(parsedBrowsers.get(0).getBrowserComments());

    }

    private String[] extractBrowserNameAndVersion() {

        var knownBrowsers = new String[]{
            "firefox", "netscape", "edge", "edg" ,"chrome", "safari", "camino", "mosaic", "opera",
            "galeon", "msie"
        };
        for (var nextBrowser : parsedBrowsers) {
            for (var nextKnown : knownBrowsers) {
                if (nextBrowser.getBrowserName().toLowerCase().startsWith(nextKnown)) {
                    return new String[]{nextBrowser.getBrowserName(), nextBrowser.getBrowserVersion()};
                }
                // TODO might need special case here for Opera's dodgy version
            }

        }
        var firstAgent = parsedBrowsers.get(0);
        if (firstAgent.getBrowserName().toLowerCase().startsWith("mozilla")) {

            if (firstAgent.getBrowserComments() != null) {
                var comments = firstAgent.getBrowserComments().split(";");
                if (comments.length > 2 && comments[0].toLowerCase().startsWith("compatible")) {
                    var realBrowserWithVersion = comments[1].trim();
                    var firstSpace = realBrowserWithVersion.indexOf(' ');
                    var firstSlash = realBrowserWithVersion.indexOf('/');
                    if ((firstSlash > -1 && firstSpace > -1)
                            || (firstSlash > -1 && firstSpace == -1)) {
                        // we have slash and space, or just a slash,
                        // so let's choose slash for the split
                        return new String[]{
                            realBrowserWithVersion.substring(0, firstSlash),
                            realBrowserWithVersion.substring(firstSlash + 1)
                        };
                    } else if (firstSpace > -1) {
                        return new String[]{
                            realBrowserWithVersion.substring(0, firstSpace),
                            realBrowserWithVersion.substring(firstSpace + 1)
                        };
                    } else { // out of ideas for version, or no version supplied
                        return new String[]{realBrowserWithVersion, null};
                    }
                }
            }

            // Looks like a *real* Mozilla :-)
            if (valueOf(firstAgent.getBrowserVersion()) < 5.0) {
                return new String[]{"Netscape", firstAgent.getBrowserVersion()};
            } else {
                // TODO: get version from comment string
                return new String[]{"Mozilla",
                    firstAgent.getBrowserComments().split(";")[0].trim()};
            }
        } else {
            return new String[]{
                firstAgent.getBrowserName(), firstAgent.getBrowserVersion()
            };
        }

    }

    private String extractOperatingSystem(String comments) {

        if (comments == null) {
            return null;
        }

        var knownOS = new String[]{"win", "linux", "mac", "freebsd", "netbsd",
            "openbsd", "sunos", "amiga", "beos", "irix", "os/2", "warp", "iphone", "android"};
        List<String> osDetails = new ArrayList<>();
        var parts = comments.split(";");
        for (var comment : parts) {
            var lowerComment = comment.toLowerCase().trim();
            for (var os : knownOS) {
                if (lowerComment.startsWith(os)) {
                    osDetails.add(comment.trim());
                }
            }

        }
        switch (osDetails.size()) {
            case 0 -> {
                return null;
            }
            case 1 -> {
                return osDetails.get(0);
            }
            default -> {
                return osDetails.get(0);
            }
        }

    }       

    public String getBrowserName() {
        return browserName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public String getBrowserOperatingSystem() {
        return browserOperatingSystem;
    }

}
