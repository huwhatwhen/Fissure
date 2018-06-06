package com.gmail.studios.co.fiish.fissure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.math.BigDecimal;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class FissureGameScreen extends ScreenAdapter {
    private Viewport mViewport;
    private SpriteBatch mBatch;
    private Preferences mData;

    private FissureTitleUI mTitleUI;
    private FissureLogo mLogo;
    private TapPrompt mPrompt;

    private FissureWorld mWorld;
    private Miner mMiner;
    private Array<Tile> mTiles;
    private Array<Integer> mIntegers;
    private int mBreakCount;

    private FissureGameUI mGameUI;
    private GameOverPrompt mGameOverPrompt;
    private ScoreBG mScoreBG;
    private ReplayButton mReplay;
    private HomeButton mHome;

    private FreeTypeFontGenerator mGenerator;
    private FreeTypeFontParameter mParam;
    private BitmapFont mFont;
    private GlyphLayout mLayout;
    private BigDecimal mScore;
    private final float m_DELTA_FISSURE = 2.3f;
    private float mElapsedTime, mPixelX, mPixelY;
    @Override
    public void show() {
        mViewport = new ScreenViewport();
        mData = Gdx.app.getPreferences("Data");

        mMiner = new Miner(mViewport);
        mTiles = new Array<Tile>(true, 144);
        mLogo = new FissureLogo(mViewport);
        mPrompt = new TapPrompt(mViewport);
        mIntegers = new Array<Integer>(true, 144);
        for (int i = 0; i < 144; i++) mIntegers.add(new Integer(i));
        for (int i = 0; i < 144; i++) mTiles.add(new Tile(mViewport, i));

        mGameOverPrompt = new GameOverPrompt(mViewport);
        mScoreBG = new ScoreBG(mViewport);
        mReplay = new ReplayButton(mViewport);
        mHome = new HomeButton(mViewport);

        mBatch = new SpriteBatch();

        mTitleUI = new FissureTitleUI(mViewport, mBatch);
        mWorld = new FissureWorld(mViewport, mBatch);
        mGameUI = new FissureGameUI(mViewport, mBatch);

        mWorld.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                mMiner.clearActions();
                mMiner.addAction(moveTo(x - mMiner.getWidth() / 2, y, (Math.abs(x - mMiner.getX()) + Math.abs(y - mMiner.getY())) * 0.0009f));
                return true;
            }
        });

        mGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
        mParam = new FreeTypeFontParameter();
        mLayout = new GlyphLayout();

        Gdx.gl.glClearColor(0, 0, 0, 1);

        Gdx.input.setInputProcessor(mTitleUI);
    }

    @Override
    public void resize(int width, int height) {
        mViewport.update(width, height);

        mPixelX = mViewport.getScreenWidth() / 16f / 32f;
        mPixelY = mViewport.getScreenHeight() / 9f / 32f;

        mMiner.init();

        for (int i = 0; i < mTiles.size; i++){
            mTiles.get(i).init();
            mWorld.addActor(mTiles.get(i));
        }
        mGameOverPrompt.init();
        mLogo.init();
        mPrompt.init();

        mTitleUI.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                mLogo.addAction(sequence(
                        run(new Runnable() {
                            @Override
                            public void run() {
                                mPrompt.clearActions();
                                mPrompt.addAction(fadeOut(0.3f));
                                mLogo.addAction(fadeOut(0.3f));
                            }
                        }),
                        delay(0.2f, run(new Runnable() {
                            @Override
                            public void run() {
                                Gdx.input.setInputProcessor(mWorld);
                            }
                        }))));
                return true;
            }
        });


        mScoreBG.init();
        mReplay.init();
        mHome.init();

        mReplay.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                mScoreBG.setTouchable(Touchable.disabled);
                mReplay.setTouchable(Touchable.disabled);
                mHome.setTouchable(Touchable.disabled);

                mReplay.addAction(sequence(moveTo(mReplay.getX(), mReplay.getY() - 3f * mPixelY, 0.1f),
                        moveTo(mReplay.getX(), mReplay.getY() + 3f * mPixelY, 0.1f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                mGameOverPrompt.addAction(sequence(fadeOut(0.25f), moveTo(mGameOverPrompt.getX(), mGameOverPrompt.getY() + 16f * mPixelY, 0.25f)));
                                mReplay.addAction(fadeOut(0.2f));
                                mHome.addAction(fadeOut(0.2f));
                                mScoreBG.addAction(delay(0.2f, sequence(moveTo(mScoreBG.getX(), mViewport.getScreenHeight() + 10, 0.15f),
                                        Actions.run(new Runnable() {
                                            @Override
                                            public void run() {
                                                resetGame();
                                            }
                                        }))));
                            }
                        })));

                return  true;
            }
        });

        mHome.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                mScoreBG.setTouchable(Touchable.disabled);
                mReplay.setTouchable(Touchable.disabled);
                mHome.setTouchable(Touchable.disabled);

                mHome.addAction(sequence(moveTo(mHome.getX(), mHome.getY() - 3f * mPixelY, 0.1f),
                        moveTo(mHome.getX(), mHome.getY() + 3f * mPixelY, 0.1f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                mGameOverPrompt.addAction(sequence(fadeOut(0.25f), moveTo(mGameOverPrompt.getX(), mGameOverPrompt.getY() + 16f * mPixelY, 0.25f)));
                                mHome.addAction(fadeOut(0.2f));
                                mReplay.addAction(fadeOut(0.2f));
                                mScoreBG.addAction(delay(0.2f, sequence(moveTo(mScoreBG.getX(), mViewport.getScreenHeight() + 10, 0.15f),
                                        Actions.run(new Runnable() {
                                            @Override
                                            public void run() {
                                                backToHome();
                                            }
                                        }))));
                            }
                        })));
                return  true;
            }
        });

        mTitleUI.addActor(mLogo);
        mTitleUI.addActor(mPrompt);

        mWorld.addActor(mMiner);

        mGameUI.addActor(mGameOverPrompt);
        mGameUI.addActor(mScoreBG);
        mGameUI.addActor(mReplay);
        mGameUI.addActor(mHome);

        mParam.size = (int) (0.65 * (mViewport.getScreenHeight() / 9));
        mParam.color = Color.WHITE;
        mFont = mGenerator.generateFont(mParam);

        mElapsedTime = 0;
        mBreakCount = 0;

        Gdx.input.setInputProcessor(mTitleUI);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.getInputProcessor().equals(mWorld)) {
            mElapsedTime += delta;
        }
        mViewport.apply(true);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (Gdx.input.getInputProcessor().equals(mWorld) && mMiner.isAlive && mElapsedTime % m_DELTA_FISSURE < delta) {
            mIntegers.shuffle();
            if (mBreakCount < 2) {
                for (int i = 0; i < 60; i++) {
                    mTiles.get(mIntegers.get(i)).breakTile();
                }
            } else if (mBreakCount < 3) {
                for (int i = 0; i < 72; i++) {
                    mTiles.get(mIntegers.get(i)).breakTile();
                }
            } else if (mBreakCount < 4) {
                for (int i = 0; i < 84; i++) {
                    mTiles.get(mIntegers.get(i)).breakTile();
                }
            } else if (mBreakCount < 6) {
                for (int i = 0; i < 96; i++) {
                    mTiles.get(mIntegers.get(i)).breakTile();
                }
            } else if (mBreakCount < 8) {
                for (int i = 0; i < 108; i++) {
                    mTiles.get(mIntegers.get(i)).breakTile();
                }
            } else if (mBreakCount < 9) {
                for (int i = 0; i < 120; i++) {
                    mTiles.get(mIntegers.get(i)).breakTile();
                }
            } else {
                for (int i = 0; i < 132; i++) {
                    mTiles.get(mIntegers.get(i)).breakTile();
                }
            }
            mBreakCount++;
        }

        if (mMiner.isAlive || (!mMiner.isAlive && !mMiner.isDeathDone)) {
            mWorld.act(delta);
        }

        if (!mMiner.isAlive && !mTiles.get(143).isFrozen) {
            for (int i = 0; i < mTiles.size; i++) mTiles.get(i).toggleFreeze();
        }

        mWorld.draw();

        if (Gdx.input.getInputProcessor().equals(mGameUI) && !mMiner.isAlive) {
            mGameUI.act();
            mGameUI.draw();
        }

        if (Gdx.input.getInputProcessor().equals(mTitleUI)) {
            mTitleUI.act();
            mTitleUI.draw();
        }

        if (Gdx.input.getInputProcessor().equals(mWorld) && mMiner.isAlive) {
            mScore = round(mElapsedTime, 2);
        }

        if (!Gdx.input.getInputProcessor().equals(mTitleUI) && mMiner.isAlive || (!mMiner.isAlive && !mMiner.isDeathDone)) {
            mBatch.begin();
            mLayout.setText(mFont, "" + mScore);
            mFont.draw(mBatch, mLayout, mViewport.getScreenWidth() - 10 - mLayout.width, mViewport.getScreenHeight() - 10 - mLayout.height / 3);
            mBatch.end();
        }

        mMiner.checkSafe(mTiles);

       if (!mMiner.isAlive && mMiner.isDeathDone && !Gdx.input.getInputProcessor().equals(mGameUI)) {
           Gdx.input.setInputProcessor(mGameUI);

           mScoreBG.mScore = mScore;

           if (mScore.setScale(2, BigDecimal.ROUND_UP).floatValue() > mData.getFloat("highScore")) {
               mData.putFloat("highScore", mScore.setScale(2, BigDecimal.ROUND_UP).floatValue());
               mData.flush();
           }

           mScoreBG.updateHighScore();

           mGameOverPrompt.addAction(sequence(fadeIn(0.25f), moveTo(mGameOverPrompt.getX(), mGameOverPrompt.getY() - 16f * mPixelY, 0.25f)));

           mScoreBG.addAction(delay(0.25f,moveTo(mViewport.getScreenWidth() / 2 - mScoreBG.getWidth() / 2, mViewport.getScreenHeight() / 2 - mScoreBG.getHeight() / 2,
                   0.25f)));

           mReplay.addAction(delay(0.5f, sequence(fadeIn(0.2f),
                   Actions.run(new Runnable() {
                       @Override
                       public void run() {
                           mReplay.setTouchable(Touchable.enabled);
                           mHome.setTouchable(Touchable.enabled);
                       }
                   }))));

           mHome.addAction(delay(0.5f, sequence(fadeIn(0.2f))));
       }
    }

    @Override
    public void dispose() {
        mLogo.dispose();
        mPrompt.dispose();
        mTitleUI.dispose();
        mMiner.dispose();
        for (int i = 0; i < mTiles.size; i++) mTiles.get(i).dispose();
        mWorld.dispose();
        mGameOverPrompt.dispose();
        mScoreBG.dispose();
        mReplay.dispose();
        mHome.dispose();
        mGameUI.dispose();
        mGenerator.dispose();
        mFont.dispose();
        mBatch.dispose();
    }

    public void resetGame() {
        for (int i = 0; i < mTiles.size; i++) mTiles.get(i).reset();
        mMiner.reset();

        mHome.reset();
        mReplay.reset();

        mGameOverPrompt.reset();
        mScoreBG.reset();

        mElapsedTime = 0;
        mBreakCount = 0;

        Gdx.input.setInputProcessor(mWorld);
    }

    private void backToHome() {
        mLogo.reset();
        mPrompt.reset();

        for (int i = 0; i < mTiles.size; i++) mTiles.get(i).reset();
        mMiner.reset();

        mHome.reset();
        mReplay.reset();

        mGameOverPrompt.reset();
        mScoreBG.reset();

        mElapsedTime = 0;
        mBreakCount = 0;

        Gdx.input.setInputProcessor(mTitleUI);
    }

    private BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

}
