<%@ page contentType="text/javascript; charset=UTF-8" session="false" isELIgnored="true" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("script", request, response)) { return; } %>/* ============================================================
 * Kompatibilitaets-Layer fuer alte inline-Handler (openNav, ...)
 * Die neue Dropdown-Logik liegt komplett in userlist.js.
 * ============================================================ */

function openNav(username) {
    if (typeof openUserDropdown === "function") {
        openUserDropdown(username);
    }
}

function closeNav(username) {
    if (typeof closeAllUserDropdowns === "function") {
        closeAllUserDropdowns();
    }
}

function closeAllNavs() {
    if (typeof closeAllUserDropdowns === "function") {
        closeAllUserDropdowns();
    }
}
