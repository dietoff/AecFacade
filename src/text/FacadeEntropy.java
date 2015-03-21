package text;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PFont;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.Table;
import processing.data.TableRow;

public class FacadeEntropy extends PApplet {


	private float perc = .25f;
	private float fillrate = 0.7f;
	AEC aec;
	PFont font1;
	// some parameters that turned out to work best for the font we're using
//	private String font = "../data/8bitOperatorPlus8-Regular.ttf"; //
	private String font = "../data/coders_crux.ttf"; 
//	private String font = "../data/Minecraftia-Regular.ttf"; 
//	private String font = "../data/FreePixel.ttf"; 
	private float fontkern = 3;
	float FONT_SIZE = 6;
	float FONT_OFFSET_Y = 0.12f;
	float FONT_SCALE_X = 2.669f;
	float FONT_SCALE_Y = 2.67f;

	private HashMap<Integer, Pixel> idmap;
	private HashMap<Integer, Pixel> xymap;
	private HashMap<Integer, ArrayList<Pixel>> xmap;
	private HashMap<Integer, ArrayList<Pixel>> ymap;
	private int state = 1;
	private int dir = 0;
	private boolean diag = true;
	private String url = "http://sal-if.linz.at/mobile/action?type=8&pageIndex=1&pageSize=10";
	private boolean text;
	private boolean loaded=false;
	private String textString;
	private boolean rot = false;
	private HashMap<Integer, Request> requests;

	public void setup() {
		thread("requestData");
		requests = new HashMap<Integer, Request>();
		frameRate(25);
		size(1200, 400);
		readTopology();
		for (Pixel p:idmap.values()) randomize(p);
		// NOTE: This font needs to be in the data folder.
		// and it's available for free at http://www.dafont.com
		// You COULD use a different font, but you'd have to tune the above parameters. Monospaced bitmap fonts work best.
		font1 = createFont(font, 9, false);

		// font1 = createFont("CourierNewPSMT", 9, false, charactersToInclude);
		// font1 = loadFont("CourierNewPS-BoldMT-20.vlw");
		aec = new AEC(this);
		aec.init();
		frameRate(30);
	}

	private void readTopology() {
		Table table = loadTable("../data/window2mask_crop.csv", "header");
		idmap = new HashMap<Integer, Pixel>();
		xymap = new HashMap<Integer, Pixel>();
		xmap = new HashMap<Integer, ArrayList<Pixel>>();
		ymap = new HashMap<Integer, ArrayList<Pixel>>();
		int minx = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxy = Integer.MIN_VALUE;

		// read in text file
		for (TableRow row : table.rows()) {
			int id = row.getInt("window");
			int x = row.getInt("x");
			int y = row.getInt("y");

			if (x<minx) minx = x;
			if (x>maxx) maxx = x;
			if (y<miny) miny = y;
			if (y>maxy) maxy = y;

			int w = row.getInt("width");
			Pixel pix = new Pixel(id,x,y,w);

			if (!xmap.containsKey(x)) {
				ArrayList<Pixel> tmp = new ArrayList<Pixel>();
				tmp.add(pix);
				xmap.put(x, tmp);
			} else {
				xmap.get(x).add(pix);
			}

			if (!ymap.containsKey(y)) {
				ArrayList<Pixel> tmp = new ArrayList<Pixel>();
				tmp.add(pix);
				ymap.put(y, tmp);
			} else {
				ymap.get(y).add(pix);
			}
			idmap.put(id, pix);
			xymap.put(combine(x, y), pix);
		}

		for (Pixel p:idmap.values()) {
			if (!xymap.values().contains(p)) println(p.id);
		}
		
		calculateNeighbors(minx, maxx, miny, maxy);
	}

