/*
 * UsbRenderer.java
 *
 * Created on 16. April 2008, 13:23
 */
package dlcopy;

import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * A renderer for storage devices
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class LiveStorageDeviceRenderer extends JPanel implements ListCellRenderer {

    private final static Logger LOGGER =
            Logger.getLogger(DLCopy.class.getName());
    private final long systemSize;
    private final Color LIGHT_BLUE = new Color(170, 170, 255);
    private long maxStorageSize;

    /** Creates new form UsbRenderer
     * @param systemSize the size of the system to be copied in Byte
     */
    public LiveStorageDeviceRenderer(long systemSize) {
        this.systemSize = systemSize;
        initComponents();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof StorageDevice) {

            StorageDevice storageDevice = (StorageDevice) value;
            
            // set device text and icon based on storage type
            String description = null;
            long usbStorageSize = storageDevice.getSize();
            if (storageDevice instanceof UsbStorageDevice) {
                UsbStorageDevice usbStorageDevice =
                        (UsbStorageDevice) storageDevice;
                description = usbStorageDevice.getVendor() + " "
                        + usbStorageDevice.getModel() + ", "
                        + DLCopy.getDataVolumeString(usbStorageSize, 1) + " ("
                        + usbStorageDevice.getDevice() + ")";
                iconLabel.setIcon(new ImageIcon(getClass().getResource(
                        "/dlcopy/icons/32x32/drive-removable-media-usb-pendrive.png")));
            } else if (storageDevice instanceof Harddisk) {
                Harddisk harddisk = (Harddisk) storageDevice;
                description = harddisk.getVendor() + " "
                        + harddisk.getModel() + ", "
                        + DLCopy.getDataVolumeString(usbStorageSize, 1) + " ("
                        + harddisk.getDevice() + ")";
                iconLabel.setIcon(new ImageIcon(getClass().getResource(
                        "/dlcopy/icons/32x32/drive-harddisk.png")));
            } else if (storageDevice instanceof SDStorageDevice) {
                SDStorageDevice sdStorageDevice =
                        (SDStorageDevice) storageDevice;
                description = sdStorageDevice.getName() + " "
                        + DLCopy.getDataVolumeString(usbStorageSize, 1) + " ("
                        + sdStorageDevice.getDevice() + ")";
                iconLabel.setIcon(new ImageIcon(getClass().getResource(
                        "/dlcopy/icons/32x32/media-flash-sd-mmc.png")));
            } else {
                LOGGER.log(Level.WARNING,
                        "unsupported storage device: {0}", storageDevice);
            }
            descriptionLabel.setText(description);

            // TODO: fill partition panels
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
        }

        return this;
    }

    /**
     * sets the size of the largest USB stick
     * @param maxSize the size of the largest USB stick
     */
    public void setMaxSize(long maxSize) {
        this.maxStorageSize = maxSize;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        iconLabel = new javax.swing.JLabel();
        rightPanel = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();
        partitionPanel = new javax.swing.JPanel();
        partitionDescriptionPanel = new javax.swing.JPanel();
        separator = new javax.swing.JSeparator();

        setPreferredSize(new java.awt.Dimension(340, 70));
        setLayout(new java.awt.GridBagLayout());

        iconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/dlcopy/icons/32x32/drive-removable-media-usb-pendrive.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(iconLabel, gridBagConstraints);

        rightPanel.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("dlcopy/Strings"); // NOI18N
        descriptionLabel.setText(bundle.getString("LiveStorageDeviceRenderer.descriptionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        rightPanel.add(descriptionLabel, gridBagConstraints);

        partitionPanel.setLayout(new javax.swing.BoxLayout(partitionPanel, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        rightPanel.add(partitionPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        rightPanel.add(partitionDescriptionPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(rightPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(separator, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JPanel partitionDescriptionPanel;
    private javax.swing.JPanel partitionPanel;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JSeparator separator;
    // End of variables declaration//GEN-END:variables
}
