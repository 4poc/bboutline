package cc.apoc.bboutline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.input.Keyboard;

import cc.apoc.bboutline.util.BBoxInt;


import cpw.mods.fml.common.FMLLog;

public class Config {
    private File file;
    
    private static final int VERSION = 2;

    public Config(File file) {
        this.file = file;
        loadConfig();
    }

    public boolean debug = false;
    public boolean fill = false;
    public float fillColorR = 1.0f;
    public float fillColorG = 0.0f;
    public float fillColorB = 0.0f;
    public float fillColorA = 0.3f;
    public boolean drawVillage = true;
    public boolean drawScattered = true;
    public boolean drawStronghold = false;
    public boolean drawMineshaft = false;
    public boolean drawNether = true;
    public Set<Integer> hotkeyToggle = new HashSet<Integer>(Arrays.asList(new Integer[] {Keyboard.KEY_F3, Keyboard.KEY_N}));
    public Set<Integer> hotkeyReload = new HashSet<Integer>(Arrays.asList(new Integer[] {Keyboard.KEY_F3, Keyboard.KEY_M}));
    public boolean seeThrough = false;
    
    public String userBBList;
    private Vector<BBoxInt> generatedUserBB;

    public void saveConfig() {
        FMLLog.info("save configuration");
        
        Properties properties = new Properties();
        
        properties.setProperty("configVersion", Integer.toString(VERSION));
        properties.setProperty("debug", Boolean.toString(debug));
        properties.setProperty("fill", Boolean.toString(fill));
        properties.setProperty("fillColorR", Float.toString(fillColorR));
        properties.setProperty("fillColorG", Float.toString(fillColorG));
        properties.setProperty("fillColorB", Float.toString(fillColorB));
        properties.setProperty("drawVillage", Boolean.toString(drawVillage));
        properties.setProperty("drawScattered", Boolean.toString(drawScattered));
        properties.setProperty("drawStronghold", Boolean.toString(drawStronghold));
        properties.setProperty("drawMineshaft", Boolean.toString(drawMineshaft));
        properties.setProperty("drawNether", Boolean.toString(drawNether));
        properties.setProperty("seeThrough", Boolean.toString(seeThrough));
        
        properties.setProperty("hotkeyToggle", serializeHotkey(hotkeyToggle));
        properties.setProperty("hotkeyReload", serializeHotkey(hotkeyReload));
        
        if (userBBList != null) {
        	properties.setProperty("userBBList", userBBList);
        }
        
        try {
            properties.store(new FileOutputStream(file), "BBOutline Config");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadConfig() {
        FMLLog.info("load configuration");
        
        if (!file.exists()) {
            saveConfig(); // saves default config
        }
        
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
            
            // reset to default if the config version changes 
            if (!properties.containsKey("configVersion") || 
                    Integer.parseInt(properties.getProperty("configVersion")) != VERSION) {
                saveConfig();
                properties.load(new FileInputStream(file));
            }
            
            debug = Boolean.parseBoolean(properties.getProperty("debug"));
            fill = Boolean.parseBoolean(properties.getProperty("fill"));
            fillColorR = Float.parseFloat(properties.getProperty("fillColorR"));
            fillColorG = Float.parseFloat(properties.getProperty("fillColorG"));
            fillColorB = Float.parseFloat(properties.getProperty("fillColorB"));
            drawVillage = Boolean.parseBoolean(properties.getProperty("drawVillage"));
            drawScattered = Boolean.parseBoolean(properties.getProperty("drawScattered"));
            drawStronghold = Boolean.parseBoolean(properties.getProperty("drawStronghold"));
            drawMineshaft = Boolean.parseBoolean(properties.getProperty("drawMineshaft"));
            drawNether = Boolean.parseBoolean(properties.getProperty("drawNether"));
            seeThrough = Boolean.parseBoolean(properties.getProperty("seeThrough"));
            
            hotkeyToggle = unserializeHotkey(properties.getProperty("hotkeyToggle"));
            hotkeyReload = unserializeHotkey(properties.getProperty("hotkeyReload"));
            
            generatedUserBB = null;
            userBBList = properties.getProperty("userBBList");
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Set<Integer> unserializeHotkey(String hotkeyList) {
        Set<Integer> hotkey = new HashSet<Integer>();
        for (String key : hotkeyList.split(",")) {
            hotkey.add(Integer.parseInt(key));
        }
        return hotkey;
    }
    
    private String serializeHotkey(Set<Integer> hotkey) {
        StringBuilder hotkeyList = new StringBuilder();
        for (int key : hotkey) {
            if (hotkeyList.length()>0)
                hotkeyList.append(",");
            hotkeyList.append(key);
        }
        return hotkeyList.toString();
    }
    
    public Vector<BBoxInt> getUserBBList() {
    	if (generatedUserBB != null) {
    		return generatedUserBB;
    	}
    	
    	generatedUserBB = new Vector<BBoxInt>();
    	
    	// box:100,10,100@25 box:100,10,100-120,10,120
    	if (userBBList != null && !userBBList.equals("")) {
    		for(String element : userBBList.split(" ")) {
    			BBoxInt bb = null;
    			FMLLog.info("user bb: " + element);
    			// Format: box:X,Y,Z@DIAMETER
    			if (element.matches("^box:\\-?\\d+,\\-?\\d+,\\-?\\d+@\\-?\\d+$")) {
    				String[] xyz = element.substring(4, element.indexOf('@')).split(",");
    				int x = Integer.parseInt(xyz[0]);
    				int y = Integer.parseInt(xyz[1]);
    				int z = Integer.parseInt(xyz[2]);
    				int radius = (int) Math.floor(Integer.parseInt(element.substring(element.indexOf('@')+1)) / 2.0);

					bb = new BBoxInt(x - radius, y - radius, z - radius, x
							+ radius, y + radius, z + radius);
    			}
    			// Format: box:X,Y,Z-X,Y,Z
    			else if (element.matches("^box:\\-?\\d+,\\-?\\d+,\\-?\\d+>\\-?\\d+,\\-?\\d+,\\-?\\d+$")) {
    				String[] xyz1 = element.substring(4, element.indexOf('>')).split(",");
    				int x1 = Integer.parseInt(xyz1[0]);
    				int y1 = Integer.parseInt(xyz1[1]);
    				int z1 = Integer.parseInt(xyz1[2]);
    				String[] xyz2 = element.substring(element.indexOf('>') + 1).split(",");
    				int x2 = Integer.parseInt(xyz2[0]);
    				int y2 = Integer.parseInt(xyz2[1]);
    				int z2 = Integer.parseInt(xyz2[2]);
    				bb = new BBoxInt(x1, y1, z1, x2, y2, z2);
    			}
    			
    			if (bb != null) {
    				FMLLog.info("parsed user bb: " + bb.toString());
    				generatedUserBB.add(bb);
    			}
    		}
    	}
    	
    	return generatedUserBB;
    }
}
