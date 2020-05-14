package com.oswaldo1002009.obliteration;

import java.util.ArrayList;

public class Hexagon implements Runnable{
	//actual position in array
	private int posX;
	private int posY;
	//positions of click detector
	private int iniX;
	private int iniY;
	private int finX;
	private int finY;
	//end of the hexagons grid
	private int limX;
	private int limY;
	//rotation of the hexagon
	private int rotation;
	//color of the hexagon
	private int color;
	
	TestImage test;
	
	private Hexagon[][] arrHex;
	private Neighbor[] neighbors = new Neighbor[6];
	
	public Hexagon(TestImage test, int posX, int posY, int iniX, int iniY,
			int dimX, int dimY, int limX, int limY, int rotation, int color) {
		this.posX = posX;
		this.posY = posY;
		this.iniX = iniX;
		this.iniY = iniY;
		this.finX = this.iniX + dimX;
		this.finY = this.iniY + dimY;
		this.limX = limX;
		this.limY = limY;
		this.test = test;
		this.rotation = rotation;
		this.color = color;
	}
	//Set the neighbors of the actual hexagon
	public void setArrHex (Hexagon[][] arrHex) {
		this.arrHex = arrHex;
		
		if(posY-1 < 0) neighbors[0] = null;
		else neighbors[0] = new Neighbor(posX, posY-1);
		
		if(posY+1 >= limY) neighbors[3] = null;
		else neighbors[3] = new Neighbor(posX, posY+1);
		
		if(posX%2==0) {
			if(posX+1 >= limX || posY-1 < 0) neighbors[1] = null;
			else neighbors[1] = new Neighbor(posX+1, posY-1);
			
			if(posX+1 >= limX) neighbors[2] = null;
			else neighbors[2] = new Neighbor(posX+1, posY);
			
			if(posX-1 < 0) neighbors[4] = null;
			else neighbors[4] = new Neighbor(posX-1, posY);
			
			if(posX-1 < 0 || posY-1 < 0) neighbors[5] = null;
			else neighbors[5] = new Neighbor(posX-1, posY-1);
		}
		else {
			if(posX+1 >= limX) neighbors[1] = null;
			else neighbors[1] = new Neighbor(posX+1, posY);
			
			if(posX+1 >= limX || posY+1 >= limY) neighbors[2] = null;
			else neighbors[2] = new Neighbor(posX+1, posY+1);
			
			if(posX-1 < 0 || posY+1 >= limY) neighbors[4] = null;
			else neighbors[4] = new Neighbor(posX-1, posY+1);
			
			if(posX-1 < 0) neighbors[5] = null;
			else neighbors[5] = new Neighbor(posX-1, posY);
		}
	}

	
	public void convertNeighbor(ArrayList<String> conversions, int level, int color) {
		//Add info of the current hexagon
		if(conversions.size() >= level) {
			conversions.add("");
		}
		conversions.set(level, conversions.get(level) + infoHexagonToString(color));
		Neighbor n = neighbors[rotation];
		if (n != null) {
			int x = n.getX();
			int y = n.getY();
			//If neighbor has not the same color of the converter and has not the pointer...
			if(arrHex[x][y].getColor() != color && arrHex[x][y].getColor() != test.getCOLORS()-1) {
				arrHex[x][y].setColor(test.getCOLORS() - 1); //Set the pointer
				arrHex[x][y].convertNeighbor(conversions, level+1, color);//Recursion call
			}//...otherwise, ends recursive call
		}
	}
	
	private String arrayListToString(ArrayList<String> list) {
		String converted = "";
		for (int i = 0; i < list.size(); i++) {
			converted += "++" + list.get(i);
		}
		return converted;
	}
	
	private String infoHexagonToString(int color) {
		String infoHexagon = "";
		if (posX < 10) infoHexagon += "0" + posX;
		else infoHexagon += posX;
		
		if (posY < 10) infoHexagon += "0" + posY;
		else infoHexagon += posY;
		
		infoHexagon += "0" + rotation;
		
		if (color < 10) infoHexagon += "0" + color;
		else infoHexagon += color;
		return infoHexagon;
	}
	
	public Neighbor pointingAt() {
		return neighbors[rotation];
	}
	
	public void convert(int color) {
		setColor(color);
		//convertNeighbor(neighbors[this.rotation], color);
		//pointingAtMe();//Check if neighbors can be converted
	}
	
	private void pointingAtMe(){
		int x, y;
		Neighbor neighbor;
		for(int i = 0; i < 6; i++) {
			if(neighbors[i] != null) {
				x = neighbors[i].getX();
				y = neighbors[i].getY();//Get the position of the neighbor in rotation i
				if(arrHex[x][y].getColor() != color) {//If neighbor color is not the player color
					neighbor = arrHex[x][y].pointingAt();//Get at who is pointing the neighbor
					if(neighbor != null) {//If neighbor is pointing to a valid hexagon
						if(posX == neighbor.getX() && posY == neighbor.getY()) {//If neighbor is pointing at this 
							System.out.println(x + " " + y + " is pointing at " + posX + " " + posY);
							arrHex[x][y].convert(color);//Convert the neighbor to the color of this
						}
					}
				}
			}
		}
	}
	
	public int getRotation() {
		return rotation;
	}
	
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	
	private void nextRotation() {
		rotation = (rotation+1)%6;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	private boolean clicked() {
		int clickX = test.getClickX();
		int clickY = test.getClickY();
		boolean insideX = clickX >= iniX && clickX <= finX;
		boolean insideY = clickY >= iniY && clickY <= finY;
		boolean sameColor = test.getColorPlayer() == this.color;
		return insideX && insideY && sameColor;
	}
	
	private String whichPlayer() {
		int player = test.getPlayer();
		if(player == 0) {
			return "OO";//A way to represent 0
		}
		return "II";//Represents 1
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(0);
				if(clicked()) {
					test.setClickX(-1);
					test.setClickY(-1); //These two are like canceling the click
					nextRotation();
					ArrayList<String> conversions = new ArrayList<String>();
					convertNeighbor(conversions, 0, this.color);
					String player = whichPlayer();
					test.sendConversions(arrayListToString(conversions)+player);
					//test.nextTurn();
					//pointingAtMe();
					//System.out.println("Hi! I'm hexagon " + posX + "," + posY);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class Neighbor{
		private int x;
		private int y;
		
		public Neighbor(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		public int getX() {
			return x;
		}
		
		public int getY() {
			return y;
		}
	}
}
