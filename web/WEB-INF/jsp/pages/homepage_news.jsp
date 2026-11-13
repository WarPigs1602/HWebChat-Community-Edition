<%@ page contentType="text/html; charset=UTF-8" session="false" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %><section class="my-4" aria-labelledby="homepage-news-title">
  <h2 class="h4" id="homepage-news-title"><fmt:message key="page.homepage_news.latest_news"/></h2>
  <article class="card">
    <div class="card-body">
      <h3 class="h5">${homepage_news_title}</h3>
      <p class="text-muted">${homepage_news_author} &middot; ${homepage_news_published}</p>
      <div class="homepage-news-body">${homepage_news_body}</div>
      <p class="mt-3 mb-0"><a href="${homepage_news_url}"><fmt:message key="page.homepage_news.all_news"/></a></p>
    </div>
  </article>
</section>
