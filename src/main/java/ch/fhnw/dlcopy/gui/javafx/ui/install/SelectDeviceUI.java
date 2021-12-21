package ch.fhnw.dlcopy.gui.javafx.ui.install;

import ch.fhnw.dlcopy.DLCopy;
import ch.fhnw.dlcopy.gui.javafx.ui.View;
import ch.fhnw.util.StorageDevice;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.freedesktop.dbus.exceptions.DBusException;

public class SelectDeviceUI extends View {
    
    private List<StorageDevice> devices;
    
    @FXML private ListView<StorageDevice> lvDevices;
    
    public SelectDeviceUI(){
        resourcePath = getClass().getResource("/fxml/install/selectdevice.fxml");
    }
    
    protected void initControls(){
        TimerTask listUpdater = new TimerTask(){
            @Override
            public void run() {
                try {
                    devices = DLCopy.getStorageDevices(false, false, "bootDeviceName");
                    Platform.runLater(() -> {
                        lvDevices.getItems().clear();
                        lvDevices.getItems().addAll(devices);
                    });
                } catch (Exception ex) {
                    Logger.getLogger(SelectDeviceUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        
        Timer listUpdateTimer = new Timer();
        listUpdateTimer.scheduleAtFixedRate(listUpdater, 0, 1000L); // Starts the `lisstUpdater`-task each 1000ms (1sec)
    }
}