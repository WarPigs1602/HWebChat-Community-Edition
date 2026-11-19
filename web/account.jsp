<%@ page session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.*, java.io.*, java.net.*, java.nio.file.*, java.sql.*, java.util.*, java.util.logging.*, jakarta.servlet.*, jakarta.servlet.http.*, jakarta.mail.*" %>
<%@ page import="static net.midiandmore.chat.Web.*" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    Map<String, String> map = Web.requestValues(request);
    var lang = (String) request.getAttribute("lang");
        var service = map.getOrDefault("service", "");
        var data = map.getOrDefault("data", "");
        var sid = map.getOrDefault("sid", "");
        var skin = map.getOrDefault("skin", "");
        var owner = map.getOrDefault("owner", "");
        var roomName = "";
        var conf = Bootstrap.getBoot().getConfig();
        var cm = Bootstrap.getBoot().getChatManager();
        var ut = Bootstrap.getBoot().getUtil();
        var db = conf.getDb();
        if (!owner.isBlank()) {
            roomName = db.getRoomNameByOwner(owner.toLowerCase());
        }
        var napping = !roomName.isBlank();
        var form = "";
        var alert = "";
        var vals = new HashMap<String, Object>();
        java.util.function.BiConsumer<String, Object> put = (key, value) -> {
            if (value != null) {
                vals.put(key, value);
            }
        };

        skin = ut.parseHost(skin, request)[1];
        var session = request.getSession(false);
        sid = session != null ? (String) session.getAttribute("sid") : null;
        skin = session != null ? (String) session.getAttribute("skin") : null;
        sid = sid != null ? sid : map.getOrDefault("sid", "");
        skin = skin != null ? skin : map.getOrDefault("skin", "");
        var sidv = sid != null ? cm.isValidConnectionId(sid) : false;
        var reg = false;
        String nick = null;
        if (sidv) {
            nick = cm.getNameFromId(sid);
            reg = db.isRegistered(nick);
        }
        if (!sidv) {
            form = "invalid_session";
        } else if (!reg) {
            form = "not_registered";
        } else if (service.equals("account")) {
            var profileKeys = new String[]{"homepage", "city", "hobby", "description", "slogan", "signature", "icq",
                "live", "yahoo", "facebook", "twitter", "irc", "youtube", "instagram", "linkedin", "tiktok", "discord",
                "twitch", "github", "reddit", "snapchat", "pinterest", "whatsapp", "telegram", "fam_status"};
            switch (data) {
                case "" -> {
                    for (var key : profileKeys) {
                        put.accept(key, db.getData(nick, key));
                    }
                    form = "profile";
                }
                case "change" -> {
                    for (var key : profileKeys) {
                        var value = map.getOrDefault(key, "");
                        put.accept(key, value);
                        db.updateNick(nick, key, value);
                    }
                    form = "profile_success";
                }
                default -> form = "unknown";
            }
        } else if (service.equals("password")) {
            switch (data) {
                case "change" -> {
                    var oldPwd = map.getOrDefault("old_pwd", "");
                    var newPwd = map.getOrDefault("new_pwd", "");
                    var newPwd2 = map.getOrDefault("new_pwd2", "");
                    Object errorMessage = null;
                    if (!db.checkPassword(nick, oldPwd)) {
                        errorMessage = Web.message(lang, "page.account_error_message_pass_wrong.your_password_is_wrong");
                    } else if (newPwd.length() < conf.getInt("min_pwd_length")) {
                        errorMessage = Web.message(lang, "common.your_password_is_too_short");
                    } else if (newPwd.length() > conf.getInt("max_pwd_length")) {
                        errorMessage = Web.message(lang, "common.your_password_is_too_long");
                    } else if (!newPwd.equals(newPwd2)) {
                        errorMessage = Web.message(lang, "common.your_passwords_do_not_match") + "!";
                    }
                    if (errorMessage == null) {
                        form = "password_success";
                        db.updatePassword(nick, newPwd);
                    } else {
                        form = "password";
                    }
                }
                case "" -> form = "password";
                default -> form = "unknown";
            }
        } else if (service.equals("delete")) {
            switch (data) {
                case "delete" -> {
                    var pwd = map.getOrDefault("pwd", "");
                    var pwd2 = map.getOrDefault("pwd2", "");
                    Object errorMessage = null;
                    if (!pwd.equals(pwd2)) {
                        errorMessage = Web.message(lang, "common.your_passwords_do_not_match") + "!";
                    } else if (!db.checkPassword(nick, pwd)) {
                        errorMessage = Web.message(lang, "page.account_error_message_pass_wrong.your_password_is_wrong");
                    }
                    if (errorMessage == null) {
                        form = "delete_success";
                        cm.quit(nick);
                        cm.removeFriend(nick);
                        db.delFriendsFromList(nick);
                        db.delNick(nick);
                    } else {
                        form = "delete";
                    }
                }
                case "" -> form = "delete";
                default -> form = "unknown";
            }
        } else if (service.equals("napping")) {
            String title = "";
            String bg_color_1 = "ffffff";
            String bg_color_2 = "ffffff";
            String color = "000000";
            String border_color = "cccccc";
            String link_color = "0000ff";
            String room = "";
            switch (data) {
                case "change" -> {
                    title = map.getOrDefault("title", "");
                    bg_color_1 = Bootstrap.getBoot().getChatServices().stripHash(map.getOrDefault("bg_color_1", ""));
                    bg_color_2 = Bootstrap.getBoot().getChatServices().stripHash(map.getOrDefault("bg_color_2", ""));
                    color = Bootstrap.getBoot().getChatServices().stripHash(map.getOrDefault("color", ""));
                    link_color = Bootstrap.getBoot().getChatServices().stripHash(map.getOrDefault("link_color", ""));
                    border_color = Bootstrap.getBoot().getChatServices().stripHash(map.getOrDefault("border_color", ""));
                    room = map.getOrDefault("room2", "");
                    form = "napping";
                    java.util.function.Predicate<String> badColor = c -> c.length() != 6 || !c.matches("[a-fA-F0-9]*");
                    if (badColor.test(bg_color_1) || badColor.test(bg_color_2) || badColor.test(border_color)
                            || badColor.test(color) || badColor.test(link_color)) {
                        alert = "color";
                    } else if (db.ownerExists(nick) && !db.getRoomNameByOwner(nick).equals(room)) {
                        alert = "used";
                    } else if (db.roomExists(room) && db.getRoomData(room, "owner").equalsIgnoreCase(nick)) {
                        db.updateRoomData(room, "owner", nick);
                        db.updateRoomData(room, "first_bgcolor", bg_color_1);
                        db.updateRoomData(room, "second_bgcolor", bg_color_2);
                        db.updateRoomData(room, "bordercolor", border_color);
                        db.updateRoomData(room, "linkcolor", link_color);
                        db.updateRoomData(room, "textcolor", color);
                        db.updateRoomData(room, "page_title", title);
                        form = "napping_change";
                    } else if (!db.roomExists(room)) {
                        db.addRoomData(nick, room, title, bg_color_1, bg_color_2, color, border_color, link_color);
                        db.updateRoomData(room, "su", nick);
                        form = "napping_add";
                    } else {
                        alert = "error";
                        napping = false;
                    }
                }
                case "" -> {
                    room = db.getRoomNameByOwner(nick.toLowerCase());
                    if (!room.isBlank()) {
                        title = db.getRoomData(room, "page_title");
                        var savedColor = db.getRoomData(room, "first_bgcolor");
                        bg_color_1 = savedColor.isBlank() ? "ffffff" : savedColor;
                        savedColor = db.getRoomData(room, "second_bgcolor");
                        bg_color_2 = savedColor.isBlank() ? "ffffff" : savedColor;
                        savedColor = db.getRoomData(room, "textcolor");
                        color = savedColor.isBlank() ? "000000" : savedColor;
                        savedColor = db.getRoomData(room, "bordercolor");
                        border_color = savedColor.isBlank() ? "cccccc" : savedColor;
                        savedColor = db.getRoomData(room, "linkcolor");
                        link_color = savedColor.isBlank() ? "0000ff" : savedColor;
                        room = db.getRoomData(room, "room");
                    }
                    if (!map.getOrDefault("room", "").isBlank()) {
                        map.replace("room", map.getOrDefault("room", ""), room);
                        var sessionRoom = session != null && !session.isNew() ? (String) session.getAttribute("room") : null;
                        put.accept("room", !room.isBlank() ? room : (sessionRoom != null ? sessionRoom : room));
                    }
                    form = "napping";
                }
                default -> form = "unknown";
            }
            if (!form.equals("unknown")) {
                put.accept("title", title);
                put.accept("bg_color_1", bg_color_1);
                put.accept("bg_color_2", bg_color_2);
                put.accept("color", color);
                put.accept("link_color", link_color);
                put.accept("border_color", border_color);
                put.accept("room2", room);
                put.accept("nick", nick);
            }
        } else if (service.equals("data")) {
            Object errorMessage = null;
            String mail = null;
            String gender = null;
            String day = null;
            String month = null;
            String year = null;
            switch (data) {
                case "change" -> {
                    mail = map.getOrDefault("mail", "");
                    gender = map.getOrDefault("gender", "");
                    day = map.getOrDefault("day", "");
                    month = map.getOrDefault("month", "");
                    year = map.getOrDefault("year", "");
                    if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
                        errorMessage = Web.message(lang, "common.your_gender_is_flawed");
                    } else if (!ut.isValidDate(day, month, year)) {
                        errorMessage = Web.message(lang, "common.your_date_of_birth_is") + "!";
                    } else if (ut.getAge(day, month, year) > conf.getInt("max_age")) {
                        errorMessage = Web.message(lang, "common.your_age_is_unrealistic");
                    } else if (ut.getAge(day, month, year) < conf.getInt("min_age")) {
                        errorMessage = Web.message(lang, "common.you_are_too_young_for") + "!";
                    } else if (!mail.contains("@")) {
                        errorMessage = Web.message(lang, "page.reg_error_message_mail_invalid.your_email_address_is_incorrect");
                    } else if (mail.contains(" ") || mail.contains("&") || mail.contains("\"") || mail.contains("'")) {
                        errorMessage = Web.message(lang, "page.reg_error_message_mail_invalid.your_email_address_is_incorrect");
                    }
                    if (errorMessage == null) {
                        form = "data_success";
                        db.updateNick(nick, "sex", gender);
                        db.updateNick(nick, "bday_day", day);
                        db.updateNick(nick, "bday_month", month);
                        db.updateNick(nick, "bday_year", year);
                        db.updateNick(nick, "mail", mail);
                    } else {
                        form = "data";
                        put.accept("selected_no", gender.equals("m") || gender.equals("f") ? "" : " selected");
                        put.accept("selected_male", gender.equals("m") ? " selected" : "");
                        put.accept("selected_female", gender.equals("f") ? " selected" : "");
                        put.accept("mail", mail);
                        put.accept("day", day);
                        put.accept("month", month);
                        put.accept("year", year);
                    }
                }
                case "" -> {
                    mail = db.getData(nick, "mail");
                    gender = db.getData(nick, "sex");
                    day = db.getData(nick, "bday_day");
                    month = db.getData(nick, "bday_month");
                    year = db.getData(nick, "bday_year");
                    mail = mail == null ? "" : mail;
                    gender = gender == null ? "" : gender;
                    day = day == null ? "" : day;
                    month = month == null ? "" : month;
                    year = year == null ? "" : year;
                    form = "data";
                    put.accept("selected_no", gender.equals("m") || gender.equals("f") ? "" : " selected");
                    put.accept("selected_male", gender.equals("m") ? " selected" : "");
                    put.accept("selected_female", gender.equals("f") ? " selected" : "");
                    put.accept("mail", mail);
                    put.accept("day", day);
                    put.accept("month", month);
                    put.accept("year", year);
                }
                default -> form = "unknown";
            }
        } else if (service.equals("picture")) {
            form = "picture";
        } else if (service.equals("picture_del")) {
            switch (data) {
                case "delete":
                    db.updateNick(nick, "image_upload", null);
                    db.updateNick(nick, "image_url", null);
                    form = "picture_del_success";
                    break;
                case "":
                    form = "picture_del";
                    break;
                default:
                    form = "picture_del_unknown";
                    break;
            }

        } else if (!service.equals("")) {
            form = "unknown";
        } else {
            form = "menu";
        }
        if (!vals.isEmpty()) {
            Web.view("page", request, vals);
        }
        var design = "account";
        if (form.equals("invalid_session") || form.equals("not_registered")) {
            // the designs are paired the other way round than for the forms
            design = napping ? "account_error" : "account_error_napping";
        } else if (napping) {
            design = switch (form) {
                case "data", "password", "delete" -> "napping";
                case "unknown" -> "account_error_napping";
                default -> "napping_account";
            };
        }
        request.setAttribute("acc_form", form);
        request.setAttribute("acc_alert", alert);
        request.setAttribute("acc_nap", napping);
        request.setAttribute("acc_design", design);
