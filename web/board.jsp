<%@ page session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.*, java.io.*, java.net.*, java.nio.file.*, java.sql.*, java.util.*, java.util.logging.*, jakarta.servlet.*, jakarta.servlet.http.*, jakarta.mail.*" %>
<%@ page import="static net.midiandmore.chat.Web.*" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%!
    private static String button(String style, String href, String title, String icon) {
        return "<a class=\"btn btn-outline-" + style + " btn-sm\" href=\"" + href + "\" title=\"" + title
                + "\"><i class=\"fa fa-" + icon + "\"></i></a>";
    }

    private static String pageItem(String board, String pid, String id, String page) {
        return "<li class=\"page-item\"><a class=\"page-link\" href=\"" + board + "&pid=" + pid + "&id=" + id + "&p="
                + page + "\">" + page + "</a></li>";
    }

    private static String pageActive(String page) {
        return "<li class=\"page-item active\" aria-current=\"page\"><span class=\"page-link\">" + page
                + "</span></li>";
    }

    private static String pageStep(String lang, String board, String pid, String id, String page, boolean next) {
        var query = pid == null ? "" : "&pid=" + pid;
        return "<li class=\"page-item\"><a class=\"page-link\" href=\"" + board + query + "&id=" + id + "&p=" + page
                + "\" aria-label=\"" + Web.message(lang, next ? "common.further_information" : "common.back")
                + "\"><i class=\"fa fa-chevron-" + (next ? "right" : "left") + "\"></i></a></li>";
    }

    private static String pageStepDisabled(String lang, boolean next) {
        return "<li class=\"page-item disabled\"><span class=\"page-link\" aria-label=\""
                + Web.message(lang, next ? "common.further_information" : "common.back")
                + "\"><i class=\"fa fa-chevron-" + (next ? "right" : "left") + "\"></i></span></li>";
    }

    private static String threadButton(String lang, String style, String board, Object[] boards, String action,
            String key, String icon) {
        return button(style, board + "&pid=" + boards[8] + "&id=" + boards[7] + "&" + action + "=true",
                Web.message(lang, key), icon);
    }

    private static String banButton(String lang, boolean banned, String board, Object uid) {
        var href = board + "&uid=" + uid + "&ban=true";
        return banned
                ? button("success", href, Web.message(lang, "page.boards_thread_unban.remove_users"), "check-circle")
                : button("danger", href, Web.message(lang, "page.boards_thread_ban.blocking_users"), "ban");
    }

    private static String categoryButton(String lang, String style, String board, String id, String action, String key,
            String icon) {
        return button(style, board + "&" + action + "=true&cat=" + id, Web.message(lang, key), icon);
    }

    private static String forumButton(String lang, String style, String board, Object[] boards, String action,
            String key, String icon) {
        return button(style, board + "&id=" + boards[5] + "&cat=" + boards[0] + "&" + action + "=true",
                Web.message(lang, key), icon);
    }

    private static String bannedBadge(String lang) {
        return "<span class=\"badge badge-danger\"><i class=\"fa fa-ban\"></i> "
                + Web.message(lang, "page.boards_thread_banned.banned") + "</span>";
    }

    // The previous page, the numbered pages and the next page
    private static String[] pagination(String lang, String board, String pid, String stepPid, String id,
            String pageNumber, long pages) {
        var items = new StringBuilder();
        for (long l = 1; l <= pages; l++) {
            if (l != Long.valueOf(pageNumber)) {
                items.append(pageItem(board, pid, id, String.valueOf(l)));
            } else {
                items.append(pageActive(String.valueOf(l)));
            }
            items.append(" ");
        }
        return new String[]{
            pageNumber.equals("1") ? pageStepDisabled(lang, false)
            : pageStep(lang, board, stepPid, id, String.valueOf(Long.valueOf(pageNumber) - 1), false),
            items.toString(),
            Long.valueOf(pageNumber) == pages ? pageStepDisabled(lang, true)
            : pageStep(lang, board, stepPid, id, String.valueOf(Long.valueOf(pageNumber) + 1), true)};
    }

    private static Map.Entry<String, Map<String, String>> notice(String screen, String style, String role, String icon,
            String prefix, String title, String text, String suffix, String link) {
        return Map.entry(screen, Map.of("style", style, "role", role, "icon", icon, "prefix", prefix, "title", title,
                "text", text, "suffix", suffix, "link", link));
    }

    private static final String OVERVIEW = "common.forum_overview";
    private static final String START = "common.start_page";

    // Pages that only show a message: alert style, alert role, icon, title prefix, title, text, text suffix, back link
    private static final Map<String, Map<String, String>> NOTICES = Map.ofEntries(
            notice("board_unknown", "danger", "alert", "frown-o", "", "common.error_2",
                    "page.board_unknown.the_requested_document_does_not", "", OVERVIEW),
            notice("nick_not_registered", "danger", "alert", "frown-o", "", "common.error_3",
                    "common.you_are_not_registered", "", ""),
            notice("board_add_moderator", "warning", "alert", "exclamation-triangle", "", "common.eligibility_missing",
                    "common.you_are_not_authorized_to", "", OVERVIEW),
            notice("boards_thread_moderator", "warning", "alert", "exclamation-triangle", "",
                    "common.eligibility_missing", "common.you_are_not_authorized_to", "", OVERVIEW),
            notice("board_added", "success", "status", "check-circle", "", "page.board_added.category_created",
                    "page.board_added.your_category_has_been_successfully", "", START),
            notice("board_added_board", "success", "status", "check-circle", "", "page.board_added_board.forum_created",
                    "page.board_added_board.the_forum_was_successfully_registered", "", START),
            notice("board_deleted_board", "danger", "status", "trash", "", "page.board_deleted_board.category_removed",
                    "page.board_deleted_board.the_category_was_successfully_removed", "", START),
            notice("board_edited_board", "success", "status", "pencil", "Forum ", "page.board_edited_board.modified",
                    "page.board_edited_board.the_forum_has_been_successfully", ".", START),
            notice("board_opened_board", "success", "status", "undo", "", "page.board_opened_board.category_restored",
                    "page.board_opened_board.the_category_was_successfully_restored", "", START),
            notice("boards_added", "success", "status", "check-circle", "", "page.boards_added.posting_successfully",
                    "page.boards_added.your_posting_was_successfully_entered", "", ""),
            notice("boards_ban", "danger", "status", "ban", "", "page.boards_ban.user_banned",
                    "page.boards_ban.they_were_banned_from_the", "", OVERVIEW),
            notice("boards_unban", "success", "status", "check-circle", "", "page.boards_unban.users_removed",
                    "page.boards_unban.get_back_into_the_community", "", OVERVIEW),
            notice("boards_close", "warning", "status", "lock", "", "common.forum_blocked",
                    "page.boards_close.the_closing_of_the_forum", ".", OVERVIEW),
            notice("boards_open", "success", "status", "unlock", "", "page.boards_open.forum_unlocked",
                    "page.boards_open.the_unlocking_of_the_forum", "", OVERVIEW),
            notice("boards_deleted", "danger", "status", "trash", "Forum ", "common.deleted",
                    "page.boards_deleted.deleting_the_forum_was_successful", ".", OVERVIEW),
            notice("boards_undel", "success", "status", "undo", "", "page.boards_undel.successful_recovery",
                    "page.boards_undel.the_restoration_of_the_forum", "", OVERVIEW),
            notice("boards_edited", "success", "status", "pencil", "", "page.boards_edited.forum_processed",
                    "page.boards_edited.the_forum_was_successful", "", OVERVIEW),
            notice("boards_locked", "warning", "alert", "lock", "", "common.forum_blocked",
                    "page.boards_locked.you_cannot_add_an_entry", ".", OVERVIEW),
            notice("boards_not_exists", "warning", "alert", "question-circle", "",
                    "page.boards_not_exists.thread_not_found", "page.boards_not_exists.the_requested_thread_does_not", "",
                    OVERVIEW),
            notice("boards_no_user", "warning", "alert", "user-times", "", "page.boards_no_user.user_not_found",
                    "page.boards_no_user.the_specified_user_does_not", "", OVERVIEW),
            notice("boards_thread_closed", "warning", "status", "lock", "", "page.boards_thread_closed.thread_closed",
                    "page.boards_thread_closed.this_entry_has_been_closed", "", OVERVIEW),
            notice("boards_thread_del", "danger", "status", "trash", "Thread ", "common.deleted",
                    "common.this_entry_has_been_deleted", ".", OVERVIEW),
            notice("boards_thread_edited", "success", "status", "check-circle", "",
                    "page.boards_thread_edited.contribution_stored",
                    "page.boards_thread_edited.the_contribution_was_successfully_processed", "", ""),
            notice("boards_thread_opened", "success", "status", "unlock", "Thread ",
                    "page.boards_thread_opened.reopened", "page.boards_thread_opened.this_entry_has_been_reopened", ".",
                    OVERVIEW),
            notice("boards_thread_undel", "success", "status", "undo", "", "page.boards_thread_undel.thread_restored",
                    "page.boards_thread_undel.this_entry_has_been_restored", "", OVERVIEW));
