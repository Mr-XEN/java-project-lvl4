package hexlet.code;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.core.validation.ValidationException;

public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.valueOf(port);
    }

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.result("Hello World!"));
    }

    public static Javalin getApp() {

        Javalin app = Javalin.create(JavalinConfig::enableDevLogging);
        addRoutes(app);
        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

}
