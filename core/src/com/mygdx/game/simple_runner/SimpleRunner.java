package com.mygdx.game.simple_runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;


public class SimpleRunner extends ApplicationAdapter {

	SpriteBatch batch;
    Texture backgroundImage1, backgroundImage2, mainCharacterRun, mainCharacterJump,
            mainCharacterFall, fire, bird;
    TextureRegion[] mainCharacterRunTextureRegion, mainCharacterJumpTextureRegion, fireTextureRegion,
            birdTextureRegion;
	TextureRegion mainCharacterFallTextureRegion;
	ShapeRenderer mainCharacterShapeRenderer, fireShapeRenderer, birdShapeRenderer;
	Rectangle mainCharacterRectangle, fireRectangle, birdRectangle;
	BitmapFont font;
	BitmapFont.BitmapFontData fontData;
	Random random, randomBird;
	SpriteBatch fontBatch;
	Sound jumpSound, crashSound;
	int screenWidth, screenHeight, firePositionX, firePositionY,
			mainCharacterAnimationPosition, fireAnimationPosition,
			birdAnimationPosition, mainCharacterPositionY, points, backgroundPositionX, backgroundSpeed;
	double mainCharacterRunSlower, mainCharacterJumpSlower, fireSlower, birdSlower, fireCorrectionY, birdCorrectionY, randomBirdInt;
	boolean jumping, falling, gameIsRunning, gameIsOver, soundsOn;
	
	@Override
	public void create () {
        batch = new SpriteBatch();

		jumpSound = Gdx.audio.newSound(Gdx.files.internal("sound/jump.mp3"));
		crashSound = Gdx.audio.newSound(Gdx.files.internal("sound/crash.wav"));

		screenHeight = Gdx.graphics.getHeight();
		screenWidth = Gdx.graphics.getWidth();

		backgroundImage1 = new Texture("img/background-forest.png");
		backgroundImage2 = new Texture("img/background-forest.png");
		mainCharacterRun = new Texture("img/main-character-run.png");
		mainCharacterJump = new Texture("img/main-character-jump.png");
		mainCharacterFall = new Texture("img/main-character-fall.png");

		fire = new Texture("img/fire.png");
        bird = new Texture("img/bird.png");

		firePositionX = screenWidth - screenWidth / 5;
		firePositionY = (int) (screenHeight - screenHeight / 1.12);

		backgroundPositionX = 0;
		backgroundSpeed = 2;

		points = 0;

        mainCharacterAnimationPosition = 0;
		fireAnimationPosition = 0;
		birdAnimationPosition = 0;

		mainCharacterPositionY = (int) (screenHeight / 10f);
		jumping = false;
		falling = false;

		gameIsRunning = false;
		gameIsOver = false;

		fireShapeRenderer = new ShapeRenderer();
		mainCharacterShapeRenderer = new ShapeRenderer();
		birdShapeRenderer = new ShapeRenderer();

		font = new BitmapFont();
		fontBatch = new SpriteBatch();
		fontData = font.getData();
		fontData.setScale(3);

		mainCharacterRunTextureRegion = new TextureRegion[9];
		mainCharacterJumpTextureRegion = new TextureRegion[11];
		mainCharacterFallTextureRegion = new TextureRegion(mainCharacterFall);
		fireTextureRegion = new TextureRegion[8];
		birdTextureRegion = new TextureRegion[4];

		setMainCharacterRunAnimation();
		setMainCharacterJumpAnimation();

		setFairAnimation();
		setBirdAnimation();

	}

	@Override
	public void render () {

		setEnemiesYPosition();
		startPositions();

		backgroundMoving();

		mainCharacterJumping();
		mainCharacterFalling();

		fireMoving();

		gameStart();
		gameOver();

		scoring();

	}

	@Override
	public void dispose () {
		batch.dispose();
		backgroundImage1.dispose();
		backgroundImage2.dispose();
	}

    /**
     * Метод, рисующий стартовые позиции.
     */
	private void startPositions() {
		batch.begin();
		batch.draw(backgroundImage1, backgroundPositionX, 0, screenWidth, screenHeight);
		batch.draw(backgroundImage2, backgroundPositionX + screenWidth, 0, screenWidth, screenHeight);
		if (gameIsOver) {
			setMainCharacterFallPicture();
			batch.draw(mainCharacterFallTextureRegion, 0, mainCharacterPositionY,
					screenWidth / 3f, screenHeight / 2.5f);
		} else if (jumping || falling) {
			setPositionMainCharacterJumpAnimation();
			batch.draw(mainCharacterJumpTextureRegion[mainCharacterAnimationPosition], 0, mainCharacterPositionY,
					screenWidth / 3f, screenHeight / 2.5f);
		} else {
			setPositionMainCharacterRunAnimation();
			batch.draw(mainCharacterRunTextureRegion[mainCharacterAnimationPosition], 0, mainCharacterPositionY,
					screenWidth / 3f, screenHeight / 2.5f);
		}

		batch.draw(fireTextureRegion[fireAnimationPosition], firePositionX, (float) (firePositionY + fireCorrectionY),
				screenWidth / 4f, screenHeight / 3f);

        batch.draw(birdTextureRegion[birdAnimationPosition], firePositionX * 1.5f, (float) (screenHeight / 2.2f + birdCorrectionY),
                screenWidth / 5f, screenHeight / 4f);

        generateBirdType();
		setPositionFireAnimation();
		setPositionBirdAnimation();

		batch.end();
	}