%>
<t:layout design="${acc_design}">
<c:choose>
<c:when test="${acc_form eq 'invalid_session'}"><h1><fmt:message key="common.error_3"/></h1>
<fmt:message key="common.your_sessionid_is_invalid"/>!</c:when>
<c:when test="${acc_form eq 'not_registered'}"><h1><fmt:message key="common.error_3"/></h1>

<fmt:message key="common.you_are_not_registered"/>
</c:when>
<c:when test="${acc_form eq 'menu'}"><c:choose>
<c:when test="${acc_nap}"><form action="${paths.account}" method="POST" accept-charset="utf-8">
  <p class="form-section-title">
    <fmt:message key="page.account_form_napping2.here_you_can_manage_your"/>
  </p>
  <p>
  <fmt:message key="page.account_form_napping2.what_do_you_want_to"/></br>
  <select class="form-control form-control-sm col-md-6 mb-3" name="service" size="6">
    <option value="account" selected><fmt:message key="common.processing_profile"/></option>
    <option value="napping">Chatnapping</option>
    <option value="picture"><fmt:message key="common.upload_profile_picture"/></option>
    <option value="password"><fmt:message key="common.processing_the_password"/></option>
    <option value="data"><fmt:message key="common.processing_account_data"/></option>
    <option value="delete"><fmt:message key="common.delete_account"/></option>
  </select>
  </p>
  <input type="hidden" name="owner" value="${owner}">
  <p class="form-section-title">
    <fmt:message key="page.account_form_napping2.and_off_we_go"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.continue_to_the_next_page"/>">
  </p>
