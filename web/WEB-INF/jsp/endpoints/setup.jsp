<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true" import="net.midiandmore.chat.SetupService, net.midiandmore.chat.Web" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    Web.start(application);
    request.setCharacterEncoding("UTF-8");
    SetupService setup = new SetupService(application);
    String state = "form";
    boolean posted = false;
    if (setup.isSetupComplete()) {
        state = "done";
    } else if ("POST".equals(request.getMethod())) {
        posted = true;
        try {
            String error = setup.run(request);
            if (error == null) {
                state = "success";
            } else {
                request.setAttribute("error", error);
            }
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return;
        }
    }
    request.setAttribute("state", state);
    request.setAttribute("posted", posted);
%>
<c:set var="dbType" value="${posted ? param.db_type : 'sqlite'}"/>
<c:set var="dbHost" value="${posted ? param.db_host : 'localhost'}"/>
<c:set var="dbPort" value="${posted ? param.db_port : '3306'}"/>
<c:set var="dbName" value="${posted ? param.db_name : 'hwebchat'}"/>
<c:set var="dbUser" value="${posted ? param.db_user : 'user'}"/>
<c:set var="dbPassword" value="${posted ? param.db_password : 'password'}"/>
<c:set var="sqlitePath" value="${posted ? param.sqlite_path : ''}"/>
<c:set var="dbPrefix" value="${posted ? param.db_prefix : 'hwc_'}"/>
<c:set var="smtpHost" value="${posted ? param.smtp_host : 'smtp.example.org'}"/>
<c:set var="smtpPort" value="${posted ? param.smtp_port : '465'}"/>
<c:set var="smtpUser" value="${posted ? param.smtp_user : 'foo@bar'}"/>
<c:set var="smtpPassword" value="${posted ? param.smtp_password : 'password'}"/>
<c:set var="smtpFrom" value="${posted ? param.smtp_from : 'foo@bar'}"/>
<c:set var="smtpAuth" value="${posted and param.smtp_auth ne null ? param.smtp_auth : 'true'}"/>
<c:set var="smtpStarttls" value="${posted and param.smtp_starttls ne null ? param.smtp_starttls : 'true'}"/>
<c:set var="smtpSslFactory" value="${posted and param.smtp_ssl_factory ne null ? param.smtp_ssl_factory : 'javax.net.ssl.SSLSocketFactory'}"/>
<c:set var="adminUser" value="${posted ? param.admin_user : 'admin'}"/>
<c:set var="adminPassword" value="${posted ? param.admin_password : 'admin'}"/>
<c:set var="adminRealm" value="${posted and param.admin_realm ne null ? param.admin_realm : 'Console'}"/>
<c:set var="firstUserNick" value="${posted ? param.first_user_nick : ''}"/>
<c:set var="firstUserPassword" value="${posted ? param.first_user_password : ''}"/>
<c:set var="firstUserEmail" value="${posted ? param.first_user_email : ''}"/>
<c:set var="charset" value="${posted ? param.charset : 'UTF-8'}"/>
<c:set var="timezone" value="${posted ? param.timezone : 'CET'}"/>
<c:set var="skin" value="${posted ? param.skin : 'native'}"/>
<c:set var="room" value="${posted ? param.room : 'Lobby'}"/>
<c:set var="legalName" value="${posted ? param.legal_name : ''}"/>
<c:set var="legalStreet" value="${posted ? param.legal_street : ''}"/>
<c:set var="legalZip" value="${posted ? param.legal_zip : ''}"/>
<c:set var="legalCity" value="${posted ? param.legal_city : ''}"/>
<c:set var="legalPhone" value="${posted ? param.legal_phone : ''}"/>
<c:set var="legalEmail" value="${posted ? param.legal_email : ''}"/>
<c:url var="startUrl" value="/start.jsp"/>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:choose><c:when test="${state eq 'success'}">Setup completed</c:when><c:when test="${state eq 'done'}">Setup already completed</c:when><c:otherwise>Setup</c:otherwise></c:choose></title>
<style>
*{box-sizing:border-box;margin:0;padding:0;}
body{font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background:linear-gradient(135deg,#1a1a2e 0%,#16213e 50%,#0f3460 100%);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;}
.setup-container{background:rgba(255,255,255,0.95);border-radius:20px;box-shadow:0 25px 80px rgba(0,0,0,0.4);max-width:900px;width:100%;overflow:hidden;}
.setup-header{background:linear-gradient(135deg,#e94560 0%,#c23152 100%);color:white;padding:40px;text-align:center;}
.setup-header h1{font-size:2.5em;margin-bottom:10px;text-shadow:2px 2px 4px rgba(0,0,0,0.3);}
.setup-header p{opacity:0.9;font-size:1.1em;}
.setup-body{padding:40px;}
.error-message{background:#ffe6e6;color:#c23152;padding:15px 20px;border-radius:10px;margin-bottom:25px;border-left:4px solid #e94560;}
.section{margin-bottom:35px;}
.section-title{font-size:1.3em;color:#16213e;margin-bottom:20px;padding-bottom:10px;border-bottom:2px solid #e94560;display:flex;align-items:center;gap:10px;}
.section-title .icon{font-size:1.5em;}
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;}
@media(max-width:600px){.form-grid{grid-template-columns:1fr;}}
.form-group{display:flex;flex-direction:column;}
.form-group label{font-weight:600;color:#333;margin-bottom:6px;font-size:0.95em;}
.form-group input,.form-group select{padding:12px 15px;border:2px solid #ddd;border-radius:8px;font-size:1em;transition:border-color 0.3s;}
.form-group input:focus,.form-group select:focus{outline:none;border-color:#e94560;}
.form-group .hint{font-size:0.8em;color:#888;margin-top:4px;}
.btn-submit{background:linear-gradient(135deg,#e94560 0%,#c23152 100%);color:white;border:none;padding:15px 40px;font-size:1.1em;border-radius:10px;cursor:pointer;font-weight:600;display:block;margin:30px auto 0;transition:transform 0.2s,box-shadow 0.2s;}
.btn-submit:hover{transform:translateY(-2px);box-shadow:0 8px 25px rgba(233,69,96,0.4);}
.btn-link{background:#e94560;color:white;padding:12px 30px;text-decoration:none;border-radius:8px;font-weight:bold;}
.success{color:#10b981;}
.info{margin:20px 0;color:#666;}
.footer{text-align:center;padding:20px;color:#666;font-size:0.9em;border-top:1px solid #eee;}
</style>
</head>
<body>
<div class="setup-container">
<div class="setup-header"><h1>⚙️ HWebChat Setup</h1><p>Welcome to the automatic configuration of your chat server</p></div>
<div class="setup-body">
<c:choose>
<c:when test="${state eq 'done'}">
<h1>Setup already completed</h1>
<p>Configuration has already been performed. The chat is available at <a href="${startUrl}"><c:out value="${startUrl}"/></a>.</p>
</c:when>
<c:when test="${state eq 'success'}">
<h1 class="success">✅ Setup completed successfully!</h1>
<p class="info">The chat server has been configured successfully. Database tables have been created.</p>
<p class="info">The first user <strong><c:out value="${firstUserNick}"/></strong> has been created.</p>
<p class="info"><a class="btn-link" href="${startUrl}">Go to chat →</a></p>
</c:when>
<c:otherwise>
<c:if test="${not empty error}"><div class="error-message">⚠️ <c:out value="${error}"/></div></c:if>
<form method="POST" action="">
<div class="section"><div class="section-title"><span class="icon">🗄️</span> Database</div><div class="form-grid">
<div class="form-group"><label for="db_type">Database type *</label><select id="db_type" name="db_type"><option value="mysql"${dbType eq 'mysql' ? ' selected' : ''}>MySQL</option><option value="postgresql"${dbType eq 'postgresql' ? ' selected' : ''}>PostgreSQL</option><option value="sqlite"${dbType eq 'sqlite' ? ' selected' : ''}>SQLite</option></select></div>
<div class="form-group db-server"><label for="db_host">Database host *</label><input type="text" id="db_host" name="db_host" value="<c:out value="${dbHost}"/>"></div>
<div class="form-group db-server"><label for="db_port">Database port *</label><input type="text" id="db_port" name="db_port" value="<c:out value="${dbPort}"/>"></div>
<div class="form-group db-server"><label for="db_name">Database name *</label><input type="text" id="db_name" name="db_name" value="<c:out value="${dbName}"/>"></div>
<div class="form-group db-server"><label for="db_user">Database user *</label><input type="text" id="db_user" name="db_user" value="<c:out value="${dbUser}"/>"></div>
<div class="form-group db-server"><label for="db_password">Database password *</label><input type="password" id="db_password" name="db_password" value="<c:out value="${dbPassword}"/>"></div>
<div class="form-group db-sqlite"><label for="sqlite_path">SQLite database file</label><input type="text" id="sqlite_path" name="sqlite_path" value="<c:out value="${sqlitePath}"/>"><div class="hint">Leave blank to use ~/.homewebcom/data/hwebchat.sqlite</div></div>
<div class="form-group"><label for="db_prefix">Table prefix</label><input type="text" id="db_prefix" name="db_prefix" value="<c:out value="${dbPrefix}"/>"></div>
</div></div>
<div class="section"><div class="section-title"><span class="icon">📧</span> E-Mail / SMTP</div><div class="form-grid">
<div class="form-group"><label for="smtp_host">SMTP host *</label><input type="text" id="smtp_host" name="smtp_host" value="<c:out value="${smtpHost}"/>" required></div>
<div class="form-group"><label for="smtp_port">SMTP port *</label><input type="text" id="smtp_port" name="smtp_port" value="<c:out value="${smtpPort}"/>" required></div>
<div class="form-group"><label for="smtp_user">SMTP user</label><input type="text" id="smtp_user" name="smtp_user" value="<c:out value="${smtpUser}"/>"></div>
<div class="form-group"><label for="smtp_password">SMTP password</label><input type="password" id="smtp_password" name="smtp_password" value="<c:out value="${smtpPassword}"/>"></div>
<div class="form-group"><label for="smtp_from">Sender address *</label><input type="email" id="smtp_from" name="smtp_from" value="<c:out value="${smtpFrom}"/>" required></div>
</div></div>
<div class="section"><div class="section-title"><span class="icon">⚙️</span> SMTP options</div><div class="form-grid">
<div class="form-group"><label for="smtp_auth">Authentication</label><select id="smtp_auth" name="smtp_auth"><option value="true"${smtpAuth eq 'true' ? ' selected' : ''}>On</option><option value="false"${smtpAuth eq 'false' ? ' selected' : ''}>Off</option></select></div>
<div class="form-group"><label for="smtp_starttls">STARTTLS</label><select id="smtp_starttls" name="smtp_starttls"><option value="true"${smtpStarttls eq 'true' ? ' selected' : ''}>On</option><option value="false"${smtpStarttls eq 'false' ? ' selected' : ''}>Off</option></select></div>
<div class="form-group"><label for="smtp_ssl_factory">SSL socket factory</label><input type="text" id="smtp_ssl_factory" name="smtp_ssl_factory" value="<c:out value="${smtpSslFactory}"/>"></div>
</div></div>
<div class="section"><div class="section-title"><span class="icon">🔐</span> Administration console</div><div class="form-grid">
<div class="form-group"><label for="admin_user">Console username *</label><input type="text" id="admin_user" name="admin_user" value="<c:out value="${adminUser}"/>" required></div>
<div class="form-group"><label for="admin_password">Console password *</label><input type="password" id="admin_password" name="admin_password" value="<c:out value="${adminPassword}"/>" required></div>
<div class="form-group"><label for="admin_realm">Console realm</label><input type="text" id="admin_realm" name="admin_realm" value="<c:out value="${adminRealm}"/>"></div>
</div></div>
<div class="section"><div class="section-title"><span class="icon">👤</span> First user</div><div class="form-grid">
<div class="form-group"><label for="first_user_nick">Username *</label><input type="text" id="first_user_nick" name="first_user_nick" value="<c:out value="${firstUserNick}"/>" required></div>
<div class="form-group"><label for="first_user_password">Password *</label><input type="password" id="first_user_password" name="first_user_password" value="<c:out value="${firstUserPassword}"/>" required></div>
<div class="form-group"><label for="first_user_email">E-mail *</label><input type="email" id="first_user_email" name="first_user_email" value="<c:out value="${firstUserEmail}"/>" required></div>
</div></div>
<div class="section"><div class="section-title"><span class="icon">📜</span> Imprint and privacy policy</div><div class="form-grid">
<div class="form-group"><label for="legal_name">Responsible person *</label><input type="text" id="legal_name" name="legal_name" value="<c:out value="${legalName}"/>" required></div>
<div class="form-group"><label for="legal_street">Street and number *</label><input type="text" id="legal_street" name="legal_street" value="<c:out value="${legalStreet}"/>" required></div>
<div class="form-group"><label for="legal_zip">Postal code *</label><input type="text" id="legal_zip" name="legal_zip" value="<c:out value="${legalZip}"/>" required></div>
<div class="form-group"><label for="legal_city">City *</label><input type="text" id="legal_city" name="legal_city" value="<c:out value="${legalCity}"/>" required></div>
<div class="form-group"><label for="legal_phone">Phone</label><input type="text" id="legal_phone" name="legal_phone" value="<c:out value="${legalPhone}"/>"><div class="hint">Leave blank to hide it in the imprint</div></div>
<div class="form-group"><label for="legal_email">E-mail *</label><input type="email" id="legal_email" name="legal_email" value="<c:out value="${legalEmail}"/>" required></div>
</div></div>
<div class="section"><div class="section-title"><span class="icon">🌐</span> General settings</div><div class="form-grid">
<div class="form-group"><label for="charset">Charset</label><select id="charset" name="charset"><option value="UTF-8"${charset eq 'UTF-8' ? ' selected' : ''}>UTF-8</option><option value="ISO-8859-1"${charset eq 'ISO-8859-1' ? ' selected' : ''}>ISO-8859-1</option></select></div>
<div class="form-group"><label for="timezone">Timezone</label><select id="timezone" name="timezone">
<option value="CET"${timezone eq 'CET' ? ' selected' : ''}>CET (UTC+1)</option>
<option value="UTC"${timezone eq 'UTC' ? ' selected' : ''}>UTC (UTC+0)</option>
<option value="EET"${timezone eq 'EET' ? ' selected' : ''}>EET (UTC+2)</option>
<option value="EST"${timezone eq 'EST' ? ' selected' : ''}>EST (UTC-5)</option>
<option value="PST"${timezone eq 'PST' ? ' selected' : ''}>PST (UTC-8)</option>
</select></div>
<div class="form-group"><label for="skin">Default skin</label><input type="text" id="skin" name="skin" value="<c:out value="${skin}"/>"></div>
<div class="form-group"><label for="room">Default room</label><input type="text" id="room" name="room" value="<c:out value="${room}"/>"></div>
</div></div>
<button type="submit" class="btn-submit">🚀 Complete setup and start chat</button>
</form>
<script>(function(){var type=document.getElementById('db_type');function update(){var sqlite=type.value==='sqlite';document.querySelectorAll('.db-server').forEach(function(e){e.hidden=sqlite;e.querySelector('input').required=!sqlite;});document.querySelectorAll('.db-sqlite').forEach(function(e){e.hidden=!sqlite;});}type.addEventListener('change',update);update();})();</script>
</c:otherwise>
</c:choose>
</div>
<div class="footer">HWebChat Community Edition | (c) 2005-2026 by Andreas Pschorn</div>
</div>
</body>
</html>