%><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    Map<String, String> map = Web.requestValues(request);
    var lang = (String) request.getAttribute("lang");
    var boardPath = String.valueOf(((Map<?, ?>) request.getAttribute("paths")).get("board"));
    String screen = null;
    List<Map<String, Object>> categories = null;
    List<Map<String, Object>> threadRows = null;
    var listing = false;
    String editorId = null;
        var session = request.getSession(false);
        var conf = Bootstrap.getBoot().getConfig();
        var cm = Bootstrap.getBoot().getChatManager();
        var ut = Bootstrap.getBoot().getUtil();
        TreeMap<String, UsersCommunity> com = cm.getUsersCommunity();
        if (session == null || com.get((String) session.getAttribute("nick")) == null) {
            screen = "timeout";
        } else {
        var db = conf.getDb();
        var nick = (String) session.getAttribute("nick");
        var moderator = db.getData(nick, "moderator").equals("1");
        com.get((String) session.getAttribute("nick")).setTimeoutTimer(0);
        var service = map.getOrDefault("service", "");
        var skin = (String) session.getAttribute("skin");

        skin = skin != null ? skin : map.getOrDefault("skin", "");
        skin = ut.parseHost(skin, request)[1];
        var reg = false;
        reg = db.isRegistered(nick);
        var bid = map.getOrDefault("id", "0");
        if (!bid.matches("\\d+")) {
            bid = "0";
        }
        var pid = map.getOrDefault("pid", "0");
        if (!pid.matches("\\d+")) {
            pid = "0";
        }
        var ref = map.getOrDefault("ref", "0");
        if (!ref.matches("\\d+")) {
            ref = "0";
        }
        var start = map.getOrDefault("new", "");
        var addBoard = map.getOrDefault("add_board", "");
        var addCat = map.getOrDefault("add_cat", "");
        var delCat = map.getOrDefault("del_cat", "");
        var editCat = map.getOrDefault("edit_cat", "");
        var delBoards = map.getOrDefault("delete_boards", "");
        var closeBoards = map.getOrDefault("close_boards", "");
        var editBoards = map.getOrDefault("edit_boards", "");
        var edit = map.getOrDefault("edit", "");
        var close = map.getOrDefault("close", "");
        var open = map.getOrDefault("open", "");
        var del = map.getOrDefault("del", "");
        var undel = map.getOrDefault("undel", "");
        var ban = map.getOrDefault("ban", "");
        var answer = map.getOrDefault("answer", "");
        var pageNumber = map.getOrDefault("p", "1");
        if (!pageNumber.matches("\\d+")) {
            pageNumber = "1";
        }
        var search = map.getOrDefault("search", "");
        var ip = request.getLocalAddr();
        String proxyIp = null;
        var realIp = request.getHeader("x-forwarded-for");
        realIp = realIp != null ? realIp : "";
        if (realIp.contains(", ")) {
            realIp = realIp.substring(0, realIp.indexOf(", "));
        }
        if (conf.getString("cloudflare").equals("1")) {
            ip = realIp;
            realIp = "";
        }
        try {
            realIp = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
        } catch (UnknownHostException e) {
        }
        if (conf.getString("use_proxy").equals("1")) {
            proxyIp = request.getHeader(conf.getString("real_ip"));
        }
        if (proxyIp != null && !proxyIp.equals("")) {
            ip = proxyIp;
        }
        String ipResolved = null;
        String ipResolved2 = null;
        try {
            ipResolved = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(ip).getCanonicalHostName() : ip;
        } catch (UnknownHostException uhe) {
            ipResolved = ip;
        }
        try {
            ipResolved2 = conf.getString("resolve_ip").equals("1") ? InetAddress.getByName(realIp).getCanonicalHostName() : realIp;
        } catch (UnknownHostException uhe) {
            ipResolved2 = realIp;
        }
        if (db.isBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(nick));
            screen = "banned";
        } else if (db.isBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ip));
            screen = "banned";
        } else if (db.isBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ipResolved));
            screen = "banned";
        } else if (db.isBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(ipResolved2));
            screen = "banned";
        } else if (db.isBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getBanReason(realIp));
            screen = "banned";
        } else if (db.isTimedBanned(nick) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getTimedBanReason(nick));
            screen = "banned_temp";
        } else if (db.isTimedBanned(ip) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getTimedBanReason(ip));
            screen = "banned_temp";
        } else if (db.isTimedBanned(ipResolved) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getTimedBanReason(ipResolved));
            screen = "banned_temp";
        } else if (db.isTimedBanned(ipResolved2) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getTimedBanReason(ipResolved2));
            screen = "banned_temp";
        } else if (db.isTimedBanned(realIp) && (!reg || (reg && Integer.valueOf(db.getData(nick, "status")) < conf.getInt("ignore_ban_status")))) {
            request.setAttribute("reason", db.getTimedBanReason(realIp));
            screen = "banned_temp";
        }
        if (screen == null && !reg) {
            screen = "nick_not_registered";
        } else if (screen == null && !service.isBlank()) {
            screen = "board_unknown";
        } else if (screen == null && !ban.isBlank()) {
            var uid = map.getOrDefault("uid", "0");
            if (!uid.matches("\\d+")) {
                uid = "0";
            }
            var target = db.getData(Long.valueOf(uid), "nick");
            var target2 = db.getData(Long.valueOf(uid), "nick2");
            var color = db.getData(target, "color");
            var banned = db.isBanned(target);
            if (!moderator) {
                screen = "boards_thread_moderator";
            } else if (target.isBlank()) {
                screen = "boards_no_user";
            } else if (banned) {
                db.delBans(nick, target2);
                screen = "boards_unban";
            } else {
                var cmd = Bootstrap.getBoot().getCommands();
                if (cm.isOnline(nick)) {
                    cmd.ban(target + " " + conf.getString("default_ban_reason_board"), nick);
                } else {
                    db.addBan(target2, conf.getString("default_ban_reason_board"), nick);
                }
                screen = "boards_ban";
            }
            pageContext.setAttribute("banColor", color);
            pageContext.setAttribute("banUser", db.getData(target, "nick2"));
        } else if (screen == null && !undel.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                screen = "boards_locked";
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "deleted", "0");
                screen = "boards_thread_undel";
            } else {
                screen = "boards_thread_moderator";
            }
        } else if (screen == null && !del.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                screen = "boards_locked";
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "deleted", "1");
                screen = "boards_thread_del";
            } else {
                screen = "boards_thread_moderator";
            }
        } else if (screen == null && !open.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                screen = "boards_locked";
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "closed", "0");
                screen = "boards_thread_opened";
            } else {
                screen = "boards_thread_moderator";
            }
        } else if (screen == null && !close.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                screen = "boards_locked";
            } else if (moderator) {
                db.updateThread(Long.valueOf(pid), "closed", "1");
                screen = "boards_thread_closed";
            } else {
                screen = "boards_thread_moderator";
            }
        } else if (screen == null && !answer.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                screen = "boards_locked";
            } else {
                ref = db.getBoard(Long.valueOf(pid), "ref");
                var newPostId = db.addThread(map.getOrDefault("topic", ""), map.getOrDefault("thread", ""), Long.valueOf(ref), Long.valueOf(db.getData(nick, "id")), 0, ip, Long.valueOf(bid));
                screen = "boards_added";
                pageContext.setAttribute("boardId", bid);
                pid = String.valueOf(newPostId);
                pageContext.setAttribute("threadId", pid);
            }
        } else if (screen == null && !editBoards.isBlank()) {
            var edited = map.getOrDefault("edited", "").equalsIgnoreCase("true");
            if (!moderator) {
                screen = "boards_locked";
            } else {
                if (edited) {
                    var topic = map.getOrDefault("topic", "");
                    var description = map.getOrDefault("description", "");
                    db.updateBoards(Long.valueOf(bid), "topic", topic);
                    db.updateBoards(Long.valueOf(bid), "description", description);
                    screen = "boards_edited";
                } else {
                    var topic = db.getBoards(Long.valueOf(bid), "topic");
                    var description = db.getBoards(Long.valueOf(bid), "description");
                    screen = "boards_edit";
                    editorId = "edit-description";
                    pageContext.setAttribute("boardId", bid);
                    pageContext.setAttribute("topic", ut.parseHtml(topic));
                    pageContext.setAttribute("description", ut.parseHtml(description));
                }
            }
        } else if (screen == null && !edit.isBlank()) {
            var edited = map.getOrDefault("edited", "").equalsIgnoreCase("true");
            var threadId = Long.valueOf(pid);
            var thread = db.getBoardThreadById(threadId);
            if (thread == null) {
                screen = "boards_not_exists";
            } else if (!moderator && !((Long) thread[3]).equals(Long.valueOf(db.getData(nick, "id")))) {
                screen = "boards_thread_moderator";
            } else {
                if (edited) {
                    var topic = map.getOrDefault("topic", "");
                    var content = map.getOrDefault("thread", "");
                    db.updateThread(threadId, "topic", topic);
                    db.updateThread(threadId, "content", content);
                    screen = "boards_thread_edited";
                    pageContext.setAttribute("boardId", bid);
                    pageContext.setAttribute("threadId", pid);
                } else {
                    screen = "boards_thread_edit_form";
                    editorId = "edit-content";
                    pageContext.setAttribute("boardId", bid);
                    pageContext.setAttribute("threadId", pid);
                    pageContext.setAttribute("topic", ut.parseHtml(String.valueOf(thread[0])));
                    pageContext.setAttribute("editContent", ut.parseHtml(String.valueOf(thread[1])));
                }
            }
        } else if (screen == null && !delBoards.isBlank()) {
            var deleted = db.getBoards(Long.valueOf(bid), "deleted").equals("1");
            if (!moderator) {
                screen = "boards_locked";
            } else {
                if (deleted) {
                    db.updateBoards(Long.valueOf(bid), "deleted", "0");
                    screen = "boards_undel";
                } else {
                    db.updateBoards(Long.valueOf(bid), "deleted", "1");
                    screen = "boards_deleted";
                }
            }
        } else if (screen == null && !closeBoards.isBlank()) {
            var closed = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (!moderator) {
                screen = "boards_locked";
            } else {
                if (closed) {
                    db.updateBoards(Long.valueOf(bid), "readonly", "0");
                    screen = "boards_open";
                } else {
                    db.updateBoards(Long.valueOf(bid), "readonly", "1");
                    screen = "boards_close";
                }
            }
        } else if (screen == null && !pid.equals("0")) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            ref = db.getBoard(Long.valueOf(pid), "ref");
            if (!ref.matches("\\d+")) {
                ref = "0";
            }
            if (ref.equals("0")) {
                screen = "boards_not_exists";
            } else {
                if (db.getBoard(Long.valueOf(ref), "closed").equals("1")) {
                    screen = "boards3";
                } else if (locked && !moderator) {
                    screen = "boards3";
                } else {
                    screen = "boards2";
                    editorId = "reply-content";
                }
                var count = db.countBoardTreads3(Long.valueOf(ref));
                ArrayList<Object[]> board = db.getBoardFromIdStripped(Long.valueOf(ref), Long.valueOf(pageNumber));
                threadRows = new ArrayList<>();
                var topic = "";
                long id = 0;
                long pages = (long) Math.ceil((float) count / conf.getLong("board_pages"));
                if (pages == 0) {
                    pages = 1;
                }
                var navigation = pagination(lang, boardPath, pid, pid, bid, pageNumber, pages);
                pageContext.setAttribute("pagePrev", navigation[0]);
                pageContext.setAttribute("pageList", navigation[1]);
                pageContext.setAttribute("pageNext", navigation[2]);
                for (Object[] boards : board) {
                    var row = new HashMap<String, Object>();
                    if ((Long) boards[10] == 0) {
                        row.put("kind", "thread");
                        topic = String.valueOf(boards[0]);
                        row.put("topic", ut.parseHtml(String.valueOf(boards[0])));
                        var content = String.valueOf(boards[1]);
                        row.put("content", ut.parseBb(ut.parseHtml(content)));
                        var user = db.getData((Long) boards[3], "nick2");
                        var signature = ut.parseBb(ut.parseHtml(db.getData((Long) boards[3], "signature")));
                        if (moderator) {
                            row.put("delete", threadButton(lang, "danger", boardPath, boards, "del", "page.boards_thread_delete.delete_the_thread", "trash"));
                            if (boards[2].equals(boards[8]) && String.valueOf(boards[9]).equals("0")) {
                                row.put("close", threadButton(lang, "warning", boardPath, boards, "close", "page.boards_thread_close.close_the_thread", "lock"));
                            } else if (String.valueOf(boards[9]).equals("1")) {
                                row.put("close", threadButton(lang, "success", boardPath, boards, "open", "page.boards_thread_open.open_the_thread_again", "unlock"));
                            } else {
                                row.put("close", "");
                            }
                            row.put("ban", banButton(lang, db.isBanned(user), boardPath, db.getData(user, "id")));
                        } else {
                            row.put("delete", "");
                            row.put("close", "");
                            row.put("ban", "");
                        }
                        if (user.equalsIgnoreCase(nick) || moderator) {
                            row.put("edit", threadButton(lang, "primary", boardPath, boards, "edit", "page.boards_thread_edit.processing", "pencil"));
                        } else {
                            row.put("edit", "");
                        }
                        row.put("user", user);
                        row.put("color", db.getData(user, "color"));
                        row.put("posted", ut.getTime((Long) boards[5]));
                        row.put("cat", String.valueOf(boards[7]));
                        row.put("id", String.valueOf(boards[8]));
                        row.put("signature", signature);
                        row.put("banned", db.isBanned(user) ? bannedBadge(lang) : "");
                        id = (Long) boards[7];
                    } else {
                        row.put("kind", "deleted");
                        topic = String.valueOf(boards[0]);
                        var user = db.getData((Long) boards[3], "nick2");
                        if (db.getData(user, "moderator").equals("1")) {
                            row.put("delete", threadButton(lang, "success", boardPath, boards, "undel", "page.boards_thread_undelete.restoring_thread", "undo"));
                        } else {
                            row.put("delete", "");
                        }
                        row.put("user", user);
                        row.put("color", db.getData(user, "color"));
                        row.put("posted", ut.getTime((Long) boards[5]));
                        id = (Long) boards[7];
                    }
                    threadRows.add(row);
                }
                pageContext.setAttribute("boardId", String.valueOf(id));
                pageContext.setAttribute("threadId", pid);
                pageContext.setAttribute("replyPrefix", topic.startsWith(conf.getString("board_re")) ? "" : conf.getString("board_re"));
                pageContext.setAttribute("replyTopic", ut.parseHtml(topic));
            }
        } else if (screen == null && !search.isBlank()) {
            ArrayList<Object[]> board = db.searchBoard(Long.valueOf(ref), Long.valueOf(pageNumber), search);
            var count = db.countBoardTreads3(search);
            threadRows = new ArrayList<>();
            var topic = "";
            long id = 0;
            long pages = (long) Math.ceil((float) count / conf.getLong("board_pages"));
            if (pages == 0) {
                pages = 1;
            }
            screen = "boards3";
            var navigation = pagination(lang, boardPath, pid, pid, bid, pageNumber, pages);
            pageContext.setAttribute("pagePrev", navigation[0]);
            pageContext.setAttribute("pageList", navigation[1]);
            pageContext.setAttribute("pageNext", navigation[2]);
            for (Object[] boards : board) {
                var row = new HashMap<String, Object>();
                if ((Long) boards[10] == 0) {
                    row.put("kind", "thread");
                    topic = String.valueOf(boards[0]);
                    row.put("topic", ut.parseHtml(String.valueOf(boards[0])));
                    var content = String.valueOf(boards[1]);
                    row.put("content", ut.parseBb(ut.parseHtml(content)));
                    var user = db.getData((Long) boards[3], "nick2");
                    if (user.equalsIgnoreCase(nick) || moderator) {
                        row.put("edit", threadButton(lang, "primary", boardPath, boards, "edit", "page.boards_thread_edit.processing", "pencil"));
                    } else {
                        row.put("edit", "");
                    }
                    if (moderator) {
                        row.put("ban", banButton(lang, db.isBanned(user), boardPath, boards[3]));
                    } else {
                        row.put("ban", "");
                    }
                    var signature = ut.parseBb(ut.parseHtml(db.getData((Long) boards[3], "signature")));
                    row.put("delete", "");
                    row.put("close", "");
                    row.put("user", user);
                    row.put("color", db.getData(user, "color"));
                    row.put("posted", ut.getTime((Long) boards[5]));
                    row.put("cat", String.valueOf(boards[7]));
                    row.put("id", String.valueOf(boards[8]));
                    row.put("signature", signature);
                    row.put("banned", db.isBanned(user) ? bannedBadge(lang) : "");
                    id = (Long) boards[7];
                } else {
                    row.put("kind", "deleted");
                    topic = String.valueOf(boards[0]);
                    var user = db.getData((Long) boards[3], "nick2");
                    if (db.getData(user, "moderator").equals("1")) {
                        row.put("delete", threadButton(lang, "success", boardPath, boards, "undel", "page.boards_thread_undelete.restoring_thread", "undo"));
                    } else {
                        row.put("delete", "");
                    }
                    row.put("user", user);
                    row.put("color", db.getData(user, "color"));
                    row.put("posted", ut.getTime((Long) boards[5]));
                    id = (Long) boards[7];
                }
                threadRows.add(row);
            }
        } else if (screen == null && !start.isBlank()) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");

            if (locked && !moderator) {
                screen = "boards_locked";
            } else {
                var add = map.getOrDefault("add", "");
                if (add.isBlank()) {
                    screen = "boards_add";
                    editorId = "new-thread";
                    pageContext.setAttribute("boardId", bid);
                } else {
                    var newPostId = db.addThread(map.getOrDefault("topic", ""), map.getOrDefault("thread", ""), 0, Long.valueOf(db.getData(nick, "id")), 0, ip, Long.valueOf(bid));
                    screen = "boards_added";
                    pid = String.valueOf(newPostId);
                    db.updateThread(Long.valueOf(pid), "ref", pid);
                    pageContext.setAttribute("boardId", bid);
                    pageContext.setAttribute("threadId", pid);
                }
            }
        } else if (screen == null && !delCat.isBlank()) {
            if (!moderator) {
                screen = "board_add_moderator";
            } else {
                var cat = map.getOrDefault("cat", "");
                var deleted = db.getBoardCat(Long.valueOf(cat), "deleted");
                if (deleted.equals("0")) {
                    db.updateBoardCats(Long.valueOf(cat), "deleted", "1");
                    screen = "board_deleted_board";
                } else {
                    db.updateBoardCats(Long.valueOf(cat), "deleted", "0");
                    screen = "board_opened_board";
                }
            }
        } else if (screen == null && !editCat.isBlank()) {
            if (!moderator) {
                screen = "board_add_moderator";
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                var cat = map.getOrDefault("cat", "");
                if (topic.isBlank()) {
                    screen = "board_edit_board";
                    pageContext.setAttribute("boardCat", cat);
                    pageContext.setAttribute("topic", ut.parseHtml(db.getBoardCat(Long.valueOf(cat), "topic")));
                    pageContext.setAttribute("description", ut.parseHtml(db.getBoardCat(Long.valueOf(cat), "description")));
                } else {
                    db.updateBoardCats(Long.valueOf(cat), "topic", topic);
                    db.updateBoardCats(Long.valueOf(cat), "description", description);
                    screen = "board_edited_board";
                }
            }
        } else if (screen == null && !addCat.isBlank()) {
            if (!moderator) {
                screen = "board_add_moderator";
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                var cat = map.getOrDefault("cat", "");
                if (topic.isBlank()) {
                    screen = "board_add_board";
                    pageContext.setAttribute("boardCat", cat);
                } else {
                    db.addBoards(topic, description, Long.valueOf(cat));
                    screen = "board_added_board";
                }
            }
        } else if (screen == null && !addBoard.isBlank()) {
            if (!moderator) {
                screen = "board_add_moderator";
            } else {
                var topic = map.getOrDefault("topic", "");
                var description = map.getOrDefault("description", "");
                if (topic.isBlank()) {
                    screen = "board_add";
                } else {
                    db.addBoardCat(topic, description);
                    screen = "board_added";
                }
            }
        } else if (screen == null && bid.equals("0")) {
            screen = moderator ? "board_moderated" : "board";
            pageContext.setAttribute("moderator", moderator);
            categories = new ArrayList<>();
            TreeMap<Long, Object[]> cat = db.getBoardCat();
            for (Object[] cats : cat.values()) {
                var id = String.valueOf(cats[0]);
                var topic = String.valueOf(cats[1]);
                var description = String.valueOf(cats[2]);
                var deleted = (Integer) cats[3];
                var categoryRow = new HashMap<String, Object>();
                if (moderator) {
                    categoryRow.put("delete", deleted == 0
                            ? categoryButton(lang, "danger", boardPath, id, "del_cat", "page.board_cats_del.delete_category", "trash")
                            : categoryButton(lang, "success", boardPath, id, "del_cat", "page.board_cats_open.restore_category", "undo"));
                    categoryRow.put("edit", categoryButton(lang, "primary", boardPath, id, "edit_cat", "common.processing_category", "pencil"));
                } else {
                    if (deleted == 1) {
                        continue;
                    }
                    categoryRow.put("delete", "");
                    categoryRow.put("edit", "");
                }
                categoryRow.put("id", id);
                categoryRow.put("topic", ut.parseHtml(topic));
                categoryRow.put("description", ut.parseBb(ut.parseHtml(description)));
                TreeMap<Long, Object[]> board = db.getBoards(Long.valueOf(id));
                var forums = new ArrayList<Map<String, Object>>();
                var locked = false;
                for (Object[] boards : board.values()) {
                    locked = String.valueOf(boards[2]).equals("1");
                    var deleted2 = String.valueOf(boards[6]).equals("1");
                    var boardRow = new HashMap<String, Object>();
                    if (!moderator) {
                        if (deleted2) {
                            continue;
                        }
                    } else {
                        boardRow.put("del", !deleted2
                                ? forumButton(lang, "danger", boardPath, boards, "delete_boards", "page.board_boards_moderated_del.delete_the_forum", "trash")
                                : forumButton(lang, "success", boardPath, boards, "delete_boards", "page.board_boards_moderated_undel.restore_the_forum", "undo"));
                        boardRow.put("edit", forumButton(lang, "primary", boardPath, boards, "edit_boards", "page.board_boards_moderated_edit.processing_the_forum", "pencil"));
                        var readonly = String.valueOf(boards[2]).equals("1");
                        boardRow.put("lock", readonly
                                ? forumButton(lang, "success", boardPath, boards, "close_boards", "page.board_boards_moderated_open.unlocking_the_forum", "unlock")
                                : forumButton(lang, "warning", boardPath, boards, "close_boards", "page.board_boards_moderated_close.block_the_forum", "lock"));
                    }
                    boardRow.put("id", String.valueOf(boards[5]));
                    boardRow.put("topics", String.valueOf(db.countBoardTreads2((Long) boards[5])));
                    boardRow.put("threads", String.valueOf(db.countBoardTreads((Long) boards[5])));
                    boardRow.put("topic", ut.parseHtml(String.valueOf(boards[1])));
                    boardRow.put("description", ut.parseBb(ut.parseHtml(String.valueOf(boards[3]))));
                    forums.add(boardRow);
                }
                categoryRow.put("forums", forums);
                categoryRow.put("locked", locked);
                categories.add(categoryRow);
            }
        } else if (screen == null) {
            var locked = db.getBoards(Long.valueOf(bid), "readonly").equals("1");
            if (locked && !moderator) {
                screen = "boards3";
            } else {
                screen = "boards";
            }
            listing = true;
            ArrayList<Object[]> board = db.getBoardFromIdStripped2(Long.valueOf(bid), Long.valueOf(pageNumber));
            var count = db.countBoardTreads2(Long.valueOf(bid));
            long pages = (long) Math.ceil((float) count / conf.getLong("board_pages"));
            if (pages == 0) {
                pages = 1;
            }
            var navigation = pagination(lang, boardPath, pid, null, bid, pageNumber, pages);
            pageContext.setAttribute("pagePrev", navigation[0]);
            pageContext.setAttribute("pageList", navigation[1]);
            pageContext.setAttribute("pageNext", navigation[2]);
            threadRows = new ArrayList<>();
            for (Object[] boards : board) {
                if ((Long) boards[10] == 0) {
                    var row = new HashMap<String, Object>();
                    row.put("kind", "thread2");
                    row.put("topic", ut.parseHtml(db.getBoardRef((Long) boards[2], "topic")));
                    var content = String.valueOf(db.getBoardRef((Long) boards[2], "content"));
                    if (content.length() > 255) {
                        content = content.substring(0, 255);
                        content = content + "...";
                    }
                    var user = db.getData(Long.valueOf(db.getBoardRef((Long) boards[2], "user")), "nick2");
                    if (user.equalsIgnoreCase(nick) || moderator) {
                        row.put("edit", threadButton(lang, "primary", boardPath, boards, "edit", "page.boards_thread_edit.processing", "pencil"));
                    } else {
                        row.put("edit", "");
                    }
                    row.put("content", ut.removeBb(ut.parseHtml(content)));
                    if (moderator) {
                        row.put("delete", threadButton(lang, "danger", boardPath, boards, "del", "page.boards_thread_delete.delete_the_thread", "trash"));
                        if (db.getBoardRef((Long) boards[2], "id").equals(db.getBoardRef((Long) boards[2], "ref"))) {
                            row.put("close", threadButton(lang, "warning", boardPath, boards, "close", "page.boards_thread_close.close_the_thread", "lock"));
                        } else {
                            row.put("close", "");
                        }
                        row.put("ban", banButton(lang, db.isBanned(user), boardPath, boards[3]));
                    } else {
                        row.put("delete", "");
                        row.put("close", "");
                        row.put("ban", "");
                    }
                    row.put("user", user);
                    row.put("color", db.getData(user, "color"));
                    row.put("posted", ut.getTime(Long.valueOf(db.getBoardRef((Long) boards[2], "posted"))));
                    row.put("cat", String.valueOf(boards[7]));
                    row.put("id", String.valueOf(boards[8]));
                    row.put("banned", db.isBanned(user) ? bannedBadge(lang) : "");
                    threadRows.add(row);
                }
            }
            pageContext.setAttribute("boardId", bid);
        }
        }
        if (screen == null) {
            return;
        }
        pageContext.setAttribute("screen", screen);
        if (NOTICES.containsKey(screen)) {
            pageContext.setAttribute("notice", NOTICES.get(screen));
        }
        pageContext.setAttribute("categories", categories);
        pageContext.setAttribute("threadRows", threadRows);
        pageContext.setAttribute("listing", listing);
        pageContext.setAttribute("editorId", editorId);
