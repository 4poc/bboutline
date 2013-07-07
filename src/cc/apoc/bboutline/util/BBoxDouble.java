package cc.apoc.bboutline.util;

public class BBoxDouble {

    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;
    
    public BBoxDouble(double minX, double minY, double minZ, double maxX,
            double maxY, double maxZ) {
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
        long temp;
        temp = Double.doubleToLongBits(maxX);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minX);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        BBoxDouble other = (BBoxDouble) obj;
        if (Double.doubleToLongBits(maxX) != Double
                .doubleToLongBits(other.maxX))
            return false;
        if (Double.doubleToLongBits(maxY) != Double
                .doubleToLongBits(other.maxY))
            return false;
        if (Double.doubleToLongBits(maxZ) != Double
                .doubleToLongBits(other.maxZ))
            return false;
        if (Double.doubleToLongBits(minX) != Double
                .doubleToLongBits(other.minX))
            return false;
        if (Double.doubleToLongBits(minY) != Double
                .doubleToLongBits(other.minY))
            return false;
        if (Double.doubleToLongBits(minZ) != Double
                .doubleToLongBits(other.minZ))
            return false;
        return true;
    }
    
    public String toString()
    {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }
    
    
}
