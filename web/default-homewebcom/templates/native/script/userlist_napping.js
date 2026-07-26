/* ============================================================
 * Nickliste (rechte Seite) - vollstaendiges Redesign
 * Gruppiert nach Rolle, mit Suchfeld, Dropdown-Aktionen
 * und Animationen fuer join/leave.
 * ============================================================ */

var roomName = "";
var roomIsLocked = "0";
var roomIsStandard = "0";
var roomNoSmilies = "0";
var roomTopic = "";
var nickName = "";
var sessionId = "";
var user = new Array();
var collapsedGroups = {};
var currentFilter = "";
var frameInitialized = false;

/* ---------- i18n (skriptintern, Deutsch/Englisch) ---------- */
/* Sprache wird ueber die Plattform-Sprachvariable gesteuert:
   - window.userlistLang (vom Template gesetzt, z.B. "de")
   - sonst das "lang"-Cookie (von ?lang= / Sprachmenue gesetzt)
   - sonst "en" (Default, englische "_napping"-Variante). */
function detectUserlistLang() {
    if (typeof window.userlistLang !== "undefined" && window.userlistLang) {
        return window.userlistLang;
    }
    if (typeof userlistLang !== "undefined" && userlistLang) {
        return userlistLang;
    }
    try {
        const m = document.cookie.match(/(?:^|;\s*)lang=([^;]+)/);
        if (m && m[1]) return m[1];
    } catch (e) {}
    return "en";
}

var userlistLang = detectUserlistLang();

var USERLIST_I18N = {
    de: {
        "group.admin": "Team",
        "group.mod": "Superuser",
        "group.registered": "Chatter",
        "group.guest": "G\u00E4ste",
        "role.admin": "Admin",
        "role.mod": "Mod",
        "role.registered": "User",
        "role.guest": "Gast",
        "filter.placeholder": "Nicks filtern...",
        "filter.clear": "Filter loeschen",
        "empty": "Noch keine Chatter im Raum",
        "counter.one": " Chatter",
        "counter.many": " Chatter",
        "room.locked": "Abgeschlossen",
        "room.open": "Offen",
        "room.prefix": "Raum: ",
        "actions.title": "Aktionen fuer ",
        "away.title": "Abgemeldet: ",
        "birthday.title": "Hat heute Geburtstag",
        "menu.profile": "Profil anzeigen",
        "menu.query": "Privatchat starten",
        "menu.moderation": "Moderation",
        "menu.su": "Superuser-Rechte vergeben",
        "menu.rsu": "Superuser-Rechte nehmen",
        "menu.kick": "Chatter wegkicken",
        "menu.gag": "Chatter knebeln",
        "menu.ungag": "Chatter entknebeln",
        "menu.eject": "Rauswurf",
        "menu.hk": "Chatter rauswerfen",
        "menu.bantemp": "1 Stunde verbannen",
        "menu.ban": "Dauerhaft verbannen"
    },
    en: {
        "group.admin": "Team",
        "group.mod": "Superuser",
        "group.registered": "Chatter",
        "group.guest": "Guests",
        "role.admin": "Admin",
        "role.mod": "Mod",
        "role.registered": "User",
        "role.guest": "Guest",
        "filter.placeholder": "Filter nicks...",
        "filter.clear": "Clear filter",
        "empty": "No chatters in the room yet",
        "counter.one": " chatter",
        "counter.many": " chatters",
        "room.locked": "Locked",
        "room.open": "Open",
        "room.prefix": "Room: ",
        "actions.title": "Actions for ",
        "away.title": "Away: ",
        "birthday.title": "Has birthday today",
        "menu.profile": "Show profile",
        "menu.query": "Start private chat",
        "menu.moderation": "Moderation",
        "menu.su": "Grant superuser rights",
        "menu.rsu": "Revoke superuser rights",
        "menu.kick": "Kick chatter",
        "menu.gag": "Gag chatter",
        "menu.ungag": "Ungag chatter",
        "menu.eject": "Eject",
        "menu.hk": "Throw out chatter",
        "menu.bantemp": "Ban for 1 hour",
        "menu.ban": "Ban permanently"
    }
};

