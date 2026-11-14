<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" import="java.util.Map, jakarta.mail.MessagingException, net.midiandmore.chat.Web, org.owasp.encoder.Encode" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }

    Map<String, String> values = Web.requestValues(request);
    String service = values.getOrDefault("service", "");
    String nick = values.getOrDefault("reg_nick", "");
    String mail = values.getOrDefault("reg_mail", "");
    String captcha = values.getOrDefault("reg_captcha", "");
    String cid = values.getOrDefault("reg_cid", "");
    String day = values.getOrDefault("reg_day", "");
    String month = values.getOrDefault("reg_month", "");
    String year = values.getOrDefault("reg_year", "");
    String owner = values.getOrDefault("owner", "");
    String skin = Web.skin(values.getOrDefault("skin", ""), request);
    String lang = (String) request.getAttribute("lang");
    String roomName = owner.isBlank() ? "" : Web.roomNameByOwner(owner);
    String errorKey = "";
    boolean success = false;
    if (!Web.isRegistered(nick)) {
        errorKey = "page.reg_error_message_nick_not_registered.your_nickname_is_not_registered";
    } else if (!Web.userData(nick, "mail").equalsIgnoreCase(mail)) {
        errorKey = "page.reg_error_message_wrong_mail.your_email_address_is_not";
    } else if (!Web.isValidDate(day, month, year)) {
        errorKey = "common.your_date_of_birth_is";
    } else if (!Web.userData(nick, "bday_day").equalsIgnoreCase(day)
            && !Web.userData(nick, "bday_month").equalsIgnoreCase(month)
            && !Web.userData(nick, "bday_year").equalsIgnoreCase(year)) {
        errorKey = "common.your_date_of_birth_is_2";
    } else if (!Web.isCorrectCaptcha(captcha.toLowerCase(), cid)) {
        errorKey = "page.reg_error_message_captcha.your_security_code_is_invalid";
    } else {
        success = true;
    }

    String password = Web.randomPassword();
    String mailError = "";
    if (success) {
        try {
            Web.sendPasswordMail(request, values, nick, password, mail, lang);
        } catch (MessagingException ex) {
            String localized = ex.getLocalizedMessage();
            mailError = localized == null || localized.isEmpty() ? ex.getClass().getSimpleName() : localized;
            success = false;
        }
    }

    String formPage;
    if (success) {
        formPage = roomName.isBlank()
                ? "/WEB-INF/jsp/pages/pass_form_success.jsp"
                : "/WEB-INF/jsp/pages/pass_form_success_napping.jsp";
        Web.updatePassword(nick, password);
        request.setAttribute("reg_error_if_one", "");
    } else {
        formPage = roomName.isBlank()
                ? "/WEB-INF/jsp/pages/pass_form.jsp"
                : "/WEB-INF/jsp/pages/pass_form_napping.jsp";
        if (service.equals("pass_done")) {
            String message = mailError.isEmpty() ? Web.message(lang, errorKey)
                    : Encode.forHtmlContent(mailError);
            request.setAttribute("reg_error_if_one",
                    "<p class=\"form-section-title\">" + Web.message(lang, "common.password_change_is_not_possible")
                    + "!</p><p style=\"font-weight: bold; color: #ff0000\">" + message + "</p>");
        } else {
            request.setAttribute("reg_error_if_one", "");
        }
        cid = Web.newCaptchaId();
    }
    request.setAttribute("formPage", formPage);
    request.setAttribute("owner", owner);
    request.setAttribute("reg_nick", nick);
    request.setAttribute("reg_mail", mail);
    request.setAttribute("reg_cid", cid);
    request.setAttribute("reg_captcha", captcha);
    request.setAttribute("reg_day", day);
    request.setAttribute("reg_month", month);
    request.setAttribute("reg_year", year);
    request.setAttribute("skin", skin);
    Web.view("page", request, Map.of("reg_cid", cid, "skin", skin));
%><jsp:include page="${formPage}"/>
