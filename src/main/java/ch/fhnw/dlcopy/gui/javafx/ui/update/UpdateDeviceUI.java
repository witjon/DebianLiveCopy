
package ch.fhnw.dlcopy.gui.javafx.ui.update;

import ch.fhnw.dlcopy.DLCopy;
import static ch.fhnw.dlcopy.DLCopy.MEGA;
import ch.fhnw.dlcopy.PartitionState;
import ch.fhnw.dlcopy.RunningSystemSource;
import ch.fhnw.dlcopy.SystemSource;
import ch.fhnw.dlcopy.gui.javafx.ui.StartscreenUI;
import ch.fhnw.dlcopy.gui.javafx.ui.View;
import ch.fhnw.dlcopy.gui.javafx.ui.install.SelectDeviceUI;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.fxml.FXML;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.freedesktop.dbus.exceptions.DBusException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;


public class UpdateDeviceUI  extends View{
    
    private static final Logger LOGGER = Logger.getLogger(UpdateDeviceUI.class.getName());
    private static final ProcessExecutor PROCESS_EXECUTOR = new ProcessExecutor();
    private SystemSource runningSystemSource;
    private boolean showHarddisks = false;
    private final Timer listUpdateTimer = new Timer();
    private ObservableList<StorageDevice> selectedStds;
    
    @FXML private Button btnAddFileToOverwritte;
    @FXML private Button btnEditFileToOverwritte;
    @FXML private Button btnExportFileToOverwritte;
    @FXML private Button btnImportFileToOverwritte;
    @FXML private Button btnRemoveFileToOverwritte;
    @FXML private ListView<String> lvFilesToOverwritte;
    @FXML private ListView<StorageDevice> lvDevices;
    @FXML private Button btnBack;
    @FXML private Button btnExport;

    public UpdateDeviceUI() {   
        Map<String, String> environment = new HashMap<>();
        environment.put("LC_ALL", "C");
        PROCESS_EXECUTOR.setEnvironment(environment);

        try {

            runningSystemSource = new RunningSystemSource(PROCESS_EXECUTOR);
        } catch (DBusException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        resourcePath = getClass().getResource("/fxml/update/update.fxml");
    }
    
     /**
     * This function is called, when the view should be deinitalized.
     * It has to be called manually!
     */
    @Override
    public void deinitialize() {
        listUpdateTimer.cancel();
    }
    
    private void confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                stringBundle.getString("updateconfirm.confirm"), 
                ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle(stringBundle.getString("updateconfirm.header"));
        alert.setHeaderText(stringBundle.getString("updateconfirm.consequences"));
        alert.setContentText(message);
        alert.showAndWait();
    }
        
    @Override
    protected void setupEventHandlers() {
        btnBack.setOnAction(event -> {
            context.setScene(new StartscreenUI());
        });    
        
        btnExport.setOnAction(event ->{
           confirm(stringBundle.getString("updateconfirm.lastwarning"));
        });
        chbShowHarddisk.setOnAction(event -> {
            showHarddisks = valChb(chbShowHarddisk);
        });
        
        TimerTask listUpdater = new TimerTask() {
            @Override
            public void run() {
                try{
                List<StorageDevice> pluggedDevices = DLCopy.getStorageDevices(showHarddisks, false, "bootDeviceName");
                List<StorageDevice> removedDevices = new ArrayList<>();
                List<StorageDevice> addedDevices = new ArrayList<>();
                for (StorageDevice device : pluggedDevices) {
                    if (!lvDevices.getItems().contains(device)) {
                        
                        // Plugged deice is not shown yet
                        addedDevices.add(device);
                    }
                }
                for (StorageDevice device : lvDevices.getItems()) {
                    if (!pluggedDevices.contains(device)) {
                        removedDevices.add(device);
                    }
                }
                Platform.runLater(() -> {
                    lvDevices.getItems().removeAll(removedDevices);
                    lvDevices.getItems().addAll(addedDevices);
                });
                } catch (IOException | DBusException ex) {
                    Logger.getLogger(SelectDeviceUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        listUpdateTimer.scheduleAtFixedRate(listUpdater, 0, 1000L); // Starts the `lisstUpdater`-task each 1000ms (1sec)
        lvDevices.setPlaceholder(new Label(stringBundle.getString("update.lvDevices")));
        
        lvDevices.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lvDevices.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends StorageDevice> ov, StorageDevice old_val, StorageDevice new_val) -> {
             selectedStds = lvDevices.getSelectionModel().getSelectedItems();
        });

        // Add files to the list
        btnAddFileToOverwritte.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(btnAddFileToOverwritte.getScene().getWindow());
            fileChooser.setTitle(stringBundle.getString("export.chooseDirectory")); 
            if (selectedFile != null) {
                lvFilesToOverwritte.getItems().add(selectedFile.getAbsolutePath());
            }
        });
        
        // Aktivate the remove button, when a item is selected
        btnRemoveFileToOverwritte.disableProperty().bind(
                lvFilesToOverwritte.getSelectionModel().selectedItemProperty().isNull()
        );
        
        // Remove files from the list
        btnRemoveFileToOverwritte.setOnAction(event -> {
            lvFilesToOverwritte.getItems().removeAll(
                    lvFilesToOverwritte.getSelectionModel().getSelectedItems()
            );
        });
        
        // Aktivate the edit button, when one item is selected
        btnEditFileToOverwritte.disableProperty().bind(
                lvFilesToOverwritte.getSelectionModel().selectedItemProperty().isNull()
        );
        
        // Edit selected file
        btnEditFileToOverwritte.setOnAction(event -> {
            String path = lvFilesToOverwritte.getSelectionModel().getSelectedItem();
            File file = new File(path);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(file.getParent()));
            fileChooser.setTitle(stringBundle.getString("export.chooseDirectory")); 
            File selectedFile = fileChooser.showOpenDialog(btnAddFileToOverwritte.getScene().getWindow());
            lvFilesToOverwritte.getItems().remove(path);
            lvFilesToOverwritte.getItems().add(selectedFile.getAbsolutePath());
        });
        