%><c:set var="bbToolbar"><div class="btn-toolbar mb-2" role="toolbar" aria-label="<fmt:message key="common.bb_code_toolbar"/>">
                <div class="btn-group btn-group-sm mr-2" role="group" aria-label="<fmt:message key="common.text_formatting"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','b')" title="<fmt:message key="common.fat_fat"/>"><i class="fa fa-bold"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','i')" title="<fmt:message key="common.italics"/>"><i class="fa fa-italic"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','u')" title="<fmt:message key="common.underlined"/>"><i class="fa fa-underline"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','s')" title="<fmt:message key="common.crossed_out"/>"><i class="fa fa-strikethrough"></i></button>
                </div>
                <div class="btn-group btn-group-sm mr-2" role="group" aria-label="<fmt:message key="common.alignment_and_special_formatting"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','center')" title="<fmt:message key="common.centred"/>"><i class="fa fa-align-center"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','quote')" title="<fmt:message key="common.citation"/>"><i class="fa fa-quote-right"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','code')" title="Code"><i class="fa fa-code"></i></button>
                </div>
                <div class="btn-group btn-group-sm" role="group" aria-label="<fmt:message key="common.link_image_color"/>">
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','url')" title="Link"><i class="fa fa-link"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','img')" title="<fmt:message key="common.picture"/>"><i class="fa fa-image"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBCode('${editorId}','email')" title="<fmt:message key="common.e_mail"/>"><i class="fa fa-envelope"></i></button>
                  <button type="button" class="btn btn-outline-secondary" onclick="insertBBColor('${editorId}')" title="<fmt:message key="common.colour_2"/>"><i class="fa fa-tint"></i></button>
                </div>
              </div></c:set><c:set var="threadList"><c:choose><c:when test="${empty threadRows}"><c:choose><c:when test="${listing}"><div class="alert alert-light text-center py-5" role="status">
  <i class="fa fa-comments-o fa-3x text-muted mb-3"></i>
  <h4><fmt:message key="page.boards_empty.no_contributions_yet"/></h4>
  <p class="text-muted mb-0"><fmt:message key="page.boards_empty.be_the_first_and_start"/></p>
