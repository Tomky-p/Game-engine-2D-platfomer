module org.tomek.engine {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires org.mockito;
    requires org.junit.jupiter.api;

    opens org.tomek.engine to javafx.fxml;
    exports org.tomek.engine;
    exports org.tomek.engine.exceptions;
    opens org.tomek.engine.exceptions to javafx.fxml;
    exports org.tomek.engine.enums;
    opens org.tomek.engine.enums to javafx.fxml;
    exports org.tomek.engine.gameobjects;
    opens org.tomek.engine.gameobjects to javafx.fxml, org.junit.platform.commons;
    exports org.tomek.engine.controls;
    opens org.tomek.engine.controls to javafx.fxml, org.junit.platform.commons;
    exports org.tomek.engine.views;
    opens org.tomek.engine.views to javafx.fxml;
    exports org.tomek.engine.models;
    opens org.tomek.engine.models to javafx.fxml;


}