<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("document", request, response)) { return; } %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community"><h1>${SERVER_SOFTWARE}-Chattiquette</h1>
      <ol>
        <li> 
          <fmt:message key="common.meet_other_chatters_with_respect"/>.
        </li>
        <li>
          <fmt:message key="common.when_you_enter_a_chat"/>.
        </li>
        <li>

          <fmt:message key="common.do_you_like_to_flirt"/> w?"
        </li>
        <li>
          <fmt:message key="common.everyone_has_a_bad_mood"/>.
        </li>
        <li>
          <fmt:message key="common.help_the_newbies_when_a"/>.</li>
        <li>
          <fmt:message key="common.if_you_want_to_address"/>.
        </li>

        <li>
          <fmt:message key="common.chatting_is_fun_and_humor"/>.
        </li>
        <li>
          <span style="text-transform: uppercase;">
            <fmt:message key="common.capital_letters"/>
          </span>
          , <fmt:message key="common.graphic_smileys_and_other_special"/>.
        </li>
        <li> 
          <fmt:message key="common.remember_glorification_of_violence_and"/>.
         </li>

        <li> 
          <fmt:message key="common.if_someone_behaves_wrong_in"/>.
         </li>
        <li> 
          <fmt:message key="common.since_you_have_the_opportunity"/>.
        </li>
        <li> 
          <fmt:message key="common.do_not_delude_yourself_that"/>.
        </li>
        <li> 
          <fmt:message key="common.there_is_the_possibility_to"/>!
        </li>
        <li> 
          <fmt:message key="common.never_advertise_chatter_for_another"/>.
        </li>

        <li> 
          <fmt:message key="common.if_you_only_commit_a"/>.
        </li>
        <li>
          <fmt:message key="common.for_more_serious_or_repeated"/>.
        </li>
        <li> 
          <fmt:message key="common.in_case_of_serious_violations"/>.
         </li>
      </ol>
    <div style="font-weight: bold;">
      <fmt:message key="common.your_team"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message>
    </div></t:layout>