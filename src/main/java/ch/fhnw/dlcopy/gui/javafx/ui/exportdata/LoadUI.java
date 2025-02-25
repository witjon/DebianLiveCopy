package ch.fhnw.dlcopy.gui.javafx.ui.exportdata;

import ch.fhnw.dlcopy.gui.javafx.ui.View;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

public class LoadUI extends View {

    @FXML private Button btnNext;
    @FXML private ImageView imgExportFile;
    @FXML private ProgressBar pbStatus;
    @FXML private Label lblStatusInfo;

    private String tmpMessage;
    private int tmpProgress = -1;

    public LoadUI() {
        resourcePath = getClass().getResource("/fxml/exportdata/load.fxml");
    }

    public LoadUI(String message) {
        this(message, -1);
    }

    public LoadUI(String message, int progress) {
        this();
        tmpMessage = message;
        tmpProgress = progress;
    }

    @Override
    protected void initSelf() {
        if (tmpMessage != null) {
            lblStatusInfo.setText(tmpMessage);
        }
        double percent = 0;
        if (tmpProgress != 0) {
            percent = Math.min(tmpProgress, 100) / 100d;
        }
        pbStatus.setProgress(percent);
        btnNext.setDisable(true);
    }

    @Override
    protected void setupEventHandlers() {
        btnNext.setOnAction(event -> {
            context.setScene(new InfoUI());
        });

        imgExportFile.fitHeightProperty().bind(Bindings.divide(model.heightProperty(), 5.869));
        imgExportFile.fitWidthProperty().bind(Bindings.divide(model.widthProperty(), 9.8969));
    }
}
