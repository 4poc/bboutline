package cc.apoc.bboutline;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cc.apoc.bboutline.util.BBoxInt;

import cpw.mods.fml.common.FMLLog;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;


public class ClientProxy extends CommonProxy {
    
    private double playerX;
    private double playerY;
    private double playerZ;

    /**
     * If the outline is drawn, currently the data is collected nonetheless.
     */
    private boolean active = false;

    
    private long lastHotkeyPoll = System.currentTimeMillis();
    
    /**
     * Polling for the hotkeys.
     */
    public void pollHotkeys(long tickTime) {
        if (tickTime - lastHotkeyPoll < 500) return;
        
        // toggle active/deactive
        if (pressedHotkey(config.hotkeyToggle)) {
            active = (active) ? false : true;
            FMLLog.info("toggle active");
        }
        else if (pressedHotkey(config.hotkeyReload)) {
            config.loadConfig();
        }
        else {
            return;
        }
        
        lastHotkeyPoll = tickTime;
    }
    
    private boolean pressedHotkey(Set<Integer> hotkeyToggle) {
        for (int key : hotkeyToggle)
            if (!Keyboard.isKeyDown(key))
                return false;
        return true;
    }
    
    /**
     * Convert an bounding box relative to the player position for rendering.
     * 
     * @param bb
     * @param playerX
     * @param playerY
     * @param playerZ
     * @return
     */
    private AxisAlignedBB getRenderBoundingBox(BBoxInt bb) {
        AxisAlignedBB aabb = bb.toAxisAlignedBB();
        aabb.maxX += 1;
        aabb.maxY += 1;
        aabb.maxZ += 1;
        double expandBy = 0.005F;
        return aabb.expand(expandBy, expandBy, expandBy).getOffsetBoundingBox(-playerX, -playerY, -playerZ);
    }
    