function t(key) {
    var dict = USERLIST_I18N[userlistLang] || USERLIST_I18N.de;
    return (dict[key] !== undefined) ? dict[key] : (USERLIST_I18N.de[key] !== undefined ? USERLIST_I18N.de[key] : key);
}

/* ---------- Hilfsfunktionen ---------- */
function escapeHtml(s) {
    if (s === null || s === undefined) return "";
    return String(s).replace(/[&<>"']/g, function (c) {
        switch (c) {
            case "&": return "&amp;";
            case "<": return "&lt;";
            case ">": return "&gt;";
            case '"': return "&quot;";
            case "'": return "&#39;";
        }
        return c;
    });
}

function cssEscape(s) {
    if (window.CSS && window.CSS.escape) {
        return window.CSS.escape(s);
    }
    return String(s).replace(/([^\w-])/g, "\\$1");
}

function eachNode(list, fn) {
    for (let i = 0; i < list.length; i++) {
        fn(list[i], i);
    }
}

function getUserObj(nick) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick === nick) {
            return user[i];
        }
    }
    return null;
}

function getUserElement(nick) {
    return document.querySelector('[data-nick="' + cssEscape(nick) + '"]');
}

/* ---------- Basis-Setter (vom Server aufgerufen) ---------- */
function setOwnNickName(nick) { nickName = nick; }
function setTopic(topic)     { roomTopic = topic; ensureFrame(); refreshRoomInfo(); }

function setRoom(room, isLocked, isStandard, noSmilies, topic) {
    roomName       = room;
    roomIsLocked   = isLocked;
    roomIsStandard = isStandard;
    roomNoSmilies  = noSmilies;
    roomTopic      = topic || "";
    ensureFrame();
    refreshRoomInfo();
}

function setLock(isLocked) {
    roomIsLocked = isLocked;
    ensureFrame();
    refreshRoomInfo();
}

/* ---------- User-Verwaltung ---------- */
function addUser(nick, gender, registered, color, status, away, awayReason, gag) {
    if (getUser(nick)) {
        updateUser(nick, { gender: gender, registered: registered, color: color,
                           status: status, away: away, awayReason: awayReason, gag: gag });
        return;
    }
    user[user.length] = {
        nick: nick,
        gender: gender,
        registered: registered,
        color: color,
        status: status,
        away: away,
        awayReason: awayReason,
        gag: gag,
        birthday: getUserObj(nick) ? getUserObj(nick).birthday : ""
    };
    ensureFrame();
    insertUserDOM(user[user.length - 1]);
    updateGroupCount(getUserGroup(user[user.length - 1]));
    updateCounter();
    updateEmptyState();
    applyFilter();
}

function removeUser(nick) {
    let idx = -1;
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick === nick) { idx = i; break; }
    }
    if (idx === -1) return;
    const removed = user[idx];
    user.splice(idx, 1);
    removeUserDOM(nick, getUserGroup(removed));
    updateCounter();
    updateEmptyState();
    applyFilter();
}

function getUser(nick) {
    return getUserObj(nick) !== null;
}

function nick_change(nick, new_nick) {
    const u = getUserObj(nick);
    if (!u) return;
    u.nick = new_nick;
    const el = getUserElement(nick);
    if (el) {
        el.setAttribute("data-nick", new_nick);
        const nameEl = el.querySelector(".user-list-nick-text");
        if (nameEl) nameEl.textContent = new_nick;
        const dd = el.querySelector(".user-list-dropdown");
        if (dd) dd.id = "user-dropdown-" + cssEscape(new_nick);
    }
}

function removeAllUsers() {
    user = new Array();
    if (!frameInitialized) return;
    ["admin", "mod", "registered", "guest"].forEach(function (g) {
        const body = document.querySelector('[data-group-body="' + g + '"]');
        if (body) body.innerHTML = "";
        updateGroupCount(g);
    });
    updateCounter();
    updateEmptyState();
}

function setBirthday(nick, birthday) {
    const u = getUserObj(nick);
    if (!u) return;
    u.birthday = birthday;
    const el = getUserElement(nick);
    if (!el) return;
    const nickEl = el.querySelector(".user-list-nick");
    let bdayEl = el.querySelector(".user-list-birthday");
    if (birthday == "1" && !bdayEl) {
        bdayEl = document.createElement("span");
        bdayEl.className = "user-list-birthday";
        bdayEl.title = t("birthday.title");
        bdayEl.textContent = "\uD83C\uDF82";
        nickEl.appendChild(bdayEl);
    } else if (birthday != "1" && bdayEl) {
        bdayEl.remove();
    }
}