</form></c:when>
<c:otherwise><h1><fmt:message key="common.account"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>

<form action="${paths.account}" method="POST" accept-charset="utf-8">
  <p class="form-section-title"><fmt:message key="common.account_2"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.what_do_you_want_to"/></label>
      <select class="form-select form-select-sm" name="service" size="6">
        <option value="account" selected><fmt:message key="common.processing_profile"/></option>
        <option value="napping">Chatnapping</option>
        <option value="picture"><fmt:message key="common.upload_profile_picture"/></option>
        <option value="password"><fmt:message key="common.processing_the_password"/></option>
        <option value="data"><fmt:message key="common.processing_account_data"/></option>
        <option value="delete"><fmt:message key="common.delete_account"/></option>
      </select>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.continue"/></p>

  <input type="submit" class="btn btn-primary btn-sm" name="OK" value="<fmt:message key="common.continue_to_the_next_page"/>">
</form></c:otherwise>
</c:choose></c:when>

<c:when test="${acc_form eq 'unknown'}"><h1><fmt:message key="common.error"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>

<div class="card bg-light border-0 shadow-sm mb-3">
  <div class="card-body">
    <p class="mb-0"><fmt:message key="common.the_requested_document_does_not"/></p>
  </div>
</div></c:when>

<c:when test="${acc_form eq 'profile'}"><h1>${SERVER_SOFTWARE} &ndash; Account</h1>

