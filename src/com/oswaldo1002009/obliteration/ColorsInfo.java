package com.oswaldo1002009.obliteration;

import java.awt.Color;

public class ColorsInfo {
	public Color[] normalColors = new Color[10];
	public Color[] transparentColors = new Color[10];
	
	public ColorsInfo() {
		transparentColors[0] = new Color(255,0,0,200);
		transparentColors[1] = new Color(0,255,33,200);
		transparentColors[2] = new Color(0,19,127,200);
		transparentColors[3] = new Color(255,216,0,200);
		transparentColors[4] = new Color(0,255,255,200);
		transparentColors[5] = new Color(127,0,110,200);
		transparentColors[6] = new Color(0,0,0,200);
		transparentColors[7] = new Color(255,255,255,200);
		transparentColors[8] = new Color(255,106,0,200);
		transparentColors[9] = new Color(255,0,110,200);
		
		normalColors[0] = new Color(226,192,192);
		normalColors[1] = new Color(0,127,14);
		normalColors[2] = new Color(150,166,255);
		normalColors[3] = new Color(127,106,0);
		normalColors[4] = new Color(0,127,127);
		normalColors[5] = new Color(182,155,255);
		normalColors[6] = new Color(255,255,255);
		normalColors[7] = new Color(0,0,0);
		normalColors[8] = new Color(255,195,155);
		normalColors[9] = new Color(255,173,208);
	}
}
