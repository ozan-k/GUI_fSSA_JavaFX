module com.ozank.fluxgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires smartgraph;

    opens com.ozank.viewElements to javafx.fxml;
    opens com.ozank.fluxgui to javafx.fxml;
    exports com.ozank.fluxgui;
    exports com.ozank.viewElements;
}