<form action="${paths.account}" method="POST" accept-charset="utf-8">
  <input type="hidden" name="service" value="account">
  <input type="hidden" name="data" value="change"><c:if test="${acc_nap}">
  <input type="hidden" name="owner" value="${owner}"></c:if>

  <p class="form-section-title"><fmt:message key="common.personal_information"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Homepage</label>
      <input type="url" class="form-control form-control-sm" name="homepage" value="${homepage}" placeholder="https://">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.city"/></label>
      <input type="text" class="form-control form-control-sm" name="city" value="${city}" placeholder="<fmt:message key="common.city_2"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Hobby</label>
      <input type="text" class="form-control form-control-sm" name="hobby" value="${hobby}" placeholder="<fmt:message key="common.hobbies"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.family_status_2"/></label>
      <input type="text" class="form-control form-control-sm" name="fam_status" value="${fam_status}" placeholder="<fmt:message key="common.e_g_single"/>">
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.about_me"/></p>

  <div class="mb-3">
    <label class="form-label fw-semibold small"><fmt:message key="common.description_2"/></label>
    <textarea class="form-control form-control-sm" name="description" rows="3" placeholder="<fmt:message key="common.tell_us_about_yourself"/>">${description}</textarea>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Motto</label>
      <input type="text" class="form-control form-control-sm" name="slogan" value="${slogan}" placeholder="<fmt:message key="common.your_motto"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.signature_2"/></label>
      <input type="text" class="form-control form-control-sm" name="signature" value="${signature}" placeholder="<fmt:message key="common.signature_2"/>">
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.contact_social"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Facebook</label>
      <input type="text" class="form-control form-control-sm" name="facebook" value="${facebook}" placeholder="<fmt:message key="common.facebook_profile"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Twitter / X</label>
      <input type="text" class="form-control form-control-sm" name="twitter" value="${twitter}" placeholder="@username">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">YouTube</label>
      <input type="text" class="form-control form-control-sm" name="youtube" value="${youtube}" placeholder="<fmt:message key="common.youtube_channel"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Instagram</label>
      <input type="text" class="form-control form-control-sm" name="instagram" value="${instagram}" placeholder="@username">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">LinkedIn</label>
      <input type="text" class="form-control form-control-sm" name="linkedin" value="${linkedin}" placeholder="<fmt:message key="common.username"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">TikTok</label>
      <input type="text" class="form-control form-control-sm" name="tiktok" value="${tiktok}" placeholder="@username">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Discord</label>
      <input type="text" class="form-control form-control-sm" name="discord" value="${discord}" placeholder="<fmt:message key="common.username"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Twitch</label>
      <input type="text" class="form-control form-control-sm" name="twitch" value="${twitch}" placeholder="<fmt:message key="common.username"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">GitHub</label>
      <input type="text" class="form-control form-control-sm" name="github" value="${github}" placeholder="<fmt:message key="common.username"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Reddit</label>
      <input type="text" class="form-control form-control-sm" name="reddit" value="${reddit}" placeholder="<fmt:message key="common.username"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Snapchat</label>
      <input type="text" class="form-control form-control-sm" name="snapchat" value="${snapchat}" placeholder="<fmt:message key="common.username"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Pinterest</label>
      <input type="text" class="form-control form-control-sm" name="pinterest" value="${pinterest}" placeholder="<fmt:message key="common.username"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">WhatsApp</label>
      <input type="text" class="form-control form-control-sm" name="whatsapp" value="${whatsapp}" placeholder="<fmt:message key="common.phone_number_with_country_code"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Telegram</label>
      <input type="text" class="form-control form-control-sm" name="telegram" value="${telegram}" placeholder="@username">
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.save"/></p>

  <input type="submit" class="btn btn-primary btn-sm" name="OK" value="<fmt:message key="common.submit_change_2"/>">
</form></c:when>

