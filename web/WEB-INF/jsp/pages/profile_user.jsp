<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="profile"><div class="profile-page">
  <div class="profile-header mb-4">
    <div class="d-flex align-items-center gap-3 mb-3">
      <img src="${path_hwebchat}?page=image&nick=${user}" class="profile-avatar rounded-circle shadow-sm flex-shrink-0" alt="${user}" width="120" height="120">
      <div class="flex-grow-1 min-w-0">
        <h1 class="profile-name h3 mb-2" style="color: #${color};">${user}</h1>
        <div class="d-flex flex-wrap gap-2 mb-2">
          <span class="badge bg-light text-dark border"><span class="fw-semibold"><fmt:message key="common.gender_3"/></span> ${sex}</span>
          <span class="badge bg-light text-dark border"><span class="fw-semibold"><fmt:message key="common.city_3"/></span> ${city}</span>
          <span class="badge bg-light text-dark border"><span class="fw-semibold"><fmt:message key="common.points"/></span> ${points}</span>
        </div>
        <div class="profile-birthday text-muted small">
          <span class="fw-semibold text-dark"><fmt:message key="common.birthday"/></span> ${bday_day}.${bday_month}.${bday_year}
        </div>
      </div>
    </div>
    <div class="profile-dates card bg-light border-0 shadow-sm mb-0">
      <div class="card-body py-2 px-3">
        <div class="row g-2 small">
          <div class="col-md-6"><fmt:message key="common.registered_on"/> <span class="fw-semibold text-dark">${timestamp_reg}</span></div>
          <div class="col-md-6"><fmt:message key="common.last_login"/> <span class="fw-semibold text-dark">${timestamp_login}</span></div>
        </div>
      </div>
    </div>
  </div>
  <div class="row g-4 mt-4">
    <div class="col-md-7">
      <div class="card shadow-sm h-100 profile-section-card mb-4">
        <div class="card-header bg-transparent border-bottom fw-semibold py-2">
          <span style="margin-right: 8px;">📋</span><fmt:message key="common.about"><fmt:param value="${user}"/></fmt:message>
        </div>
        <div class="card-body py-3">
          ${profile.description}
          ${profile.hobby}
          ${profile.status}
          ${profile.slogan}
          ${profile.signature}
        </div>
      </div>
    </div>
    <div class="col-md-5">
      <div class="card shadow-sm mb-4 profile-section-card">
        <div class="card-header bg-transparent border-bottom fw-semibold py-2">
          <span style="margin-right: 8px;">📡</span><fmt:message key="common.contact"/>
        </div>
         <div class="card-body py-3">
          ${profile.homepage}
          ${profile.facebook}
          ${profile.twitter}
          ${profile.youtube}
          ${profile.instagram}
          ${profile.linkedin}
          ${profile.tiktok}
          ${profile.discord}
          ${profile.twitch}
          ${profile.github}
          ${profile.reddit}
          ${profile.snapchat}
          ${profile.pinterest}
          ${profile.whatsapp}
          ${profile.telegram}
        </div>
      </div>
      <div class="card shadow-sm mb-4 profile-section-card">
        <div class="card-header bg-transparent border-bottom fw-semibold py-2">
          <span style="margin-right: 8px;">👤</span><fmt:message key="common.personal"/>
        </div>
        <div class="card-body py-3">
          ${profile.fam_status}
          ${profile.last_visitor}
        </div>
      </div>
    </div>
  </div>
  <div class="row g-4 mt-4">
    <div class="col-12">
      <div class="card shadow-sm profile-section-card mb-4">
        <div class="card-header bg-transparent border-bottom fw-semibold py-2">
          <span style="margin-right: 8px;">📖</span><fmt:message key="common.guestbook"/>
        </div>
        <div class="card-body py-3">
          ${GUESTBOOK}
        </div>
      </div>
    </div>
  </div>
</div>
</t:layout>