</div>
</c:when><c:otherwise><div class="alert alert-light text-center py-5" role="status">
  <i class="fa fa-comment-o fa-3x text-muted mb-3"></i>
  <h4><fmt:message key="page.boards_empty2.no_answers_yet"/></h4>
  <p class="text-muted mb-0"><fmt:message key="page.boards_empty2.write_the_first_answer_on"/></p>
</div>
</c:otherwise></c:choose></c:when><c:otherwise><c:forEach var="row" items="${threadRows}"><c:choose><c:when test="${row.kind eq 'deleted'}"><article class="card forum-thread-card mb-3 border-secondary">
  <div class="card-header bg-light">
    <div class="d-flex justify-content-between align-items-center flex-wrap">
      <small class="text-muted">
        <i class="fa fa-clock-o"></i> ${row.posted} &middot;
        <i class="fa fa-user"></i> <strong style="color: #${row.color}">${row.user}</strong>
      </small>
      <div class="forum-thread-actions btn-group btn-group-sm" role="group">${row.delete}</div>
    </div>
  </div>
  <div class="card-body forum-thread-content text-muted">
    <i class="fa fa-trash-o"></i> <fmt:message key="common.this_entry_has_been_deleted"/>.
  </div>
</article>
</c:when><c:otherwise><article class="card forum-thread-card mb-3">
  <div class="card-header bg-light">
    <div class="d-flex justify-content-between align-items-start flex-wrap">
      <div>
        <h5 class="mb-1">
          <a href="${paths.board}&pid=${row.id}&id=${row.cat}" class="text-decoration-none">${row.topic}</a>
        </h5>
        <small class="text-muted">
          <i class="fa fa-clock-o"></i> ${row.posted} &middot;
          <i class="fa fa-user"></i> <strong style="color: #${row.color}">${row.user}</strong>
          ${row.banned}
        </small>
      </div>
      <div class="forum-thread-actions btn-group btn-group-sm" role="group">
        ${row.edit} ${row.close} ${row.delete} ${row.ban}
      </div>
    </div>
  </div>
  <div class="card-body forum-thread-content">
    ${row.content}
  </div>
