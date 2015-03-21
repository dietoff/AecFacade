package text;
import java.awt.Color;
import org.aec.facade.*;
import processing.core.PApplet;

public class AEC {
	AECPlugin plugin;
	HouseDrawer house;
	private PApplet p;

	public AEC(PApplet _p) {
		p=_p;
		plugin = new AECPlugin();
		house = new HouseDrawer(plugin, p);
	}

	void init() {
		plugin.setFrameWidth(p.width);
		plugin.init();
		loadConfig();
	}

	void loadConfig() {
		plugin.loadConfig();
	}

	public void beginDraw() {
		p.scale(2 * plugin.scale, plugin.scale);
	}

	public void endDraw() {
		// reset of the transformation
		p.resetMatrix();
		p.loadPixels();
		plugin.update(p.pixels);
	}

	public void drawSides() {
		house.draw();
	}

	public void keyPressed(int value) {
		plugin.keyPressed(value, p.keyCode);
		if (value == 'i') {
			house.toggleIds();
		}
	}

	public void setActiveColor(Color c) {
		plugin.setActiveColor(c);
	}

	public void setInActiveColor(Color c) {
		plugin.setInActiveColor(c);
	}

	public int getScaleX() {
		return 2 * plugin.scale;
	}

	public int getScaleY() {
		return plugin.scale;
	}  
}
