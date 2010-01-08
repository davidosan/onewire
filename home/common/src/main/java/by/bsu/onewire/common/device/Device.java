package by.bsu.onewire.common.device;

/**
 * This interface provide representation of OneWire device.
 * 
 * @author Aliaksandr_Zlobich
 */
public interface Device {

    /**
     * Retrieve device unique address. 
     */
    public String getAddress();
    
    /**
     * Change device address. 
     */
    public void setAddress(String address);
    
    /**
     * Return device label. Device label is a string that associate with this
     * device.
     */
    public String getLabel();

    /**
     * Set device label
     * 
     * @param label
     *            a new string that should be associated with this device.
     */
    public void setLabel(String label);

    /**
     * Get text description of this device
     */
    public String getDescription();

    /**
     * Set text description of this device
     */
    public void setDescription(String description);

    /**
     * Get type of this device.
     * 
     * @return object of <code>DeviceType</code> class that represent type of
     *         this device
     */
    public DeviceType getDeviceType();

}
