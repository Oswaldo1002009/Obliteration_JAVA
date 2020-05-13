package com.oswaldo1002009.obliteration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestImage implements Runnable{
	private String ip = "localhost";
	private int port = 22222;
	
	private boolean accepted = false;
	
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;

	private ServerSocket serverSocket;
	
	private JFrame frame;
	private final int WIDTH = 506;
	private final int HEIGHT = 527;
	//Size of an hexagon
	private final int SIZEHX = 18;
	private final int SIZEHY = 17;
	//Displacements to paint hexagons
	private final int HX = 13;
	private final int HM = 8;
	private final int HY = 16;
	//Dimensions of the hexagon grid
	private final int NUM_HEX_X = 30;//30
	private final int NUM_HEX_Y = 26;//25
	//Dimensions of click areas for hexagons
	private final int DIM_X = 10;
	private final int DIM_Y = 15;
	//Initial position for the areas;
	private final int START_X = 4;
	private final int START_Y = 1;
	//Possible color and rotation combinations
	private final int ROTATIONS = 6;
	private final int COLORS = 11;
	
	private Random rand = new Random();
	private boolean unableToConnectWithOpponent = false;
	private boolean start = false;
	private boolean yourTurn = false;
	private int player;
	private int[] playerColors = new int[2];
	
	private Thread thread;
	
	private BufferedImage[][] hexagonSprites = new BufferedImage[6][11];
	private Hexagon hexagons[][] = new Hexagon[NUM_HEX_X][NUM_HEX_Y];
	private Thread threadHexagons[][] = new Thread[NUM_HEX_X][NUM_HEX_Y];
	
	private Font font = new Font("Verdana", Font.BOLD, 32);
	
	private Painter painter;
	
	//Coordinates of the click
	private int clickX = -1;
	private int clickY = -1;
	
	public void setClickX(int x) {
		this.clickX = x;
	}
	
	public void setClickY(int y) {
		this.clickY = y;
	}
	
	public int getClickX() {
		return clickX;
	}
	
	public int getClickY() {
		return clickY;
	}
	
	public int getPlayer() {
		return player;
	}
	
	public TestImage() {
		System.out.println("IP is: " + ip);
		System.out.println("Port is: " + port);
		
		loadImages();
		
		painter = new Painter();
		painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		//This also gives the first turn to the player who created the server
		if(!connect()) {
			initializeServer();
			setPlayerColors();
		}
		else {
			requestPlayerColors();
			//requestHexagonGrid();
		}
		createHexagons();
		
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
	
	private void setPlayerColors() {
		player = 0;//Player 1 is 0, player 2 is 0 for practical purposes
		//It's COLORS - 1 because the last is considered for the non taken hexagons
		playerColors[0] = Math.abs(rand.nextInt()%(COLORS-1));
		playerColors[1] = Math.abs(rand.nextInt()%(COLORS-1));
		while(playerColors[0] == playerColors[1]) {
			playerColors[1] = Math.abs(rand.nextInt()%(COLORS-1));
		}
	}
	
	private void setPlayerColors(String colors) {
		player = 1;
		//The first 4 characters are the player colors
		playerColors[0] = Integer.parseInt(colors.substring(0,2));
		playerColors[1] = Integer.parseInt(colors.substring(2,4));
		setHexagonGrid(colors.substring(4));
	}
	
	private void setHexagonGrid(String grid) {//This receives an string of NUM_HEX_X*NUM_HEX_Y*3
		int hexagonColor;
		int hexagonRotation;
		for (int i = 0; i < NUM_HEX_X; i++) {
			for(int j = 0; j < NUM_HEX_Y; j++) {
				hexagonColor = Integer.parseInt(grid.substring(0,2));//The first two digits are the color
				grid = grid.substring(2);//Cut those two digits
				hexagons[i][j].setColor(hexagonColor);
				hexagonRotation = Integer.parseInt(grid.substring(0,1));//The third digit is the rotation
				grid = grid.substring(1);//Cut that digit
				hexagons[i][j].setRotation(hexagonRotation);
			}
		}
	}
	
	private void createHexagons() {
		//Creating the threads for hexagons
		int moveY;
		for (int i = 0; i < NUM_HEX_X; i++) {
			for(int j = 0; j < NUM_HEX_Y; j++) {
				moveY = i%2 * HM;
				int rotation = Math.abs(rand.nextInt())%ROTATIONS;
				int color = COLORS - 1;
				if (i == j && j == 0) {
					rotation = 1; //The first hexagon points upper right
					color = playerColors[0];
				}
				else if(i == NUM_HEX_X-1 && j == NUM_HEX_Y-1){//If the last hexagon...
					//...set rotation to lower left or down depending if the x position is even or odd
					rotation = ROTATIONS - NUM_HEX_X%2 - 2;
					color = playerColors[1];
				}
				hexagons[i][j] = new Hexagon(this, i, j, //this class and actual position in array
						START_X + i*HX, START_Y + j*HY+moveY,//position of the rectangle to click
						DIM_X, DIM_Y,//dimensions of the rectangle
						NUM_HEX_X, NUM_HEX_Y,//total number of rectangles in matrix
						rotation, color);//Rotation and color position (between 0 and 5)
				threadHexagons[i][j] = new Thread(hexagons[i][j]);
				threadHexagons[i][j].start();
			}
		}
		//Let each hexagon meet all the other
		for (int i = 0; i < NUM_HEX_X; i++) {
			for(int j = 0; j < NUM_HEX_Y; j++) {
				hexagons[i][j].setArrHex(hexagons);
			}
		}
	}
	
	public void run() {
		while(true) {
			tick();
			painter.repaint();
			if(!accepted) {
				listenForServerRequest();
			}
		}
	}
	
	private void render(Graphics g) {
		if(!accepted) {
			g.setColor(new Color(0, 0, 0));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.LIGHT_GRAY);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth("Waiting for player 2 to join");
			g.drawString("Waiting for player 2 to join", WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}
		else if(accepted && !start && !yourTurn) {
			g.setColor(new Color(0, 0, 0));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.RED);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth("Waiting for player 1");
			g.drawString("Waiting for player 1", WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			
		}
		else if(accepted && !start && yourTurn) {
			g.setColor(new Color(0, 0, 0));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.GREEN);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth("Click to start");
			g.drawString("Click to start", WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}
		else if(accepted && start) {
			int moveY;
			int hexagonRotation;
			int hexagonColor;
			for (int i = 0; i < NUM_HEX_X; i++) {
				for(int j = 0; j < NUM_HEX_Y; j++) {
					moveY = i%2 * HM;
					hexagonRotation = hexagons[i][j].getRotation();
					hexagonColor = hexagons[i][j].getColor();
					g.drawImage(hexagonSprites[hexagonRotation][hexagonColor], i*HX, j*HY+moveY, null);
					//g.drawImage(hexagonSprites[hexagonRotation][Math.abs(rand.nextInt())%6], i*HX, j*HY+moveY, null);
					//Verify the correct position of rectangles
					/*g.setColor(new Color(255, 0, 0));
					g.fillRect(START_X + i*HX, START_Y + j*HY+moveY, DIM_X, DIM_Y);*/
				}
			}
		}
	}
	
	private void tick() {
		if(!yourTurn && !unableToConnectWithOpponent) {
			try {
				String str = dis.readUTF();
				switch(str.substring(0,1)) {
					case "1":
						yourTurn = true;
						break;
					case "2":
						sendPlayerColors();
						break;
					case "3":
						setPlayerColors(str.substring(1));
						break;
				}
			}catch (IOException e) {
				e.printStackTrace();
				unableToConnectWithOpponent = true;
			}
		}
	}
	
	private void requestPlayerColors() {
		try {
			dos.writeUTF("2");//2 is for send player colors
			dos.flush();
			System.out.println("Request for colors was sent to player 1");
		}catch(IOException e2) {
			unableToConnectWithOpponent = true;
			e2.printStackTrace();
		}
	}
	
	private void sendPlayerColors() {
		//String colors = Integer.toString(playerColors[0]) + Integer.toString(playerColors[1]);
		String colors = "";
		if(playerColors[0] < 10) colors += "0" + playerColors[0];
		else colors += playerColors[0];
		if(playerColors[1] < 10) colors += "0" + playerColors[1];
		else colors += playerColors[1];
		String grid = stringHexagonGrid();
		try {
			dos.writeUTF("3"+colors+grid);//3 is for update player colors
			dos.flush();
			System.out.println("Colors were sent to player 2");
		}catch(IOException e2) {
			unableToConnectWithOpponent = true;
			e2.printStackTrace();
		}
	}
	
	private String stringHexagonGrid() {
		String grid = "";
		int rot;
		for (int i = 0; i < NUM_HEX_X; i++) {
			for(int j = 0; j < NUM_HEX_Y; j++) {
				rot = hexagons[i][j].getColor();
				if(rot < 10) grid += ("0" + rot);
				else grid += rot;
				grid += hexagons[i][j].getRotation();
			}
		}
		return grid;
	}
	
	private void loadImages() {
		try {
			BufferedImage hexagonGrid = ImageIO.read(getClass().getResourceAsStream("/Hexagons.png"));
			for(int i = 0; i < ROTATIONS; i++) {
				for(int j = 0; j < COLORS; j++) {
					hexagonSprites[i][j] = hexagonGrid.getSubimage(SIZEHX*i, SIZEHY*j, SIZEHX, SIZEHY);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		TestImage testImage = new TestImage();
	}
	
	private class Painter extends JPanel implements MouseListener{
		private static final long serialVersionUID = 1L;
		
		public Painter() {
			setFocusable(true);
			requestFocus();
			setBackground(Color.BLACK);
			addMouseListener(this);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			render(g);
		}
		
		@Override
		public synchronized void mouseClicked(MouseEvent e) {
			if(accepted) {
				if(yourTurn && !unableToConnectWithOpponent) {
					if(!start) {
						start = true;
					}
					else {
						clickX = e.getX();
						clickY = e.getY();
					}
					yourTurn = false;
					try {
						dos.writeUTF("1");//1 is for giving their turn to the other player
						dos.flush();
					}catch(IOException e2) {
						unableToConnectWithOpponent = true;
						e2.printStackTrace();
					}
					System.out.println("Data was sent to the other player");
				}
				//System.out.println(clickX + " " + clickY);
			}
		}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
	}
	
	//Tries to connect with another client
	private void listenForServerRequest() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
			System.out.println("Client has requested to join and we have accepted");
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/********Connection management********/
	/*
	 * boolean connect(): Tries to connect to an existing server. If
	 * connection is unsuccessful, this function starts a new server.
	*/
	private boolean connect() {
		try {
			socket = new Socket(ip, port);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			accepted = true;
		}catch(IOException E) {
			System.out.println("Unable to connect to " + ip + ":" + port + ".");
			System.out.println("Starting a server.");
			return false;
		}
		System.out.println("Succesfully connected to the server.");
		return true;
	}
	
	private void initializeServer() {
		try {
			/* There are different ways to write an ip address:
			 * www.domain.com, 172.12.12.12, localhost...
			 * For this program, it is used the string "localhost"
			*/
			/* Backlog is 4, I'm going to try to play 2 to 4 players*/
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName(ip));
		}catch(BindException e) {
			System.out.println("Error: " + ip + ":" + port + " already in use.");
			System.exit(1);
		}catch(IOException e) {
			e.printStackTrace();
		}
		yourTurn = true;
	}
}