    @ForgeSubscribe
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        // determine current player location
        EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;
        playerX = entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * (double) event.partialTicks;
        playerY = entityPlayer.lastTickPosY + (entityPlayer.posY - entityPlayer.lastTickPosY) * (double) event.partialTicks;
        playerZ = entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * (double) event.partialTicks;

        
        if (this.active) {
            int activeDimensionId = entityPlayer.worldObj.provider.dimensionId;
            if (cache.containsKey(activeDimensionId)) {
            	renderBBoxMap(cache.get(activeDimensionId).getCache());
            }
        }
    }
    


    private void renderBBoxMap(Map<BBoxInt, Vector<BBoxInt>> map) {
        

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(3.0f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (config.seeThrough) {
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        }

        BBoxInt hitBB = null;
        for (BBoxInt struct : map.keySet()) {
        	renderOutlineList(map.get(struct));
        }
        renderOutlineList(config.getUserBBList());

        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        
        
        if (config.debug) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution var5 = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            int screenWidth = var5.getScaledWidth();
            int screenHeight = var5.getScaledHeight();
            mc.entityRenderer.setupOverlayRendering();
            int count = 0;
            for (BBoxInt bb : map.keySet()) {
                count += map.get(bb).size();
            }
            String debug = String.format("%d/%d", map.keySet().size(), count);
            //if (hitBB != null)
            //    debug += " " + hitBB.toString();
            int width = screenWidth - mc.fontRenderer.getStringWidth(debug);
            
            mc.fontRenderer.drawStringWithShadow(debug, width - 2, 2, 16777215);
        }
    }
    
    private void renderOutlineList(Vector<BBoxInt> bbList) {
        World world = Minecraft.getMinecraft().theWorld;
        Set activeChunks = world.activeChunkSet;
        for (BBoxInt bb : bbList) {
            AxisAlignedBB relBB = getRenderBoundingBox(bb);
            
            if (activeChunks.contains(world.getChunkFromBlockCoords((int) Math.floor(bb.minX), (int) Math.floor(bb.minZ)).getChunkCoordIntPair()) ||
                    activeChunks.contains(world.getChunkFromBlockCoords((int) Math.floor(bb.maxX), (int) Math.floor(bb.maxZ)).getChunkCoordIntPair())) {
                
                if (config.fill) { // TODO: depth-sort vertices for real transparency
                    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                    config.fillColorA = 0.2f;
                    renderOutline(relBB);
                    GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
                    GL11.glPolygonOffset(-1.f,-1.f);
                    config.fillColorA = 1.0f;
                }
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                renderOutline(relBB);
            }
        }
    }

    private void renderOutline(AxisAlignedBB bb) {
        Tessellator tessellator = Tessellator.instance;
        //System.out.println(bb);
        float colorR = config.fillColorR;
        float colorG = config.fillColorG;
        float colorB = config.fillColorB;
        float colorA = config.fillColorA;

        tessellator.startDrawing(GL11.GL_QUADS);
        tessellator.setColorRGBA_F(colorR, colorG, colorB, colorA);
        tessellator.addVertex(bb.minX, bb.minY, bb.minZ);
        tessellator.addVertex(bb.maxX, bb.minY, bb.minZ);
        tessellator.addVertex(bb.maxX, bb.minY, bb.maxZ);
        tessellator.addVertex(bb.minX, bb.minY, bb.maxZ);
        tessellator.draw();
        
        tessellator.startDrawing(GL11.GL_QUADS);
        tessellator.setColorRGBA_F(colorR, colorG, colorB, colorA);
        tessellator.addVertex(bb.minX, bb.maxY, bb.minZ);
        tessellator.addVertex(bb.maxX, bb.maxY, bb.minZ);
        tessellator.addVertex(bb.maxX, bb.maxY, bb.maxZ);
        tessellator.addVertex(bb.minX, bb.maxY, bb.maxZ);
        tessellator.draw();
        
        tessellator.startDrawing(GL11.GL_QUADS);
        tessellator.setColorRGBA_F(colorR, colorG, colorB, colorA);
        tessellator.addVertex(bb.minX, bb.minY, bb.maxZ);
        tessellator.addVertex(bb.minX, bb.maxY, bb.maxZ);
        tessellator.addVertex(bb.maxX, bb.maxY, bb.maxZ);
        tessellator.addVertex(bb.maxX, bb.minY, bb.maxZ);
        tessellator.draw();
        
        tessellator.startDrawing(GL11.GL_QUADS);
        tessellator.setColorRGBA_F(colorR, colorG, colorB, colorA);
        tessellator.addVertex(bb.minX, bb.minY, bb.minZ);
        tessellator.addVertex(bb.minX, bb.maxY, bb.minZ);
        tessellator.addVertex(bb.maxX, bb.maxY, bb.minZ);
        tessellator.addVertex(bb.maxX, bb.minY, bb.minZ);
        tessellator.draw();
        
        tessellator.startDrawing(GL11.GL_QUADS);
        tessellator.setColorRGBA_F(colorR, colorG, colorB, colorA);
        tessellator.addVertex(bb.minX,bb.minY, bb.minZ);
        tessellator.addVertex(bb.minX,bb.minY, bb.maxZ);
        tessellator.addVertex(bb.minX,bb.maxY, bb.maxZ);
        tessellator.addVertex(bb.minX,bb.maxY, bb.minZ);
        tessellator.draw();
        
        tessellator.startDrawing(GL11.GL_QUADS);
        tessellator.setColorRGBA_F(colorR, colorG, colorB, colorA);
        tessellator.addVertex(bb.maxX,bb.minY, bb.minZ);
        tessellator.addVertex(bb.maxX,bb.minY, bb.maxZ);
        tessellator.addVertex(bb.maxX,bb.maxY, bb.maxZ);
        tessellator.addVertex(bb.maxX,bb.maxY, bb.minZ);
        tessellator.draw();
    }
    
    @Override
    public File getMinecraftDir() {
        return Minecraft.getMinecraft().mcDataDir;
    }
}
