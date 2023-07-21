module com.ozank.fluxgui {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.ozank.simulation to javafx.fxml;
    opens com.ozank.fluxgui to javafx.fxml;
    exports com.ozank.fluxgui;
    exports com.ozank.simulation;
}