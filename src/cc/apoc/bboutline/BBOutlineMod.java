/**
 * This is a mod that outlines component bounding boxes of nether fortresses. 
 * 
 * Those are important because they directly influence the spawning of Wither Skeletons.
 * 
 * @author apoc <http://apoc.cc>
 * @version v0.6
 * @license 3-clause BSD
 */
package cc.apoc.bboutline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * BBOutlineMod Base Class of this Forge mod.
 * 
 * This class acts as the main class of the mod, it mainly
 * loads ClientProxy/(or just)CommonProxy depending if the mod
 * is run in client or server.
 */
@Mod(modid="BBOutlineMod", name="BBox Outline Mod", version="@VERSION@")
@NetworkMod(clientSideRequired=false, serverSideRequired=false,
    channels={"BBOutlineModCV3"}, packetHandler = PacketHandler.class)
public class BBOutlineMod {
    @Instance("BBOutlineMod")
    public static BBOutlineMod instance;
    
    public static final String channel = "BBOutlineModCV3";

    public Config config;
    


    @SidedProxy(clientSide="cc.apoc.bboutline.ClientProxy", serverSide="cc.apoc.bboutline.CommonProxy")
    public static CommonProxy proxy;

    private long tickTime;

    public ConcurrentHashMap<EntityPlayerMP, Integer> playerDimensions = 
            new ConcurrentHashMap<EntityPlayerMP, Integer>();
    
    @Init
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(proxy);

        
        TickRegistry.registerTickHandler(new CommonTickHandler(), Side.SERVER);
        TickRegistry.registerTickHandler(new CommonTickHandler(), Side.CLIENT);
        

        
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.SERVER) {
            
            NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());
            
        }
        
        // IConnectionHandler
        
        config = new Config(new File(proxy.getMinecraftDir(), "config/bboutline.properties"));
        proxy.config = config;
    }

    public void tick() {
        tickTime = System.currentTimeMillis();

        if (proxy instanceof ClientProxy) {
            ((ClientProxy) proxy).pollHotkeys(tickTime);
        }
        else {
            for (EntityPlayerMP player : playerDimensions.keySet()) {
                
                MinecraftServer mc = MinecraftServer.getServer(); 
                if (!mc.getConfigurationManager().playerEntityList.contains(player)) {
                    playerDimensions.remove(player);
                }
                else {
                    int dimensionId = playerDimensions.get(player);
                    if (proxy.cache.containsKey(dimensionId)) {
                        proxy.cache.get(dimensionId).sendToPlayer(player);
                    }
                }
            }
        }
    }

    /**
     * This returns true if this minecraft instance is currently running
     * as a client connected to a server.
     * @return boolean
     */
    public boolean isClientConnection() {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            if (!Minecraft.getMinecraft().isSingleplayer()) {
                return true;
            }
        }
        return false;
        
        /*
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.SERVER) {
            return false;
        } else if (side == Side.CLIENT) {
            return true;
        }
        return false;
        */
    }
    
    
}


