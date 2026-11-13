<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><%@ taglib prefix="t" tagdir="/WEB-INF/tags" %><t:layout design="napping_account"><h1>${SERVER_SOFTWARE} &ndash; Account</h1>

<form action="${paths.account}" method="POST" accept-charset="utf-8">${account_error_if_one}
  <input type="hidden" name="service" value="account">
  <input type="hidden" name="data" value="change">
  <input type="hidden" name="owner" value="${owner}">

  <p class="form-section-title"><fmt:message key="common.personal_information"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Homepage</label>
      <input type="url" class="form-control form-control-sm" name="homepage" value="${homepage}" placeholder="https://">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.city"/></label>
      <input type="text" class="form-control form-control-sm" name="city" value="${city}" placeholder="<fmt:message key="common.city_2"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Hobby</label>
      <input type="text" class="form-control form-control-sm" name="hobby" value="${hobby}" placeholder="<fmt:message key="common.hobbies"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.family_status_2"/></label>
      <input type="text" class="form-control form-control-sm" name="fam_status" value="${fam_status}" placeholder="<fmt:message key="common.e_g_single"/>">
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.about_me"/></p>

  <div class="mb-3">
    <label class="form-label fw-semibold small"><fmt:message key="common.description_2"/></label>
    <textarea class="form-control form-control-sm" name="description" rows="3" placeholder="<fmt:message key="common.tell_us_about_yourself"/>">${description}</textarea>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Motto</label>
      <input type="text" class="form-control form-control-sm" name="slogan" value="${slogan}" placeholder="<fmt:message key="common.your_motto"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small"><fmt:message key="common.signature_2"/></label>
      <input type="text" class="form-control form-control-sm" name="signature" value="${signature}" placeholder="<fmt:message key="common.signature_2"/>">
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.contact_social"/></p>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Facebook</label>
      <input type="text" class="form-control form-control-sm" name="facebook" value="${facebook}" placeholder="<fmt:message key="common.facebook_profile"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Twitter / X</label>
      <input type="text" class="form-control form-control-sm" name="twitter" value="${twitter}" placeholder="@username">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">YouTube</label>
      <input type="text" class="form-control form-control-sm" name="youtube" value="${youtube}" placeholder="<fmt:message key="common.youtube_channel"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Instagram</label>
      <input type="text" class="form-control form-control-sm" name="instagram" value="${instagram}" placeholder="@username">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">LinkedIn</label>
      <input type="text" class="form-control form-control-sm" name="linkedin" value="${linkedin}" placeholder="<fmt:message key="common.username"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">TikTok</label>
      <input type="text" class="form-control form-control-sm" name="tiktok" value="${tiktok}" placeholder="@username">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Discord</label>
      <input type="text" class="form-control form-control-sm" name="discord" value="${discord}" placeholder="<fmt:message key="common.username"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Twitch</label>
      <input type="text" class="form-control form-control-sm" name="twitch" value="${twitch}" placeholder="<fmt:message key="common.username"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">GitHub</label>
      <input type="text" class="form-control form-control-sm" name="github" value="${github}" placeholder="<fmt:message key="common.username"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Reddit</label>
      <input type="text" class="form-control form-control-sm" name="reddit" value="${reddit}" placeholder="<fmt:message key="common.username"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Snapchat</label>
      <input type="text" class="form-control form-control-sm" name="snapchat" value="${snapchat}" placeholder="<fmt:message key="common.username"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Pinterest</label>
      <input type="text" class="form-control form-control-sm" name="pinterest" value="${pinterest}" placeholder="<fmt:message key="common.username"/>">
    </div>
  </div>

  <div class="row g-2 mb-3">
    <div class="col-md-6">
      <label class="form-label fw-semibold small">WhatsApp</label>
      <input type="text" class="form-control form-control-sm" name="whatsapp" value="${whatsapp}" placeholder="<fmt:message key="common.phone_number_with_country_code"/>">
    </div>
    <div class="col-md-6">
      <label class="form-label fw-semibold small">Telegram</label>
      <input type="text" class="form-control form-control-sm" name="telegram" value="${telegram}" placeholder="@username">
    </div>
  </div>

  <p class="form-section-title"><fmt:message key="common.save"/></p>

  <input type="submit" class="btn btn-primary btn-sm" name="OK" value="<fmt:message key="common.submit_change_2"/>">
</form>
</t:layout>