function setColor(nick, color) {
    const u = getUserObj(nick);
    if (!u) return;
    u.color = color;
    const el = getUserElement(nick);
    if (el) {
        const nameEl = el.querySelector(".user-list-nick-text");
        if (nameEl) nameEl.style.color = "#" + color;
    }
}

function setStatus(nick, status) {
    const u = getUserObj(nick);
    if (!u) return;
    const oldGroup = getUserGroup(u);
    u.status = status;
    const newGroup = getUserGroup(u);
    if (oldGroup !== newGroup) {
        const el = getUserElement(nick);
        if (el) el.remove();
        insertUserDOM(u);
        updateGroupCount(oldGroup);
        updateGroupCount(newGroup);
    } else {
        refreshUserElement(nick);
    }
}

function setRegistered(nick, registered) {
    const u = getUserObj(nick);
    if (!u) return;
    const oldGroup = getUserGroup(u);
    u.registered = registered;
    const newGroup = getUserGroup(u);
    if (oldGroup !== newGroup) {
        const el = getUserElement(nick);
        if (el) el.remove();
        insertUserDOM(u);
        updateGroupCount(oldGroup);
        updateGroupCount(newGroup);
    } else {
        refreshUserElement(nick);
    }
}

function setGender(nick, gender) {
    const u = getUserObj(nick);
    if (!u) return;
    u.gender = gender;
    refreshUserElement(nick);
}

function updateUser(nick, fields) {
    const u = getUserObj(nick);
    if (!u) return;
    const oldGroup = getUserGroup(u);
    if (fields.gender !== undefined)       u.gender = fields.gender;
    if (fields.registered !== undefined)   u.registered = fields.registered;
    if (fields.color !== undefined)          u.color = fields.color;
    if (fields.status !== undefined)         u.status = fields.status;
    if (fields.away !== undefined)         u.away = fields.away;
    if (fields.awayReason !== undefined)   u.awayReason = fields.awayReason;
    if (fields.gag !== undefined)          u.gag = fields.gag;
    const newGroup = getUserGroup(u);
    if (oldGroup !== newGroup) {
        const el = getUserElement(nick);
        if (el) el.remove();
        insertUserDOM(u);
        updateGroupCount(oldGroup);
        updateGroupCount(newGroup);
    } else {
        refreshUserElement(nick);
    }
}

function addAway(nick, awayReason) {
    const u = getUserObj(nick);
    if (!u) return;
    u.away = "1";
    u.awayReason = awayReason;
    refreshUserElement(nick);
}

function removeAway(nick) {
    const u = getUserObj(nick);
    if (!u) return;
    u.away = "0";
    u.awayReason = "";
    refreshUserElement(nick);
}

function addGag(nick) {
    const u = getUserObj(nick);
    if (!u) return;
    u.gag = "1";
    refreshUserElement(nick);
}

function setSessionId(sid) { sessionId = sid; }

function removeGag(nick) {
    const u = getUserObj(nick);
    if (!u) return;
    u.gag = "0";
    refreshUserElement(nick);
}

function isRegistered(nick) {
    const u = getUserObj(nick);
    return u !== null && u.registered == "1";
}

function getOwnStatus() {
    const u = getUserObj(nickName);
    if (!u) return 0;
    const s = parseInt(u.status, 10);
    return isNaN(s) ? 0 : s;
}

/* ---------- Symbole & Rollen ---------- */
function getUserSymbol(re, ge) {
    if (re == "0") return "\uD83D\uDC64";
    if (ge == "m")  return "\u2642";
    if (ge == "f")  return "\u2640";
    if (ge == "-")  return "\u00B7";
    return "?";
}

function getUserGroup(u) {
    const s = parseInt(u.status, 10) || 0;
    if (s >= 5) return "admin";
    if (s >= 3) return "mod";
    if (u.registered == "1") return "registered";
    return "guest";
}

