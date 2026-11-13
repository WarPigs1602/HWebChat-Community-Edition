<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ include file="/WEB-INF/jsp/includes/lang.jspf" %>
<% response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND); %>
<!DOCTYPE html>
<html lang="${lang}">
<head>
  <title>HWebChat - <fmt:message key="web.error404.title"/></title>
  <meta content="Andreas Pschorn" name="author">
  <meta content="&copy;2005-2026 by Andreas Pschorn" name="description">
  <meta content="HWebChat 1.0.0-Alpha 1" name="software">
  <meta content="text/html; charset=UTF-8" http-equiv="content-type">
  <style>
    a, a:hover { 
    color: #AA0000;
    }

  a { 
    text-decoration: none;
    }

  a:hover { 
    text-decoration: underline;
    }

  p {
    padding: 3px;
    margin: 3px;
  }

  h1 {
    padding: 3px;
    font-weight: bold;
    font-family: Verdana,Arial,Helvetica,sans-serif;
    font-size: 24px;
    margin: 3px;
  }

  h2 {
    padding: 3px;
    font-weight: bold;
    font-family: Verdana,Arial,Helvetica,sans-serif;
    font-size: 16px;
    margin: 3px;
  }

  td,tr,table { 
    font-family: Verdana,Arial,Helvetica,sans-serif;
    font-size: 11px;
    }

  body { 
    font-family: Verdana,Arial,Helvetica,sans-serif;
    font-size: 11px;
    color: #000000;
    background-color: #660000;
    }
 
  html { 
    overflow-y: scroll;
    }
	    
  #logo { 
    border: 1px solid #0000AA;
    font-family: verdana,arial,helvetica,sans-serif;
    font-size: 11px;
    height: 70px;
    width: 987px;
    position: absolute;
    left: 10px;
    top: 10px;
    text-align: center;
    background-color: #ffffff;
    background-repeat: no-repeat;
    background-position: center;
    background-image: url(<c:url value="/file/gfx/logo.gif"/>);
    }

  #blank {
    font-family: verdana,arial,helvetica,sans-serif;
    font-size: 11px;
    height: 10px;
    position: relative;
    } 

  #pagetitle { 
    font-family: verdana,arial,helvetica,sans-serif;
    font-size: 16px;
    font-weight: bold;
    height: 21px;
    width: 300px;
    left: 0px;
    top: 0px;
    text-align: center;
    background-color: #ffffff;
    border-color: #0000AA;
    border-top-width: 1px;
    border-right-width: 1px;
    border-bottom-width: 0px;
    border-left-width: 1px;
    border-style: solid;
    position: relative;
    z-index: 2;
    padding: 3px;
    }

  #pagecontent { 
    font-family: verdana,arial,helvetica,sans-serif;
    font-size: 11px;
    width: 980px;
    left: 0px;
    top: -1px;
    text-align: left;
    background-color: #ffffff;
    border-color: #0000AA;
    border-right-width: 1px;
    border-top-width: 1px;
    border-bottom-width: 1px;
    border-left-width: 1px;
    border-style: solid;
    z-index: 1;
    padding: 3px;
    position: relative;
    }

  #page { 
    font-family: verdana,arial,helvetica,sans-serif;
    font-size: 11px;
    width: 987px;
    position: absolute;
    left: 10px;
    top: 85px;
    }
	</style>
</head>
<body>
<div id="logo"></div>
<div id="page">

<div id="pagetitle">
:: <fmt:message key="web.error404.title"/> ::
</div>
<div id="pagecontent">
<h2><fmt:message key="web.error404.heading"/></h2>
<p><fmt:message key="web.error404.text"/></p>
<p><a href="<c:url value="/"/>"><fmt:message key="web.error404.back"/></a></p>
</div>

</div>

</body>
</html>
