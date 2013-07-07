package cc.apoc.bboutline;

import java.io.*;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import cc.apoc.bboutline.util.BBoxInt;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

    @Override
    public void onPacketData(INetworkManager manager,
            Packet250CustomPayload packet, Player player) {
        System.out.printf("receive packet channel %s\n", packet.channel);
        if (packet.channel.equals(BBOutlineMod.channel)) {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
            try {
                switch (inputStream.readByte()) {
                case 0:
                    readBBoxUpdatePacket(inputStream);
                    break;
                case 1:
                    readChangeDimensionPacket((EntityPlayerMP) player, inputStream);
                    break;
                case 2:
                    readRemoveBBPacket(inputStream);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    
    private void readChangeDimensionPacket(EntityPlayerMP player,
            DataInputStream inputStream) throws IOException {
        int dimensionId = inputStream.readByte();
        
        FMLLog.info("Receive Client(%s) -> Server: Change Dimension (%d)", player.username, dimensionId);
        
        BBOutlineMod.instance.playerDimensions.put(player, dimensionId);
        
        // send all entries once, so the client is uptodate
        if (BBOutlineMod.proxy.cache.containsKey(dimensionId)) {
            BBOutlineMod.proxy.cache.get(dimensionId).sendToPlayer((EntityPlayerMP) player);
        }
    }

    public static void writeChangeDimensionPacket(int dimensionId) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(bos);
        try {
            outputStream.writeByte(1);
            outputStream.writeByte(dimensionId);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = BBOutlineMod.channel;
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        
        FMLLog.info("Send Client -> Server: Change Dimension (%d)", dimensionId);

        PacketDispatcher.sendPacketToServer(packet);
    }
    
    
    
    public static void writeRemoveBBPacket(int dimensionId, BBoxInt bb) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(bos);
        try {
            outputStream.writeByte(2);
            outputStream.writeByte(dimensionId);
            outputStream.writeInt(bb.minX);
            outputStream.writeInt(bb.minY);
            outputStream.writeInt(bb.minZ);
            outputStream.writeInt(bb.maxX);
            outputStream.writeInt(bb.maxY);
            outputStream.writeInt(bb.maxZ);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = BBOutlineMod.channel;
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        
        FMLLog.info("Send Server -> Client: Remove BB (%s)", bb.toString());

        PacketDispatcher.sendPacketToAllInDimension(packet, dimensionId);
        //PacketDispatcher.sendPacketToPlayer(packet, (Player) player);
        //PacketDispatcher.sendPacketToServer(packet);
    }
    
    private void readRemoveBBPacket(DataInputStream inputStream)
            throws IOException {
        int dimensionId = inputStream.readByte();
        BBoxInt bb = new BBoxInt(inputStream.readInt(),
                inputStream.readInt(), inputStream.readInt(),
                inputStream.readInt(), inputStream.readInt(),
                inputStream.readInt());
        
        FMLLog.info("Receive Server -> Client: Remove BB (%s)", bb.toString());
        
        BBOutlineMod.instance.proxy.removeBB(dimensionId, bb);
    }
    
    

    private void readBBoxUpdatePacket(DataInputStream inputStream)
            throws IOException {
        int dimensionId = inputStream.readByte();
        BBoxInt structBB = new BBoxInt(inputStream.readInt(),
                inputStream.readInt(), inputStream.readInt(),
                inputStream.readInt(), inputStream.readInt(),
                inputStream.readInt());
        BBoxInt bb;
        Vector<BBoxInt> bbList = new Vector<BBoxInt>();

        int bbCount = inputStream.readInt();

        for (int i = 0; i < bbCount; i++) {
            bb = new BBoxInt(inputStream.readInt(), inputStream.readInt(),
                    inputStream.readInt(), inputStream.readInt(),
                    inputStream.readInt(), inputStream.readInt());
            bbList.add(bb);
        }
        
        FMLLog.info("receive %d cache entries.", bbList.size());
        
        // fill cache with the new bb entries (select cache by dimension)
        if (BBOutlineMod.proxy.cache.containsKey(dimensionId)) {
            BBOutlineMod.proxy.cache.get(dimensionId).merge(structBB, bbList);
        }
    }

    public static void writeBBoxUpdatePacket(EntityPlayerMP player, int dimensionId, 
            BBoxInt structureStartBB, Vector<BBoxInt> componentBBList) {
        
        FMLLog.info("send %d cache entries to %s.", componentBBList.size(), player.username);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(bos);
        try {
            outputStream.writeByte(0);
            outputStream.writeByte(dimensionId);
            
            outputStream.writeInt(structureStartBB.minX);
            outputStream.writeInt(structureStartBB.minY);
            outputStream.writeInt(structureStartBB.minZ);
            outputStream.writeInt(structureStartBB.maxX);
            outputStream.writeInt(structureStartBB.maxY);
            outputStream.writeInt(structureStartBB.maxZ);
            
            outputStream.writeInt(componentBBList.size());
            
            for (BBoxInt componentBB : componentBBList) {
                outputStream.writeInt(componentBB.minX);
                outputStream.writeInt(componentBB.minY);
                outputStream.writeInt(componentBB.minZ);
                outputStream.writeInt(componentBB.maxX);
                outputStream.writeInt(componentBB.maxY);
                outputStream.writeInt(componentBB.maxZ);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = BBOutlineMod.channel;
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        
        PacketDispatcher.sendPacketToPlayer(packet, (Player) player);
    }
}
