package com.oswaldo1002009.obliteration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestImage implements Runnable{
	private BufferedImage test_hexagon;
	private JFrame frame;
	private final int WIDTH = 506;
	private final int HEIGHT = 527;
	private final int HX = 13;
	private final int HM = 8;
	private final int HY = 16;
	private final int NUM_HEX_X = 30;
	private final int NUM_HEX_Y = 25;
	private Thread thread;
	
	private Painter painter;
	
	public TestImage() {
		loadImages();
		
		painter = new Painter();
		painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		frame = new JFrame();
		frame.setTitle("Tic-Tac-Toe");
		frame.setContentPane(painter);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		
		thread = new Thread(this, "TicTacToe");
		thread.start();
	}
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			painter.repaint();
		}
	}
	
	private void render(Graphics g) {
		g.drawImage(test_hexagon, 0, 0, null);
		int moveY;
		for (int i = 0; i < NUM_HEX_X; i++) {
			for(int j = 0; j < NUM_HEX_Y; j++) {
				moveY = i%2 * HM;
				g.drawImage(test_hexagon, i*HX, j*HY+moveY, null);
			}
		}
	}
	
	private void loadImages() {
		try {
			test_hexagon = ImageIO.read(getClass().getResourceAsStream("/BasicHexagon.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("hi!");
		TestImage testImage = new TestImage();
	}
	
	private class Painter extends JPanel implements MouseListener{
		private static final long serialVersionUID = 1L;
		
		public Painter() {
			setFocusable(true);
			requestFocus();
			setBackground(Color.WHITE);
			addMouseListener(this);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		
	}
}
