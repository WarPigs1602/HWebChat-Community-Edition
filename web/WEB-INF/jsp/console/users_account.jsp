<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="console"><h1><fmt:message key="common.user_configuration"><fmt:param value="${SERVER_SOFTWARE}"/></fmt:message></h1>
<form action="${paths.config_users}&update=true&user=${nick}&change=data" method="POST" accept-charset="utf-8">${account_error_if_one}
  <p class="form-section-title">
    <fmt:message key="common.necessary_information"/>
  </p>
  <p>
    <table>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="console.users_account.the_sex"/> </span>
		</td>
		<td>
            <select class="form-control form-control-sm col-md-3 mb-2" name="gender" size="1">
            <option value="-"${selected_no}><fmt:message key="common.not_specified"/></option>
            <option value="m"${selected_male}><fmt:message key="common.man"/></option>
            <option value="f"${selected_female}><fmt:message key="common.woman"/></option>
          </select>
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="console.users_account.the_email_address"/> </span>
		</td>
		<td>
	      <input type="email" size="50" maxlength="255" class="form-control form-control-sm"  name="mail" value="${mail}">
        </td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="console.users_account.date_of_birth"/> </span>
		</td>
		<td>
		  <div class="input-group">
	      <input type="text" size="2" maxlength="2" class="form-control form-control-sm col-md-2 mb-1" name="day" value="${day}"><input type="text" size="2" maxlength="2" class="form-control form-control-sm col-md-2 mb-1" name="month" value="${month}"><input type="text" size="4" maxlength="4" class="form-control form-control-sm col-md-2 mb-1" name="year" value="${year}">
          </div>
		</td>
      </tr>
	  <tr>
        <td>
	      <span style="font-weight: bold"><fmt:message key="console.users_account.status"/> </span>
		</td>
		<td>
	      <input type="text" size="3" maxlength="3" class="form-control form-control-sm"  name="status" value="${status}">
        </td>
      </tr>
    </table>
  </p>
  <p class="form-section-title">
    <fmt:message key="common.and_already_done"/>
  </p>
  <p>
  <input type="submit" class="btn btn-secondary btn-sm" name="OK" value="<fmt:message key="common.send_a_change"/>!!">
  </p>
</form>
</t:layout>