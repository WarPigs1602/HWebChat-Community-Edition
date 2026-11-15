<%@ page session="false" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.*, java.util.*, java.util.regex.*, jakarta.servlet.http.*" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%!
    private static final Pattern LEGACY_QUERY = Pattern.compile("(%PATH_\\[([^\\]%]+)\\]%)&(?:amp;)?(?=[A-Za-z_][A-Za-z0-9_.-]*=)");
    private static final Pattern TOKEN = Pattern.compile("%(?:PATH_\\[([^\\]%]+)\\]|com\\[([^\\]%]+)\\]|([A-Za-z_][A-Za-z0-9_]*))%");

    private static String lookupMap(HttpServletRequest request, String map, String key) {
        return request.getAttribute(map) instanceof Map<?, ?> values && values.get(key) != null ? String.valueOf(values.get(key)) : null;
    }

    /** Resolves the %name%, %PATH_[name]% and %com[name]% tokens of the configured profile texts. */
    private static String expand(String text, Map<String, String> fields, HttpServletRequest request) {
        if (text == null) {
            return "";
        }
        for (int pass = 0; pass < 6 && text.indexOf('%') >= 0; pass++) {
            var legacy = LEGACY_QUERY.matcher(text);
            var normalized = new StringBuilder();
            while (legacy.find()) {
                var path = lookupMap(request, "paths", legacy.group(2));
                var replacement = path != null && !path.contains("?") ? legacy.group(1) + "?" : legacy.group();
                legacy.appendReplacement(normalized, Matcher.quoteReplacement(replacement));
            }
            legacy.appendTail(normalized);
            text = normalized.toString();
            var matcher = TOKEN.matcher(text);
            var sb = new StringBuilder();
            boolean changed = false;
            while (matcher.find()) {
                String replacement;
                if (matcher.group(1) != null) {
                    replacement = lookupMap(request, "paths", matcher.group(1));
                    replacement = replacement == null ? "" : replacement;
                } else if (matcher.group(2) != null) {
                    replacement = lookupMap(request, "com", matcher.group(2));
                    replacement = replacement == null ? "" : replacement;
                } else {
                    Object value = fields.get(matcher.group(3));
                    value = value != null ? value : request.getAttribute(matcher.group(3));
                    replacement = value == null ? matcher.group() : String.valueOf(value);
                }
                changed |= !replacement.equals(matcher.group());
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            text = sb.toString();
            if (!changed) {
                break;
            }
        }
        return text;
    }

    private static void addField(Map<String, String> labels, Map<String, String> fields, Map<String, String> texts,
            String label, String key, String value) {
        if (value != null && !value.equals("")) {
            labels.put(label, texts.getOrDefault(label, ""));
            fields.put(key, value);
        } else {
            labels.put(label, "");
        }
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
%><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    Map<String, String> map = Web.requestValues(request);
        var user = map.getOrDefault("user", "");
        var conf = Bootstrap.getBoot().getConfig();
        var cm = Bootstrap.getBoot().getChatManager();
        var ut = Bootstrap.getBoot().getUtil();
        var sid = map.getOrDefault("sid", "");
        var skin = map.getOrDefault("skin", "");
        var owner = map.getOrDefault("owner", "");
        skin = ut.parseHost(skin, request)[1];
        var db = conf.getDb();
        var reg = db.isRegistered(user);
        var view = "";
        String failure = null;
        var session = request.getSession(false);
        sid = session != null ? (String) session.getAttribute("sid") : null;
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        var nick = session != null ? (String) session.getAttribute("nick") : null;
        nick = nick != null ? nick : map.getOrDefault("nick", "");
        var roomName = "";
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        if (!map.getOrDefault("guestbook", "").isBlank() && sid != null && cm.isValidConnectionId(sid)) {
            var gbNick = cm.getNameFromId(sid);
            var gbText = map.getOrDefault("guestbook_text", "").trim();
            if (!gbText.isEmpty() && gbText.length() <= 1024) {
                db.addGuestbookEntry(user, gbNick, gbText);
            }
        }
        if (!map.getOrDefault("guestbook_delete", "").isBlank() && sid != null && cm.isValidConnectionId(sid) && nick.equalsIgnoreCase(user)) {
            try {
                db.deleteGuestbookEntry(Integer.parseInt(map.get("guestbook_delete")), user);
            } catch (NumberFormatException ignored) {
            }
        }
        request.setAttribute("profileDesign", roomName.isBlank() ? "profile" : "napping_profile");
        if (!cm.isValidConnectionId(sid)) {
            view = "invalid";
        } else if (!reg) {
            view = "unregistered";
        } else {
            try {
                var lang = (String) request.getAttribute("lang");
                var texts = lang != null && lang.equalsIgnoreCase("en") && db.getProfileEn() != null
                        ? db.getProfileEn()
                        : db.getProfile();
                var labels = new HashMap<String, String>();
                var fields = new HashMap<String, String>();
                var buf = db.getData(user, "city");
                fields.put("city", buf != null && !buf.equals("") ? buf : texts.getOrDefault("unknown", ""));
                var stat = db.getLongData(user, "status").intValue();
                var level = stat >= 10 ? 10 : stat >= 2 ? stat : 1;
                addField(labels, fields, texts, "status", "status",
                        Bootstrap.getBoot().getChatManager().getCommand(nick, "status_" + level));
                addField(labels, fields, texts, "icq", "icq",
                        db.getLongData(user, "icq") != 0 ? db.getData(user, "icq") : null);
                var plain = new String[][]{
                    {"homepage", "homepage", "homepage"}, {"hobby", "hobby", "hobby"},
                    {"description", "description", "description"}, {"slogan", "slogan", "slogan"},
                    {"signature", "signature", "signature"}, {"live", "skype", "live"},
                    {"yahoo", "yahoo", "yahoo"}, {"facebook", "facebook", "facebook"},
                    {"twitter", "twitter", "twitter"}, {"irc", "irc", "irc"},
                    {"youtube", "youtube", "youtube"}, {"instagram", "instagram", "instagram"},
                    {"linkedin", "linkedin", "linkedin"}, {"tiktok", "tiktok", "tiktok"},
                    {"discord", "discord", "discord"}, {"twitch", "twitch", "twitch"},
                    {"github", "github", "github"}, {"reddit", "reddit", "reddit"},
                    {"snapchat", "snapchat", "snapchat"}, {"pinterest", "pinterest", "pinterest"},
                    {"whatsapp", "whatsapp", "whatsapp"}, {"telegram", "telegram", "telegram"},
                    {"visitors", "last_visitor", "visitors"}, {"fam_status", "fam_status", "fam_status"},
                    {"image_url", "image", "image_url"}
                };
                for (var field : plain) {
                    addField(labels, fields, texts, field[1], field[2], db.getData(user, field[0]));
                }
                var sex = switch (db.getData(user, "sex")) {
                    case "m" ->
                        texts.getOrDefault("gender_m", "");
                    case "f" ->
                        texts.getOrDefault("gender_f", "");
                    default ->
                        texts.getOrDefault("gender_n", "");
                };
                for (var label : labels.entrySet()) {
                    label.setValue(expand(label.getValue(), fields, request));
                }
                request.setAttribute("profile", labels);
                request.setAttribute("city", fields.get("city"));
                request.setAttribute("sex", sex);
                request.setAttribute("displayName", db.getData(user, "nick2") != null ? db.getData(user, "nick2") : user);
                request.setAttribute("points", db.getData(user, "points"));
                request.setAttribute("timestamp_login", ut.getTime(db.getLongData(user, "timestamp_login")));
                request.setAttribute("timestamp_reg", ut.getTime(db.getLongData(user, "timestamp_reg")));
                request.setAttribute("bday_day", orEmpty(db.getData(user, "bday_day")));
                request.setAttribute("bday_month", orEmpty(db.getData(user, "bday_month")));
                request.setAttribute("bday_year", orEmpty(db.getData(user, "bday_year")));
                request.setAttribute("color", orEmpty(db.getData(user, "color")));
                var isOwner = nick.equalsIgnoreCase(user);
                request.setAttribute("gbEntries", db.getGuestbook(user));
                request.setAttribute("gbOwner", isOwner);
                if (isOwner) {
                    request.setAttribute("gbPath", expand("%PATH_[profile]%", fields, request));
                    request.setAttribute("gbPage", expand("%path_profile%", fields, request));
                    request.setAttribute("gbSkin", skin);
                }
                request.setAttribute("gbForm", db.isRegistered(nick) && sid != null && cm.isValidConnectionId(sid));
                view = "user";
            } catch (NullPointerException npe) {
                failure = ut.getStackTrace(npe);
            }
        }
        db.updateNick(user, "visitors", nick);
        if (failure != null) {
            out.print(failure);
            return;
        }
        request.setAttribute("profileView", view);
%><t:layout design="${profileDesign}"><c:choose>
<c:when test="${profileView eq 'invalid'}"><h1><fmt:message key="common.error_3"/></h1>

<fmt:message key="common.your_sessionid_is_invalid"/>!
</c:when>
<c:when test="${profileView eq 'unregistered'}"><h1><fmt:message key="common.error_3"/></h1>

<fmt:message key="common.the_specified_user"/> <span style="font-weight: bold;">${user}</span> <fmt:message key="common.is_not_registered"/>
</c:when>
<c:otherwise><div class="profile-page">
  <div class="profile-header mb-4">
    <div class="d-flex align-items-center gap-3 mb-3">
      <img src="${page_base}/image.jsp?nick=${displayName}" class="profile-avatar rounded-circle shadow-sm flex-shrink-0" alt="${displayName}" width="120" height="120">
      <div class="flex-grow-1 min-w-0">
        <h1 class="profile-name h3 mb-2" style="color: #${color};">${displayName}</h1>
        <div class="d-flex flex-wrap gap-2 mb-2">
          <span class="badge bg-light text-dark border"><span class="fw-semibold"><fmt:message key="common.gender_3"/></span> ${sex}</span>
          <span class="badge bg-light text-dark border"><span class="fw-semibold"><fmt:message key="common.city_3"/></span> ${city}</span>
          <span class="badge bg-light text-dark border"><span class="fw-semibold"><fmt:message key="common.points"/></span> ${points}</span>
        </div>
        <div class="profile-birthday text-muted small">
          <span class="fw-semibold text-dark"><fmt:message key="common.birthday"/></span> ${bday_day}.${bday_month}.${bday_year}
        </div>
      </div>
    </div>
    <div class="profile-dates card bg-light border-0 shadow-sm mb-0">
      <div class="card-body py-2 px-3">
        <div class="row g-2 small">
          <div class="col-md-6"><fmt:message key="common.registered_on"/> <span class="fw-semibold text-dark">${timestamp_reg}</span></div>
          <div class="col-md-6"><fmt:message key="common.last_login"/> <span class="fw-semibold text-dark">${timestamp_login}</span></div>
        </div>
      </div>
    </div>
  </div>
  <div class="row g-4 mt-4">
    <div class="col-md-7">
      <div class="card shadow-sm h-100 profile-section-card mb-4">
        <div class="card-header bg-transparent border-bottom fw-semibold py-2">
          <span style="margin-right: 8px;">📋</span><fmt:message key="common.about"><fmt:param value="${displayName}"/></fmt:message>
        </div>
        <div class="card-body py-3">
          ${profile.description}
          ${profile.hobby}
          ${profile.status}
          ${profile.slogan}
          ${profile.signature}
        </div>
      </div>
    </div>
    <div class="col-md-5">
      <div class="card shadow-sm mb-4 profile-section-card">
        <div class="card-header bg-transparent border-bottom fw-semibold py-2">
          <span style="margin-right: 8px;">📡</span><fmt:message key="common.contact"/>
        </div>
         <div class="card-body py-3">
          ${profile.homepage}
          ${profile.facebook}
          ${profile.twitter}
          ${profile.youtube}
          ${profile.instagram}
          ${profile.linkedin}
          ${profile.tiktok}
          ${profile.discord}
          ${profile.twitch}
          ${profile.github}
          ${profile.reddit}
          ${profile.snapchat}
          ${profile.pinterest}
          ${profile.whatsapp}
          ${profile.telegram}
        </div>
      </div>
      <div class="card shadow-sm mb-4 profile-section-card">
        <div class="card-header bg-transparent border-bottom fw-semibold py-2">
          <span style="margin-right: 8px;">👤</span><fmt:message key="common.personal"/>
        </div>
        <div class="card-body py-3">
          ${profile.fam_status}
          ${profile.last_visitor}
        </div>
      </div>
    </div>
  </div>
  <div class="row g-4 mt-4">
    <div class="col-12">
      <div class="card shadow-sm profile-section-card mb-4">
        <div class="card-header bg-transparent border-bottom fw-semibold py-2">
          <span style="margin-right: 8px;">📖</span><fmt:message key="common.guestbook"/>
        </div>
        <div class="card-body py-3">
          <c:forEach var="entry" items="${gbEntries}">
<div class="guestbook-entry card mb-3">
  <div class="card-body py-2 px-3">
    <div class="d-flex align-items-center" style="gap: 0.75rem; margin-bottom: 0.25rem;">
      <span style="color: #${entry[2]}; font-weight: bold;">${entry[1]}</span>
      <span class="text-muted small">${entry[4]}</span>
      <c:if test="${gbOwner}"><form action="${gbPath}" method="get" accept-charset="utf-8" style="display:inline;margin-left:8px;"><input type="hidden" name="page" value="${gbPage}"><input type="hidden" name="user" value="${user}"><input type="hidden" name="owner" value="${owner}"><input type="hidden" name="skin" value="${gbSkin}"><input type="hidden" name="guestbook_delete" value="${entry[0]}"><button class="btn btn-sm btn-outline-danger" type="submit">Löschen</button></form></c:if>
    </div>
    <div class="profile-text">${entry[3]}</div>
  </div>
</div>
          </c:forEach>
          <c:if test="${gbForm}">
<div class="guestbook-form mt-3">
  <form action="${paths.profile}" method="get" accept-charset="utf-8">
    <input type="hidden" name="user" value="${user}">
    <input type="hidden" name="skin" value="${skin}">
    <input type="hidden" name="owner" value="${owner}">
    <input type="hidden" name="guestbook" value="1">
    <div class="input-group">
      <textarea class="form-control form-control-sm" name="guestbook_text" rows="2" placeholder="<fmt:message key="page.guestbook_form.write_a_guestbook_entry"/>"></textarea>
      <button class="btn btn-primary btn-sm" type="submit"><fmt:message key="page.guestbook_form.submit"/></button>
    </div>
  </form>
</div>
          </c:if>
        </div>
      </div>
    </div>
  </div>
</div>
</c:otherwise>
</c:choose></t:layout>
