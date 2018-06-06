package com.gmail.studios.co.fiish.fissure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.math.BigDecimal;

public class ScoreBG extends Actor {
    private Viewport mViewport;
    public BigDecimal mScore, mHighScore;
    private Preferences mData;

    private float mPixelX, mPixelY;

    private Texture mTexture;

    private Animation<TextureRegion> mGoldAnimation, mSilverAnimation;
    private TextureAtlas mGoldAtlas, mSilverAtlas;

    private FreeTypeFontGenerator mGenerator;
    private FreeTypeFontGenerator.FreeTypeFontParameter mParam;
    private BitmapFont mFont;
    private GlyphLayout mLayout;

    private float mElapsedTime;

    public ScoreBG(Viewport viewport) {
        this.mViewport = viewport;
        mData = Gdx.app.getPreferences("Data");

        mTexture = new Texture(Gdx.files.internal("scorebg.png"));
        mGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));

        mGoldAtlas = new TextureAtlas(Gdx.files.internal("spritesheets/goldsheet.atlas"));
        mGoldAnimation = new Animation(1f/6f, mGoldAtlas.getRegions());

        mSilverAtlas = new TextureAtlas(Gdx.files.internal("spritesheets/silversheet.atlas"));
        mSilverAnimation = new Animation(1f/6f, mSilverAtlas.getRegions());

        mHighScore = new BigDecimal(0);

        setTouchable(Touchable.disabled);
    }

    public void init() {
        this.setWidth(mViewport.getScreenWidth() / 16 * 6);
        this.setHeight(mViewport.getScreenHeight() / 9 * 4);

        this.setX(mViewport.getScreenWidth() / 2 - this.getWidth() / 2);
        this.setY(mViewport.getScreenHeight() + 10);

        this.setBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());

        this.setTouchable(Touchable.disabled);

        mParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        mLayout = new GlyphLayout();
        mParam.size = (int) (mViewport.getScreenHeight() / 17.0f);
        mParam.color = Color.WHITE;
        mFont = mGenerator.generateFont(mParam);

        mElapsedTime = 0.0f;

        mPixelX = mViewport.getScreenWidth() / 16.0f / 32.0f;
        mPixelY = mViewport.getScreenHeight() / 9.0f / 32.0f;
    }

    public void reset() {
        this.clearActions();
        this.setX(mViewport.getScreenWidth() / 2 - this.getWidth() / 2);
        this.setY(mViewport.getScreenHeight() + 10);

        mElapsedTime = 0.0f;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        mElapsedTime += delta;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * alpha);
        batch.draw(mTexture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        if (mScore != null && mScore.floatValue() >= 45.0f) {
            batch.draw(mGoldAnimation.getKeyFrame(mElapsedTime, true),
                    this.getX() + 15.0f * mPixelX, this.getY() + 17.0f * mPixelY, 63.0f * mPixelX, 61.0f * mPixelY);
        } else if (mScore != null && mScore.floatValue() >= 30.0f) {
            batch.draw(mSilverAnimation.getKeyFrame(mElapsedTime, true),
                    this.getX() + 15.0f * mPixelX, this.getY() + 17.0f * mPixelY, 63.0f * mPixelX, 61.0f * mPixelY);
        }

        mLayout.setText(mFont, "" + mScore);
        mFont.draw(batch, mLayout, getX() + getWidth() * 0.928f - mLayout.width, getY() + getHeight() * 0.735f - mLayout.height);

        mLayout.setText(mFont, "" + mHighScore.setScale(2, BigDecimal.ROUND_HALF_UP));
        mFont.draw(batch, mLayout, getX() + getWidth() * 0.928f - mLayout.width, getY() + getHeight() * 0.34f - mLayout.height);

    }

    public void dispose() {
        mTexture.dispose();
        mGenerator.dispose();
        mGoldAtlas.dispose();
        mSilverAtlas.dispose();
        mFont.dispose();
    }

    public void updateHighScore() {
        mHighScore = new BigDecimal(mData.getFloat("highScore"));
    }
}
