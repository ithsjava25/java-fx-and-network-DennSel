module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.xml;
    requires java.net.http;
    requires java.desktop;
    requires tools.jackson.databind;
    requires java.sql;
    requires javafx.graphics;

    opens com.example to javafx.fxml;
    exports com.example;
}