	private void calculateNeighbors(int minx, int maxx, int miny, int maxy) {
		// calculate neighbors
		for (int x=minx;x<maxx+1;x++) {
			for (int y=miny;y<maxy+1;y++) {
				Pixel pixel = xymap.get(combine(x,y));
				if (pixel!=null) {

					Integer top =  combine(x,y-1);
					Integer bot =  combine(x,y+1);
					Integer lef =  combine(x-1,y);
					Integer rig =  combine(x+1,y);

					Integer tr =  combine(x+1,y-1);
					Integer bl =  combine(x-1,y+1);
					Integer tl =  combine(x-1,y-1);
					Integer br =  combine(x+1,y+1);

					if (xymap.containsKey(top)) {
						Pixel px = xymap.get(top);
						pixel.addNeighbor("top", px);
					}
					if (xymap.containsKey(bot)) {
						Pixel px = xymap.get(bot);
						pixel.addNeighbor("bottom", px);
					}
					if (xymap.containsKey(lef)) {
						Pixel px = xymap.get(lef);
						pixel.addNeighbor("left", px);
					}
					if (xymap.containsKey(rig)) {
						Pixel px = xymap.get(rig);
						pixel.addNeighbor("right", px);
					}
					if (xymap.containsKey(tr)) {
						Pixel px = xymap.get(tr);
						pixel.addNeighbor("topright", px);
					}
					if (xymap.containsKey(bl)) {
						Pixel px = xymap.get(bl);
						pixel.addNeighbor("bottomleft", px);
					}
					if (xymap.containsKey(tl)) {
						Pixel px = xymap.get(tl);
						pixel.addNeighbor("topleft", px);
					}
					if (xymap.containsKey(br)) {
						Pixel px = xymap.get(br);
						pixel.addNeighbor("bottomright", px);
					}

				}
			}
		}
	}

	private int combine(int x, int y) {
		return x|(y<<8);
	}

	public void draw() {
		aec.beginDraw();
		background(0,0,0);
		dotDisplay();
		if (text) textDisplay();
		executeState();
		aec.endDraw();
		aec.drawSides();
	}

	private void dotDisplay() {
		stroke(255,255,255);
		if(diag) point(1,1);
		for (Pixel p:idmap.values()) {
			if (p.on) point(p.x,p.y);
		}
	}

	private void textDisplay() {
		noStroke();
		fill(255,0,100,220);
		// determines the speed (number of frames between text movements)
		int frameInterval = 3;
		// min and max grid positions at which the text origin should be. we scroll from max (+40) to min (-80)
		int minPos = -1500;
		int maxPos = 50;
		int loopFrames = (maxPos-minPos) * frameInterval;
		// vertical grid pos
		int yPos = 15;
		displayText(max(minPos, maxPos - (frameCount%loopFrames) / frameInterval), yPos);
	}

	void displayText(int x, int y)
		{
			// push & translate to the text origin
			pushMatrix();
			if (rot) {
				rotate(PI/2);
				translate(0,-42);
			}
			translate(x,-5+y+FONT_OFFSET_Y);
	
			// scale the font up by fixed parameters so it fits our grid
			scale(FONT_SCALE_X,FONT_SCALE_Y);
			textFont(font1);
			textSize(FONT_SIZE);
	
			text(textString, 0, 0);
			text(textString, 0, 4);
			text(textString, 0, 8);
			// draw the font glyph by glyph, because the default kerning doesn't align with our grid
	//		for(int i = 0; i < textString.length(); i++)
	//		{
	//			text(textString.charAt(i), (float)(i*fontkern), 0);
	//		}
			popMatrix();
		}

	private void checker(Pixel p) {
			if (p.x%2+p.y%2==1) p.on = true; else p.on = false;
	}
	private void checker2(Pixel p) {
			if (p.x%2+p.y%2!=1) p.on = true; else p.on = false;
	}
	private void diag(Pixel p) {
			if ((p.x+p.y)%4==0) p.on = true; else p.on = false;
	}
	private void diag2(Pixel p) {
		if ((p.x-p.y)%4==0) p.on = true; else p.on = false;
	}
	private void vert(Pixel p) {
			if (p.x%3==0) p.on = true; else p.on = false;
	}
	private void hor(Pixel p) {
		if (p.y%3==0) p.on = true; else p.on = false;
}
	private void randomize(Pixel p) {
			float random = random(0,1);
			if (random > fillrate) p.on = true; else p.on = false;
	}
	private boolean forward(int dir,Pixel p) {if (canGo(dir,p)) {
		move(dir,p);
		return true;
		} else return false;
	}
	
