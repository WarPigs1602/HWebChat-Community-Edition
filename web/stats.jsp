<%@ page contentType="text/html; charset=UTF-8" session="false" import="net.midiandmore.chat.Web,jakarta.servlet.http.HttpSession" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><%
    Web.start(application);
    if (!Web.begin("page", request, response)) {
        return;
    }
    Web.Stats stats = Web.stats();
    // The ten most active pairs of users
    java.util.List<java.util.Map<String, Object>> pairs = stats.relationships().subList(0, Math.min(10, stats.relationships().size()));
    java.util.List<String[]> rows = new java.util.ArrayList<>();
    for (java.util.Map<String, Object> pair : pairs) {
        rows.add(new String[] {Web.markup((String) pair.get("user1")), Web.markup((String) pair.get("user2")),
            String.valueOf(pair.get("interaction_count")), Web.formatTime((Long) pair.get("last_interaction"))});
    }
    String lang = (String) request.getAttribute("lang");
    HttpSession httpSession = request.getSession(false);
    pageContext.setAttribute("design", httpSession != null && httpSession.getAttribute("nick") != null ? "frameset_community" : "start");
    pageContext.setAttribute("totalMessages", stats.messages());
    pageContext.setAttribute("registered", stats.registered());
    pageContext.setAttribute("rooms", stats.rooms());
    pageContext.setAttribute("relationshipRows", rows);
    pageContext.setAttribute("dailyData", Web.json(stats.daily()));
    pageContext.setAttribute("hourlyData", Web.json(stats.hourly()));
    pageContext.setAttribute("monthlyData", Web.json(stats.monthly()));
    pageContext.setAttribute("peaksData", Web.json(stats.peaks()));
    pageContext.setAttribute("topRoomsData", Web.json(stats.topRooms()));
    pageContext.setAttribute("relData", Web.json(pairs));
    pageContext.setAttribute("labelMessages", Web.json(Web.message(lang, "stats.messages")));
    pageContext.setAttribute("labelPeakUsers", Web.json(Web.message(lang, "stats.peak_users")));
    pageContext.setAttribute("labelAvgUsers", Web.json(Web.message(lang, "stats.avg_users")));
    pageContext.setAttribute("labelActiveUsers", Web.json(Web.message(lang, "stats.active_users")));
%><t:layout design="${design}"><script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<div class="row g-3 mb-4">
    <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><h6 class="text-muted mb-2"><fmt:message key="common.total_chat_messages"/></h6><div class="display-6 fw-bold text-primary">${totalMessages}</div></div></div></div>
    <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><h6 class="text-muted mb-2"><fmt:message key="common.registered_users"/></h6><div class="display-6 fw-bold text-success">${registered}</div></div></div></div>
    <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><h6 class="text-muted mb-2"><fmt:message key="common.active_rooms"/></h6><div class="display-6 fw-bold text-warning">${rooms}</div></div></div></div>
</div>
<div class="row g-3 mb-4">
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold"><fmt:message key="common.daily_messages"/></div><div class="card-body"><canvas id="dailyMsgChart"></canvas></div></div></div>
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold">📅 <fmt:message key="common.daily_users"/> (Peak)</div><div class="card-body"><canvas id="dailyUserChart"></canvas></div></div></div>
</div>
<div class="row g-3 mb-4">
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold"><fmt:message key="common.hourly_messages"/></div><div class="card-body"><canvas id="hourlyMsgChart"></canvas></div></div></div>
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold">⏰ <fmt:message key="common.hourly_users_avg"/>)</div><div class="card-body"><canvas id="hourlyUserChart"></canvas></div></div></div>
</div>
<div class="row g-3 mb-4">
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold"><fmt:message key="common.monthly_messages"/></div><div class="card-body"><canvas id="monthlyMsgChart"></canvas></div></div></div>
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold">📈 <fmt:message key="common.monthly_users"/> (Peak)</div><div class="card-body"><canvas id="monthlyUserChart"></canvas></div></div></div>
</div>
<div class="row g-3 mb-4">
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold"><fmt:message key="common.top_rooms_messages"/></div><div class="card-body"><canvas id="topRoomsMsgChart"></canvas></div></div></div>
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold"><fmt:message key="common.top_rooms_users"/></div><div class="card-body"><canvas id="topRoomsUserChart"></canvas></div></div></div>
</div>
<div class="row g-3 mb-4">
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold">🌟 <fmt:message key="common.user_peaks_daily"/>)</div><div class="card-body"><canvas id="peaksChart"></canvas></div></div></div>
    <div class="col-lg-6"><div class="card border-0 shadow-sm h-100"><div class="card-header bg-white border-0 fw-bold">👥 <fmt:message key="common.direct_relationships"/> (Private Chats)</div><div class="card-body"><canvas id="relationsChart"></canvas></div></div></div>
