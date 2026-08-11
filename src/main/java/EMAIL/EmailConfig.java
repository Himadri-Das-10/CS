package EMAIL;

import io.github.cdimascio.dotenv.Dotenv;

public class EmailConfig {

    private static final Dotenv dotenv = Dotenv.load();

    public static final String EMAIL_USERNAME =
            dotenv.get("EMAIL_USERNAME");

    public static final String EMAIL_PASSWORD =
            dotenv.get("EMAIL_PASSWORD");

    public static final String EMAIL_HOST =
            dotenv.get("EMAIL_HOST");

    public static final String EMAIL_PORT =
            dotenv.get("EMAIL_PORT");
}