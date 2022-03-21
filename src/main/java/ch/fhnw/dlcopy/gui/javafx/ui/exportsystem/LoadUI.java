package ch.fhnw.dlcopy.gui.javafx.ui.install;

import static ch.fhnw.dlcopy.DLCopy.STRINGS;
import ch.fhnw.dlcopy.gui.javafx.ui.exportsystem.*;
import ch.fhnw.dlcopy.gui.javafx.ui.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class LoadUI extends View {

    @FXML private ImageView imgExportFile;
    @FXML private ProgressBar pbStatus;
    @FXML private Label taExtraInfo;    
    @FXML private Button btnBack;    
    //TODO private Textflow bPStatus;



    private String tmpMessage;
    private Timer overwriteTimer;
    private OverwriteRandomTimerTask overwriteRandomTimerTask;
    private int tmpProgress = -1;
    private List<Label> bulletPoint;
    private Label currBP;

    public LoadUI() {
<<<<<<< HEAD
        resourcePath = getClass().getResource("/fxml/exportsystem/load.fxml");
    }

    public LoadUI(String message) {
        this(message, -1);
=======
        resourcePath = getClass().getResource("/fxml/exportSystem/load.fxml");
        btnBack.setVisible(false);
        //set BtnNextToVisible
>>>>>>> 6475ba1c (feat: progress bar + progressinfo)
    }

    public LoadUI(String message, int progress) {
        this();
        tmpMessage = message;
        tmpProgress = progress;
    }
    
    //indeterminate
    public LoadUI(String message) {
        this(message, -1);
        showIndeterminateProgressBar(message);
    }
  
    
    //determinate
    public LoadUI(long value, long maximum) {
        this();
        showOverwriteRandomProgressBar(value, maximum);
    
    }

    
    public void setBulletpoint(String bPStr) {
        TextFlow textFlow = new TextFlow();
        //TODO add to dictionary.
        List<String>  bulletpoints = new ArrayList<String>();
        //TODO add to dictionary.
        bulletpoints.add("Creating_File_Systems");
        bulletpoints.add("Writing_Boot_Sector");
        bulletpoints.add("Unmounting_File_Systems");
                
        for (String s : bulletpoints){
            Text bPText= new Text(s);
            if (bPStr == s){
                bPText.setStyle("-fx-font-color: grey");
            }
            textFlow.getChildren().add(bPText);
        }
        
        
    }
    
    public void showOverwriteRandomProgressBar(long value, long maximum) {
        if (overwriteTimer == null) {
            overwriteTimer = new Timer();
            overwriteTimer.schedule(new OverwriteRandomTimerTask(pbStatus, value), 0,1000);
        }
        overwriteRandomTimerTask.setDone(value);
    }
    
    public void showIndeterminateProgressBar(final String text) {

        if (overwriteTimer != null) {
            overwriteTimer.cancel();
            overwriteTimer = null;
        }

        taExtraInfo.setText(STRINGS.getString(text));
    }
    
    public void finished(){
        pbStatus.setVisible(false);
        btnBack.setVisible(true);
        //TODO set next Button to visible, once implemented..
    }
    

    @Override
    protected void initSelf() {
        if (tmpMessage != null) {
            taExtraInfo.setText(tmpMessage);
        }
        bulletPoint = new ArrayList<Label>();
        double percent = Math.max(tmpProgress, 100) / 100;
        pbStatus.setProgress(percent);
    }

    @Override
    protected void setupEventHandlers() {
        imgExportFile.fitHeightProperty().bind(Bindings.divide(model.heightProperty(), 5.869));
        imgExportFile.fitWidthProperty().bind(Bindings.divide(model.widthProperty(), 9.8969));
    }
    
   

    /*
 AC
AC-1
AC-2
AC-3
The progress in % is shown, if provided.
The name of the ongoing task is shown (if from the backend provided)
The single steps are shown as bulletpoints witch change the colour when fullfilled
*/
    
    
    
 /*needed:
    next Button
    multilabelfxelement
    name on Label
    how should the progressbar->event communication happen? Static Progressbar?
*/
}

