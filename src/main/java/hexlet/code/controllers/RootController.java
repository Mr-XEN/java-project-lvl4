package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class RootController {

    public static Handler welcome = ctx -> {
        ctx.render("index.html");
    };

//    public static Handler addUrl = ctx -> {
////        String url = ctx.formParam("url");
//        String url = "yoyoyo";
//        ctx.attribute("urls", url);
//        ctx.render("urls/show.html");
//    };
}
