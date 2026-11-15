<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.util.Map, net.midiandmore.chat.Web" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }

    Map<String, String> values = Web.requestValues(request);
    String service = values.getOrDefault("service", "");
    String gender = values.getOrDefault("reg_gender", "");
    String nick = values.getOrDefault("reg_nick", "");
    String mail = values.getOrDefault("reg_mail", "");
    String mail2 = values.getOrDefault("reg_mail2", "");
    String question = values.getOrDefault("reg_reminder_question", "");
    String answer = values.getOrDefault("reg_reminder_answer", "");
    String captcha = values.getOrDefault("reg_captcha", "");
    String cid = values.getOrDefault("reg_cid", "");
    String day = values.getOrDefault("reg_day", "");
    String month = values.getOrDefault("reg_month", "");
    String year = values.getOrDefault("reg_year", "");
    String owner = values.getOrDefault("owner", "");
    String skin = Web.skin(values.getOrDefault("skin", ""), request);
    String lang = (String) request.getAttribute("lang");
    String errorKey = "";
    String errorSuffix = "";
    boolean success = false;
    Web.RegistrationAddresses addresses = Web.registrationAddresses(request);

    if (!gender.equals("-") && !gender.equals("m") && !gender.equals("f")) {
        errorKey = "common.your_gender_is_flawed";
    } else if (!nick.matches(Web.configValue("allowed_chars"))) {
        errorKey = "page.reg_error_message_nick_chars.your_nickname_must_contain_only";
    } else if (Web.configInt("guest") == 1
            && nick.toLowerCase().startsWith(Web.configValue("guest_prefix").toLowerCase())) {
        errorKey = "page.reg_error_message_nick_guest.your_nickname_may_not_be";
    } else if (nick.length() < Web.configInt("min_nick_length")) {
        errorKey = "page.reg_error_message_nick_short.your_nickname_is_too_short";
    } else if (nick.length() > Web.configInt("max_nick_length")) {
        errorKey = "page.reg_error_message_nick_long.your_nickname_is_too_long";
    } else if (Web.isCommandNick(nick)) {
        errorKey = "page.reg_error_message_nick_command.your_nickname_corresponds_to_a";
    } else if (!Web.isValidDate(day, month, year)) {
        errorKey = "common.your_date_of_birth_is";
        errorSuffix = "!";
    } else if (Web.age(day, month, year) > Web.configInt("max_age")) {
        errorKey = "common.your_age_is_unrealistic";
    } else if (Web.age(day, month, year) < Web.configInt("min_age")) {
        errorKey = "common.you_are_too_young_for";
        errorSuffix = "!";
    } else if (Web.isRegistered(nick)) {
        errorKey = "page.reg_error_message_nick_registered.your_nickname_is_already_registered";
    } else if (Web.isBanned(addresses.raw()) || Web.isBanned(addresses.resolved())) {
        errorKey = "page.reg_error_message_nick_banned.you_have_been_banned";
    } else if (!mail.equals(mail2)) {
        errorKey = "common.your_email_addresses_do_not";
        errorSuffix = "!";
    } else if (!mail.contains("@") || mail.contains(" ") || mail.contains("&")
            || mail.contains("\"") || mail.contains("'")) {
        errorKey = "page.reg_error_message_mail_invalid.your_email_address_is_incorrect";
    } else if (!Web.isCorrectCaptcha(captcha.toLowerCase(), cid)) {
        errorKey = "page.reg_error_message_captcha.your_security_code_is_invalid";
        errorSuffix = "!";
    } else {
        success = true;
    }

    String password = Web.randomPassword();
    if (success) {
        Web.sendRegistrationMail(request, values, nick, password, mail, lang);
    }

    String roomName = owner.isBlank() ? "" : Web.roomNameByOwner(owner);
    if (success) {
        Web.addRegisteredUser(gender, nick, mail, Web.randomColor(), password, question, answer, day, month, year);
    } else {
        if (service.equals("reg_done")) {
            String message = Web.message(lang, errorKey) + errorSuffix;
            request.setAttribute("reg_error_if_one",
                    "<p class=\"form-section-title\">" + Web.message(lang, "page.reg_failed.registration_is_not_possible")
                    + "!</p><p style=\"font-weight: bold; color: #ff0000\">" + message + "</p>");
        } else {
            request.setAttribute("reg_error_if_one", "");
        }
        cid = Web.newCaptchaId();
    }
    request.setAttribute("formSuccess", success);
    request.setAttribute("formNapping", !roomName.isBlank());
    request.setAttribute("owner", owner);
    request.setAttribute("title", roomName.isBlank() ? "" : Web.roomData(roomName, "page_title"));
    request.setAttribute("reg_nick", nick);
    request.setAttribute("reg_mail", mail);
    request.setAttribute("reg_cid", cid);
    request.setAttribute("reg_captcha", captcha);
    request.setAttribute("reg_reminder_question", question);
    request.setAttribute("reg_reminder_answer", answer);
    request.setAttribute("reg_day", day);
    request.setAttribute("reg_month", month);
    request.setAttribute("reg_year", year);
    request.setAttribute("skin", skin);
    request.setAttribute("selected_no", gender.equals("m") || gender.equals("f") ? "" : " selected");
    request.setAttribute("selected_male", gender.equals("m") ? " selected" : "");
    request.setAttribute("selected_female", gender.equals("f") ? " selected" : "");
    Web.view("page", request, Map.of("reg_cid", cid, "skin", skin));