<c:if test="${row.kind eq 'thread'}">  <div class="card-footer bg-white forum-thread-signature">
    ${row.signature}
  </div>
</c:if></article>
</c:otherwise></c:choose></c:forEach></c:otherwise></c:choose></c:set><c:set var="categoryList"><c:choose><c:when test="${empty categories}"><c:choose><c:when test="${moderator}"><div class="alert alert-light text-center py-4" role="status">
  <i class="fa fa-folder-open-o fa-2x text-muted mb-2"></i>
  <p class="mb-2 text-muted"><fmt:message key="page.board_cats_empty_moderated.there_are_no_categories_yet"/></p>
  <a href="${paths.board}&add_board=true" class="btn btn-success btn-sm">
    <i class="fa fa-plus"></i> <fmt:message key="page.board_cats_empty_moderated.create_a_new_category"/>
  </a>
</div>
</c:when><c:otherwise><div class="alert alert-light text-center py-4" role="status">
  <i class="fa fa-folder-open-o fa-2x text-muted mb-2"></i>
  <p class="mb-0 text-muted"><fmt:message key="page.board_cats_empty.there_are_no_forums_yet"/></p>
</div>
</c:otherwise></c:choose></c:when><c:otherwise><c:forEach var="category" items="${categories}"><section class="card forum-category-card mb-4">
  <div class="card-header bg-dark text-white">
    <div class="d-flex justify-content-between align-items-center flex-wrap">
      <h3 class="h5 mb-0" style="color: #fff !important;"><i class="fa fa-folder-open" style="color: #fff !important;"></i> ${category.topic}</h3>
      <div class="btn-group btn-group-sm mt-2 mt-sm-0" role="group">${category.edit} ${category.delete}</div>
    </div>
  </div>
  <div class="card-body p-0">
    <div class="forum-category-description px-3 py-2 text-dark border-bottom">${category.description}</div>
    <div class="forum-category-boards p-3"><c:choose><c:when test="${empty category.forums}"><c:choose><c:when test="${not moderator and category.locked}"><div class="alert alert-warning text-center py-4" role="status">
  <i class="fa fa-lock fa-2x mb-2"></i>
  <p class="mb-0"><fmt:message key="page.board_boards_empty_locked.you_cannot_add_a_post"/>.</p>
