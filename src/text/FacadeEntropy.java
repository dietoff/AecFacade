package text;
import gifAnimation.Gif;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.Table;
import processing.data.TableRow;

public class FacadeEntropy extends PApplet {

	private float perc = .15f;
	private float fillrate = 0.5f;
	AEC aec;
	FacFont[] fontarray = { new FacFont(this, "../data/wendy.ttf", 5, 2f, 2.0f, 0.5f, 0.5f),
			new FacFont(this, "../data/04B_03__.ttf", 4, 2f, 2f, 0.5f, 0.5f),
			new FacFont(this,"../data/uni0553-webfont.ttf", 4, 2.f, 2.f, 0.5f, 0.5f)
	};
	int fontnr = 2;
	private HashMap<Integer, Pixel> idmap;
	private HashMap<Integer, Pixel> xymap;
	private ArrayList<Pixel> activePixels;
	private int state = 1;
	private int direction = 0;
	private boolean friction = true;
	private String url = "http://sal-if.linz.at/mobile/action?type=8&pageIndex=1&pageSize=150";
	private boolean textOn;
	private boolean verticalText = false;
	private HashMap<Integer, Request> reqMap;
	private ArrayList<Request> reqList;
	private boolean incoming = false;
	private int reqNr = 0;
	private int txtx = 11;
	private HashMap<Integer,ArrayList<Request>> timeline;
	private HashMap<Integer, ArrayList<Request>> timelineErledigt;
	private boolean stop = false;
	private char keysent;
	Gif gif;
	private int textcolor = color(255,0,100);
	
	private int unerledigt = color(222,0,0);
	private int erledigt = color(0,222,0);
	private int inBearb = color(255,222,0);
	private int na = color(255,255,255);
	private boolean imgOn = false;
	private int startFrame=0;
	private boolean schwund=false;
	private PImage gifBG;

	public void setup() {
		thread("requestData");
		reqMap = new HashMap<Integer, Request>();
		reqList = new ArrayList<Request>();
		activePixels = new ArrayList<Pixel>();
		gif = new Gif(this, "../data/10.gif");
		gifBG = gif.get(0, 0, 1, 1);
		gif.loop();
		frameRate(25);
		size(1200, 400);
		readTopology();
		for (Pixel p:idmap.values()) moderandom(p);
		
		for (int i=0; i< fontarray.length;i++) fontarray[i].createFont();
		
		aec = new AEC(this);
		aec.init();
		frameRate(25);
		noSmooth();
	}
	
	private void pullLottery(float perc){
		ArrayList<Pixel> shuffle = shuffle();
		for (int i=0; i < shuffle.size()*perc; i++) {
			shuffle.get(i);
			activePixels.add(shuffle.get(i));
		}
	}

	private ArrayList<Pixel> shuffle() {
		ArrayList<Pixel> v2 = new ArrayList<Pixel>(); 
				v2.addAll(idmap.values());
				Collections.shuffle(v2);
				return v2;
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
//		if (frameCount%50==0) {
//			thread("requestFont");
//		}
//		if (frameCount%50==25) {
//			thread("requestKey");
//		}
		aec.beginDraw();
		
		background(0,0,0);
		dotDisplay();
		if (textOn) {
			textDisplay();
		}
		//		if (frameCount%100==0) rot = !rot;
		//		if (frameCount%100==0) rx = (int) random(0f,10f);
		//		if (frameCount%150==0) state = (int) random(0f,20f);
		executeState();
		
		if (imgOn) animation();
		
//		fill(50);
//		rect(0,0,20,20);
		
		aec.endDraw();
		aec.drawSides();
	}

	private void animation() {
		image(gif, 10f,2);
		image(gif, 20f,2);
		image(gif, 30f,2);
		image(gifBG,40,2,32,20);
		image(gifBG,0,2,10,20);
	}

