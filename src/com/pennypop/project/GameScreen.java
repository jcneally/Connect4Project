package com.pennypop.project;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class GameScreen implements Screen {
	
	// UI Objects
	private Stage stage;
	private Table wrapper;
	private Table gameWrapper;
	private Table textWrapper;
	
	// Asset controllers
	private AssetManager manager;
	
	// Assets
	private Texture boxTexture;
	private Texture player1Texture;
	private Texture player2Texture;
	private BitmapFont pennyPopFont;

	private Game game;
	
	private boolean loaded = false;					// Whether the screen has properly loaded
	private boolean gameOver = false;
	private boolean useAI = true;
	
	// Game details
	private int rows = 6;
	private int cols = 7;
	private int sequenceToWin = 4;
	
	// Two dimensional visual grid representation
	private Square[][] boxes;
	
	private ArrayList<Point> validMoves;
	
	enum PlayerTurn {
		PLAYER_ONE,
		PLAYER_TWO
	}
	
	enum AiLevel {
		SIMPLE,
		COMPLEX
	}
	
	private PlayerTurn currentTurn = PlayerTurn.PLAYER_ONE;
	
	private AiLevel intelligence = AiLevel.SIMPLE;

	/**
	 * This class allows an object representation of a square, for details about which player
	 * occupies the space, and its graphical details.
	 * 
	 * @author James Neally
	 *
	 */
	public class Square {
		public Group group;
		public PlayerTurn player;
		
		public Square() {
			group = new Group();
		}
	}

	public GameScreen(Game g) {
		this.game = g;
		
		stage = new Stage();
		wrapper = new Table();
		gameWrapper = new Table();
		textWrapper = new Table();
		wrapper.setFillParent(true);
		wrapper.add(gameWrapper).center();
		wrapper.add(textWrapper).center().right().pad(30);
		stage.addActor(wrapper);
		
		validMoves = new ArrayList<Point>();
		
		// The initial valid moves are the bottom row
		for(int i = 0; i < cols; i++) {
			validMoves.add(new Point(rows - 1, i));
		}

	    manager = new AssetManager();
	    
		manager.load("assets/box.png", Texture.class);
		manager.load("assets/yellow.png", Texture.class);
		manager.load("assets/red.png", Texture.class);
		manager.load("assets/font.fnt", BitmapFont.class);
		
	    boxes = new Square[rows][cols];
	}
	
	// Run when the screen assets are ready
	public void init() {
	    for(int i = 0; i < rows; i++) {
	    	for(int j = 0; j < cols; j++) {
	    		boxes[i][j] = new Square();
	    		
	    		gameWrapper.add(boxes[i][j].group).width(100).height(100).center();
	    		
	    		TextureRegion btnRegion = new TextureRegion(boxTexture);
	    		
	    		ImageButtonStyle btnStyle = new ImageButtonStyle();
	    		btnStyle.imageUp = new TextureRegionDrawable(btnRegion);
	    		Button btn = new Button(new Image(btnRegion), btnStyle);
	    		btn.addListener(new TakeTurnListener(new Point(i, j)));
	    		boxes[i][j].group.addActor(btn);
	    	}
	    	gameWrapper.row();
	    }
		
	}
	
	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	public void render(float delta) {
		if(!loaded && manager.update()) {
			pennyPopFont = manager.get("assets/font.fnt", BitmapFont.class);
			boxTexture = manager.get("assets/box.png", Texture.class);
			player1Texture = manager.get("assets/red.png", Texture.class);
			player2Texture = manager.get("assets/yellow.png", Texture.class);
			
			init();
			
			loaded = true;
	      }
		
		// Use AI if applicable
		if(!gameOver && useAI && currentTurn == PlayerTurn.PLAYER_TWO) {
			aiTurn();
		}
		
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, false);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}
	
	/**
	 * Simple AI will choose a random valid move.
	 */
	private void aiTurn() {
		if(intelligence == AiLevel.SIMPLE) {
			Point selection = validMoves.get(new Random().nextInt(validMoves.size()));
			setGamePiece(selection);
		}
	}
	
	/**
	 * Checks whether the point is in the list of valid moves
	 * 
	 * @param p The point to check if valid
	 * @return true if move is valid, false otherwise
	 */
	private boolean isValidMove(Point p) {
		if(validMoves.contains(p)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Places a new game piece at the specified point and checks win condition.
	 * 
	 * @param p Point to set the current player's game piece
	 */
	private void setGamePiece(Point p) {
		Image im;
		if(currentTurn == PlayerTurn.PLAYER_ONE) {
    		im = new Image(player1Texture);
		} else {
			im = new Image(player2Texture);
		}
		im.setPosition(50 - im.getWidth()/2, 50 - im.getHeight()/2);
		boxes[p.x][p.y].group.addActor(im);
		boxes[p.x][p.y].player = currentTurn;
		
		// Check win condition
		if(isWinner(p)) {
			gameOver = true;
			
			String winText = "";
			winText += (currentTurn == PlayerTurn.PLAYER_ONE) ? "Player One " : "Player Two ";
			winText += "is the winner!!";
			Label.LabelStyle pennyPopTextStyle = new Label.LabelStyle(pennyPopFont, Color.RED);
			Label winnerText = new Label(winText, pennyPopTextStyle);
			textWrapper.add(winnerText).top();
			textWrapper.row();
			
			TextButton.TextButtonStyle returnToMenuStyle = new TextButton.TextButtonStyle();
			returnToMenuStyle.font = pennyPopFont;
			returnToMenuStyle.fontColor = Color.BLUE;
			
			TextButton returnToMenuBtn = new TextButton("Return To Main Menu", returnToMenuStyle);
			returnToMenuBtn.addListener(new ChangeListener() {
				public void changed (ChangeEvent event, Actor actor) {
					game.setScreen(new MainScreen(game));
				}
			});
			
			textWrapper.add(returnToMenuBtn).top();
		} else {
		
			// Remove the point specified and if the above position is valid, add it
			validMoves.remove(p);
			if(p.x - 1 >= 0) {
				validMoves.add(new Point(p.x - 1, p.y));
			}
        	
			changeTurns();
		}
	}

	/**
	 * Moves the current turn to next player.
	 */
	private void changeTurns() {
		if(currentTurn == PlayerTurn.PLAYER_ONE) {
			currentTurn = PlayerTurn.PLAYER_TWO;
		} else {
			currentTurn = PlayerTurn.PLAYER_ONE;
		}
	}
	
	/**
	 * Checks in the 8 possible directions whether the specific play resulted in a win.
	 * 
	 * @param p Point of last play
	 * @return true if winner based on win conditions, false otherwise
	 */
	private boolean isWinner(Point p) {
		PlayerTurn player = boxes[p.x][p.y].player;
		
		ArrayList<Point> directions = new ArrayList<Point>();
		directions.add(new Point(0, 1));
		directions.add(new Point(1, 1));
		directions.add(new Point(1, 0));
		directions.add(new Point(1, -1));
		directions.add(new Point(0, -1));
		directions.add(new Point(-1, -1));
		directions.add(new Point(-1, 0));
		directions.add(new Point(-1, -1));
		
		for(Point dir : directions) {
			if(isWinnerHelper(p, dir, player) >= sequenceToWin) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Recursive helper method for win check. Returns max sequence based on the specified direction.
	 * 
	 * @param currentPoint The point to check
	 * @param direction Which direction the check is heading (in 2D vector format)
	 * @param player The player that last played
	 * @return number of sequential plays by the player in the direction
	 */
	private int isWinnerHelper(Point currentPoint, Point direction, PlayerTurn player) {
		// Base case: went out of bounds or hit other player's chip
		if(outOfBounds(currentPoint) || boxes[currentPoint.x][currentPoint.y].player != player) {
			return 0;
		}
		
		// increment and continue in direction
		return 1 + isWinnerHelper(new Point(currentPoint.x + direction.x, currentPoint.y + direction.y), direction, player);
	}
	
	/**
	 * Checks out of bounds condition
	 * 
	 * @param p Point to check
	 * @return true if out of bounds, false otherwise.
	 */
	private boolean outOfBounds(Point p) {
		return p.x < 0 || p.x > rows - 1 || p.y < 0 || p.y > cols - 1;
	}
	
	
	/**
	 * Custom listener class on the squares.
	 * 
	 * @author James Neally
	 *
	 */
	public class TakeTurnListener extends InputListener {
		private Point point;
		
		public TakeTurnListener(Point p) {
			this.point = p;
		}
		
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        if(!gameOver && isValidMove(point)) {
	        	setGamePiece(point);
	        }
	        return true;
	    }
	}
}
