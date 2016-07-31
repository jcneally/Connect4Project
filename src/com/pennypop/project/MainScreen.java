package com.pennypop.project;

import org.json.JSONObject;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.openal.Wav.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * This is where your screen code will go, any UI should be in here
 * 
 * @author Richard Taylor, James Neally
 */
public class MainScreen implements Screen {
	
	// UI Objects
	private Stage stage;
	private Table wrapper;
	private Table menuTable;
	private Table apiTable;
	private Table apiTableInnerWrapper;
	
	// Asset Controllers
	private AssetManager manager;
	private SpriteBatch batch;
	
	// Assets
	private BitmapFont pennyPopFont;
	private Texture sfxBtnTexture;
	private Texture apiBtnTexture;
	private Texture gameBtnTexture;
	
	// UI Widgets
	private Label pennyPopText;
	private Button sfxButton;
	private Button apiButton;
	private Button gameButton;
	
	private final String WEATHER_API_URL = "http://api.openweathermap.org/data/2.5/weather?q=San%20Francisco,US&appid=2e32d2b4b825464ec8c677a49531e9ae";
	
	private Game game;
	
	// Whether the screen has been properly loaded
	private boolean loaded = false;
	
	public MainScreen(Game game) {
		this.game = game;
		
		batch = new SpriteBatch();
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, batch);
		
		wrapper = new Table();
		menuTable = new Table();
		apiTable = new Table();
		
		wrapper.setFillParent(true);
		
		wrapper.add(menuTable).width(400);
		wrapper.add(apiTable).width(400);
	    stage.addActor(wrapper);
		
	    manager = new AssetManager();
	    
	    // Set our assets to begin loading
		manager.load("assets/font.fnt", BitmapFont.class);
		manager.load("assets/sfxButton.png", Texture.class);
		manager.load("assets/apiButton.png", Texture.class);
		manager.load("assets/gameButton.png", Texture.class);
		
	}
	
	/**
	 * This method is called when the AssetManager is ready to set up the screen
	 */
	private void init() {

		// PennyPop Text
		Label.LabelStyle pennyPopTextStyle = new Label.LabelStyle(pennyPopFont, Color.RED);
		pennyPopText = new Label("PennyPop", pennyPopTextStyle);
		
		// SFX ImageButton
		TextureRegion sfxBtnRegion = new TextureRegion(sfxBtnTexture);
		
		ImageButtonStyle sfxBtnStyle = new ImageButtonStyle();
		sfxBtnStyle.imageUp = new TextureRegionDrawable(sfxBtnRegion);
		sfxButton = new Button(new Image(sfxBtnRegion), sfxBtnStyle);

		// API ImageButton
		TextureRegion apiBtnRegion = new TextureRegion(apiBtnTexture);
		
		ImageButtonStyle apiBtnStyle = new ImageButtonStyle();
		apiBtnStyle.imageUp = new TextureRegionDrawable(apiBtnRegion);
		apiButton = new Button(new Image(apiBtnRegion), apiBtnStyle);
		
		// Game Button
		TextureRegion gameBtnRegion = new TextureRegion(gameBtnTexture);
		
		ImageButtonStyle gameBtnStyle = new ImageButtonStyle();
		gameBtnStyle.imageUp = new TextureRegionDrawable(gameBtnRegion);
		gameButton = new Button(new Image(gameBtnRegion), gameBtnStyle);

		// Set up leftside menu
		menuTable.add(pennyPopText).center();
		menuTable.row();
		Table buttonRowTable = new Table();
		buttonRowTable.add(sfxButton).center().pad(10);
		buttonRowTable.add(apiButton).center().pad(10);
		buttonRowTable.add(gameButton).center().pad(10);
		menuTable.add(buttonRowTable);
	    
	    sfxButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				Sound sound = (Sound) Gdx.audio.newSound(Gdx.files.internal("assets/button_click.wav"));
				sound.play(1.0f);
			}
		});
	    
	    // If api has not been called, display info. Otherwise already called, toggle off.
	    apiButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				if(apiTableInnerWrapper == null) {
					apiTableInnerWrapper = new Table();
					try {
						JSONObject json = JsonReader.readJsonFromUrl(WEATHER_API_URL);
						
						// Title label
						Label.LabelStyle apiTitleSyle = new Label.LabelStyle(pennyPopFont, new Color(0.5f, 0f, 0f, 1f));
						Label apiTitle = new Label("Current Weather", apiTitleSyle);
						
						// Weather Location
						Label.LabelStyle apiLocationStyle = new Label.LabelStyle(pennyPopFont, Color.BLUE);
						Label apiLocation = new Label(json.get("name").toString(), apiLocationStyle);
						
						// Weather short description
						Label.LabelStyle apiWeatherStyle = new Label.LabelStyle(pennyPopFont, Color.RED);
						Label apiWeather = new Label("Sky is " + json.getJSONArray("weather").getJSONObject(0).get("main").toString(), apiWeatherStyle);
						
						// Temp appears in kelvin, use F instead
						float temp_kelvin = Float.parseFloat(json.getJSONObject("main").get("temp").toString());
						float temp_fahrenheit = temp_kelvin * 9f / 5f - 459.97f;
						
						// Weather additional details
						Label.LabelStyle apiWeatherDetailsStyle = new Label.LabelStyle(pennyPopFont, Color.RED);
						Label apiWeatherDetails = new Label(String.format("%.2f", temp_fahrenheit) + " degrees, " + json.getJSONObject("wind").get("speed").toString() + "mph wind", apiWeatherDetailsStyle);
						apiWeatherDetails.setFontScale(0.5f);
					    
						apiTableInnerWrapper.add(apiTitle).center();
						apiTableInnerWrapper.row();
						apiTableInnerWrapper.add(apiLocation).center();
						apiTableInnerWrapper.row();
						apiTableInnerWrapper.add(apiWeather).center().padTop(30);
						apiTableInnerWrapper.row();
						apiTableInnerWrapper.add(apiWeatherDetails).center();
						apiTable.add(apiTableInnerWrapper);
						
					} catch(Exception e) {
						System.out.println("There was an error reading from the API.");
						e.printStackTrace();
					}
				} else {
					apiTable.clear();
					apiTableInnerWrapper = null;
				}
			}
		});
	    
	    gameButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				game.setScreen(new GameScreen(game));
			}
		});
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	public void render(float delta) {
		if(!loaded && manager.update()) {
			pennyPopFont = manager.get("assets/font.fnt", BitmapFont.class);
			sfxBtnTexture = manager.get("assets/sfxButton.png", Texture.class);
			apiBtnTexture = manager.get("assets/apiButton.png", Texture.class);
			gameBtnTexture = manager.get("assets/gameButton.png", Texture.class);
			
			init();
			
			loaded = true;
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
		// Irrelevant on desktop, ignore this
	}

	@Override
	public void resume() {
		// Irrelevant on desktop, ignore this
	}

}