	private void dotDisplay() {
		stroke(255,255,255);
		if(!friction) point(1,1);
		for (Pixel p:idmap.values()) {
			if (p.on) {
				stroke(p.color);
				point(p.x,p.y);
			}
		}
	}

	private void textDisplay() {
		noStroke();
		// determines the speed (number of frames between text movements)
		int frameInterval = 6;
		
		Request request = reqList.get(reqNr);
		
		int status = request.getStatus();
		switch (status) {
		case 1:fill(unerledigt);break;
		case 2:fill(inBearb);break;
		case 3:fill(erledigt);break;
		case 4:fill(na);break;
		}
		
		String txt = request.getTitle();
		int maxPos = 35;
		FacFont f = fontarray[fontnr];
		int minPos = (int) (-txt.length()*f.getFONT_SIZE()*f.getFONT_SCALE_Y()); // loop point based on length of string
		int loopFrames = (maxPos-minPos) * frameInterval;
		
		renderText(11, max(minPos, maxPos - ((frameCount-startFrame)%loopFrames) / frameInterval), txt );
		renderText(8, max(minPos, maxPos - ((frameCount-startFrame)%loopFrames) / frameInterval), txt );
		renderText(5, max(minPos, maxPos - ((frameCount-startFrame)%loopFrames) / frameInterval), txt );
//		renderText(txtx, 10,txt );
	}

	void renderText(int x, int y, String txt)
	{
		// push & translate to the text origin
		pushMatrix();
		if (verticalText) {
			rotate(PI/2);
			translate(-50,-43);
		}
		FacFont f = fontarray[fontnr];
		translate(x+f.getFONT_OFFSET_X(),y+f.getFONT_OFFSET_Y());
		
		// scale the font up by fixed parameters so it fits our grid
		scale(f.getFONT_SCALE_X(),f.getFONT_SCALE_Y());
		
		textFont(f.getFont());
		textSize(f.getFONT_SIZE());
		textAlign(CENTER);
		 
			for(int i = 0; i < txt.length(); i++)
			{
				text(txt.charAt(i), x,(float)(i*4));
			}
		popMatrix();
	}

	private double getDay(Request r) {
		long datec = r.getDatec().getTime();
		double diff = (System.currentTimeMillis()-datec)/(double)(3600000*24);
		return diff;
	}

	private void executeState() {
		ArrayList<Pixel> a = shuffle();
		for (int i = 0; i< a.size()*perc; i++ ) {
			Pixel p = a.get(i);
			if (schwund) {
				if (random(0,1)<0.15f) p.on=false;
			}
			{
				switch (state) {
				case 1:direction=0;forwDiag(p);break;
				case 2:direction=1;forwDiag(p);break;
				case 3:direction=2;forwDiag(p);break;
				case 4:direction=3;forwDiag(p);break;
				case 5:direction=4;forwDiag(p);break;
				case 6:direction=5;forwDiag(p);break;
				case 7:direction=6;forwDiag(p);break;
				case 8:direction=7;forwDiag(p);break;
				case 11:moderandom(p);break;
				case 12:modechecker(p);break;
				case 13:modechecker2(p);break;
				case 14:modediag(p);break;
				case 15:modediag2(p);break;
				case 16:modevert(p);break;
				case 17:modehor(p);break;
				case 18:modefull(p);break;
				case 19:modeempty(p);break;
				case 20:modeTimeline(p);break;
				}
			}
		}
	}
	/*
	private void modeTimeline(Pixel p) {
		boolean containsKey = timeline.containsKey(p.x-10);
		if (containsKey&&p.y<19&&timeline.get(p.x-10)>(18-p.y)) {
			 p.on = true; 
		}
		else p.on = false;
	}*/
	
