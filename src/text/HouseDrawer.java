package text;
import org.aec.facade.AECPlugin;
import org.aec.facade.Building;
import org.aec.facade.Side;
import processing.core.PApplet;
import processing.core.PFont;

public class HouseDrawer {
  AECPlugin aec;
  int size = 10;
  PFont font;
  boolean showIds = false;
  private PApplet p;
  
  public HouseDrawer(AECPlugin aec_, PApplet p_) {
    aec = aec_;
    p = p_;
    font = p.loadFont("../data/LucidaConsole-8.vlw"); 
  }
  
  public void toggleIds() {
    showIds = !showIds;
  }
  
  public void draw() {
	  p.resetMatrix();
    
    for (int i = 0; i < Building.SIDE.values().length; ++i) {
      Building.SIDE sideEnum = Building.SIDE.values()[i];
      Side side = aec.getSide(sideEnum);
      
      p.stroke(side.getColor().getRed(), side.getColor().getGreen(), side.getColor().getBlue(), side.getColor().getAlpha());
      p.noFill();
      drawSide(side);
    }
  }
  
  void drawSide(Side s) {
    int[][] adr = s.getWindowAddress();
    int pWidth = s.getPixelWidth();
    int pHeight = s.getPixelHeight();

    for (int y = 0; y < adr.length; ++y) {
      for (int x = 0; x < adr[y].length; ++x) {
        if (adr[y][x] > -1) {
          int fx = (s.getX() + x * pWidth) * aec.scale;
          int fy = (s.getY() + y * pHeight) * aec.scale;
          p.rect(fx, fy, pWidth * aec.scale, pHeight * aec.scale);
          
          if (showIds) {
        	  p.textFont(font, 8); 
        	  p.text("" + adr[y][x], fx + pWidth * aec.scale / 4, (float) (fy + pHeight * aec.scale * 0.9f));
          }
        }
      }
    }
  }
}