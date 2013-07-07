package cc.apoc.bboutline.util;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class BBoxFactory {
    public static BBoxInt createBBoxInt(StructureBoundingBox bb) {
        return new BBoxInt(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }
    
    public static BBoxInt createBBoxInt(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new BBoxInt(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static BBoxDouble createBBoxDouble(AxisAlignedBB bb) {
        return new BBoxDouble(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }
    
    public static BBoxDouble createBBoxDouble(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new BBoxDouble(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
