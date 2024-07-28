/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.midiandmore.chat;

public class UserAgentDetails {

    private final String browserName;
    private final String browserVersion;
    private final String browserComments;

      UserAgentDetails(String browserName, String browserVersion, String browserComments) {
        this.browserName = browserName;
        this.browserVersion = browserVersion;
        this.browserComments = browserComments;
    }

    public String getBrowserComments() {
        return browserComments;
    }

    public String getBrowserName() {
        return browserName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

}