</div>
</c:when><c:when test="${moderator}"><div class="alert alert-light text-center py-4" role="status">
  <i class="fa fa-folder-open-o fa-2x text-muted mb-2"></i>
  <p class="mb-2 text-muted"><fmt:message key="page.board_boards_empty_moderated.no_forums_set_up_for"/>.</p>
  <a href="${paths.board}&add_cat=true&cat=${category.id}" class="btn btn-success btn-sm">
    <i class="fa fa-plus"></i> <fmt:message key="common.creating_a_new_forum"/>
  </a>
</div>
</c:when><c:otherwise><div class="text-center text-muted py-4">
  <i class="fa fa-folder-open-o fa-2x mb-2"></i>
  <p class="mb-0"><fmt:message key="page.board_boards_empty.there_are_no_forums_in"/></p>
</div>
</c:otherwise></c:choose></c:when><c:otherwise><c:forEach var="forum" items="${category.forums}"><div class="card forum-board-card mb-3">
  <div class="card-body">
    <div class="row align-items-center">
      <div class="col-auto">
        <div class="forum-board-icon">
          <i class="fa fa-comments"></i>
        </div>
      </div>
      <div class="col">
        <h5 class="card-title mb-1">
          <a href="${paths.board}&id=${forum.id}" class="text-decoration-none">${forum.topic}</a>
        </h5>
        <p class="card-text text-dark mb-2">${forum.description}</p>
        <div class="forum-board-stats small text-dark">
          <span class="mr-3"><i class="fa fa-file-text-o"></i> <strong>${forum.topics}</strong> <c:choose><c:when test="${moderator}"><fmt:message key="page.board_boards_moderated.contributions"/></c:when><c:otherwise><fmt:message key="page.board_boards.contributions"/></c:otherwise></c:choose></span>
          <span><i class="fa fa-comments-o"></i> <strong>${forum.threads}</strong> Threads</span>
        </div>
      </div>
      <div class="col-auto"><c:choose><c:when test="${moderator}">
        <div class="btn-group btn-group-sm forum-board-actions" role="group">
          <fmt:message key="page.board_boards_moderated.edit"><fmt:param value="${forum.lock}"/><fmt:param value="${forum.del}"/><fmt:param value="${forum.edit}"/></fmt:message>
        </div>
</c:when><c:otherwise>
        <a href="${paths.board}&id=${forum.id}" class="btn btn-outline-primary btn-sm">
          <fmt:message key="page.board_boards.opening"/> <i class="fa fa-chevron-right"></i>
        </a>
</c:otherwise></c:choose>      </div>
    </div>
  </div>
</div>
</c:forEach></c:otherwise></c:choose></div>
  </div>
<c:if test="${moderator}">  <div class="card-footer bg-light border-top-0">
    <a href="${paths.board}&add_cat=true&cat=${category.id}" class="btn btn-success btn-sm">
      <i class="fa fa-plus"></i> <fmt:message key="common.creating_a_new_forum"/>
    </a>
  </div>
</c:if></section>
</c:forEach></c:otherwise></c:choose></c:set><t:layout design="${screen eq 'timeout' or screen eq 'banned' or screen eq 'banned_temp' ? 'start' : 'frameset_community_reg'}"><c:choose>
<c:when test="${screen eq 'timeout'}"><h1>${SERVER_SOFTWARE}-Timeout</h1>
<p>
<fmt:message key="page.timeout.your_session_expired_because_of"/>...
</p>
</c:when>
<c:when test="${screen eq 'banned' or screen eq 'banned_temp'}"><h1>
<fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
<p>
<fmt:message key="${screen eq 'banned' ? 'common.your_nickname_or_your_ip_3' : 'common.your_nickname_or_your_ip_4'}"/>!
</p>
<p>
<fmt:message key="common.the_reason_given_to_you"/> <span style="font-weight: bold">${reason}</span>
</p>
<p>
<table>
<tr>
<td style="text-align: left; font-weight: bold;">
<fmt:message key="common.this_can_have_several_causes"/>
</td>
</tr>
<tr>
<td style="text-align: left;">
<fmt:message key="common.you_have_disturbed_the_chat"/>!<br>
<fmt:message key="common.you_have_violated_applicable_law"/>!<br>
<fmt:message key="common.you_have_done_unwanted_advertising"/>!<br>
<c:choose><c:when test="${screen eq 'banned'}">
<fmt:message key="common.you_have_repeatedly_flooded_the"/>!<br>
<fmt:message key="common.you_have_become_a_victim"/>)
</c:when><c:otherwise>
<fmt:message key="common.you_have_not_followed_the"/>!<br>
<fmt:message key="common.you_have_built_too_many"/>)!
</c:otherwise></c:choose>
</td>
</tr>
</table>
</p>
<p>
<fmt:message key="common.please_try_again_later"/>!
</p>
</c:when>
<c:when test="${not empty notice}"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="alert alert-${notice.style}" role="${notice.role}">
        <h4 class="alert-heading"><i class="fa fa-${notice.icon}"></i> <c:choose><c:when test="${screen eq 'board_unknown'}"><fmt:message key="common.error_2"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></c:when><c:otherwise>${notice.prefix}<fmt:message key="${notice.title}"/></c:otherwise></c:choose></h4>
        <p><c:if test="${screen eq 'boards_ban' or screen eq 'boards_unban'}"><fmt:message key="common.the_user"/> <strong style="color: #${banColor}">${banUser}</strong> </c:if><fmt:message key="${notice.text}"/>${notice.suffix}</p>
        <hr>
        <c:choose><c:when test="${screen eq 'boards_added' or screen eq 'boards_thread_edited'}"><a href="${paths.board}<c:choose><c:when test="${screen eq 'boards_added'}">&id=${boardId}&pid=${threadId}</c:when><c:otherwise>&pid=${threadId}&id=${boardId}</c:otherwise></c:choose>" class="btn btn-primary btn-sm"><i class="fa fa-arrow-right"></i> <fmt:message key="common.contribution_2"/></a>
        <a href="${paths.board}" class="btn btn-outline-secondary btn-sm"><i class="fa fa-home"></i> <fmt:message key="common.forum_overview_2"/></a>
