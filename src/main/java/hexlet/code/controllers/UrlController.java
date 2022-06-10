package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public final class UrlController {

    public static Handler showUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        final int rowsPerPage = 10;

        PagedList<Url> pagedArticles = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedArticles.getList();

        int lastPage = pagedArticles.getTotalPageCount() + 1;
        int currentPage = pagedArticles.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl().id.equalTo(id).findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .orderBy().id.desc()
                .findList();

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("urls/show.html");
    };

    public static Handler addUrl = ctx -> {
        try {

            String urlFromForm = ctx.formParam("url");
            String validUrl = getHostFromUrl(urlFromForm);
            URL url = new URL(validUrl);
//            String validUrl = getHostFromUrl(urlFromForm);

            boolean urlExist = new QUrl()
                    .name.equalTo(validUrl)
                    .exists();

            if (urlExist) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
            } else {
                new Url(validUrl).save();

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flash-type", "success");
            }
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        ctx.redirect("/urls");
    };

//    public static Handler addUrl = ctx -> {
//
//        String urlFromForm = ctx.formParam("url");
//        String validUrl = getHostFromUrl(urlFromForm);
//        if (validUrl == null) {
//            ctx.sessionAttribute("flash", "Некорректный URL");
//            ctx.sessionAttribute("flash-type", "danger");
//            ctx.redirect("/");
//            return;
//        }
//        Url one = new QUrl().name.contains(validUrl).findOne();
//
//        if (one != null) {
//            ctx.sessionAttribute("flash", "Страница уже существует");
//            ctx.sessionAttribute("flash-type", "info");
//            ctx.redirect("/");
//
//        }
//
//        Url newUrl = new Url(validUrl);
//        newUrl.save();
//
//        ctx.sessionAttribute("flash", "Страница успешно добавлена");
//        ctx.sessionAttribute("flash-type", "success");
//        ctx.redirect("/urls");
//    };

    public static Handler addCheck = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = new QUrl().id.equalTo(id).findOne();

        try {

            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            int status = response.getStatus();

            Document doc = Jsoup.parse(response.getBody());
            String title = doc.title();
            String h1 = doc.select("h1").text();
            String description = doc.select("meta[name=description]").attr("content");

            UrlCheck newCheck = new UrlCheck(status, title, h1, description, url);
            newCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Страница недоступна");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };


    private static String getHostFromUrl(String newUrl) {

        try {
            URL url = new URL(newUrl);
            int port = url.getPort();
            if (port < 1) {
                return url.getProtocol() + "://" + url.getHost();
            } else {
                return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
            }
        } catch (Exception e) {
            System.out.println("Not valid site");
        }
        return null;
    }
}
