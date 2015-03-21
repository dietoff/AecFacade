package text;

import java.util.ArrayList;
import java.util.HashMap;

public class Pixel {
	int id;
	int x;
	int y;
	int width;
	public Pixel up = null;
	public Pixel down = null;
	public Pixel left = null;
	public Pixel right = null;
	public Pixel upright = null;
	public Pixel downright = null;
	public Pixel upleft = null;
	public Pixel downleft = null;

	HashMap<String,Pixel> neighbors = new HashMap<String,Pixel>();
	HashMap<Integer,Pixel> nbhd = new HashMap<Integer,Pixel>();
	public Boolean on = false;


	public Pixel(int id_, int x_, int y_, int w_) {
		id=id_;
		x=x_;
		y=y_;
		width = w_;
	}
	//directions
	//7  0	1
	//6		2
	//5  4	3
	public void addNeighbor(int dir, Pixel p) {
		nbhd.put(dir, p);
	}
	public boolean hasFront(int dir) {
		return nbhd.containsKey(dir%8);
	}
	public boolean hasBack(int dir) {
		return nbhd.containsKey((dir+4)%8);
	}
	public boolean hasLeft(int dir) {
		return nbhd.containsKey((dir+6)%8);
	}
	public boolean hasRight(int dir) {
		return nbhd.containsKey((dir+2)%8);
	}
	public boolean hasFrontRight(int dir) {
		return nbhd.containsKey((dir+1)%8);
	}
	public boolean hasBackRight(int dir) {
		return nbhd.containsKey((dir+3)%8);
	}
	public boolean hasFrontLeft(int dir) {
		return nbhd.containsKey((dir+7)%8);
	}
	public boolean hasBackLeft(int dir) {
		return nbhd.containsKey((dir+5)%8);
	}
	public boolean hasDir(int dir) {
		return nbhd.containsKey(dir%8);
	}
	public boolean hasDirOff(int dir) {
		return nbhd.containsKey(dir%8)&&!nbhd.get(dir%8).on;
	}
	public boolean hasDirOn(int dir) {
		return nbhd.containsKey(dir%8)&&nbhd.get(dir%8).on;
	}
	public Pixel getDir(int dir) {
		return nbhd.get(dir%8);
	}
	public Pixel getFront(int dir) {
		return nbhd.get(dir%8);
	}
	public Pixel getBack(int dir) {
		return nbhd.get((dir+4)%8);
	}
	public Pixel getLeft(int dir) {
		return nbhd.get((dir+6)%8);
	}
	public Pixel getRight(int dir) {
		return nbhd.get((dir+2)%8);
	}
	public Pixel getFrontRight(int dir) {
		return nbhd.get((dir+1)%8);
	}
	public Pixel getBackRight(int dir) {
		return nbhd.get((dir+3)%8);
	}
	public Pixel getFrontLeft(int dir) {
		return nbhd.get((dir+7)%8);
	}
	public Pixel getBackLeft(int dir) {
		return nbhd.get((dir+5)%8);
	}
	
	public Pixel getN() {
		return nbhd.get(0);
	}
	public Pixel getNE() {
		return nbhd.get(1);
	}
	public Pixel getE() {
		return nbhd.get(2);
	}
	public Pixel getSE() {
		return nbhd.get(3);
	}
	public Pixel getS() {
		return nbhd.get(4);
	}
	public Pixel getSW() {
		return nbhd.get(5);
	}
	public Pixel getW() {
		return nbhd.get(6);
	}
	public Pixel getNW() {
		return nbhd.get(7);
	}
	public boolean hasN() {
		return nbhd.containsKey(0);
	}
	public boolean hasNE() {
		return nbhd.containsKey(1);
	}
	public boolean hasE() {
		return nbhd.containsKey(2);
	}
	public boolean hasSE() {
		return nbhd.containsKey(3);
	}
	public boolean hasS() {
		return nbhd.containsKey(4);
	}
	public boolean hasSW() {
		return nbhd.containsKey(5);
	}
	public boolean hasW() {
		return nbhd.containsKey(6);
	}
	public boolean hasNW() {
		return nbhd.containsKey(7);
	}

	public void addNeighbor(String s, Pixel p) {
		if (s=="top") {
			neighbors.put("top", p);
			nbhd.put(0, p);
			up=p;
		}
		if (s=="bottom") {
			neighbors.put("bottom", p);
			nbhd.put(4, p);
			down=p;
		}
		if (s=="left") {
			neighbors.put("left", p);
			nbhd.put(6, p);
			left=p;
		}
		if (s=="right") {
			neighbors.put("right", p);
			nbhd.put(2, p);
			right=p;
		}
		if (s=="topright") {
			neighbors.put("topright", p);
			nbhd.put(1, p);
			upright=p;
		}
		if (s=="bottomright") {
			neighbors.put("bottomright", p);
			nbhd.put(3, p);
			downright=p;
		}
		if (s=="topleft") {
			neighbors.put("topleft", p);
			nbhd.put(7, p);
			upleft=p;
		}
		if (s=="bottomleft") {
			neighbors.put("bottomleft", p);
			nbhd.put(5, p);
			downleft=p;
		}
	}
}