<c:when test="${acc_form eq 'profile_success'}"><h1>${SERVER_SOFTWARE}-Account!</h1>

  <p class="form-section-title">
    <fmt:message key="common.profile_change_from_successful"><fmt:param value="${nick}"/></fmt:message>
  </p>
  <div class="card bg-light border-0 shadow-sm mb-3">
    <div class="card-body">
      <div class="row g-2">
        <div class="col-md-6">
          <div class="profile-field mb-2"><span class="me-1">🌐</span> <strong>Homepage:</strong> <span class="profile-text">${homepage}</span></div>
          <div class="profile-field mb-2"><span class="me-1">📍</span> <strong><fmt:message key="common.city_3"/></strong> <span class="profile-text">${city}</span></div>
          <div class="profile-field mb-2"><span class="me-1">🎯</span> <strong>Hobby:</strong> <span class="profile-text">${hobby}</span></div>
          <div class="profile-field mb-2"><span class="me-1">❤️</span> <strong><fmt:message key="common.family_status"/></strong> <span class="profile-text">${fam_status}</span></div>
        </div>
        <div class="col-md-6">
          <div class="profile-field mb-2"><span class="me-1">📝</span> <strong><fmt:message key="common.description"/></strong> <span class="profile-text">${description}</span></div>
          <div class="profile-field mb-2"><span class="me-1">💬</span> <strong>Motto:</strong> <span class="profile-text">${slogan}</span></div>
          <div class="profile-field mb-2"><span class="me-1">✍️</span> <strong><fmt:message key="common.signature"/></strong> <span class="profile-text">${signature}</span></div>
        </div>
      </div>
      <hr class="my-3">
      <div class="row g-2">
        <div class="col-md-6">
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-facebook"></i></span> <strong>Facebook:</strong> <span class="profile-text">${facebook}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-instagram"></i></span> <strong>Instagram:</strong> <span class="profile-text">${instagram}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-linkedin-in"></i></span> <strong>LinkedIn:</strong> <span class="profile-text">${linkedin}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-tiktok"></i></span> <strong>TikTok:</strong> <span class="profile-text">${tiktok}</span></div>
        </div>
        <div class="col-md-6">
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-x-twitter"></i></span> <strong>Twitter / X:</strong> <span class="profile-text">${twitter}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-youtube"></i></span> <strong>YouTube:</strong> <span class="profile-text">${youtube}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-discord"></i></span> <strong>Discord:</strong> <span class="profile-text">${discord}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-twitch"></i></span> <strong>Twitch:</strong> <span class="profile-text">${twitch}</span></div>
        </div>
      </div>
      <hr class="my-3">
      <div class="row g-2">
        <div class="col-md-6">
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-github"></i></span> <strong>GitHub:</strong> <span class="profile-text">${github}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-reddit-alien"></i></span> <strong>Reddit:</strong> <span class="profile-text">${reddit}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-snapchat"></i></span> <strong>Snapchat:</strong> <span class="profile-text">${snapchat}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-pinterest"></i></span> <strong>Pinterest:</strong> <span class="profile-text">${pinterest}</span></div>
        </div>
        <div class="col-md-6">
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-whatsapp"></i></span> <strong>WhatsApp:</strong> <span class="profile-text">${whatsapp}</span></div>
          <div class="profile-field mb-2"><span class="profile-social-icon"><i class="fa-brands fa-telegram"></i></span> <strong>Telegram:</strong> <span class="profile-text">${telegram}</span></div>
        </div>
      </div>
    </div>
  </div></c:when>

<c:when test="${acc_form eq 'password'}"><h1>${SERVER_SOFTWARE} &ndash; <fmt:message key="common.password_2"/></h1>

<form action="${paths.account}" method="POST" accept-charset="utf-8">
  <input type="hidden" name="service" value="password">
  <input type="hidden" name="data" value="change">

  <p class="form-section-title"><fmt:message key="common.change_password"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.old_password"/></label>
      <input type="password" class="form-control form-control-sm" name="old_pwd" value="" placeholder="<fmt:message key="common.old_password"/>">
      <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span>
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.new_password"/></label>
      <input type="password" class="form-control form-control-sm" name="new_pwd" value="" placeholder="<fmt:message key="common.new_password"/>">
      <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span>
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.repeat_new_password"/></label>
      <input type="password" class="form-control form-control-sm" name="new_pwd2" value="" placeholder="<fmt:message key="common.repeat_new_password"/>">
      <span class="form-hint"><fmt:message key="common.to_make_sure_you_didn"/></span>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.save"/></p>

  <input type="submit" class="btn btn-primary btn-sm" name="OK" value="<fmt:message key="common.submit_change"/>">
</form></c:when>

<c:when test="${acc_form eq 'password_success'}"><h1>${SERVER_SOFTWARE}<fmt:message key="common.password_3"/></h1>

  <p class="form-section-title">
    <fmt:message key="common.password_change_successful"/>!
  </p>
  <div class="card bg-light border-0 shadow-sm mb-3">
    <div class="card-body">
      <p class="mb-0"><fmt:message key="common.you_have_successfully_changed_your"/> <strong>${nick}</strong><fmt:message key="common.ge_ndert"/></p>
    </div>
  </div></c:when>

<c:when test="${acc_form eq 'delete'}"><h1>${SERVER_SOFTWARE} &ndash; <fmt:message key="common.delete_account"/></h1>

<form action="${paths.account}" method="POST" accept-charset="utf-8">
  <input type="hidden" name="service" value="delete">
  <input type="hidden" name="data" value="delete">

  <p class="form-section-title"><fmt:message key="common.required_information"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.your_password"/></label>
      <input type="password" class="form-control form-control-sm" name="pwd" value="" placeholder="<fmt:message key="common.password_2"/>">
      <span class="form-hint"><fmt:message key="common.should_be_at_least_four"/></span>
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.repeat_your_password"/></label>
      <input type="password" class="form-control form-control-sm" name="pwd2" value="" placeholder="<fmt:message key="common.repeat_password"/>">
      <span class="form-hint"><fmt:message key="common.to_make_sure_you_didn"/></span>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.confirm"/></p>

  <input type="submit" class="btn btn-danger btn-sm" name="OK" value="<fmt:message key="common.delete_account"/>">
</form></c:when>

