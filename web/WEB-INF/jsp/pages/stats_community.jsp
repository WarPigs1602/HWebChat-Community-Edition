<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="frameset_community"><script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<div class="row g-3 mb-4">
    <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><h6 class="text-muted mb-2"><fmt:message key="common.total_chat_messages"/></h6><div class="display-6 fw-bold text-primary">${stat_total_messages}</div></div></div></div>
    <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><h6 class="text-muted mb-2"><fmt:message key="common.registered_users"/></h6><div class="display-6 fw-bold text-success">${stat_registered}</div></div></div></div>
    <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><h6 class="text-muted mb-2"><fmt:message key="common.active_rooms"/></h6><div class="display-6 fw-bold text-warning">${stat_rooms}</div></div></div></div>
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
        <tbody>${stat_relationships}</tbody>
    </table></div></div></div></div>
</div>
${stat_chart_script}
</t:layout>