</c:when><c:otherwise><a href="${paths.board}" class="btn btn-primary btn-sm"><i class="fa fa-arrow-left"></i> <fmt:message key="${notice.link}"/></a>
</c:otherwise></c:choose>      </div>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'board' or screen eq 'board_moderated'}"><div class="container-fluid py-4 forum-page">
  <div class="row">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="mb-0">${SERVER_SOFTWARE}-Forum</h1>
<c:choose><c:when test="${moderator}">        <a href="${paths.board}&add_board=true" class="btn btn-success btn-sm">
          <i class="fa fa-plus"></i> <fmt:message key="page.board_moderated.new_category"/>
        </a>
</c:when><c:otherwise>        <a href="${paths.board}" class="btn btn-outline-secondary btn-sm">
          <i class="fa fa-home"></i> <fmt:message key="page.board.forum_overview"/>
        </a>
</c:otherwise></c:choose>      </div>
      <div class="forum-content">
        ${categoryList}
      </div>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'board_add'}"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card forum-form-card">
        <div class="card-header">
          <h2 class="h4 mb-0"><i class="fa fa-folder-open"></i> <fmt:message key="page.board_add.opening_a_new_category"/></h2>
        </div>
        <div class="card-body">
          <form method="POST" name="tread" action="${paths.board}&add_board=true" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="cat-topic">Name</label>
              <input id="cat-topic" class="form-control" name="topic" required autofocus>
            </div>
            <div class="form-group">
              <label for="cat-description"><fmt:message key="common.description_2"/></label>
              <textarea id="cat-description" class="form-control" name="description" rows="4" required></textarea>
            </div>
            <div class="d-flex justify-content-between">
              <a href="${paths.board}" class="btn btn-outline-secondary"><i class="fa fa-times"></i> <fmt:message key="common.abortions"/></a>
              <button class="btn btn-success" type="submit"><i class="fa fa-plus"></i> <fmt:message key="page.board_add.opening_category"/></button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'board_add_board'}"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card forum-form-card">
        <div class="card-header">
          <h2 class="h4 mb-0"><i class="fa fa-comments"></i> <fmt:message key="common.opening_a_new_forum"/></h2>
        </div>
        <div class="card-body">
          <form method="POST" name="tread" action="${paths.board}&cat=${boardCat}&add_cat=true" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="board-topic">Name</label>
              <input id="board-topic" class="form-control" name="topic" required autofocus>
            </div>
            <div class="form-group">
              <label for="board-description"><fmt:message key="common.description_2"/></label>
              <textarea id="board-description" class="form-control" name="description" rows="4" required></textarea>
            </div>
            <div class="d-flex justify-content-between">
              <a href="${paths.board}" class="btn btn-outline-secondary"><i class="fa fa-times"></i> <fmt:message key="common.abortions"/></a>
              <button class="btn btn-success" type="submit"><i class="fa fa-plus"></i> <fmt:message key="common.opening_of_the_forum"/></button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'board_edit_board'}"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card forum-form-card">
        <div class="card-header">
          <h2 class="h4 mb-0"><i class="fa fa-pencil"></i> <fmt:message key="common.processing_category"/></h2>
        </div>
        <div class="card-body">
          <form method="POST" name="tread" action="${paths.board}&cat=${boardCat}&edit_cat=true" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="edit-topic">Name</label>
              <input id="edit-topic" class="form-control" name="topic" value="${topic}" required autofocus>
            </div>
            <div class="form-group">
              <label for="edit-description"><fmt:message key="common.description_2"/></label>
              <textarea id="edit-description" class="form-control" name="description" rows="4" required>${description}</textarea>
            </div>
            <div class="d-flex justify-content-between">
              <a href="${paths.board}" class="btn btn-outline-secondary"><i class="fa fa-times"></i> <fmt:message key="common.abortions"/></a>
              <button class="btn btn-primary" type="submit"><i class="fa fa-save"></i> <fmt:message key="common.processing_category"/></button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'boards_add'}"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card forum-form-card">
        <div class="card-header">
          <h2 class="h4 mb-0"><i class="fa fa-plus-circle"></i> <fmt:message key="page.boards_add.opening_a_new_thread"/></h2>
        </div>
        <div class="card-body">
          <form method="POST" name="thread" action="${paths.board}&id=${boardId}&new=true&add=true" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="new-topic"><fmt:message key="common.title"/></label>
              <input id="new-topic" class="form-control" name="topic" maxlength="255" required autofocus>
            </div>
            <div class="form-group">
              <label for="new-thread"><fmt:message key="common.contribution"/></label>
              ${bbToolbar}
              <textarea id="new-thread" class="form-control" name="thread" rows="10" required></textarea>
            </div>
            <div class="d-flex justify-content-between">
              <a href="${paths.board}&id=${boardId}" class="btn btn-outline-secondary">
                <i class="fa fa-times"></i> <fmt:message key="common.abortions"/>
              </a>
              <button class="btn btn-success" type="submit">
                <i class="fa fa-paper-plane"></i> <fmt:message key="page.boards_add.opening_the_thread"/>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'boards_edit'}"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card forum-form-card">
        <div class="card-header">
          <h2 class="h4 mb-0"><i class="fa fa-pencil"></i> <fmt:message key="common.contribution_processing"/></h2>
        </div>
        <div class="card-body">
          <form method="POST" name="thread" action="${paths.board}&id=${boardId}&pid=&edit_boards=true&edited=true" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="edit-topic"><fmt:message key="common.title"/></label>
              <input id="edit-topic" class="form-control" name="topic" value="${topic}" maxlength="255" required>
            </div>
            <div class="form-group">
              <label for="edit-description"><fmt:message key="common.contribution"/></label>
              ${bbToolbar}
              <textarea id="edit-description" class="form-control" name="description" rows="5" required>${description}</textarea>
            </div>
            <div class="d-flex justify-content-between">
              <a href="${paths.board}&id=" class="btn btn-outline-secondary"><i class="fa fa-times"></i> <fmt:message key="common.abortions"/></a>
              <button class="btn btn-primary" type="submit"><i class="fa fa-save"></i> <fmt:message key="common.storage"/></button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'boards_thread_edit_form'}"><div class="container py-4">
  <div class="row justify-content-center">
    <div class="col-lg-8">
      <div class="card forum-form-card">
        <div class="card-header">
          <h2 class="h4 mb-0"><i class="fa fa-pencil"></i> <fmt:message key="common.contribution_processing"/></h2>
        </div>
        <div class="card-body">
          <form method="POST" name="thread" action="${paths.board}&id=${boardId}&pid=${threadId}&edit=true&edited=true" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="edit-topic"><fmt:message key="common.title"/></label>
              <input id="edit-topic" class="form-control" name="topic" value="${topic}" maxlength="255" required autofocus>
            </div>
            <div class="form-group">
              <label for="edit-content"><fmt:message key="common.contribution"/></label>
              ${bbToolbar}
              <textarea id="edit-content" class="form-control" name="thread" rows="10" required>${editContent}</textarea>
            </div>
            <div class="d-flex justify-content-between">
              <a href="${paths.board}&pid=${threadId}&id=${boardId}" class="btn btn-outline-secondary"><i class="fa fa-times"></i> <fmt:message key="common.abortions"/></a>
              <button class="btn btn-primary" type="submit"><i class="fa fa-save"></i> <fmt:message key="common.storage"/></button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'boards'}"><div class="container-fluid py-4 forum-page">
  <div class="row">
    <div class="col-12">
      <h1 class="mb-4">${SERVER_SOFTWARE}-Forum</h1>
      
      <!-- Pagination oben -->
      <nav aria-label="<fmt:message key="common.forum_navigation"/>" class="mb-3">
        <ul class="pagination justify-content-center">
          ${pagePrev} ${pageList} ${pageNext}
        </ul>
      </nav>
      
      <!-- Suchleiste -->
      <div class="card mb-4">
        <div class="card-body">
          <form method="POST" name="login" action="${paths.board}" target="_top" accept-charset="utf-8" class="form-row align-items-center">
            <div class="col-md-8">
              <input class="form-control" name="search" placeholder="<fmt:message key="common.search_the_forum"/>">
            </div>
            <div class="col-md-2">
              <button class="btn btn-primary w-100" type="submit">
                <i class="fa fa-search"></i> <fmt:message key="common.search"/>
              </button>
            </div>
            <div class="col-md-2">
              <a href="${paths.board}&id=${boardId}&new=true" class="btn btn-success w-100">
                <i class="fa fa-plus"></i> <fmt:message key="page.boards.new_contribution"/>
              </a>
            </div>
            <input name="skin" value="${skin}" type="hidden">
          </form>
        </div>
      </div>
      
      <!-- Boards-Liste -->
      <div class="boards-list">
        ${threadList}
      </div>
      
      <!-- Pagination unten -->
      <nav aria-label="<fmt:message key="common.forum_navigation"/>" class="mt-4">
        <ul class="pagination justify-content-center">
          ${pagePrev} ${pageList} ${pageNext}
        </ul>
      </nav>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'boards2'}"><div class="container-fluid py-4 forum-page">
  <div class="row">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="mb-0">${SERVER_SOFTWARE}-Forum</h1>
        <a href="${paths.board}" class="btn btn-outline-secondary btn-sm">
          <i class="fa fa-arrow-left"></i> <fmt:message key="page.boards2.back"/>
        </a>
      </div>

      <nav aria-label="<fmt:message key="page.boards2.thread_navigation"/>" class="mb-3">
        <ul class="pagination justify-content-center">${pagePrev} ${pageList} ${pageNext}</ul>
      </nav>

      <div class="card mb-4">
        <div class="card-body">
          <form method="POST" name="login" action="${paths.board}" target="_top" accept-charset="utf-8" class="input-group">
            <input class="form-control" name="search" placeholder="<fmt:message key="page.boards2.in_this_forum_search"/>">
            <input name="skin" value="${skin}" type="hidden">
            <div class="input-group-append">
              <button class="btn btn-primary" type="submit"><i class="fa fa-search"></i> <fmt:message key="common.search"/></button>
            </div>
          </form>
        </div>
      </div>

      <div class="forum-threads mb-4">${threadList}</div>

      <div class="card forum-reply-card">
        <div class="card-header">
          <h5 class="mb-0"><i class="fa fa-reply"></i> <fmt:message key="page.boards2.response"/></h5>
        </div>
        <div class="card-body">
          <form method="POST" name="thread" action="${paths.board}&id=${boardId}&answer=true&pid=${threadId}" target="_top" accept-charset="utf-8">
            <div class="form-group">
              <label for="reply-topic"><fmt:message key="page.boards2.subject"/></label>
              <input id="reply-topic" class="form-control" name="topic" value="${replyPrefix}${replyTopic}" required>
            </div>
            <div class="form-group">
              <label for="reply-content"><fmt:message key="page.boards2.message"/></label>
              ${bbToolbar}
              <textarea id="reply-content" class="form-control" name="thread" rows="6" required></textarea>
            </div>
            <button class="btn btn-success" type="submit">
              <i class="fa fa-paper-plane"></i> <fmt:message key="page.boards2.submit_a_reply"/>
            </button>
          </form>
        </div>
      </div>

      <nav aria-label="<fmt:message key="page.boards2.thread_navigation"/>" class="mt-4">
        <ul class="pagination justify-content-center"><fmt:message key="page.boards2.text"><fmt:param value="${pagePrev}"/><fmt:param value="${pageList}"/><fmt:param value="${pageNext}"/></fmt:message></ul>
      </nav>
    </div>
  </div>
