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
	
	Obliteration obliteration;
	
	private Hexagon[][] arrHex;
	private Neighbor[] neighbors = new Neighbor[6];
	
	public Hexagon(Obliteration obliteration, int posX, int posY, int iniX, int iniY,
			int dimX, int dimY, int limX, int limY, int rotation, int color) {
		this.posX = posX;
		this.posY = posY;
		this.iniX = iniX;
		this.iniY = iniY;
		this.finX = this.iniX + dimX;
		this.finY = this.iniY + dimY;
		this.limX = limX;
		this.limY = limY;
		this.obliteration = obliteration;
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

	
	public void convertNeighbors(ArrayList<String> conversions, int level, int color) {
		//Add info of the current hexagon
		if(conversions.size() >= level) {
			conversions.add("");
		}
		conversions.set(level, conversions.get(level) + infoHexagonToString(color));
		Neighbor n = neighbors[rotation];
		int x, y;
		if (n != null) {
			x = n.getX();
			y = n.getY();
			//If neighbor has not the same color of the converter and has not the pointer...
			if(arrHex[x][y].getColor() != color && arrHex[x][y].getColor() < obliteration.getCOLORS()) {
				arrHex[x][y].setColor(obliteration.getCOLORS() + arrHex[x][y].getColor()); //Set the pointer
				arrHex[x][y].convertNeighbors(conversions, level+1, color);//Recursion call
			}//...otherwise, ends recursive call
		}
		for (int i = 0; i < 6; i++) {
			if(neighbors[i] != null) {
				x = neighbors[i].getX();
				y = neighbors[i].getY();//Get the position of the neighbor in rotation i
				//If neighbor has not the same color of the converter and has not the pointer...
				if(arrHex[x][y].getColor() != color && arrHex[x][y].getColor() < obliteration.getCOLORS()) {
					n = arrHex[x][y].pointingAt();//Get at who is pointing the neighbor
					if(n != null) {//If neighbor is pointing to a valid hexagon
						if(posX == n.getX() && posY == n.getY()) {//If neighbor is pointing at this 
							arrHex[x][y].setColor(obliteration.getCOLORS() + arrHex[x][y].getColor()); //Set the pointer
							arrHex[x][y].convertNeighbors(conversions, level+1, color);//Recursion call
						}
					}
				}
			}
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
	
	private Neighbor pointingAt() {
		return neighbors[rotation];
	}
	
	public void convert(int color) {
		setColor(color);
		//convertNeighbor(neighbors[this.rotation], color);
		//pointingAtMe();//Check if neighbors can be converted
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
	
	public void convertColor(int color) {
		if(this.color == color) return;
		if (this.color == obliteration.getCOLORS()-1 //If this is a non-taken hexagon or its pointer
				|| this.color == obliteration.getCOLORS()*2-1) {
			if(color == obliteration.getColorPlayer()) {
				if(obliteration.getPlayer() == 0) {
					obliteration.updateScores(1, 0);
				}else {
					obliteration.updateScores(0, 1);
				}
			}else {
				if(obliteration.getPlayer() == 0) {
					obliteration.updateScores(0, 1);
				}else {
					obliteration.updateScores(1, 0);
				}
			}
		}
		else {
			if(color == obliteration.getColorPlayer()) {
				if(obliteration.getPlayer() == 0) {
					obliteration.updateScores(1,-1);
				}else {
					obliteration.updateScores(-1,1);
				}
			}else {
				if(obliteration.getPlayer() == 0) {
					obliteration.updateScores(-1,1);
				}else {
					obliteration.updateScores(1,-1);
				}
			}
		}
		this.color = color;
	}
	
	private boolean clicked() {
		int clickX = obliteration.getClickX();
		int clickY = obliteration.getClickY();
		boolean insideX = clickX >= iniX && clickX <= finX;
		boolean insideY = clickY >= iniY && clickY <= finY;
		boolean sameColor = obliteration.getColorPlayer() == this.color;
		return insideX && insideY && sameColor;
	}
	
	private String whichPlayer() {
		int player = obliteration.getPlayer();
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
					obliteration.setClickX(-1);
					obliteration.setClickY(-1); //These two are like canceling the click
					nextRotation();
					ArrayList<String> conversions = new ArrayList<String>();
					convertNeighbors(conversions, 0, this.color);
					String player = whichPlayer();
					obliteration.sendConversions(arrayListToString(conversions)+player);
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