function getUserGroupLabel(group) {
    switch (group) {
        case "admin":      return t("group.admin");
        case "mod":        return t("group.mod");
        case "registered": return t("group.registered");
        case "guest":      return t("group.guest");
    }
    return group;
}

function getUserRoleClass(u) {
    const g = getUserGroup(u);
    return g;
}

function getUserRoleBadge(u) {
    const g = getUserGroup(u);
    if (g === "admin")      return t("role.admin");
    if (g === "mod")        return t("role.mod");
    if (g === "registered") return t("role.registered");
    return t("role.guest");
}

/* ---------- Dropdown-Inhalt ---------- */
function buildUserMenu(un, st, ga) {
    let menu = "";
    const isReg = isRegistered(un);
    const own   = getOwnStatus();
    const isSelf = (un === nickName);

    if (isReg) {
        const profileUrl = "?page=%path_profile%&user=" + encodeURIComponent(un) +
                           "&skin=%skin%&owner=%owner%";
        menu += `<a href="javascript:showChatPanel('${profileUrl}','${t("menu.profile")}: ${un}');"><span class="icon">\uD83D\uDC64</span>${t("menu.profile")}</a>`;
    }

    if (!isSelf) {
        menu += `<a href="#" data-act="query"><span class="icon">\uD83D\uDCAC</span>${t("menu.query")}</a>`;
    }

    if (own >= 3 && !isSelf) {
        menu += `<div class="divider"></div><div class="group-label">${t("menu.moderation")}</div>`;
        if (st == 1) {
            menu += `<a href="#" data-act="su"><span class="icon">\u2B50</span>${t("menu.su")}</a>`;
        }
        if (st == 3) {
            menu += `<a href="#" data-act="rsu"><span class="icon">\uD83D\uDEAB</span>${t("menu.rsu")}</a>`;
        }
        if (own >= st) {
            menu += `<a href="#" data-act="k"><span class="icon">\uD83D\uDC62</span>${t("menu.kick")}</a>`;
            if (ga == 1) {
                menu += `<a href="#" data-act="ungag"><span class="icon">\uD83D\uDD0A</span>${t("menu.ungag")}</a>`;
            } else {
                menu += `<a href="#" data-act="gag"><span class="icon">\uD83D\uDD07</span>${t("menu.gag")}</a>`;
            }
        }
    }
    if (own >= 5 && !isSelf) {
        menu += `<div class="divider"></div><div class="group-label">${t("menu.eject")}</div>`;
        menu += `<a href="#" data-act="hk" class="danger"><span class="icon">\uD83D\uDEAA</span>${t("menu.hk")}</a>`;
        menu += `<a href="#" data-act="bantemp" class="danger"><span class="icon">\u23F1</span>${t("menu.bantemp")}</a>`;
    }
    if (own >= 9 && !isSelf) {
        menu += `<a href="#" data-act="ban" class="danger"><span class="icon">\u26D4</span>${t("menu.ban")}</a>`;
    }
    return menu;
}

function actionCommandFor(act, nick) {
    switch (act) {
        case "query":   return "/query " + nick;
        case "su":      return "/su " + nick;
        case "rsu":     return "/rsu " + nick;
        case "k":       return "/k " + nick;
        case "gag":     return "/gag " + nick;
        case "ungag":   return "/gag " + nick;
        case "hk":      return "/hk " + nick;
        case "bantemp": return "/ban -t 3600 " + nick;
        case "ban":     return "/ban " + nick;
    }
    return null;
}

