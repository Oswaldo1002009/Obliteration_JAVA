package com.oswaldo1002009.obliteration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Obliteration implements Runnable{
	private String ip = "localhost";
	private int port = 2222;
	private Scanner scanner = new Scanner(System.in);
	
	private boolean accepted = false;
	
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;

	private ServerSocket serverSocket;
	
	private JFrame frame;
	private final int WIDTH = 700;
	private final int HEIGHT = 501;
	//Absolute displacement of the grid
	private final int DISPX = 30;
	private final int DISPY = 30;
	//Size of an hexagon
	private final int SIZEHX = 24;//18
	private final int SIZEHY = 22;//17
	//Displacements to paint hexagons
	private final int HX = 17;//13
	private final int HM = 10;//8
	private final int HY = 20;//16
	//Dimensions of the hexagon grid
	private final int NUM_HEX_X = 24;
	private final int NUM_HEX_Y = 20;
	//Dimensions of click areas for hexagons
	private final int DIM_X = 14;//10
	private final int DIM_Y = 20;//15
	//Initial position for the areas;
	private final int START_X = 5;//4
	private final int START_Y = 1;//1
	//Possible color and rotation combinations
	private final int ROTATIONS = 6;
	private final int COLORS = 11;
	private ColorsInfo colorsInfo = new ColorsInfo();
	
	private Random rand = new Random();
	private boolean unableToConnectWithOpponent = false;
	private boolean start = false;
	private boolean yourTurn = false;
	private int player;
	private int[] playerColors = new int[2];
	
	private String conversions = "NO";
	
	private Thread thread;
	
	private BufferedImage[][] hexagonSprites = new BufferedImage[ROTATIONS][COLORS*2];
	private Hexagon hexagons[][] = new Hexagon[NUM_HEX_X][NUM_HEX_Y];
	private Thread threadHexagons[][] = new Thread[NUM_HEX_X][NUM_HEX_Y];
	
	private Font fontSmall;
	private Font fontSmallBold;
	private Font font;
	
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
	
	public int getColorPlayer() {
		return playerColors[player];
	}
	
	public int getCOLORS() {
		return COLORS;
	}
	
	private void setConversions(String converting) {
		conversions = converting;
	}
	
	private String whichPlayer() {
		int player = getPlayer();
		if(player == 0) {
			return "OO";//A way to represent 0
		}
		return "II";//Represents 1
	}
	
	public Obliteration() {
		boolean predetermined = true;
		if(predetermined) {
			System.out.println("IP is: " + ip);
			System.out.println("Port is: " + port);
		}
		else {
			System.out.println("Please input the IP: ");
			ip = scanner.nextLine();
			System.out.println("Please input the port: ");
			port = scanner.nextInt();
			while (port < 1 || port > 65535) {
				System.out.println("Please input a port between 1 and 65535: ");
				port = scanner.nextInt();
			}
		}
		
		loadImages();
		loadFonts();
		
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
		frame.setTitle("Obliteration");
		frame.setContentPane(painter);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		
		thread = new Thread(this, "Obliteration");
		thread.start();
	}
	
	private void setPlayerColors() {
		player = 0;//Player 1 is 0, player 2 is 0 for practical purposes
		//It's COLORS - 2 because COLORS - 2 is considered for the non taken hexagons
		//and COLORS - 1 is for the pointer used in Hexagon class
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
	
	//initialize the grid
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
	
	//initialize the hexagons
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
						DISPX + START_X + i*HX, DISPY + START_Y + j*HY+moveY,//position of the rectangle to click
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
	
	//from tick() option "4" the player can take its turn once
	//it finishes processing the string of conversions
	//and just if the string was sent by the other player
	public void run() {
		while(true) {
			if(!conversions.equals("NO")) {//Means it could have pending conversions
				if(conversions.equals("OO") || conversions.equals("II")) {//No more conversions to do
					//Turn of this player activates if the message came from the other player
					if(!whichPlayer().equals(conversions)) yourTurn = true;
					conversions = "NO";
					painter.repaint();
				}else {//This starts with ++
					conversions = conversions.substring(2);
					convertHexagons();
					painter.repaint();
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				tick();
				painter.repaint();
			}
			//tick();
			if(!accepted) {
				listenForServerRequest();
			}
		}
	}
	
	//process the string to convert the hexagons
	//All hexagons are converted at the same time until it is find
	//a "++", "OO" or "II", then the void run waits and resend
	//the string to this
	private void convertHexagons() {
		int x, y, rotation, color;
		while(!conversions.equals("")) {
			if(conversions.substring(0,2).equals("++")) return;
			if(conversions.substring(0,2).equals("OO")) return;
			if(conversions.substring(0,2).equals("II")) return;
			x = Integer.parseInt(conversions.substring(0,2));
			conversions = conversions.substring(2);
			
			y = Integer.parseInt(conversions.substring(0,2));
			conversions = conversions.substring(2);
			
			rotation = Integer.parseInt(conversions.substring(0,2));
			conversions = conversions.substring(2);
			
			color = Integer.parseInt(conversions.substring(0,2));
			conversions = conversions.substring(2);
			
			hexagons[x][y].setRotation(rotation);
			hexagons[x][y].setColor(color);
		}
	}
	
	private int[] obliterationScores() {
		int[] scores = {0,0};
		for (int i = 0; i < NUM_HEX_X; i++) {
			for (int j = 0; j < NUM_HEX_Y; j++) {
				if(playerColors[0] == hexagons[i][j].getColor()
						|| playerColors[0] == hexagons[i][j].getColor()+COLORS) scores[0]++;
				else if(playerColors[1] == hexagons[i][j].getColor()
						|| playerColors[1] == hexagons[i][j].getColor()+COLORS) scores[1]++;
			}
		}
		return scores;
	}
	
	private void render(Graphics g) {
		if(unableToConnectWithOpponent) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setFont(fontSmallBold);
			g.setColor(Color.RED);
			int stringWidth = g2.getFontMetrics().stringWidth("Error. Unable to connect with opponent.");
			g.drawString("Error. Unable to connect with opponent.", WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}
		else if(!accepted) {
			g.setColor(new Color(0, 0, 0));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.LIGHT_GRAY);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth("WAITING FOR PLAYER 2 TO JOIN");
			g.drawString("WAITING FOR PLAYER 2 TO JOIN", WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
		}
		else if(accepted && !start && !yourTurn) {
			g.setColor(new Color(0, 0, 0));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.YELLOW);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth("WAITING FOR PLAYER 1");
			g.drawString("WAITING FOR PLAYER 1", WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
			
		}
		else if(accepted && !start && yourTurn) {
			g.setColor(new Color(0, 0, 0));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.GREEN);
			g.setFont(font);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth("CLICK TO START");
			g.drawString("CLICK TO START", WIDTH / 2 - stringWidth / 2, HEIGHT - 50);
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
					g.drawImage(hexagonSprites[hexagonRotation][hexagonColor], DISPX + i*HX, DISPY + j*HY+moveY, null);
					//Verify the correct position of rectangles
					/*g.setColor(new Color(255, 0, 0));
					g.fillRect(START_X + i*HX, START_Y + j*HY+moveY, DIM_X, DIM_Y);*/
				}
			}
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			int[] oS = obliterationScores();
			int stringWidth;
			if(yourTurn && player == 0) {
				g.setFont(fontSmallBold);
				g.setColor(colorsInfo.transparentColors[playerColors[0]]);
				g.fillRoundRect(475, 55, 180, 180, 10, 10);
				g.setColor(colorsInfo.normalColors[playerColors[0]]);
				g.drawRoundRect(475, 55, 180, 180, 10, 10);
			}
			else g.setFont(fontSmall);
			g.setColor(colorsInfo.normalColors[playerColors[0]]);
			stringWidth = g2.getFontMetrics().stringWidth("PLAYER 1");
			g.drawString("PLAYER 1", 565 - stringWidth/2, 80);
			g.setFont(font);
			//String of a 2 decimals number
			g.drawString("" + String.format("%.4g%n",(100*(0.0+oS[0])/(oS[0]+oS[1]))) + "%", 525, 130);
			
			if(yourTurn && player == 1) {
				g.setFont(fontSmallBold);
				g.setColor(colorsInfo.transparentColors[playerColors[1]]);
				g.fillRoundRect(475, 255, 180, 180, 10, 10);
				g.setColor(colorsInfo.normalColors[playerColors[1]]);
				g.drawRoundRect(475, 255, 180, 180, 10, 10);
			}
			else g.setFont(fontSmall);
			g.setColor(colorsInfo.normalColors[playerColors[1]]);
			g.drawString("PLAYER 2", 500, 280);
			g.setFont(font);
			g.drawString("" + String.format("%.4g%n",(100*(0.0+oS[1])/(oS[0]+oS[1]))) + "%", 525, 330);
		}
	}
	
	//manages the string sent by the other player
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
					case "4":
						setConversions(str.substring(1));
						
				}
			}catch (IOException e) {
				System.out.println("Error. Connection lost.");
				unableToConnectWithOpponent = true;
			}
		}
	}
	
	//cancels the turn of this player and gives it to the other
	public void nextTurn() {
		yourTurn = false;
		try {
			dos.writeUTF("1");//1 is for giving their turn to the other player
			dos.flush();
			System.out.println("Data was sent to the other player");
		}catch(IOException e2) {
			unableToConnectWithOpponent = true;
			System.out.println("Error. Connection lost.");
		}
	}
	
	//asks for player colors to the other player
	private void requestPlayerColors() {
		try {
			dos.writeUTF("2");//2 is for send player colors
			dos.flush();
			System.out.println("Request for colors was sent to player 1");
		}catch(IOException e2) {
			unableToConnectWithOpponent = true;
			System.out.println("Error. Connection lost.");
		}
	}
	
	//sends the colors of both players to the other player
	private void sendPlayerColors() {
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
			System.out.println("Error. Connection lost.");
		}
	}
	
	//Send the string of conversions to the other player
	//this leads to the other player to get its turn (on another function)
	public void sendConversions(String conversions) {
		setConversions(conversions);
		yourTurn = false;
		try {
			dos.writeUTF("4"+conversions);//1 is for giving their turn to the other player
			dos.flush();
			System.out.println("Data was sent to the other player");
		}catch(IOException e2) {
			unableToConnectWithOpponent = true;
			System.out.println("Error. Connection lost.");
		}
	}
	
	//Generates a string with all actual rotations of the grid
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
			BufferedImage hexagonGrid = ImageIO.read(getClass().getResourceAsStream("/HexagonsM.png"));
			for(int i = 0; i < ROTATIONS; i++) {
				for(int j = 0; j < COLORS*2; j++) {
					hexagonSprites[i][j] = hexagonGrid.getSubimage(SIZEHX*i, SIZEHY*j, SIZEHX, SIZEHY);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadFonts() {
		try {
		    //create the font to use with sizes
			Font fileFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/Elianto-Regular.ttf"));
		    font = fileFont.deriveFont(36f);
		    fontSmall = fileFont.deriveFont(24f);
		    fontSmallBold = fileFont.deriveFont(Font.BOLD, 24f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    //register the fonts
		    ge.registerFont(font);
		    ge.registerFont(fontSmall);
		} catch (IOException e) {
		    e.printStackTrace();
		} catch(FontFormatException e) {
		    e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Obliteration obliteration = new Obliteration();
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
		public synchronized void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {
			if(accepted) {
				if(yourTurn && !unableToConnectWithOpponent) {
					if(!start) {
						start = true;
						nextTurn();
					}
					else {
						clickX = e.getX();
						clickY = e.getY();
					}
				}
			}
		}
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
			System.out.println("Error: " + ip + ":" + port + " doesn't exists or is already in use.");
			System.exit(1);
		}catch(IOException e) {
			e.printStackTrace();
		}
		yourTurn = true;
	}
}
