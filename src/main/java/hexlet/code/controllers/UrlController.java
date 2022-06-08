package hexlet.code.controllers;


import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;


import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UrlController {

    public static Handler showUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

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

        ctx.attribute("urls", url);
        ctx.render("urls/show.html");
    };

//    public static Handler newArticle = ctx -> {
//        Url article = new Url();
//
//        ctx.attribute("article", article);
//        ctx.render("articles/new.html");
//    };

    public static Handler addUrl = ctx -> {
        String urlFromForm = ctx.formParam("url");
        String validUrl = getHostFromUrl(urlFromForm);
        if (validUrl == null) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        Url one = new QUrl().name.contains(validUrl).findOne();
        System.out.println("349857349543098577534908750349875" + one);

        if (one != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        Url newUrl = new Url(validUrl);
        newUrl.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };


    private static String getHostFromUrl(String newUrl) {

        try {
            URL url = new URL(newUrl);
            int port = url.getPort();
            if (port < 1) {
                return url.getHost();
            } else {
                return url.getHost() + ":" + url.getPort();
            }
        } catch (Exception e) {
            System.out.println("Not valid site");
        }
        return null;
    }
}