        // Export the overwritte files to a file
        btnExportFileToOverwritte.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("files_to_overwritte.txt");
            fileChooser.setTitle(stringBundle.getString("export.chooseDirectory")); 
            File selectedFile = fileChooser.showSaveDialog(btnAddFileToOverwritte.getScene().getWindow());
            try {
                selectedFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(UpdateDeviceUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!selectedFile.canWrite()){
                // Can't writte to the file
            } else {
                // Writte lines to the file
                try {
                    PrintWriter writer;
                    writer = new PrintWriter(selectedFile);
                    lvFilesToOverwritte.getItems().forEach(file -> {
                        writer.println(file);
                    });
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(UpdateDeviceUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        // Import files from exteral file
        btnImportFileToOverwritte.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(stringBundle.getString("export.chooseDirectory")); 
            File selectedFile = fileChooser.showOpenDialog(btnAddFileToOverwritte.getScene().getWindow());
            if (!selectedFile.canRead()){
                // Can't read to the file
            } else {
                // Read lines from the file
                try {
                    lvFilesToOverwritte.getItems().clear();
                    BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                    reader.lines().forEach(file -> lvFilesToOverwritte.getItems().add(file));
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(UpdateDeviceUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
      public void updateInstallSelectionCountAndExchangeInfo() {
        // check all selected storage devices
        long minOverhead = Long.MAX_VALUE;
        boolean exchange = true;

        if (selectedStds.isEmpty()) {
            minOverhead = 0;
            exchange = false;
        } else {
            if (runningSystemSource == null) {
                LOGGER.warning("No valid system source selected!");
            } else {
                long enlargedSystemSize = DLCopy.getEnlargedSystemSize(
                        runningSystemSource.getSystemSize());

                for (StorageDevice device : selectedStds) {
                    long overhead = device.getSize()
                            - (DLCopy.EFI_PARTITION_SIZE * DLCopy.MEGA)
                            - enlargedSystemSize;

                    minOverhead = Math.min(minOverhead, overhead);

                    PartitionState partitionState = DLCopy.getPartitionState(
                            device.getSize(),
                            (DLCopy.EFI_PARTITION_SIZE * DLCopy.MEGA)
                            + enlargedSystemSize);

                    if (partitionState != PartitionState.EXCHANGE) {
                        exchange = false;
                        break; // for
                    }
                }
            }
        }

        // TODO: update all other parts of the UI
        // see ch.fhnw.dlcopy.gui.swing.InstallerPanels
    }
    
    /**
     * Validates a checkbox filed.
     *
     * @param chb
     * @return true if selected and enabled.
     */
    private boolean valChb(CheckBox chb) {
        return chb.isSelected() && !chb.isDisabled();
    }
 }
