<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><table style="width: 100%; border: 1px solid #AAAAAA;">
<tr style="padding: 0px; margin: 0px;">
  <td style="background-color: #AAAAAA;">
     <span style="color: #${msg_color}; font-weight: bold">${msg_nick}</span> <fmt:message key="page.message_view.wrote_on"/> <span style="font-weight: bold">${msg_time}</span>:
  </td>
</tr>
<tr style="padding: 0px; margin: 0px;">
  <td>
     ${msg}
  </td>
</tr>
</table>
<br>