<c:when test="${acc_form eq 'delete_success'}"><h1>${SERVER_SOFTWARE}<fmt:message key="common.delete_account_2"/></h1>

  <p class="form-section-title">
    <fmt:message key="common.your_account_has_been_deleted"/>!
  </p>
  <div class="card bg-light border-0 shadow-sm mb-3">
    <div class="card-body">
      <p class="mb-0"><fmt:message key="common.you_have_successfully_deleted_your"/> <strong>${nick}</strong><fmt:message key="common.gel_scht"/></p>
    </div>
  </div></c:when>

<c:when test="${acc_form eq 'data'}"><h1>
${SERVER_SOFTWARE} <fmt:message key="common.standard_data"/>
</h1>
<form action="${paths.account}" method="POST" accept-charset="utf-8">
  <input type="hidden" name="service" value="data">
  <input type="hidden" name="data" value="change">

  <p class="form-section-title"><fmt:message key="common.necessary_information"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-4">
      <label class="form-label fw-semibold small"><fmt:message key="common.gender_2"/></label>
      <select class="form-select form-select-sm" name="gender" size="1">
        <option value="-"${selected_no}><fmt:message key="common.not_specified"/></option>
        <option value="m"${selected_male}><fmt:message key="common.man"/></option>
        <option value="f"${selected_female}><fmt:message key="common.woman"/></option>
      </select>
    </div>
    <div class="col-md-8">
      <label class="form-label fw-semibold small"><fmt:message key="common.email_address"/></label>
      <input type="email" class="form-control form-control-sm" name="mail" value="${mail}" placeholder="max.mustermann@example.com">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-4">
      <label class="form-label fw-semibold small"><fmt:message key="common.day"/></label>
      <select class="form-select form-select-sm" name="day" size="1" aria-label="<fmt:message key="common.day"/>">
<c:forEach var="d" begin="1" end="31"><c:set var="dd" value="${d lt 10 ? '0' : ''}${d}"/>        <option value="${dd}">${dd}</option>
</c:forEach>      </select>
    </div>
    <div class="col-md-4">
      <label class="form-label fw-semibold small"><fmt:message key="common.month"/></label>
      <select class="form-select form-select-sm" name="month" size="1" aria-label="<fmt:message key="common.month"/>">
        <option value="01"><fmt:message key="common.january"/></option>
        <option value="02"><fmt:message key="common.february"/></option>
        <option value="03"><fmt:message key="common.march"/></option>
        <option value="04">April</option>
        <option value="05"><fmt:message key="common.may"/></option>
        <option value="06"><fmt:message key="common.june"/></option>
        <option value="07"><fmt:message key="common.july"/></option>
        <option value="08">August</option>
        <option value="09">September</option>
        <option value="10"><fmt:message key="common.october"/></option>
        <option value="11">November</option>
        <option value="12"><fmt:message key="common.december"/></option>
      </select>
    </div>
    <div class="col-md-4">
      <label class="form-label fw-semibold small"><fmt:message key="common.year"/></label>
      <select class="form-select form-select-sm" name="year" size="1" aria-label="<fmt:message key="common.year"/>">
<c:forEach var="y" begin="1916" end="2026">        <option value="${y}"${y eq 2026 ? ' selected' : ''}>${y}</option>
</c:forEach>      </select>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.save"/></p>

  <input type="submit" class="btn btn-primary btn-sm" name="OK" value="<fmt:message key="common.submit_change"/>">
</form></c:when>

<c:when test="${acc_form eq 'data_success'}"><h1>${SERVER_SOFTWARE}-Account!</h1>

  <p class="form-section-title">
    <fmt:message key="common.account_data_for_successfully_changed"><fmt:param value="${nick}"/></fmt:message>!
  </p>
  <div class="card bg-light border-0 shadow-sm mb-3">
    <div class="card-body">
      <p class="mb-0"><fmt:message key="common.account_data_successfully_updated"/></p>
    </div>
  </div></c:when>

<c:when test="${acc_form eq 'napping'}"><h1>
${SERVER_SOFTWARE} &ndash; Chatnapping
</h1>
<c:choose>
<c:when test="${acc_alert eq 'color'}"><p class="alert alert-warning">
  <fmt:message key="common.colours_must_be_hexadecimal_and"/>!
</p>
</c:when>
<c:when test="${acc_alert eq 'used'}"><p class="alert alert-warning">
  <fmt:message key="common.you_can_only_register_one"/>
</p>
</c:when>
<c:when test="${acc_alert eq 'error'}"><p class="alert alert-danger">
  <fmt:message key="common.the_room_is_already_taken"/>