/* ---------- DOM-Aufbau ---------- */
function buildUserItem(u) {
    const un = u.nick;
    const st = u.status;
    const ga = u.gag;
    const ge = u.gender;
    const re = u.registered;
    const co = u.color;
    const as = u.away;
    const sr = u.awayReason;
    const birthday = u.birthday;
    const role      = getUserRoleClass(u);
    const symbol    = getUserSymbol(re, ge);
    const menu      = buildUserMenu(un, st, ga);

    const nickTextStyle = "color: #" + escapeHtml(co) + ";";
    let nameHtml = `<span class="user-list-nick-text" style="${nickTextStyle}">${escapeHtml(un)}</span>`;
    if (st >= 3) {
        nameHtml = `<strong>${nameHtml}</strong>`;
    }

    const nickClasses = ["user-list-nick"];
    if (as == 1) nickClasses.push("away");
    const itemClasses = ["user-list-item"];
    if (ga == 1) itemClasses.push("gag");
    if (as == 1) itemClasses.push("away");
    const itemClass = itemClasses.join(" ");
    const nickClass = nickClasses.join(" ");

    const awayTitle = as == 1 ? t("away.title") + escapeHtml(sr || "") : t("actions.title") + escapeHtml(un);
    const bdayHtml  = birthday == 1
        ? `<span class="user-list-birthday" title="${t("birthday.title")}">\uD83C\uDF82</span>`
        : "";

    const roleBadge = `<span class="badge ${role}">${getUserRoleBadge(u)}</span>`;

    return `
        <div class="${itemClass}" data-nick="${escapeHtml(un)}" data-role="${role}">
            <span class="user-list-symbol ${role}" title="${escapeHtml(role)}">${symbol}</span>
            <span class="${nickClass}" title="${awayTitle}">${nameHtml}${bdayHtml}</span>
             <button class="user-list-toggle" type="button" aria-label="${t("actions.title")}${escapeHtml(un)}" aria-expanded="false">\u22EE</button>
            <div class="user-list-dropdown" id="user-dropdown-${cssEscape(un)}" data-nick="${escapeHtml(un)}">
                <div class="user-list-dropdown-header">${roleBadge}<span class="name">${escapeHtml(un)}</span></div>
                ${menu}
            </div>
        </div>
    `;
}

