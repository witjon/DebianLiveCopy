package ch.fhnw.dlcopy.gui.javafx.ui.exportdata;

import ch.fhnw.dlcopy.RunningSystemSource;
import ch.fhnw.dlcopy.SystemSource;
import ch.fhnw.dlcopy.gui.javafx.SwitchButton;
import ch.fhnw.dlcopy.gui.javafx.ui.StartscreenUI;
import ch.fhnw.dlcopy.gui.javafx.ui.View;
import ch.fhnw.util.LernstickFileTools;
import ch.fhnw.util.ProcessExecutor;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import org.freedesktop.dbus.exceptions.DBusException;

public class ExportDataUI extends View {

    private SystemSource runningSystemSource;
    private static final ProcessExecutor PROCESS_EXECUTOR = new ProcessExecutor();
    private static final Logger LOGGER = Logger.getLogger(ExportDataUI.class.getName());

    @FXML private Label lblTargetDirectory;
    @FXML private TextField tfTargetDirectory;
    @FXML private Label lblWriteable;
    @FXML private Label lblWriteableDisplay;
    @FXML private Label lblFreeSpace;
    @FXML private Label lblFreeSpaceDisplay;
    @FXML private CheckBox chbInformationDialog;
    @FXML private CheckBox chbInstallationProgram;
    @FXML private Button btnExport;
    @FXML private Button btnBack;
    @FXML private SwitchButton switchBtn;

    public ExportDataUI() {
        Map<String, String> environment = new HashMap<>();
        environment.put("LC_ALL", "C");
        PROCESS_EXECUTOR.setEnvironment(environment);

        try {
            runningSystemSource = new RunningSystemSource(PROCESS_EXECUTOR);
        } catch (DBusException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        resourcePath = getClass().getResource("/fxml/exportdata/exportdata.fxml");
    }

    @Override
    protected void initControls() {
        btnExport.setDisable(true);
        
        addToolTip(tfTargetDirectory, stringBundle.getString("export.tooltip.targetDirectory"));
        addToolTip(chbInformationDialog,stringBundle.getString("export.tooltip.informationDialog"));
        addToolTip(chbInstallationProgram,stringBundle.getString("export.tooltip.installationProgram"));
        addToolTip(lblFreeSpace, stringBundle.getString("export.tooltip.freeSpace"));
        addToolTip(lblTargetDirectory, stringBundle.getString("export.tooltip.targetDirectory"));
        addToolTip(lblFreeSpaceDisplay, stringBundle.getString("export.tooltip.freeSpace"));
        addToolTip(lblWriteable, stringBundle.getString("export.tooltip.writeable"));
        addToolTip(lblWriteableDisplay, stringBundle.getString("export.tooltip.writeable"));
        addToolTip(switchBtn, stringBundle.getString("global.tooltip.expertMode"));
    }
    

    @Override
    protected void setupEventHandlers() {
        
        switchBtn.setOnAction(event -> {
            toggleExpertMode();
        });

        tfTargetDirectory.setOnAction(event -> {
            selectDirectory();
        });
        
        tfTargetDirectory.setOnMouseClicked(event -> {
            selectDirectory();
        });

        btnBack.setOnAction(event -> {
            context.setScene(new StartscreenUI());
        });

        btnExport.setOnAction((ActionEvent event) -> {
            if (tfTargetDirectory.getText().isBlank()) {
                LOGGER.log(Level.WARNING, "No directory choosen.");
                return;
            }
            createDataPartiton();
        });
    }  

    private void selectDirectory() {
        DirectoryChooser folder = new DirectoryChooser();
        File selectedDirectory = folder.showDialog(tfTargetDirectory.getScene().getWindow());
        folder.setTitle(stringBundle.getString("export.chooseDirectory"));
        if (selectedDirectory != null) {
            tfTargetDirectory.setText(selectedDirectory.getAbsolutePath());
            checkFreeSpace();
        }
    }

    private void checkFreeSpace() {
        File tmpDir = new File(tfTargetDirectory.getText());
        if (tmpDir.exists()) {
            long freeSpace = tmpDir.getFreeSpace();
            lblFreeSpaceDisplay.setText(
                    LernstickFileTools.getDataVolumeString(freeSpace, 1));
            if (tmpDir.canWrite()) {
                lblWriteableDisplay.setText(stringBundle.getString("global.yes"));
                lblWriteableDisplay.getStyleClass().clear();
                lblWriteableDisplay.getStyleClass().add("target-rw");
                btnExport.setDisable(false);
            } else {
                lblWriteableDisplay.setText(stringBundle.getString("global.no"));
                lblWriteableDisplay.getStyleClass().clear();
                lblWriteableDisplay.getStyleClass().add("target-ro");
                btnExport.setDisable(true);
                printInfo(stringBundle.getString("error.error") + ": " + stringBundle.getString("error.notWriteable"));
            }
        } else {
            lblWriteableDisplay.setText(
                stringBundle.getString("error.directoryDoesNotExist"));
            lblWriteableDisplay.getStyleClass().clear();
            lblWriteableDisplay.getStyleClass().add("target-na");
            btnExport.setDisable(true);
            printInfo(stringBundle.getString("error.error") + ": " + stringBundle.getString("error.directoryDoesNotExist"));
        }
    }

    private void createDataPartiton() {
        Task exporter = new ExportDataTask(
            ExportControler.getInstance(context),
            runningSystemSource,
            tfTargetDirectory.getText(),
            chbInformationDialog.isSelected(),
            chbInstallationProgram.isSelected()
        );
        new Thread(exporter).start();
        context.setScene(new LoadUI());
    }

    private void toggleExpertMode() {
        for (Node n : new Node[]{chbInformationDialog, chbInstallationProgram}) {
            n.setVisible(switchBtn.isEnabled());
        }
    }
}
