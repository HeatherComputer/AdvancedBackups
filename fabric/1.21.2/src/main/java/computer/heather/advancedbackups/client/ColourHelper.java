package computer.heather.advancedbackups.client;

//A colour helper, loosely based on that of vanilla 1.16.
//What is the modern equivalent..?
public class ColourHelper {
    
    public static int alpha(int in) {
        return in >>> 24;
    }
    
    public static int red(int in) {
        return in >> 16 & 255;
    }
    
    public static int green(int in) {
        return in >> 8 & 255;
    }
    
    public static int blue(int in) {
        return in & 255;
    }
    
    public static int colour(int a, int r, int g, int b) {
        return a << 24 | r << 16 | g << 8 | b;
    }
    
    public static int colour(int a, long r, long g, long b) {
        return colour(a, (int) r, (int) g, (int) b);
    }
    
}