</p>
</c:when>
</c:choose><form id="napping-form" action="${paths.account}" method="POST" accept-charset="utf-8">
  <input type="hidden" name="service" value="napping">
  <input type="hidden" name="data" value="change"><c:if test="${acc_nap}">
  <input type="hidden" name="owner" value="${owner}"></c:if>

  <p class="form-section-title"><fmt:message key="common.preview"/></p>

  <fmt:message key="common.preview_2" var="napPreviewTitle"><fmt:param value="__TITLE__"/></fmt:message>
  <div class="card mb-3">
    <div class="card-header" id="preview_header" style="background-color: #${bg_color_2}; color: #${bg_color_1};">
      <span id="preview_title" data-template="<c:out value='${napPreviewTitle}'/>"><c:out value="${title}"/></span>
    </div>
    <div class="card-body" id="preview_body" style="background-color: #${bg_color_1}; color: #${color};">
      <p id="preview_text"><fmt:message key="common.sample_text_in_the_chosen"/></p>
      <a href="#" id="preview_link" style="color: #${link_color};"><fmt:message key="common.sample_link"/></a>
      <div id="preview_box" style="border: 2px solid #${border_color}; padding: 10px; margin-top: 10px;">
        <fmt:message key="common.sample_border"/>
      </div>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.room_configuration_2"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.room_name"/></label>
      <input type="text" class="form-control form-control-sm" name="room2" value="${room2}" placeholder="<fmt:message key="common.room_name"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.your_chat_title"/></label>
      <input type="text" class="form-control form-control-sm" id="room-title" name="title" value="${title}" placeholder="<fmt:message key="common.your_chat_title"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.background_colour_1"/></label>
      <div class="input-group input-group-sm">
        <span class="input-group-text">#</span>
        <input type="text" class="form-control form-control-sm color-hex" id="bg_color_1" name="bg_color_1" value="${bg_color_1}" placeholder="RRGGBB" maxlength="7">
        <input type="color" class="color-picker" id="bg_color_1_picker" value="#${bg_color_1}">
      </div>
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.background_colour_2"/></label>
      <div class="input-group input-group-sm">
        <span class="input-group-text">#</span>
        <input type="text" class="form-control form-control-sm color-hex" id="bg_color_2" name="bg_color_2" value="${bg_color_2}" placeholder="RRGGBB" maxlength="7">
        <input type="color" class="color-picker" id="bg_color_2_picker" value="#${bg_color_2}">
      </div>
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.text_colour"/></label>
      <div class="input-group input-group-sm">
        <span class="input-group-text">#</span>
        <input type="text" class="form-control form-control-sm color-hex" id="color" name="color" value="${color}" placeholder="RRGGBB" maxlength="7">
        <input type="color" class="color-picker" id="color_picker" value="#${color}">
      </div>
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.link_colour"/></label>
      <div class="input-group input-group-sm">
        <span class="input-group-text">#</span>
        <input type="text" class="form-control form-control-sm color-hex" id="link_color" name="link_color" value="${link_color}" placeholder="RRGGBB" maxlength="7">
        <input type="color" class="color-picker" id="link_color_picker" value="#${link_color}">
      </div>
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.border_colour"/></label>
      <div class="input-group input-group-sm">
        <span class="input-group-text">#</span>
        <input type="text" class="form-control form-control-sm color-hex" id="border_color" name="border_color" value="${border_color}" placeholder="RRGGBB" maxlength="7">
        <input type="color" class="color-picker" id="border_color_picker" value="#${border_color}">
      </div>
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.save"/></p>

  <input type="submit" class="btn btn-primary btn-sm" name="OK" value="<fmt:message key="common.submit_change"/>">
</form>

