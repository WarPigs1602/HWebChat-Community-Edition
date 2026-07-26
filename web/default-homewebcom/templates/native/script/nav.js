/* ============================================================
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
