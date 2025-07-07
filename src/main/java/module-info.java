module com.ozank.fluxgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires com.google.gson;
    requires smartgraph;
//    requires jcodec;
//    requires jcodec.javase;

    opens com.ozank.viewElements to javafx.fxml;
    opens com.ozank.fluxgui to javafx.fxml;
    opens com.ozank.dataElements to com.google.gson;
    exports com.ozank.fluxgui;
    exports com.ozank.viewElements;
}