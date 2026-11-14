<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web" %><% Web.start(application); if (!Web.begin("document", request, response)) { return; } %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="start"><div class='impressum'><h1><fmt:message key="doc.impressum.imprint"/></h1><p><fmt:message key="doc.impressum.information_according_to_5_tmg"/></p><p><c:out value="${legal.name}"/> <br>
<c:out value="${legal.street}"/><br>
<c:out value="${legal.zip}"/> <c:out value="${legal.city}"/> <br>
</p><p> <strong><fmt:message key="doc.impressum.represented_by"/> </strong><br>
<c:out value="${legal.name}"/><br>
</p><p><strong><fmt:message key="doc.impressum.contact"/></strong> <br>
<c:if test="${not empty legal.phone}"><fmt:message key="doc.impressum.phone"/> <c:out value="${legal.phone}"/><br></c:if>
<fmt:message key="doc.impressum.e_mail"/> <a href='mailto:<c:out value="${legal.email}"/>'><c:out value="${legal.email}"/></a></br></p><p><strong><fmt:message key="doc.impressum.disclaimer"/> </strong><br><br><strong><fmt:message key="doc.impressum.liability_for_content"/></strong><br><br>
<fmt:message key="doc.impressum.the_content_of_our_pages"/><br><br><strong><fmt:message key="doc.impressum.liability_for_links"/></strong><br><br>
<fmt:message key="doc.impressum.our_offer_contains_links_to"/><br><br><strong><fmt:message key="doc.impressum.copyright"/></strong><br><br>
<fmt:message key="doc.impressum.the_content_and_works_created"/></p><br> 
<fmt:message key="doc.impressum.website_imprint_of"/> <a href="https://www.impressum-generator.de"><fmt:message key="doc.impressum.imprint_generator_de"/></a>
 </div>
 </t:layout>