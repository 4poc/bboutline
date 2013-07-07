package cc.apoc.bboutline;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cc.apoc.bboutline.util.BBoxFactory;
import cc.apoc.bboutline.util.BBoxInt;


import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.ReflectionHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

/*
 * Handles forge events for the client and server.
 * 
 * 
 */
public class CommonProxy {
    
    public Config config;
    
    /**
     * Each dimension has a seperate BBoxCache entry, that gets
     * created/destroyed when the world is loaded/unloaded.
     * 
     * overworld=0 nether=-1 end=1
     */
    protected Map<Integer, BBoxCache> cache = new ConcurrentHashMap<Integer, BBoxCache>();
    
    
    @ForgeSubscribe
    public void worldEvent(WorldEvent.Load event) {
        int dimensionId = event.world.provider.dimensionId;

        IChunkProvider chunkProvider = null;
        if (BBOutlineMod.instance.isClientConnection()) {
            FMLLog.info("client connection");
            // this chunk provider does not generate anything, it only
            // receives the chunk data from the server.
            
            if (event.world.getChunkProvider() instanceof ChunkProviderClient) {
                chunkProvider = (ChunkProviderClient) event.world.getChunkProvider();

                // TODO: I could not find any forge event for the server to know
                // when a client is changing/entering/leaving a dimension. As a
                // workaround I send a custom "change dimension" packet.

                PacketHandler.writeChangeDimensionPacket(dimensionId);
            }
        }
        else {
            FMLLog.info("server or singleplayer");
            
            // the server world contains an encapsulated chunk provider that holds
            // the instances of the MapGen type that generate map features with bounding boxes
            
            if (event.world.getChunkProvider() instanceof ChunkProviderServer) {
                // gets the currentChunkProvider field
                chunkProvider = ReflectionHelper.getPrivateValue(
                        ChunkProviderServer.class, (ChunkProviderServer) event.world.getChunkProvider(), 2);
            }
        }
        
        if (chunkProvider != null) {
            FMLLog.info("create world dimension: %d, %s (chunkprovider: %s)", dimensionId, event.world.getClass().toString(), chunkProvider.getClass().toString());
            cache.put(dimensionId, new BBoxCache(config, event.world, dimensionId, chunkProvider));
        }
    }
    
    @ForgeSubscribe
    public void worldEvent(WorldEvent.Unload event) {
        int dimensionId = event.world.provider.dimensionId;
        if (cache.containsKey(dimensionId)) {
            FMLLog.info("unload world dimension cache: %d, %s", dimensionId, event.world.getClass().toString());
            cache.get(dimensionId).unload();
            cache.remove(dimensionId);
        }
    }

    @ForgeSubscribe
    public void chunkEvent(ChunkEvent.Load event) {
        int dimensionId = event.world.provider.dimensionId;
        if (cache.containsKey(dimensionId)) {
            cache.get(dimensionId).update();
        }
    }

    public File getMinecraftDir() {
        return new File(".");
    }

    public void removeBB(int dimensionId, BBoxInt bb) {
        if (cache.containsKey(dimensionId)) {
            cache.get(dimensionId).remove(bb);
        }
    }
}


