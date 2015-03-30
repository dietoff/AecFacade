package text;

import processing.core.PApplet;
import processing.core.PFont;

public class FacFont {
	private String font;
	private float FONT_SIZE;
	private float FONT_SCALE_X;
	private float FONT_SCALE_Y;
	private float FONT_OFFSET_Y;
	private float FONT_OFFSET_X;
	PFont PFon;
	PApplet p;

	public FacFont(PApplet p_, String font, float fONT_SIZE, float fONT_SCALE_X,
			float fONT_SCALE_Y, float fONT_OFFSET_Y, float fONT_OFFSET_X) {
		this.font = font;
		FONT_SIZE = fONT_SIZE;
		FONT_SCALE_X = fONT_SCALE_X;
		FONT_SCALE_Y = fONT_SCALE_Y;
		FONT_OFFSET_Y = fONT_OFFSET_Y;
		FONT_OFFSET_X = fONT_OFFSET_X;
		p = p_;
	}
	
	public void createFont() {
		this.PFon = p.createFont(font, 9, false);
	}

	public PFont getFont() {
		return PFon;
	}

	public float getFONT_SIZE() {
		return FONT_SIZE;
	}

	public void setFONT_SIZE(float fONT_SIZE) {
		FONT_SIZE = fONT_SIZE;
	}

	public float getFONT_SCALE_X() {
		return FONT_SCALE_X;
	}

	public void setFONT_SCALE_X(float fONT_SCALE_X) {
		FONT_SCALE_X = fONT_SCALE_X;
	}

	public float getFONT_SCALE_Y() {
		return FONT_SCALE_Y;
	}

	public void setFONT_SCALE_Y(float fONT_SCALE_Y) {
		FONT_SCALE_Y = fONT_SCALE_Y;
	}

	public float getFONT_OFFSET_Y() {
		return FONT_OFFSET_Y;
	}

	public void setFONT_OFFSET_Y(float fONT_OFFSET_Y) {
		FONT_OFFSET_Y = fONT_OFFSET_Y;
	}

	public float getFONT_OFFSET_X() {
		return FONT_OFFSET_X;
	}

	public void setFONT_OFFSET_X(float fONT_OFFSET_X) {
		FONT_OFFSET_X = fONT_OFFSET_X;
	}
}