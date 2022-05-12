package ch.fhnw.dlcopy.gui.javafx.ui.update;

import ch.fhnw.dlcopy.DLCopy;
import ch.fhnw.dlcopy.PartitionState;
import ch.fhnw.dlcopy.RepartitionStrategy;
import ch.fhnw.dlcopy.RunningSystemSource;
import ch.fhnw.dlcopy.SystemSource;
import ch.fhnw.dlcopy.Upgrader;
import ch.fhnw.dlcopy.gui.javafx.ui.StartscreenUI;
import ch.fhnw.dlcopy.gui.javafx.ui.View;
import ch.fhnw.dlcopy.gui.javafx.ui.install.SelectDeviceUI;
import ch.fhnw.util.ProcessExecutor;
import ch.fhnw.util.StorageDevice;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.FileChooser;
import org.freedesktop.dbus.exceptions.DBusException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;


public class UpdateDeviceUI extends View {

    private static final Logger LOGGER = Logger.getLogger(UpdateDeviceUI.class.getName());
    private static final ProcessExecutor PROCESS_EXECUTOR = new ProcessExecutor();
    private SystemSource runningSystemSource;
    private boolean showHarddisks = false;
    private final Timer listUpdateTimer = new Timer();
    private ObservableList<StorageDevice> selectedStds;
    private RepartitionStrategy repartitionStrategy = RepartitionStrategy.KEEP;

    @FXML private Button btnAddFileToOverwritte;
    @FXML private Button btnBack;
    @FXML private Button btnEditFileToOverwritte;
    @FXML private Button btnExportFileToOverwritte;
    @FXML private Button btnFileDown;
    @FXML private Button btnFileUp;
    @FXML private Button btnFilesSort;
    @FXML private Button btnFilesSortReverse;
    @FXML private Button btnImportFileToOverwritte;
    @FXML private Button btnRemoveFileToOverwritte;
    @FXML private Button btnUpgrade;
    @FXML private CheckBox chbShowHarddisk;
    @FXML private ListView<StorageDevice> lvDevices;
    @FXML private ListView<String> lvFilesToOverwritte;
    @FXML private RadioButton rdbOriginalExchange;
    @FXML private RadioButton rdbRemoveExchange;
    @FXML private RadioButton rdbResizeExchange;
    @FXML private TextField tfResizeExchange;

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


