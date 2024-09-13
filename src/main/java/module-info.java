module org.example.telemetrysystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires eu.hansolo.tilesfx;
    requires org.snmp4j;

    opens org.example.telemetrysystem to javafx.fxml;
    exports org.example.telemetrysystem;
}