	private void forwDiag(Pixel p) {
		if (!p.on) return;
		if (forward(dir,p)); else {
			if (!diag) return;
			if (forward(frontleft(),p)); else {
				if (forward(frontright(),p));
			}
		};
		if (p.hasLeft(dir)&&p.getLeft(dir).hasDirOff(frontleft())) {
			forward(left(),p);
			forward(frontleft(),p);
		}
		else {
			if (p.hasRight(dir)&&p.getRight(dir).hasDirOff(frontright())) {
				forward(right(),p);
				forward(frontright(),p);
			}
		}
	}
	
	private void move(int dir, Pixel p) {
		p.on=false;
		p.getDir(dir).on=true;
	}
	
	private boolean canGo(int dir,Pixel p) {
		return p.on&&p.hasDirOff(dir);
	}

	private void full(Pixel p) {
		p.on = true;
	}
	private void empty(Pixel p) {
		p.on = false;
	}

	private void executeState() {
		ArrayList<Pixel> a = new ArrayList<Pixel>();
		a.addAll(idmap.values());
		Collections.shuffle(a);
	
		for (int i = 0; i< a.size()*perc; i++ ) {
			Pixel p = a.get(i);
			//if (p.x%10!=0)
			{
				switch (state) {
				case 1:dir=0;forwDiag(p);break;
				case 2:dir=1;forwDiag(p);break;
				case 3:dir=2;forwDiag(p);break;
				case 4:dir=3;forwDiag(p);break;
				case 5:dir=4;forwDiag(p);break;
				case 6:dir=5;forwDiag(p);break;
				case 7:dir=6;forwDiag(p);break;
				case 8:dir=7;forwDiag(p);break;
				case 11:randomize(p);break;
				case 12:checker(p);break;
				case 13:checker2(p);break;
				case 14:diag(p);break;
				case 15:diag2(p);break;
				case 16:vert(p);break;
				case 17:hor(p);break;
				case 18:full(p);break;
				case 19:empty(p);break;
				}
			}
		}
	}

	public void keyPressed() {
		//aec.keyPressed(key);
		switch(key) {
		case 't':state=1;break;
		case 'y':state=2;break;
		case 'h':state=3;break;
		case 'n':state=4;break;
		case 'b':state=5;break;
		case 'v':state=6;break;
		case 'f':state=7;break;
		case 'r':state=8;break;
		case 'g':diag=!diag;break;
		case '0':state=11;break;
		case '9':state=12;break;
		case '8':state=13;break;
		case '7':state=14;break;
		case '6':state=15;break;
		case '5':state=16;break;
		case '4':state=17;break;
		case '3':state=18;break;
		case '2':state=19;break;
		case 'q':text=!text;break;
		case 'w':rot=!rot;break;
		}
	}

	public int left() {
		return dir+6%8;
	}
	public int right() {
		return dir+2%8;
	}
	public int back() {
		return dir+4%8;
	}
	public int frontleft() {
		return dir+7%8;
	}
	public int frontright() {
		return dir+1%8;
	}
	public int backleft() {
		return dir+5%8;
	}
	public int backright() {
		return dir+3%8;
	}

	public void requestData() throws ParseException {
		  JSONObject json = loadJSONObject(url);
		  JSONArray msg = json.getJSONArray("MESSAGELIST");
		  textString="";
		  for (int i = 0; i < msg.size(); i++) {
			    
			    JSONObject m = msg.getJSONObject(i); 
			    Request r = new Request();
			    r.setId(m.getInt("id"));
			    r.setTitle(m.getString("title",""));
			    r.setText(m.getString("text",""));
			    r.setStatus(m.getInt("status",0));
			    r.setCategory(m.getString("category",""));
			    r.setLon(m.getInt("lon",0));
			    r.setLat(m.getInt("lat",0));
			    r.setActive(m.getInt("active",0));
			    r.setDistrict(m.getString("district",""));
			    r.setDatec( DateFormat.getDateInstance().parse(m.getString("dateCreated")));
			    r.setDatem( DateFormat.getDateInstance().parse(m.getString("dateLastModified")));
			    
			    if (!requests.containsKey(r.getId())) {
			    	// new report!
			    }
			    
			    requests.put(r.getId(),r);
			    
			    String title = m.getString("title");
			    textString = textString+title+". ";
			  }
		  text = true;
		}
	
}