    /**
     * Метод, устанавливающий позиции врагов по оси Y.
     */
	private void setEnemiesYPosition() {
		if (!gameIsRunning && !gameIsOver) {
			birdCorrectionY = screenHeight;
		}
		if (firePositionX < -screenWidth / 5) {
			random = new Random();
			if (random.nextInt(2) == 0) {
				fireCorrectionY = 0;
				birdCorrectionY = screenHeight;
			} else {
				fireCorrectionY = screenHeight;
				birdCorrectionY = 0;
			}
		}
	}

    /**
     * Метод анимации заднего фона.
     */
	private void backgroundMoving() {
		if (gameIsRunning) {
			backgroundPositionX -= backgroundSpeed;
		}
		if (backgroundPositionX + screenWidth == 0) {
			backgroundPositionX = 0;
		}
	}

    /**
     * Установка условий старта игры.
     */
	private void gameStart() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !gameIsRunning && !gameIsOver) {
			gameIsRunning = true;
			soundsOn = true;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && gameIsOver) {
			gameIsOver = false;
			mainCharacterPositionY = (int) (screenHeight / 10f);
			firePositionX = screenWidth - screenWidth / 5;
			firePositionY = (int) (screenHeight - screenHeight / 1.12);
			jumping = false;
			falling = false;
			startPositions();
			points = 0;
		}
	}

    /**
     * Метод установки позиции анимации главного персонажа при беге.
     */
	private void setPositionMainCharacterRunAnimation() {
		mainCharacterRunSlower += 0.25;
		if (mainCharacterAnimationPosition >= mainCharacterRunTextureRegion.length - 1) {
			mainCharacterAnimationPosition = 0;
		} else {
			if (mainCharacterRunSlower >= 1) {
				mainCharacterAnimationPosition++;
				mainCharacterRunSlower = 0;
			}
		}
	}

    /**
     * Метод установки позиции анимации главного персонажа при прыжке.
     */
	private void setPositionMainCharacterJumpAnimation() {
		mainCharacterJumpSlower += 0.1;
		if (mainCharacterAnimationPosition == mainCharacterJumpTextureRegion.length - 1) {
			mainCharacterAnimationPosition = 0;
		} else {
			if (mainCharacterJumpSlower >= 1) {
				mainCharacterAnimationPosition++;
				mainCharacterJumpSlower = 0;
			}
		}
	}

    /**
     * Метод подсчета и отображения набранных очков.
     */
	private void scoring() {
		if (gameIsRunning) {
			points++;
		}
		fontBatch.begin();
		font.setColor(Color.WHITE);
		font.draw(fontBatch, "Score: " + points, screenWidth / 2.6f , screenHeight - screenHeight / 9f);
		fontBatch.end();
	}

    /**
     * Метод, устанавливающий анимацию главного персонажа при беге.
     */
	private void setMainCharacterRunAnimation() {
		for (int i = 0; i < mainCharacterRunTextureRegion.length; i++) {
			mainCharacterRunTextureRegion[i] = new TextureRegion(mainCharacterRun);
			mainCharacterRunTextureRegion[i].setRegion(mainCharacterRun.getWidth() / 9 * i, 0,
					mainCharacterRun.getWidth() / 9, mainCharacterRun.getHeight());
		}
	}

    /**
     * Метод, устанавливающий анимацию главного персонажа при прыжке.
     */
	private void setMainCharacterJumpAnimation() {
		for (int i = 0; i < mainCharacterJumpTextureRegion.length; i++) {
			mainCharacterJumpTextureRegion[i] = new TextureRegion(mainCharacterJump);
			mainCharacterJumpTextureRegion[i].setRegion(mainCharacterJump.getWidth() / 11 * i, 0,
					mainCharacterJump.getWidth() / 11, mainCharacterJump.getHeight());
		}
	}

    /**
     * Метод, устанавливающий изображение главного персонажа при падении.
     */
	private void setMainCharacterFallPicture() {
		mainCharacterFallTextureRegion.setRegion(mainCharacterFall.getWidth() / 2, 0,
				mainCharacterFall.getWidth() / 2, mainCharacterFall.getHeight());
	}

    /**
     * Метод, настаивающий прыжок главного персонажа.
     */
	private void mainCharacterJumping() {
		if (gameIsRunning) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
				if (mainCharacterPositionY == (int) (screenHeight / 10f)) {
					jumping = true;
					jumpSound.play();
				}
			}
			if (jumping && mainCharacterPositionY < screenHeight / 2f) {
				mainCharacterPositionY += screenWidth / 130;
			}
			if (mainCharacterPositionY >= screenHeight / 2f) {
				jumping = false;
				falling = true;
			}
			if (falling) {
				mainCharacterPositionY -= screenWidth / 130;
			}
			if (mainCharacterPositionY == (int) (screenHeight / 10f)) {
				jumping = false;
				falling = false;
			}
		}
	}

    /**
     * Метод, настаивающий падение главного персонажа.
     */
	public void mainCharacterFalling() {
		if (gameIsOver && mainCharacterPositionY >= (int) (screenHeight / 10f)) {
			mainCharacterPositionY = mainCharacterPositionY - 6;
		}
	}

    /**
     * Метод, устанавливающий движение огня.
     */
	public void fireMoving() {
		if (gameIsRunning) {
			if (firePositionX < -screenWidth / 5) {
				firePositionX = screenWidth;
			}
			firePositionX = firePositionX - screenWidth / 110;
		}
	}

    /**
     * Метод установки позиции анимации огня.
     */
	private void setPositionFireAnimation() {
		fireSlower += 0.09;
		if (fireAnimationPosition == fireTextureRegion.length - 1) {
			fireAnimationPosition = 0;
		} else {
			if(fireSlower >= 1) {
				fireAnimationPosition++;
				fireSlower = 0;
			}
		}
	}

    /**
     * Метод, устанавливающий анимацию огня.
     */
	private void setFairAnimation() {
		for (int i = 0; i < fireTextureRegion.length; i++) {
			fireTextureRegion[i] = new TextureRegion(fire);
			fireTextureRegion[i].setRegion(fire.getWidth() / 8 * i, 0, fire.getWidth() / 8, fire.getHeight());
		}
	}

    /**
     * Метод установки позиции анимации птицы.
     */
	private void setPositionBirdAnimation() {
		birdSlower += 0.05;
		if (birdAnimationPosition == birdTextureRegion.length - 1) {
			birdAnimationPosition = 0;
		} else {
			if(birdSlower >= 1) {
				birdAnimationPosition++;
				birdSlower = 0;
			}
		}
	}

    /**
     * Метод, устанавливающий анимацию птицы.
     */
	private void setBirdAnimation() {
		for (int i = 0; i < birdTextureRegion.length; i++) {
			birdTextureRegion[i] = new TextureRegion(bird);
			birdTextureRegion[i].setRegion(bird.getWidth() / 4 * i, 0, bird.getWidth() / 4, bird.getHeight());
		}
	}

    /**
     * Метод генерации движения птицы (при наборе 2500 очков птица может лететь по верху, либо опускаться).
     */
    private void generateBirdType() {
        if (points > 2500) {
            if (firePositionX < -screenWidth / 5) {
                randomBird = new Random();
                randomBirdInt = randomBird.nextInt(2);
            }
            if (randomBird != null && randomBirdInt == 0 && !gameIsOver) {
                birdCorrectionY--;
            }
        }
    }

    /**
     * Настройка условий при которых игра окончена.
     */
	private void gameOver() {

//		gameOverVisualTest();

		mainCharacterRectangle = new Rectangle(screenWidth / 10f, mainCharacterPositionY + screenWidth / 33f, screenWidth / 5f, screenHeight / 8f);
		fireRectangle = new Rectangle(firePositionX + screenWidth / 15f, (float) (firePositionY + fireCorrectionY), screenWidth / 9f, screenHeight / 5f);
		birdRectangle = new Rectangle(firePositionX * 1.5f + screenWidth / 20f, (float) (screenHeight / 2.15f + birdCorrectionY), screenWidth / 6f, screenHeight / 7f);

		if (Intersector.overlaps(mainCharacterRectangle, fireRectangle)
				|| Intersector.overlaps(mainCharacterRectangle, birdRectangle)) {
			gameIsRunning = false;
			gameIsOver = true;
			if (soundsOn) {
				crashSound.play();
			}
			soundsOn = false;
		}
	}

    /**
     * Метод визуального теста игры
     * (при установке его в методе gameOver() будут видны фигуры персонажей,
     * в случае пересечений которых игра будет окончена).
     */
	public void gameOverVisualTest() {
		fireShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		fireShapeRenderer.setColor(1, 0, 0, 1);
		fireShapeRenderer.rect(firePositionX + screenWidth / 15f, (float) (firePositionY + fireCorrectionY),
				screenWidth / 9f, screenHeight / 5f);
		fireShapeRenderer.end();

		mainCharacterShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		mainCharacterShapeRenderer.setColor(1, 0, 0, 1);
		mainCharacterShapeRenderer.rect(screenWidth / 10f, mainCharacterPositionY + screenWidth / 33f,
				screenWidth / 5f, screenHeight / 8f);
		mainCharacterShapeRenderer.end();

		birdShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		birdShapeRenderer.setColor(1, 0, 0, 1);
		birdShapeRenderer.rect(firePositionX * 1.5f + screenWidth / 20f, (float) (screenHeight / 2.15f + birdCorrectionY),
				screenWidth / 6f, screenHeight / 7f);
		birdShapeRenderer.end();
	}
}


