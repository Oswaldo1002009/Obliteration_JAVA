package com.oswaldo1002009.obliteration;

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
		
		if(posX+1 >= limX) neighbors[1] = null;
		else neighbors[1] = new Neighbor(posX+1, posY);
		
		if(posX+1 >= limX || posY+1 >= limY) neighbors[2] = null;
		else neighbors[2] = new Neighbor(posX+1, posY+1);
		
		if(posY+1 >= limY) neighbors[3] = null;
		else neighbors[3] = new Neighbor(posX, posY+1);
		
		if(posX-1 < 0 || posY+1 > limY) neighbors[4] = null;
		else neighbors[4] = new Neighbor(posX-1, posY+1);
		
		if(posX-1 < 0) neighbors[5] = null;
		else neighbors[5] = new Neighbor(posX-1, posY);
	}
	
	public int getRotation() {
		return rotation;
	}
	
	public void nextRotation() {
		rotation = (rotation+1)%6;
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColot(int color) {
		this.color = color;
	}
	
	private boolean clicked() {
		int clickX = test.getClickX();
		int clickY = test.getClickY();
		boolean insideX = clickX >= iniX && clickX <= finX;
		boolean insideY = clickY >= iniY && clickY <= finY;
		return insideX && insideY;
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(0);
				if(clicked()) {
					test.setClickX(-1);
					test.setClickY(-1); //These two are like canceling the click
					System.out.println("Hi! I'm hexagon " + posX + "," + posY);
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
