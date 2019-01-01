package ycble.runchinaup.ota.absimpl.htx;

import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
	private static HashMap<String, String> attributes = new HashMap<String, String>();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static SoundPool mSoundPool;
    
    public static String RSSI_VALUE = "0000ffa1-0000-1000-8000-00805f9b34fb";
    
    public static String RSSI_CONFIGARATION = "0000ffa2-0000-1000-8000-00805f9b34fb";
    
    public static String BLUE_RECV_VALUE = "0000ffe4-0000-1000-8000-00805f9b34fb";
    
    public static String TEMP_MEASUREMENT = "00002a1c-0000-1000-8000-00805f9b34fb";
    
    
    public static String otas_tx_cmd_uuid = "0000ff01-0000-1000-8000-00805f9b34fb";
    
    public static String otas_tx_dat_uuid = "0000ff02-0000-1000-8000-00805f9b34fb";

	public static String otas_tx_ips_cmd_uuid = "6e40ff02-b5a3-f393-e0a9-e50e24dcca9e";

	public static String otas_rx_ips_cmd_uuid = "6e40ff03-b5a3-f393-e0a9-e50e24dcca9e";

	public static String otas_rx_cmd_uuid = "0000ff03-0000-1000-8000-00805f9b34fb";
    
    public static String otas_rx_dat_uuid = "0000ff04-0000-1000-8000-00805f9b34fb";

    public static String color_lamp_control_uuid = "0000ffb2-0000-1000-8000-00805f9b34fb";

	// sp
	public static final String SP_APP_NAME = "app_name";
	public static final String SP_APP_URI = "app_uri";
	public static final String SP_CFG_NAME = "cfg_name";
	public static final String SP_CFG_URI = "cfg_uri";
	public static final String SP_PATCH_NAME = "patch_name";
	public static final String SP_PATCH_URI = "patch_uri";
	public static final String SP_USER_NAME = "user_name";
	public static final String SP_USER_URI = "user_uri";
	public static final String SP_USER_ADDR = "user_addr";



	static {
    	//System Service
    	attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
    	attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
    	attributes.put("00001802-0000-1000-8000-00805f9b34fb", "Immediate Alert Service");
    	attributes.put("00001803-0000-1000-8000-00805f9b34fb", "Link Loss Service");
    	attributes.put("00001804-0000-1000-8000-00805f9b34fb", "Tx Power Service");
    	attributes.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
    	attributes.put("00001806-0000-1000-8000-00805f9b34fb", "Reference Time Update Service");
    	attributes.put("00001807-0000-1000-8000-00805f9b34fb", "Next DST Change Service");
    	attributes.put("00001808-0000-1000-8000-00805f9b34fb", "Glucose");
    	attributes.put("00001809-0000-1000-8000-00805f9b34fb", "Health Thermometer");
    	attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
    	attributes.put("0000180b-0000-1000-8000-00805f9b34fb", "Network Availability Service");
    	//attributes.put("0000180c-0000-1000-8000-00805f9b34fb", "Heart Rate");
    	attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate");
    	attributes.put("0000180e-0000-1000-8000-00805f9b34fb", "Phone Alert Status Service");
    	attributes.put("00001810-0000-1000-8000-00805f9b34fb", "Blood Pressure");
    	attributes.put("00001812-0000-1000-8000-00805f9b34fb", "Human Interface Device");
    	attributes.put("00001813-0000-1000-8000-00805f9b34fb", "Scan Parameters");
    	
    	attributes.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence");
    	attributes.put("00001815-0000-1000-8000-00805f9b34fb", "Automation IO");
    	attributes.put("00001816-0000-1000-8000-00805f9b34fb", "Cycling Speed and Cadence");
    	attributes.put("00001818-0000-1000-8000-00805f9b34fb", "Cycling Power");
    	attributes.put("00001819-0000-1000-8000-00805f9b34fb", "Location and Navigation");
    	
    	attributes.put("00001811-0000-1000-8000-00805f9b34fb", "Alert Notification Service");
    	attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
    	
    	attributes.put("00001234-0000-1000-8000-00805f9b34fb", "Huntersun OTA Service");
    	//System Characteristics.
    	attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
    	attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
    	attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
    	attributes.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
    	attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Pre Con Params");
    	attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
    	attributes.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
    	attributes.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
    	attributes.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
    	attributes.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
    	attributes.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
    	attributes.put("00002a0b-0000-1000-8000-00805f9b34fb", "Exact Time 100");
    	attributes.put("00002a0c-0000-1000-8000-00805f9b34fb", "Exact Time 256");
    	attributes.put("00002a0d-0000-1000-8000-00805f9b34fb", "DST Offset");
    	attributes.put("00002a0e-0000-1000-8000-00805f9b34fb", "Time Zone");
    	attributes.put("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
    	attributes.put("00002a10-0000-1000-8000-00805f9b34fb", "Secondary Time Zone");
    	attributes.put("00002a11-0000-1000-8000-00805f9b34fb", "Time with DST");
    	attributes.put("00002a12-0000-1000-8000-00805f9b34fb", "Time Accuracy");
    	attributes.put("00002a13-0000-1000-8000-00805f9b34fb", "Time Source");
    	attributes.put("00002a14-0000-1000-8000-00805f9b34fb", "Reference Time Information");
    	attributes.put("00002a15-0000-1000-8000-00805f9b34fb", "Time Broadcast");
    	attributes.put("00002a16-0000-1000-8000-00805f9b34fb", "Time Update Control Point");
    	
    	attributes.put("00002a17-0000-1000-8000-00805f9b34fb", "Time Update State");
    	attributes.put("00002a18-0000-1000-8000-00805f9b34fb", "Glucose Measurement");
    	attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
    	attributes.put("00002a1a-0000-1000-8000-00805f9b34fb", "Battery Power State");
    	attributes.put("00002a1b-0000-1000-8000-00805f9b34fb", "Battery Level State");
    	attributes.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
    	attributes.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
    	attributes.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
    	attributes.put("00002a1f-0000-1000-8000-00805f9b34fb", "Temperature in Celsius");
    	attributes.put("00002a20-0000-1000-8000-00805f9b34fb", "Temperature in Fahrenheit");
    	//System Descriptor
    	attributes.put("00002900-0000-1000-8000-00805f9b34fb", "Characteristic Extended Properties");
    	attributes.put("00002901-0000-1000-8000-00805f9b34fb", "Characteristic User Description");
    	attributes.put("00002902-0000-1000-8000-00805f9b34fb", "Client Characteristic Configuration");
    	attributes.put("00002903-0000-1000-8000-00805f9b34fb", "Server Characteristic Configuration");
    	attributes.put("00002904-0000-1000-8000-00805f9b34fb", "Characteristic Presentation Format");
    	attributes.put("00002905-0000-1000-8000-00805f9b34fb", "Characteristic Aggregate Format");
    	attributes.put("00002906-0000-1000-8000-00805f9b34fb", "Valid Range");
    	attributes.put("00002907-0000-1000-8000-00805f9b34fb", "External Report Reference");
    	attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        // Sample Services.
    	attributes.put("0000ffe5-0000-1000-8000-00805f9b34fb", "Blue Serial TX");
    	attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "Blue Serial RX"); 
    	attributes.put("0000ffa0-0000-1000-8000-00805f9b34fb", "Rssi Report");
        // Sample Characteristics.
    	attributes.put("0000ffe9-0000-1000-8000-00805f9b34fb", "Write Value");
    	attributes.put("0000ffe4-0000-1000-8000-00805f9b34fb", "Notification Value");
    	attributes.put("0000ffa1-0000-1000-8000-00805f9b34fb", "Rssi Value");
    	attributes.put("0000ffa2-0000-1000-8000-00805f9b34fb", "Rssi Configuration");
    	
    	//Huntersun OTA Characteristic
    	attributes.put("0000ff01-0000-1000-8000-00805f9b34fb", "otas tx cmd uuid");
    	attributes.put("0000ff02-0000-1000-8000-00805f9b34fb", "otas tx dat uuid");
    	attributes.put("0000ff03-0000-1000-8000-00805f9b34fb", "otas rx cmd uuid");
    	attributes.put("0000ff04-0000-1000-8000-00805f9b34fb", "otas rx dat uuid");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
    
   
    
    public static void initalerpaly(){
    	mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 5);
    }
}