function ensureFrame() {
    if (frameInitialized) return;
    const host = document.getElementById("right");
    if (!host) return;
    if (host.querySelector(".user-list-container")) {
        frameInitialized = true;
        return;
    }

    host.innerHTML = `
        <div class="user-list-container" id="user-list-container">
            <div class="user-list-room-info" id="user-list-room-info"></div>
            <div class="user-list-search" id="user-list-search">
                <input type="text" id="user-list-filter" placeholder="${t("filter.placeholder")}" autocomplete="off" spellcheck="false">
                <button type="button" class="clear-btn" id="user-list-filter-clear" aria-label="${t("filter.clear")}">\u00D7</button>
            </div>
            <div class="user-list-scroll" id="user-list-scroll">
                <div class="user-list-empty" id="user-list-empty">
                     <span class="emoji">\uD83D\uDC4B</span>
                     ${t("empty")}
                </div>
                <div class="user-list-group" data-group="admin">
                    <div class="user-list-group-header" data-toggle="admin">
                        <span class="group-label"><span class="caret">\u25BC</span> \u2B50 <span class="lbl">${getUserGroupLabel("admin")}</span></span>
                        <span class="user-list-group-count zero" data-group-count="admin">0</span>
                    </div>
                    <div class="user-list-group-body" data-group-body="admin"></div>
                </div>
                <div class="user-list-group" data-group="mod">
                    <div class="user-list-group-header" data-toggle="mod">
                        <span class="group-label"><span class="caret">\u25BC</span> \uD83D\uDEE1 <span class="lbl">${getUserGroupLabel("mod")}</span></span>
                        <span class="user-list-group-count zero" data-group-count="mod">0</span>
                    </div>
                    <div class="user-list-group-body" data-group-body="mod"></div>
                </div>
                <div class="user-list-group" data-group="registered">
                    <div class="user-list-group-header" data-toggle="registered">
                        <span class="group-label"><span class="caret">\u25BC</span> \uD83D\uDC65 <span class="lbl">${getUserGroupLabel("registered")}</span></span>
                        <span class="user-list-group-count zero" data-group-count="registered">0</span>
                    </div>
                    <div class="user-list-group-body" data-group-body="registered"></div>
                </div>
                <div class="user-list-group" data-group="guest">
                    <div class="user-list-group-header" data-toggle="guest">
                        <span class="group-label"><span class="caret">\u25BC</span> \uD83D\uDC64 <span class="lbl">${getUserGroupLabel("guest")}</span></span>
                        <span class="user-list-group-count zero" data-group-count="guest">0</span>
                    </div>
                    <div class="user-list-group-body" data-group-body="guest"></div>
                </div>
            </div>
            <div class="user-list-counter" id="user-list-counter">0${t("counter.many")}</div>
        </div>
    `;

    /* Event-Wiring (nur einmalig) */
    host.classList.add("user-list-ready");

    const filterInput = document.getElementById("user-list-filter");
    if (filterInput) {
        filterInput.addEventListener("input", function (e) {
            onFilterInput(e.target.value);
        });
        filterInput.addEventListener("keydown", function (e) {
            if (e.key === "Escape") {
                filterInput.value = "";
                onFilterInput("");
                filterInput.blur();
            }
        });
    }
    const clearBtn = document.getElementById("user-list-filter-clear");
    if (clearBtn) {
        clearBtn.addEventListener("click", function () {
            const f = document.getElementById("user-list-filter");
            if (f) {
                f.value = "";
                onFilterInput("");
                f.focus();
            }
        });
    }
    eachNode(document.querySelectorAll(".user-list-group-header"), function (h) {
        h.addEventListener("click", function () {
            const g = h.getAttribute("data-toggle");
            toggleGroup(g);
        });
    });

    /* Delegated Click-Handler fuer Dropdown-Toggle und Aktionen */
    const scroll = document.getElementById("user-list-scroll");
    if (scroll) {
        scroll.addEventListener("click", function (e) {
            const toggle = e.target.closest(".user-list-toggle");
            if (toggle) {
                e.stopPropagation();
                const item = toggle.closest(".user-list-item");
                if (item) openUserDropdown(item.getAttribute("data-nick"));
                return;
            }
            const nickSpan = e.target.closest(".user-list-nick");
            if (nickSpan) {
                const item = nickSpan.closest(".user-list-item");
                if (item) openUserDropdown(item.getAttribute("data-nick"));
                return;
            }
            const actionLink = e.target.closest(".user-list-dropdown a[data-act]");
            if (actionLink) {
                e.preventDefault();
                const dd = actionLink.closest(".user-list-dropdown");
                const nick = dd ? dd.getAttribute("data-nick") : null;
                const act  = actionLink.getAttribute("data-act");
                if (nick && act) {
                    const cmd = actionCommandFor(act, nick);
                    if (cmd && typeof submitTextMessage === "function") {
                        submitTextMessage(cmd);
                    }
                }
                closeAllUserDropdowns();
                return;
            }
        });
    }

    /* Globaler Click-Handler schliesst Dropdowns */
    document.addEventListener("click", function (e) {
        if (e.target.closest(".user-list-dropdown")) return;
        if (e.target.closest(".user-list-toggle")) return;
        if (e.target.closest(".user-list-nick")) return;
        closeAllUserDropdowns();
    });

    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            closeAllUserDropdowns();
        }
    });

    frameInitialized = true;
    refreshRoomInfo();
}

function refreshRoomInfo() {
    const el = document.getElementById("user-list-room-info");
    if (!el) return;
    const isLocked = (roomIsLocked == "1");
    const statusClass = isLocked ? "locked" : "open";
    const statusText  = isLocked ? t("room.locked") : t("room.open");
    let html = `<div class="user-list-room-name">` +
               `<span class="user-list-status-dot ${statusClass}" title="${statusText}"></span>` +
               `${t("room.prefix")}<span>${escapeHtml(roomName || "")}</span>` +
               ` <span class="user-list-status-label ${statusClass}">${statusText}</span>` +
               `</div>`;
    if (roomTopic && roomTopic.length > 0) {
        html += `<div class="user-list-topic">\uD83D\uDCCC ${escapeHtml(roomTopic)}</div>`;
    }
    el.innerHTML = html;
}

function insertUserDOM(u) {
    const group = getUserGroup(u);
    const body  = document.querySelector('[data-group-body="' + group + '"]');
    if (!body) return;
    const wrap = document.createElement("div");
    wrap.innerHTML = buildUserItem(u).trim();
    const el = wrap.firstChild;
    if (!el) return;
    el.classList.add("entering");
    body.appendChild(el);
    setTimeout(function () { el.classList.remove("entering"); }, 600);
}

function removeUserDOM(nick, group) {
    const el = getUserElement(nick);
    if (!el) return;
    el.classList.add("leaving");
    const finish = function () {
        el.remove();
        updateGroupCount(group);
    };
    setTimeout(finish, 320);
}

