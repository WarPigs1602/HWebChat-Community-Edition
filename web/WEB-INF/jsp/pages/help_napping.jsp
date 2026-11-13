<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping_info"><h1><fmt:message key="common.chat_commands"/></h1>
<table>
<tr><td style="font-weight: bold;"><fmt:message key="common.commander"/></td><td style="font-weight: bold">Status</td><td style="font-weight: bold"><fmt:message key="common.description_2"/></td></tr>
<fmt:message key="common.whisper_with_it_you_the"/>
<fmt:message key="common.whisper_with_it_you_the_2"/>
${help.accept_invite}
${help.add_friend}
${help.superuser_room_set}
${help.away}
${help.ban}
${help.ban_timed}
${help.beam}
${help.clear}
${help['catch']}
${help.change_rights}
${help.color}
${help.delete_picture}
${help.dice}
${help.superuser_room_remove}
${help.gag}
${help.moderator_set}
${help.fcol}
${help.hardkick}
${help.invite}
${help.ignore}
${help.info}
${help.change_room}
${help.kick}
${help.lock}
${help.list}
${help.mail}
${help.me}
${help.moderator}
${help.moderate_room}
${help.nick}
${help.ip}
${help.query}
${help.quit}
${help.del_friend}
${help.moderator_remove}
${help.rehash}
${help.superuser_remove}
${help.shout}
${help.list_friends}
${help.show_bans}
${help.sepa}
${help.shutdown}
${help.superuser_set}
${help.supervisor}
${help.system}
${help.topic_private}
${help.unban}
${help.who_room}
${help.whois}
${help.who}
${help.wipe}
<tr><td style="font-weight: bold;"><fmt:message key="common.fun_commands"/></td><td style="font-weight: bold">&nbsp;</td><td style="font-weight: bold">&nbsp;</td></tr>
${help.fun}
</table>


</t:layout>