%><t:layout design="${formNapping ? 'napping' : 'start'}"><c:choose>
<c:when test="${formSuccess}"><h1>
<fmt:message key="common.registration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
</h1>
  <p>
    <fmt:message key="common.you_became_successful_as"/> <span style="font-weight: bold;">${reg_nick}</span> <fmt:message key="common.registered"/>
  </p>
  <p>
    <fmt:message key="common.your_password_was_successfully_sent"/>!
  </p>
</c:when>
<c:otherwise><h1>
${SERVER_SOFTWARE} <fmt:message key="common.registration_2"/>
</h1>
<form action="${paths.register}" method="POST" accept-charset="utf-8">${reg_error_if_one}
  <input type="hidden" name="service" value="reg_done">
  <p class="form-section-title">
    <fmt:message key="common.necessary_information"/>
  </p>
  <p>
  <div class="input-group mb-2">
  <select class="form-control form-control-sm col-md-1" name="reg_gender" size="1">
    <option value="-"${selected_no}><fmt:message key="common.gender"/></option>
    <option value="m"${selected_male}><fmt:message key="common.male"/></option>
    <option value="f"${selected_female}><fmt:message key="common.female"/></option>
  </select>
  <input type="text" size="20" maxlength="20" class="form-control form-control-sm" name="reg_nick" value="${reg_nick}" placeholder="Nickname (<fmt:message key="common.minimum_2_characters_without_special"/>)">
  </div>
        <div class="input-group mb-2">
  <select class="form-control form-control-sm col-md-1" name="reg_day" size="1" aria-label="<fmt:message key="common.day"/>">
    <option value="01">01</option>
    <option value="02">02</option>
    <option value="03">03</option>
    <option value="04">04</option>
    <option value="05">05</option>
    <option value="06">06</option>
    <option value="07">07</option>
    <option value="08">08</option>
    <option value="09">09</option>
    <option value="10">10</option>
    <option value="11">11</option>
    <option value="12">12</option>
    <option value="13">13</option>
    <option value="14">14</option>
    <option value="15">15</option>
    <option value="16">16</option>
    <option value="17">17</option>
    <option value="18">18</option>
    <option value="19">19</option>
    <option value="20">20</option>
    <option value="21">21</option>
    <option value="22">22</option>
    <option value="23">23</option>
    <option value="24">24</option>
    <option value="25">25</option>
    <option value="26">26</option>
    <option value="27">27</option>
    <option value="28">28</option>
    <option value="29">29</option>
    <option value="30">30</option>
    <option value="31">31</option>
  </select>
  <select class="form-control form-control-sm col-md-2" name="reg_month" size="1" aria-label="<fmt:message key="common.month"/>">
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
  <select class="form-control form-control-sm col-md-2" name="reg_year" size="1" aria-label="<fmt:message key="common.year"/>">
    <option value="2026" selected>2026</option>
  </select>
  </div>
  <input type="email" size="50" maxlength="255" class="form-control form-control-sm mb-2" name="reg_mail" value="${reg_mail}" placeholder="<fmt:message key="common.your_email_address"/>">
  <input type="email" size="50" maxlength="255" class="form-control form-control-sm mb-2" name="reg_mail2" value="" placeholder="<fmt:message key="common.email_address_repetition"/>)">
  </p>
  <p class="form-section-title">
    <fmt:message key="common.security_code"/>
  </p>
  <p>
    <img src="${paths.captcha}" alt="Captcha" id="captcha_image" style="vertical-align: middle;"/><button type="button" class="btn btn-outline-secondary btn-sm ms-2" onclick="refreshCaptcha()" title="<fmt:message key="common.reload_captcha"/>">&#x21bb;</button><br>
    <input type="text" size="10" maxlength="10" class="form-control form-control-sm mb-2" name="reg_captcha" value="${reg_captcha}" placeholder="<fmt:message key="common.please_observe_exact_spelling"/>"/>
    <input type="hidden" name="reg_cid" value="${reg_cid}"/>
    <c:if test="${formNapping}"><input type="hidden" name="owner" value="${owner}"/></c:if>
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done_2"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.complete_registration"/>">
  </p>
</form>
<script>
function refreshCaptcha() {
    var img = document.getElementById('captcha_image');
    if (img) {
        img.src = img.src.split('&t=')[0] + '&t=' + new Date().getTime();
    }
}
</script>
<script>
(function() {
    var regDay = '${reg_day}';
    if (regDay) {
        var daySelect = document.querySelector('select[name="reg_day"]');
        if (daySelect) daySelect.value = regDay;
    }
    var regMonth = '${reg_month}';
    if (regMonth) {
        var monthSelect = document.querySelector('select[name="reg_month"]');
        if (monthSelect) monthSelect.value = regMonth;
    }
    var yearSelect = document.querySelector('select[name="reg_year"]');
    if (yearSelect) {
        var currentYear = new Date().getFullYear();
        var startYear = currentYear - 100;
        var endYear = currentYear - 13;
        yearSelect.innerHTML = '';
        for (var year = startYear; year <= endYear; year++) {
            var option = document.createElement('option');
            option.value = year;
            option.text = year;
            yearSelect.appendChild(option);
        }
        var regYear = '${reg_year}';
        if (regYear) {
            yearSelect.value = regYear;
        } else {
            yearSelect.value = currentYear;
        }
    }
})();
</script>
</c:otherwise>
</c:choose></t:layout>