function refreshUserElement(nick) {
    const u = getUserObj(nick);
    if (!u) return;
    const oldEl = getUserElement(nick);
    if (!oldEl) return;
    const wrap = document.createElement("div");
    wrap.innerHTML = buildUserItem(u).trim();
    const newEl = wrap.firstChild;
    if (!newEl) return;
    oldEl.parentNode.replaceChild(newEl, oldEl);
}

function updateGroupCount(group) {
    const body   = document.querySelector('[data-group-body="' + group + '"]');
    const countEl = document.querySelector('[data-group-count="' + group + '"]');
    if (body && countEl) {
        const n = body.querySelectorAll(".user-list-item").length;
        countEl.textContent = n;
        countEl.classList.toggle("zero", n === 0);
    }
}

function updateCounter() {
    const el = document.getElementById("user-list-counter");
    if (el) {
        const n = user.length;
        el.textContent = n + (n === 1 ? t("counter.one") : t("counter.many"));
    }
}

function updateEmptyState() {
    const empty = document.getElementById("user-list-empty");
    if (empty) {
        empty.style.display = user.length === 0 ? "" : "none";
    }
}

function toggleGroup(group) {
    const el = document.querySelector('[data-group="' + group + '"]');
    if (!el) return;
    el.classList.toggle("collapsed");
    collapsedGroups[group] = el.classList.contains("collapsed");
}

function openUserDropdown(nick) {
    if (!nick) return;
    const target = document.getElementById("user-dropdown-" + cssEscape(nick));
    if (!target) return;
    const item = target.parentElement;
    const btn  = item ? item.querySelector(".user-list-toggle") : null;
    const wasOpen = target.classList.contains("open");
    closeAllUserDropdowns();
    if (wasOpen) return;

    target.classList.remove("flip-up");
    target.classList.add("open");
    if (btn) {
        btn.classList.add("active");
        btn.setAttribute("aria-expanded", "true");
    }

    /* Bei Platzmangel nach oben kippen */
    requestAnimationFrame(function () {
        const rect = target.getBoundingClientRect();
        if (rect.bottom > window.innerHeight - 8) {
            target.classList.add("flip-up");
        }
    });
}

function closeAllUserDropdowns() {
    eachNode(document.querySelectorAll(".user-list-dropdown.open"), function (d) {
        d.classList.remove("open");
        d.classList.remove("flip-up");
        const item = d.parentElement;
        if (item) {
            const btn = item.querySelector(".user-list-toggle");
            if (btn) {
                btn.classList.remove("active");
                btn.setAttribute("aria-expanded", "false");
            }
        }
    });
}

function onFilterInput(value) {
    currentFilter = String(value || "").toLowerCase().trim();
    const wrap = document.getElementById("user-list-search");
    if (wrap) wrap.classList.toggle("has-value", currentFilter.length > 0);
    applyFilter();
}

function applyFilter() {
    eachNode(document.querySelectorAll(".user-list-item"), function (el) {
        const nick = el.getAttribute("data-nick") || "";
        if (!currentFilter || nick.toLowerCase().indexOf(currentFilter) !== -1) {
            el.style.display = "";
        } else {
            el.style.display = "none";
        }
    });
}

function drawFrame() {
    ensureFrame();
    /* Alle Gruppen leeren und neu befuellen */
    ["admin", "mod", "registered", "guest"].forEach(function (g) {
        const body = document.querySelector('[data-group-body="' + g + '"]');
        if (body) body.innerHTML = "";
    });
    user.forEach(function (u) {
        const group = getUserGroup(u);
        const body  = document.querySelector('[data-group-body="' + group + '"]');
        if (!body) return;
        const wrap = document.createElement("div");
        wrap.innerHTML = buildUserItem(u).trim();
        body.appendChild(wrap.firstChild);
    });
    refreshRoomInfo();
    ["admin", "mod", "registered", "guest"].forEach(updateGroupCount);
    updateCounter();
    updateEmptyState();
    applyFilter();
}

/* Initialen Frame aufbauen, sobald das Script geladen ist */
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", function () { ensureFrame(); });
} else {
    ensureFrame();
}