    private void confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                stringBundle.getString("updateconfirm.confirm"),
                ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle(stringBundle.getString("updateconfirm.header"));
        alert.setHeaderText(stringBundle.getString("updateconfirm.consequences"));
        alert.setContentText(message);
        alert.showAndWait();
    }


     /**
     * This function is called, when the view should be deinitalized.
     * It has to be called manually!
     */
    @Override
    public void deinitialize() {
        listUpdateTimer.cancel();
    }

    @Override
    protected void setupEventHandlers() {
        btnBack.setOnAction(event -> {
            context.setScene(new StartscreenUI());
        });

        chbShowHarddisk.setOnAction(event -> {
            showHarddisks = valChb(chbShowHarddisk);
        });

        TimerTask listUpdater = new TimerTask() {
            @Override
            public void run() {
                try {
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
            }
        );

        lvFilesToOverwritte.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Add files to the list
        btnAddFileToOverwritte.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(btnAddFileToOverwritte.getScene().getWindow());
            fileChooser.setTitle(stringBundle.getString("export.chooseDirectory"));
            if (!selectedFiles.isEmpty()) {
                for (File f : selectedFiles) {
                    lvFilesToOverwritte.getItems().add(f.getAbsolutePath());
                }
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
        });

        // Import files from exteral file
        btnImportFileToOverwritte.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(stringBundle.getString("export.chooseDirectory"));
            File selectedFile = fileChooser.showOpenDialog(btnAddFileToOverwritte.getScene().getWindow());
            // Read lines from the file
            try {
                lvFilesToOverwritte.getItems().clear();
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                reader.lines().forEach(file -> lvFilesToOverwritte.getItems().add(file));
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(UpdateDeviceUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        // Activate up button, when a file is selected and it is not the most top one
        lvFilesToOverwritte.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean firstItem = lvFilesToOverwritte.getItems().indexOf(lvFilesToOverwritte.getSelectionModel().getSelectedItem()) == 0;
            if (newValue != null) {
                // new selection is not empty
                btnFileUp.setDisable(firstItem || (lvFilesToOverwritte.getSelectionModel().getSelectedIndices().size() != 1));
            }
        });

        // Move file up in the file list
        btnFileUp.setOnAction(event -> {
            String selection = lvFilesToOverwritte.getSelectionModel().getSelectedItem();
            int selection_index = lvFilesToOverwritte.getItems().indexOf(selection);
            String tmp = lvFilesToOverwritte.getItems().get(selection_index - 1);
            lvFilesToOverwritte.getItems().set(selection_index - 1, selection);
            lvFilesToOverwritte.getItems().set(selection_index, tmp);
            lvFilesToOverwritte.getSelectionModel().clearSelection();
            lvFilesToOverwritte.getSelectionModel().select(selection);
        });

        // Activate up button, when a file is selected and it is not the most top one
        lvFilesToOverwritte.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int lastIndex = lvFilesToOverwritte.getItems().size() - 1;
            boolean lastItem = lvFilesToOverwritte.getItems().indexOf(lvFilesToOverwritte.getSelectionModel().getSelectedItem()) == lastIndex;
            if (newValue != null) {
                // new selection is not empty
                btnFileDown.setDisable(lastItem || (lvFilesToOverwritte.getSelectionModel().getSelectedIndices().size() != 1));
            }
        });

        // Move file down in the file list
        btnFileDown.setOnAction(event -> {
            String selection = lvFilesToOverwritte.getSelectionModel().getSelectedItem();
            int selection_index = lvFilesToOverwritte.getItems().indexOf(selection);
            String tmp = lvFilesToOverwritte.getItems().get(selection_index + 1);
            lvFilesToOverwritte.getItems().set(selection_index + 1, selection);
            lvFilesToOverwritte.getItems().set(selection_index, tmp);
            lvFilesToOverwritte.getSelectionModel().clearSelection();
            lvFilesToOverwritte.getSelectionModel().select(selection);
        });

        // Sort file list alphabeticly
        btnFilesSort.setOnAction(event -> {
            lvFilesToOverwritte.getItems().sort(Comparator.naturalOrder());
        });

        // Sort file list alphabeticly
        btnFilesSortReverse.setOnAction(event -> {
            lvFilesToOverwritte.getItems().sort(Comparator.reverseOrder());
        });

        tfResizeExchange.setDisable(true);
        rdbOriginalExchange.setOnAction(event -> {
            repartitionStrategy = RepartitionStrategy.KEEP;
            tfResizeExchange.setDisable(true);
        });
        rdbRemoveExchange.setOnAction(event -> {
            repartitionStrategy = RepartitionStrategy.REMOVE;
            tfResizeExchange.setDisable(true);
        });
        rdbResizeExchange.setOnAction(event -> {
            repartitionStrategy = RepartitionStrategy.RESIZE;
            tfResizeExchange.setDisable(false);
        });

        btnUpgrade.setOnAction(event -> {
            confirm(stringBundle.getString("updateconfirm.lastwarning"));
            update();
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

    private void update() {
        new Upgrader(
                runningSystemSource, // the system source
                lvDevices.getItems(), // the list of StorageDevices to upgrade
                "", // the label of the exchange partition
                "", // the file system of the exchange partition
                "", // the file system of the data partition
                new HashMap<>(), // a global digest cache for speeding up repeated file checks
                null, // the main DLCopy instance
                context, // the DLCopy GUI
                RepartitionStrategy.KEEP, // the repartition strategie for the exchange partition
                0, // the new size of the exchange partition if we want to resize it during upgrade
                false, // if an automatic backup should be run before upgrading
                "", // the destination for automatic backups
                false, // if temporary backups should be deleted
                false, // if the system partition should be upgraded
                false, // if the data partition should be reset
                false, // if the printer settings should be kept when upgrading
                false, // if the network settings should be kept when upgrading
                false, // if the firewall settings should be kept when upgrading
                false, // if the user name, password and groups should be kept when upgrading
                false, // if the welcome program should be reactivated
                false, // if hidden files in the user's home of the storage device should be deleted
                lvFilesToOverwritte.getItems(), // the list of files to copy from the currently
                DLCopy.getEnlargedSystemSize(runningSystemSource.getSystemSize()), // the "enlarged" system size (multiplied with a small file system overhead factor)
                new ReentrantLock() // the lock to aquire before executing in background
        ).execute();
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
