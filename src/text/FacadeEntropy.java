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


	private float perc = .15f;
	private float fillrate = 0.5f;
	AEC aec;
	PFont font1;
	private float fontkern = 3;

	private String font = "../data/coders_crux.ttf"; 
	float FONT_SIZE = 5;
	float FONT_SCALE_X = 3.155f;
	float FONT_SCALE_Y = 3.2f;
	float FONT_OFFSET_Y = 0.55f;
	float FONT_OFFSET_X = 0.5f;
	
//	private String font = "../data/04B_03__.TTF";
//	float FONT_SIZE = 3;
//	float FONT_SCALE_X = 2.66f;
//	float FONT_SCALE_Y = 2.6f;
//	float FONT_OFFSET_Y = 0.5f;
//	float FONT_OFFSET_X = 0.5f;
	
//	private String font = "../data/8bitOperatorPlus8-Regular.ttf"; 
//	float FONT_SIZE = 4;
//	float FONT_SCALE_X = 2.885f;
//	float FONT_SCALE_Y = 2.86f;
//	float FONT_OFFSET_Y = 0.5f;
//	float FONT_OFFSET_X = 0.5f;
	
	//Freepixel
//	private String font = "../data/FreePixel.ttf"; 
//	float FONT_SIZE = 6;
//	float FONT_SCALE_X = 2.6589997f;
//	float FONT_SCALE_Y = 2.67f;
//	float FONT_OFFSET_X = 0.5f;
//	float FONT_OFFSET_Y = 0.5f;
//	


	private HashMap<Integer, Pixel> idmap;
	private HashMap<Integer, Pixel> xymap;
	private int state = 1;
	private int dir = 0;
	private boolean diag = true;
	private String url = "http://sal-if.linz.at/mobile/action?type=8&pageIndex=1&pageSize=10";
	private boolean text;
	private boolean loaded=false;
	private String textString;
	private boolean rot = false;
	private HashMap<Integer, Request> reqMap;
	private ArrayList<Request> reqList;
	private boolean incoming = false;
	private int rx = 0;
	private int txtx=20;

	public void setup() {
		thread("requestData");
		reqMap = new HashMap<Integer, Request>();
		reqList = new ArrayList<Request>();
		frameRate(25);
		size(1200, 400);
		readTopology();
		for (Pixel p:idmap.values()) moderandom(p);
		// NOTE: This font needs to be in the data folder.
		// and it's available for free at http://www.dafont.com
		// You COULD use a different font, but you'd have to tune the above parameters. Monospaced bitmap fonts work best.
		font1 = createFont(font, 9, false);

		// font1 = createFont("CourierNewPSMT", 9, false, charactersToInclude);
		// font1 = loadFont("CourierNewPS-BoldMT-20.vlw");
		aec = new AEC(this);
		aec.init();
		frameRate(25);
	}

	private void readTopology() {
		Table table = loadTable("../data/window2mask_crop.csv", "header");
		idmap = new HashMap<Integer, Pixel>();
		xymap = new HashMap<Integer, Pixel>();
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
			idmap.put(id, pix);
			xymap.put(combine(x, y), pix);
		}

		for (Pixel p:idmap.values()) {
			if (!xymap.values().contains(p)) println(p.id);
		}

		calculateNeighbors(minx, maxx, miny, maxy);
		
		//remove duplicate pixels per x,y
		ArrayList<Pixel> rem = new ArrayList<Pixel>();
		for (Pixel p:idmap.values()) {
			if (!xymap.containsValue(p)) {
				rem.add(p);
			}
		}
		for (Pixel p:rem) idmap.remove(p.id);
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
		noSmooth();
		background(0,0,0);
		dotDisplay();
		if (text) textDisplay();
		//		if (frameCount%100==0) rot = !rot;
		//		if (frameCount%100==0) rx = (int) random(0f,10f);
		//		if (frameCount%150==0) state = (int) random(0f,20f);

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
		noSmooth();
		fill(255,0,100);
		// determines the speed (number of frames between text movements)
		int frameInterval = 3;
		// min and max grid positions at which the text origin should be. we scroll from max (+40) to min (-80)
		String txt = reqList.get(rx).getTitle();
		int maxPos = 80;
		int minPos = (int) (-txt.length()*FONT_SIZE); // looppoint based on lenght of string
		int loopFrames = (maxPos-minPos) * frameInterval;

		// vertical grid pos
		int yPos = 10;
