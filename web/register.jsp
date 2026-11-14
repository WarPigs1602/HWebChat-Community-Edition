<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.util.Map, net.midiandmore.chat.Web" %><%
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
    String formPage;
    if (success) {
        Web.addRegisteredUser(gender, nick, mail, Web.randomColor(), password, question, answer, day, month, year);
        formPage = roomName.isBlank()
                ? "/WEB-INF/jsp/pages/reg_form_success.jsp"
                : "/WEB-INF/jsp/pages/reg_form_success_napping.jsp";
    } else {
        formPage = roomName.isBlank()
                ? "/WEB-INF/jsp/pages/reg_form.jsp"
                : "/WEB-INF/jsp/pages/reg_form_napping.jsp";
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
    request.setAttribute("formPage", formPage);
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
%><jsp:include page="${formPage}"/>
