package cc.apoc.bboutline.util;

import net.minecraft.util.AxisAlignedBB;

public class BBoxInt {
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;
    
    public BBoxInt(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super();
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + maxX;
        result = prime * result + maxY;
        result = prime * result + maxZ;
        result = prime * result + minX;
        result = prime * result + minY;
        result = prime * result + minZ;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BBoxInt other = (BBoxInt) obj;
        if (maxX != other.maxX)
            return false;
        if (maxY != other.maxY)
            return false;
        if (maxZ != other.maxZ)
            return false;
        if (minX != other.minX)
            return false;
        if (minY != other.minY)
            return false;
        if (minZ != other.minZ)
            return false;
        return true;
    }
    
    public String toString()
    {
        return "(" + this.minX + ", " + this.minY + ", " + this.minZ + "; " + this.maxX + ", " + this.maxY + ", " + this.maxZ + ")";
    }
    
    public AxisAlignedBB toAxisAlignedBB() {
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