</div>
</c:when>
<c:when test="${screen eq 'boards3'}"><div class="container-fluid py-4 forum-page">
  <div class="row">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h1 class="mb-0">${SERVER_SOFTWARE}-Forum</h1>
        <a href="${paths.board}" class="btn btn-outline-secondary btn-sm">
          <i class="fa fa-home"></i> <fmt:message key="common.forum_overview_2"/>
        </a>
      </div>

      <nav aria-label="<fmt:message key="common.forum_navigation"/>" class="mb-3">
        <ul class="pagination justify-content-center">${pagePrev} ${pageList} ${pageNext}</ul>
      </nav>

      <div class="card mb-4">
        <div class="card-body">
          <form method="POST" name="login" action="${paths.board}" target="_top" accept-charset="utf-8" class="form-row align-items-center">
            <div class="col-md-9 mb-2 mb-md-0">
              <input class="form-control" name="search" placeholder="<fmt:message key="common.search_the_forum"/>">
            </div>
            <div class="col-md-3">
              <button class="btn btn-primary w-100" type="submit">
                <i class="fa fa-search"></i> <fmt:message key="common.search"/>
              </button>
            </div>
            <input name="skin" value="${skin}" type="hidden">
          </form>
        </div>
      </div>

      <div class="forum-threads mb-4">${threadList}</div>

      <nav aria-label="<fmt:message key="common.forum_navigation"/>" class="mt-4">
        <ul class="pagination justify-content-center">${pagePrev} ${pageList} ${pageNext}</ul>
      </nav>
    </div>
  </div>
</div>
</c:when>
</c:choose><c:if test="${not empty editorId}"><script>
(function() {
  function getArea(areaId) {
    var area = document.getElementById(areaId);
    return area && area.tagName.toLowerCase() === 'textarea' ? area : null;
  }
  function insertAtCursor(area, before, after) {
    var start = area.selectionStart || 0;
    var end = area.selectionEnd || 0;
    var text = area.value || '';
    var selected = text.substring(start, end);
    area.value = text.substring(0, start) + before + selected + after + text.substring(end);
    area.focus();
    var pos = start + before.length;
    if (selected) {
      pos = start + before.length + selected.length + after.length;
    }
    area.setSelectionRange(pos, pos);
  }
  window.insertBBCode = function(areaId, tag) {
    var area = getArea(areaId);
    if (!area || !tag) return;
    tag = String(tag).toLowerCase();
    var open, close, value;
    switch (tag) {
      case 'url':
        var url = prompt('<fmt:message key="common.enter_url"/>:', 'https://');
        if (url === null) return;
        url = url.trim() || 'https://';
        var start = area.selectionStart || 0;
        var end = area.selectionEnd || 0;
        var text = area.value || '';
        var selected = text.substring(start, end);
        var linkText = selected || url;
        insertAtCursor(area, '[url=' + url + ']', '[/url]');
        return;
      case 'img':
        var imgUrl = prompt('<fmt:message key="common.enter_picture_url"/>:', 'https://');
        if (imgUrl === null) return;
        imgUrl = imgUrl.trim();
        if (!imgUrl) return;
        insertAtCursor(area, '[img]' + imgUrl + '[/img]', '');
        return;
      case 'email':
        var mail = prompt('<fmt:message key="common.enter_e_mail_address"/>:', '');
        if (mail === null) return;
        mail = mail.trim();
        if (!mail) return;
        insertAtCursor(area, '[email]' + mail + '[/email]', '');
        return;
      default:
        open = '[' + tag + ']';
        close = '[/' + tag + ']';
    }
    insertAtCursor(area, open, close);
  };
  window.insertBBColor = function(areaId) {
    var area = getArea(areaId);
    if (!area) return;
    var color = prompt('<fmt:message key="common.enter_colour_as_hex_value"/>. ff0000):', 'ff0000');
    if (color === null) return;
    color = color.replace(/^#/, '').trim() || '000000';
    insertAtCursor(area, '[color=' + color + ']', '[/color]');
  };
})();
</script>
</c:if></t:layout>