	private void modeTimeline(Pixel p) {
		int n=0;
		int zeroline = 11;
		int width = 20;
		if (timeline.containsKey(p.x%width)) n = timeline.get(p.x%width).size(); 
		if (p.y<zeroline&&n>(zeroline-p.y-1)) {
			 p.on = true;
			Request r = timeline.get(p.x%width).get(zeroline-p.y-1);
			 if (r.getStatus()==1) p.color = unerledigt; else p.color = inBearb;
		} else p.on = false;
		
		if (timelineErledigt.containsKey(p.x%width)) n = timelineErledigt.get(p.x%width).size(); 
		if (p.y>zeroline&&n>(p.y-zeroline-1)) {
			 p.on = true; 
			 p.color = 0xff00ff00;
			 ArrayList<Request> arrayList = timelineErledigt.get(p.x%width);
			Request r = arrayList.get(p.y-zeroline-1);
			 if (r.getStatus()==3) p.color = erledigt; else p.color = na;
		}
		
		if (!timelineErledigt.containsKey(p.x%width)&&!timeline.containsKey(p.x%width)) {
			p.on = false;
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
//	private void moderandom(Pixel p) {
//		float random = random(0,1);
//		if (random > fillrate) p.on = true; else p.on = false;
//	}
	
	private void moderandom(Pixel p) {
		if (!p.on) return;
		ArrayList<Pixel> shuffle = shuffle();
		Pixel pixel = shuffle.get(0);
		if (!pixel.on) {
			p.on=false;
			pixel.on=true;
			pixel.color=p.color;
			p.color=0xffffffff;
		} 
	}
	
	
	
	// move 
	private boolean forward(int dir,Pixel p) {if (canGo(dir,p)) {
		move(dir,p);
		return true;
	} else return false;
	}
	private void forwDiag(Pixel p) {
		if (!p.on) return;
		if (forward(direction,p)); else {
			if (friction) return;
			if (forward(p.frontleft(direction),p)); else {
				if (forward(p.frontright(direction),p));
			}
		};
		if (p.hasLeft(direction)&&p.getLeft(direction).hasDirOff(p.frontleft(direction))) {
			forward(p.left(direction),p);
			forward(p.frontleft(direction),p);
		}
		else {
			if (p.hasRight(direction)&&p.getRight(direction).hasDirOff(p.frontright(direction))) {
				forward(p.right(direction),p);
				forward(p.frontright(direction),p);
			}
		}
	}

	private void move(int dir, Pixel p) {
		p.on=false;
		p.getDir(dir).on=true;
		p.getDir(dir).color=p.color;
		p.color=0xffffffff;
	}

	private boolean canGo(int dir,Pixel p) {
		return p.on&&p.hasDirOff(dir);
	}

	public void keyPressed() {
		FacFont f = fontarray[fontnr];
//		aec.keyPressed(key);
		switch(key) {
		case 't':state=1;textOn=false;imgOn=false;;break;
		case 'y':state=2;textOn=false;imgOn=false;break;
		case 'h':state=3;textOn=false;imgOn=false;;break;
		case 'n':state=4;textOn=false;imgOn=false;;break;
		case 'b':state=5;textOn=false;imgOn=false;;break;
		case 'v':state=6;textOn=false;imgOn=false;;break;
		case 'f':state=7;textOn=false;imgOn=false;;break;
		case 'r':state=8;textOn=false;imgOn=false;;break;
		case 'g':friction=!friction;break;
		case '0':state=11;textOn=false;imgOn=false;break;
		case '9':state=12;textOn=false;imgOn=false;break;
		case '8':state=13;textOn=false;imgOn=false;break;
		case '7':state=14;textOn=false;imgOn=false;break;
		case '6':state=15;textOn=false;imgOn=false;break;
		case '5':state=16;textOn=false;imgOn=false;break;
		case '4':state=17;textOn=false;imgOn=false;break;
		case '3':state=18;textOn=false;imgOn=false;break;
		case '2':state=19;textOn=false;imgOn=false;break;
		case '1':state=20;textOn=false;imgOn=false;break;
		case 'q':startFrame=frameCount;textOn=!textOn;state=19;imgOn=false;break;
		case 'w':schwund=!schwund;break;
		case 'e':imgOn=!imgOn;textOn=false;state=19;break;
		case ',':f.setFONT_SCALE_X(f.getFONT_SCALE_X()+0.005f);println(f.getFONT_SCALE_X());break;
		case '.':f.setFONT_SCALE_X(f.getFONT_SCALE_X()-0.005f);println(f.getFONT_SCALE_X());break;
		case ';':f.setFONT_SCALE_Y(f.getFONT_SCALE_Y()+0.005f);println(f.getFONT_SCALE_Y());break;
		case '/':f.setFONT_SCALE_Y(f.getFONT_SCALE_Y()-0.005f);println(f.getFONT_SCALE_Y());break;
		case 'a':reqNr = Math.max(0,(reqNr-1)%reqList.size());break;
		case 's':reqNr = Math.max(0,(reqNr+1)%reqList.size());break;
		}
		
		switch(keyCode) {
		case RIGHT:txtx++;break;
		case LEFT:txtx--;break;
		}
	}

	
	public void requestFont() {
		String lines[] = loadStrings("http://www.azmar.org/php/state.txt");
		 int k = Integer.parseInt(lines[0]);
		if (fontnr!=k) {
			fontnr = k;
			 println("font "+k);
		 }
		println("font "+fontnr);
	}
	public void requestKey() {	
		String lines[] = loadStrings("http://www.azmar.org/php/key.txt");
		 char k = lines[0].charAt(0);
		 if (keysent!=k) {
			 keysent = k;
			 key = k;
			 println("key "+k);
			 keyPressed();
		 }
		
	}
	
	
	public void requestData() throws ParseException {
//		JSONObject json = loadJSONObject(url);
		JSONObject json = loadJSONObject("../data/sample.json");
		JSONArray msg = json.getJSONArray("MESSAGELIST");
		for (int i = 0; i < msg.size(); i++) {

			JSONObject m = msg.getJSONObject(i); 
			Request r = new Request();
			r.setId(m.getInt("id"));
			r.setTitle(m.getString("title","").toUpperCase());
			r.setText(m.getString("text","").toUpperCase());
			r.setStatus(m.getInt("status",0));
			r.setCategory(m.getString("category",""));
			r.setLon(m.getInt("lon",0));
			r.setLat(m.getInt("lat",0));
			r.setActive(m.getInt("active",0));
			r.setDistrict(m.getString("district","").toUpperCase());
			DateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
			r.setDatec(df.parse(m.getString("dateCreated")));
			r.setDatem(df.parse(m.getString("dateLastModified")));

			if (!reqMap.containsKey(r.getId())) {
				incoming  = true;
				reqMap.put(r.getId(),r);
				reqList.add(r);
				
				
			}

		}
		textOn = true;
		println("msgs loaded");
		createTimeline();
	}

	private void createTimeline() {
		timeline = new HashMap<Integer, ArrayList<Request>>();
		timelineErledigt = new HashMap<Integer, ArrayList<Request>>();
		for (Request r:reqList) {
			if (r.getStatus()>2)
				addTimelineErledigt(r);
			else
				addTimelineUnerledigt(r);
		}
	}

	private void addTimelineUnerledigt(Request r) {
		int day = (int) getDay(r);
		if (timeline.containsKey(day)) {
			ArrayList<Request> l = timeline.get(day);
			l.add(r);
		} else {
			ArrayList<Request> l = new ArrayList<Request>();
			l.add(r);
			timeline.put(day, l);
		}
	}
	private void addTimelineErledigt(Request r) {
		int day = (int) getDay(r);
		if (timelineErledigt.containsKey(day)) {
			ArrayList<Request> l = timelineErledigt.get(day);
			l.add(r);
		} else {
			ArrayList<Request> l = new ArrayList<Request>();
			l.add(r);
			timelineErledigt.put(day, l);
		}
	}
}