</div>
<div class="row g-3 mb-4">
    <div class="col-12"><div class="card border-0 shadow-sm"><div class="card-header bg-white border-0 fw-bold">🔄 <fmt:message key="common.direct_relationships_top_10_interactions"/>)</div><div class="card-body"><div class="table-responsive"><table class="table table-hover">
        <thead><tr><th><fmt:message key="common.user_a"/></th><th><fmt:message key="common.user_b"/></th><th><fmt:message key="common.interactions"/></th><th><fmt:message key="common.last_interaction"/></th></tr></thead>
        <tbody><c:forEach var="row" items="${relationshipRows}"><tr><td>${row[0]}</td><td>${row[1]}</td><td>${row[2]}</td><td>${row[3]}</td></tr></c:forEach><c:if test="${empty relationshipRows}"><tr><td colspan="4" class="text-center text-muted"><fmt:message key="stats.no_relationships"/></td></tr></c:if></tbody>
    </table></div></div></div></div>
</div>
<script>
const dailyData=${dailyData};
const hourlyData=${hourlyData};
const monthlyData=${monthlyData};
const peaksData=${peaksData};
const topRoomsData=${topRoomsData};
const relData=${relData};
function buildLabels(data,key){return data.map(function(it){return it[key];});}
function buildSeries(data,valKey){return data.map(function(it){return it[valKey];});}
if(dailyData.length){new Chart(document.getElementById('dailyMsgChart'),{type:'bar',data:{labels:buildLabels(dailyData,'day_bucket'),datasets:[{label:${labelMessages},data:buildSeries(dailyData,'messages'),backgroundColor:'#0d6efd'}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}
if(dailyData.length){new Chart(document.getElementById('dailyUserChart'),{type:'bar',data:{labels:buildLabels(dailyData,'day_bucket'),datasets:[{label:${labelPeakUsers},data:buildSeries(dailyData,'peak_users'),backgroundColor:'#ffc107'}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}
if(hourlyData.length){new Chart(document.getElementById('hourlyMsgChart'),{type:'line',data:{labels:buildLabels(hourlyData,'hour_bucket'),datasets:[{label:${labelMessages},data:buildSeries(hourlyData,'messages'),borderColor:'#198754',backgroundColor:'rgba(25,135,84,0.1)',fill:true,tension:0.3}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}
if(hourlyData.length){new Chart(document.getElementById('hourlyUserChart'),{type:'line',data:{labels:buildLabels(hourlyData,'hour_bucket'),datasets:[{label:${labelAvgUsers},data:buildSeries(hourlyData,'avg_users'),borderColor:'#fd7e14',tension:0.3}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}
if(monthlyData.length){new Chart(document.getElementById('monthlyMsgChart'),{type:'bar',data:{labels:buildLabels(monthlyData,'month_bucket'),datasets:[{label:${labelMessages},data:buildSeries(monthlyData,'messages'),backgroundColor:'#6610f2'}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}
if(monthlyData.length){new Chart(document.getElementById('monthlyUserChart'),{type:'bar',data:{labels:buildLabels(monthlyData,'month_bucket'),datasets:[{label:${labelPeakUsers},data:buildSeries(monthlyData,'peak_users'),backgroundColor:'#dc3545'}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}
if(topRoomsData.length){new Chart(document.getElementById('topRoomsMsgChart'),{type:'bar',data:{labels:buildLabels(topRoomsData,'room'),datasets:[{label:${labelMessages},data:buildSeries(topRoomsData,'msg_count'),backgroundColor:'#20c997'}]},options:{indexAxis:'y',responsive:true,scales:{x:{beginAtZero:true}}}});}
if(topRoomsData.length){new Chart(document.getElementById('topRoomsUserChart'),{type:'bar',data:{labels:buildLabels(topRoomsData,'room'),datasets:[{label:${labelActiveUsers},data:buildSeries(topRoomsData,'users'),backgroundColor:'#0dcaf0'}]},options:{indexAxis:'y',responsive:true,scales:{x:{beginAtZero:true}}}});}
if(peaksData.length){new Chart(document.getElementById('peaksChart'),{type:'line',data:{labels:buildLabels(peaksData,'date'),datasets:[{label:${labelPeakUsers},data:buildSeries(peaksData,'peak_users'),borderColor:'#d63384',backgroundColor:'rgba(214,51,132,0.1)',fill:true,tension:0.3}]},options:{responsive:true,scales:{y:{beginAtZero:true}}}});}
if(relData.length){new Chart(document.getElementById('relationsChart'),{type:'doughnut',data:{labels:relData.map(function(it){return it.user1+' ↔ '+it.user2;}),datasets:[{data:relData.map(function(it){return it.interaction_count;}),backgroundColor:['#0d6efd','#6610f2','#d63384','#dc3545','#fd7e14','#ffc107','#198754','#20c997','#0dcaf0','#6f42c1']}]},options:{responsive:true}});}
</script>
</t:layout>