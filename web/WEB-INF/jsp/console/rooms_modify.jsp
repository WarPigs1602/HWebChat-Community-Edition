<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.room_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<p style="font-weight: bold"><fmt:message key="common.change_successfully"/>!</p>
<form action="${paths.config_rooms}&update=true&room=${room}&change=modify" method="POST" accept-charset="utf-8">
  <p>
    <table>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.the_spatial_theme"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="topic" value="${topic}">
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.if_the_room_is_closed"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="lock" value="${lock}">
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.the_blocking_reason"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="lock_reason" value="${lock_reason}">
        </td>
      </tr>	  
      <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.is_a_standard_room"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="standard" value="${standard}">
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="common.allow_smilies"/> </span>
		</td>
		<td>
	      <input type="text" size="50" maxlength="255" class="form-control form-control-sm"  name="allow_smilies" value="${allow_smilies}">
        </td>
      </tr>
    </table>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.send_a_change"/>!!">
  </p>
</form>
</t:layout>