//		renderText(max(minPos, maxPos - (frameCount%loopFrames) / frameInterval), yPos, txt );
		renderText(txtx, yPos, txt );

	}

	void renderText(int x, int y, String txt)
	{
		// push & translate to the text origin
		pushMatrix();
		if (rot) {
			rotate(PI/2);
			translate(-50,-43);
		}
		translate(x+FONT_OFFSET_X,y+FONT_OFFSET_Y);

		// scale the font up by fixed parameters so it fits our grid
		scale(FONT_SCALE_X,FONT_SCALE_Y);
		textFont(font1);
		textSize(FONT_SIZE);

		text(txt, 0, 0);
		translate(0,-FONT_OFFSET_Y);
		text(txt, 0, 4);
		translate(0,-FONT_OFFSET_Y);
		text(txt, 0, 8);

//		 draw the font glyph by glyph, because the default kerning doesn't align with our grid
//				for(int i = 0; i < textString.length(); i++)
//				{
//					text(textString.charAt(i), (float)(i*fontkern), 0);
//				}
		popMatrix();
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
				case 11:moderandom(p);break;
				case 12:modechecker(p);break;
				case 13:modechecker2(p);break;
				case 14:modediag(p);break;
				case 15:modediag2(p);break;
				case 16:modevert(p);break;
				case 17:modehor(p);break;
				case 18:modefull(p);break;
				case 19:modeempty(p);break;
				}
			}
		}
	}

	private void modechecker(Pixel p) {
		if (p.x%2+p.y%2==1) p.on = true; else p.on = false;
	}
	private void modechecker2(Pixel p) {
		if (p.x%2+p.y%2!=1) p.on = true; else p.on = false;
	}
	private void modediag(Pixel p) {
		if ((p.x+p.y)%4==0) p.on = true; else p.on = false;
	}
	private void modediag2(Pixel p) {
		if ((p.x-p.y)%4==0) p.on = true; else p.on = false;
	}
	private void modevert(Pixel p) {
		if (p.x%3==0) p.on = true; else p.on = false;
	}
	private void modehor(Pixel p) {
		if (p.y%3==0) p.on = true; else p.on = false;
	}
	private void modefull(Pixel p) {
		p.on = true;
	}
	private void modeempty(Pixel p) {
		p.on = false;
	}
	private void moderandom(Pixel p) {
		float random = random(0,1);
		if (random > fillrate) p.on = true; else p.on = false;
	}
	
	
	
	// move 
	private boolean forward(int dir,Pixel p) {if (canGo(dir,p)) {
		move(dir,p);
		return true;
	} else return false;
	}
	private void forwDiag(Pixel p) {
		if (!p.on) return;
		if (forward(dir,p)); else {
			if (!diag) return;
			if (forward(p.frontleft(dir),p)); else {
				if (forward(p.frontright(dir),p));
			}
		};
		if (p.hasLeft(dir)&&p.getLeft(dir).hasDirOff(p.frontleft(dir))) {
			forward(p.left(dir),p);
			forward(p.frontleft(dir),p);
		}
		else {
			if (p.hasRight(dir)&&p.getRight(dir).hasDirOff(p.frontright(dir))) {
				forward(p.right(dir),p);
				forward(p.frontright(dir),p);
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
//		case ',':FONT_SCALE_X=FONT_SCALE_X+0.005f;println(FONT_SCALE_X);break;
//		case '.':FONT_SCALE_X=FONT_SCALE_X-0.005f;println(FONT_SCALE_X);break;
//		case ';':FONT_SCALE_Y=FONT_SCALE_Y+0.005f;println(FONT_SCALE_Y);break;
//		case '/':FONT_SCALE_Y=FONT_SCALE_Y-0.005f;println(FONT_SCALE_Y);break;
		case 'a':rx = Math.max(0,(rx-1)%reqList.size());break;
		case 's':rx = Math.max(0,(rx+1)%reqList.size());break;
		}
		
		switch(keyCode) {
		case RIGHT:txtx++;break;
		case LEFT:txtx--;break;
		}
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
			r.setDatec(DateFormat.getDateInstance().parse(m.getString("dateCreated")));
			r.setDatem(DateFormat.getDateInstance().parse(m.getString("dateLastModified")));

			if (!reqMap.containsKey(r.getId())) {
				// new report!
				incoming  = true;
				reqMap.put(r.getId(),r);
				reqList.add(r);
			}


			String title = m.getString("title");
			textString = textString+title+". ";
		}
		text = true;
	}

}