<script>
(function() {
    function syncColor(pickerId, textId) {
        var picker = document.getElementById(pickerId);
        var text = document.getElementById(textId);
        if (!picker || !text) return;

        picker.addEventListener('input', function() {
            text.value = this.value.substring(1);
            updatePreview();
        });

        text.addEventListener('input', function() {
            var val = this.value.trim().replace(/^#/, '').slice(0, 6);
            this.value = val;
            if (val.length === 6 && /^[a-fA-F0-9]+$/.test(val)) {
                picker.value = '#' + val;
            }
            updatePreview();
        });
    }

    function updatePreview() {
        function colorValue(id, fallback) {
            var value = document.getElementById(id).value.trim().replace(/^#/, '');
            return /^[a-fA-F0-9]{6}$/.test(value) ? value : fallback;
        }
        var bg1 = colorValue('bg_color_1', 'ffffff');
        var bg2 = colorValue('bg_color_2', 'ffffff');
        var color = colorValue('color', '000000');
        var link = colorValue('link_color', '0000ff');
        var border = colorValue('border_color', 'cccccc');

        var header = document.getElementById('preview_header');
        var body = document.getElementById('preview_body');
        var textEl = document.getElementById('preview_text');
        var linkEl = document.getElementById('preview_link');
        var box = document.getElementById('preview_box');

        if (header) { header.style.backgroundColor = '#' + bg2; header.style.color = '#' + bg1; }
        if (body) body.style.backgroundColor = '#' + bg1;
        if (body) body.style.color = '#' + color;
        if (textEl) textEl.style.color = '#' + color;
        if (linkEl) linkEl.style.color = '#' + link;
        if (box) box.style.borderColor = '#' + border;
    }

    syncColor('bg_color_1_picker', 'bg_color_1');
    syncColor('bg_color_2_picker', 'bg_color_2');
    syncColor('color_picker', 'color');
    syncColor('link_color_picker', 'link_color');
    syncColor('border_color_picker', 'border_color');

    var titleInput = document.getElementById('room-title');
    var previewTitle = document.getElementById('preview_title');
    function updatePreviewTitle() {
        if (titleInput && previewTitle) {
            previewTitle.textContent = previewTitle.dataset.template.replace('__TITLE__', titleInput.value);
        }
    }
    if (titleInput) titleInput.addEventListener('input', updatePreviewTitle);

    var form = document.getElementById('napping-form');
    if (form) {
        form.addEventListener('submit', function() {
            form.querySelectorAll('.color-hex').forEach(function(input) {
                input.value = input.value.trim().replace(/^#/, '').slice(0, 6);
            });
        });
    }
    updatePreviewTitle();
    updatePreview();
})();
</script></c:when>

<c:when test="${acc_form eq 'napping_add'}"><h1>
${SERVER_SOFTWARE} &ndash; Chatnapping
</h1>
<p>
  <fmt:message key="common.you_have_added_the_room"/> <strong>${room2}</strong> <fmt:message key="common.and_can_add_the_room"/>:
</p>
<textarea readonly class="prism-live line-numbers language-html fill"><form method="POST" name="login" action="${host_http}${paths.login_chat}" target="_blank" accept-charset="utf-8">
  <input maxlength="20" name="nick" placeholder="Nickname"><br>
  <input maxlength="20" name="pwd" type="password" placeholder="<fmt:message key="common.password_2"/>"><br>
  <input name="room" value="${room2}" type="hidden">
  <input name="owner" value="${nick}" type="hidden">
  <input name="skin" value="${skin}" type="hidden">
  <input value="Login" type="submit">
</form>
<p>
  <a href="${host_http}${paths.login_chat}?skin=${skin}&room=${room2}&owner=${nick}">Guest access</a><br>
  <a href="${host_http}${paths.register}&owner=${nick}">Register</a><br>
  <a href="${host_http}${paths.password}&owner=${nick}">Forgot password?</a>
</p>
<p class="users">
<script type="text/javascript" src="${host_http}${paths.script_room_napping}"></script>
</p>
</textarea></c:when>

<c:when test="${acc_form eq 'napping_change'}"><h1>
${SERVER_SOFTWARE} &ndash; Chatnapping
</h1>
<p>
  <fmt:message key="common.you_have_changed_the_room"/> <strong>${room2}</strong> <fmt:message key="common.and_can_add_the_room_2"/>:
</p>
<textarea readonly class="prism-live line-numbers language-html fill"><form method="POST" name="login" action="${host_http}${paths.login_chat}" target="_blank" accept-charset="utf-8">
  <input maxlength="20" name="nick" placeholder="Nickname"><br>
  <input maxlength="20" name="pwd" type="password" placeholder="<fmt:message key="common.password_2"/>"><br>
  <input name="room" value="${room2}" type="hidden">
  <input name="owner" value="${nick}" type="hidden">
  <input name="skin" value="${skin}" type="hidden">
  <input value="Login" type="submit">
</form>
<p>
  <a href="${host_http}${paths.login_chat}?skin=${skin}&room=${room2}&owner=${nick}"><fmt:message key="common.guest_access"/></a><br>
  <a href="${host_http}${paths.register}&owner=${nick}"><fmt:message key="common.register"/></a><br>
  <a href="${host_http}${paths.password}&owner=${nick}"><fmt:message key="common.forgot_password"/></a>
</p>
<p class="users">
<script type="text/javascript" src="${host_http}${paths.script_room_napping}"></script>
</p>
</textarea></c:when>

<c:when test="${acc_form eq 'picture'}"><h1>${SERVER_SOFTWARE} <fmt:message key="common.profile_picture"/></h1>


<form action="${page_base}/upload.jsp" method="post" enctype="multipart/form-data"><input type="hidden" name="upload_context" value="chat">
  <input type="hidden" name="service" value="password">
  <input type="hidden" name="data" value="change"><c:if test="${acc_nap}">
  <input type="hidden" name="owner" value="${owner}"></c:if>
  <p class="form-section-title">
    <fmt:message key="common.choose_a_file_to_upload"/>!
  </p>
  <p>
  <input type="file" name="file" size="50" class="form-control-file mb-2"/>
  <a href="${page_base}/account.jsp?service=picture_del&amp;data=delete&amp;skin=${skin}"><fmt:message key="common.click_here_to_delete_an"/>!</a>
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done_2"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.submit_change"/>">
  </p>
</form></c:when>

<c:when test="${acc_form eq 'picture_del'}"><h1>${SERVER_SOFTWARE}<fmt:message key="common.delete_the_profile_picture"/>!!</h1>


  <p class="form-section-title">
    <fmt:message key="common.delete_profile_picture"/>!
  </p>
  <p>
    <fmt:message key="common.please_click_2"/> <a href="${page_base}/account.jsp?service=picture_del&amp;data=delete&amp;skin=${skin}"><fmt:message key="common.here"/></a> <fmt:message key="common.to_continue_the_deletion_process"/>!
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done"/>
  </p></c:when>

<c:when test="${acc_form eq 'picture_del_success'}"><h1>${SERVER_SOFTWARE}<fmt:message key="common.delete_the_profile_picture"/>!!</h1>


  <p class="form-section-title">
    <fmt:message key="common.delete_successfully"/>!
  </p>
  <p>
    <fmt:message key="common.your_profile_picture_has_been"/>!
  </p>
  <p class="form-section-title">

  </p></c:when>

<c:when test="${acc_form eq 'picture_del_unknown'}"><h1>${SERVER_SOFTWARE}<fmt:message key="common.delete_the_profile_picture"/>!!</h1>


  <p class="form-section-title">
    <fmt:message key="common.do_not_delete_successfully"/>!
  </p>
  <p>
    <fmt:message key="common.your_profile_picture_could_not"/>!
  </p>
  <p class="form-section-title">

  </p></c:when>
</c:choose>
</t:layout>
