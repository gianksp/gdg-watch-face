package wear.gianksp.com.gdgwatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Gravity;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GDGFace extends CanvasWatchFaceService {

    /**
     * Engine creation and instantiation
     * @return
     */
    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    //Identify order
    private static int IT       = 1 << 0;
    private static int IS       = 1 << 1;
    private static int HALF     = 1 << 2;
    private static int TEN_H    = 1 << 3;
    private static int QUARTER  = 1 << 4;
    private static int TWENTY_H = 1 << 5;
    private static int FIVE_H   = 1 << 6;
    private static int MINUTES  = 1 << 7;
    private static int TO       = 1 << 8;
    private static int PAST     = 1 << 9;
    private static int ONE      = 1 << 10;
    private static int THREE    = 1 << 11;
    private static int TWO      = 1 << 12;
    private static int FOUR     = 1 << 13;
    private static int FIVE     = 1 << 14;
    private static int SIX      = 1 << 15;
    private static int SEVEN    = 1 << 16;
    private static int EIGHT    = 1 << 17;
    private static int NINE     = 1 << 18;
    private static int TEN      = 1 << 19;
    private static int ELEVEN   = 1 << 2;
    private static int TWELVE   = 1 << 21;
    private static int AM       = 1 << 22;
    private static int PM       = 1 << 23;

    private class Engine extends CanvasWatchFaceService.Engine {
        private static final int MSG_UPDATE_TIME = 1;

        private Time mTime;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private Random randomGenerator;
        private List<Integer> colors;
        private Bitmap mBackgroundBitmap;
        private Paint enabledPaint;
        private Paint disabledPaint;
        private int mTextWidth, mTextHeight;
        private double mLineHeight;
        boolean mRegisteredTimeZoneReceiver = false;

        String[] words = Word.getWords();
        String[] lines = Word.getLines();
        int[] xCoords  = Word.getCoordinatesX();
        int[] yCoords  = Word.getCoordinatesY();

        // onTimeTick() is only called in ambient mode, so it can't be used to drive the watch.
        // Install a timer that fires every 5 minutes and use it to drive the watch in interactive
        // mode.
        final Handler mUpdateTimeHandler = new Handler() {

            @Override
            public void handleMessage(Message message) {
                if (message.what != MSG_UPDATE_TIME) return;

                invalidate();
                if (shouldTimerBeRunning()) {
                    // Set next timer to the next round multiple of 5 min, and then some.
                    Time time = new Time();
                    time.setToNow();
                    long nowMs = time.toMillis(false);
                    time.minute += 5 - time.minute % 5;
                    time.second = 1;
                    long delayMs = time.toMillis(false) - nowMs;
                    mUpdateTimeHandler
                            .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                }
            }
        };


        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        /***
         * Instantiate visual elements, initialise coordinate system for the words inside the canvas
         * @param holder
         */
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            //Initialise
            mBackgroundBitmap   = BitmapFactory.decodeResource(getResources(), R.drawable.gdg);
            randomGenerator     = new Random();
            mTime               = new Time();

            //Setup
            setupColors();
            setupWordCoordinates();
            setupSystemUI();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            float mScale = ((float) width) / (float) mBackgroundBitmap.getWidth();
            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    (int)(mBackgroundBitmap.getWidth() * mScale),
                    (int)(mBackgroundBitmap.getHeight() * mScale), true);
        }

        /**
         * Setup the color palette to use in the words when active and disabled
         */
        private void setupColors() {

            //Google colors available
            Typeface defaultFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
            colors = new ArrayList<Integer>();
            colors.add(getResources().getColor(R.color.gdg_blue));
            colors.add(getResources().getColor(R.color.gdg_green));
            colors.add(getResources().getColor(R.color.gdg_yellow));
            colors.add(getResources().getColor(R.color.gdg_red));

            //Enabled / Disabled text
            enabledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            enabledPaint.setColor(Color.WHITE);
            enabledPaint.setTextSize(35f);
            enabledPaint.setTypeface(defaultFont);
            disabledPaint = new Paint(enabledPaint);
            disabledPaint.setColor(Color.DKGRAY);
        }

        /**
         * Setup the color palette to use in the words when active and disabled
         */
        private void setupBlackAndWhite() {

            //Google colors available
            Typeface defaultFont = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
            colors = new ArrayList<Integer>();
            colors.add(Color.WHITE);

            //Enabled / Disabled text
            enabledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            enabledPaint.setColor(Color.WHITE);
            enabledPaint.setTextSize(35f);
            enabledPaint.setTypeface(defaultFont);
            disabledPaint = new Paint(enabledPaint);
            disabledPaint.setColor(Color.BLACK);
        }

        /**
         * Get random color from the palette selected
         * @param index
         * @return
         */
        private int getColorForIndex(int index) {
            return colors.get(randomGenerator.nextInt(colors.size()));
        }

        /**
         * Define a set of coordinates for every word in the canvas
         */
        private void setupWordCoordinates() {

            //For every word
            for (int i = 1; i < words.length; ++i) {
                //First word in line
                if (yCoords[i] != yCoords[i - 1]) {
                    continue;
                }
                int index   = lines[yCoords[i]].indexOf(words[i]);
                xCoords[i]  = Math.round(enabledPaint.measureText(lines[yCoords[i]], 0, index));
            }

            mLineHeight = 0.58 * enabledPaint.getFontSpacing();
            for (int i = 0; i < words.length; i++) {
                yCoords[i] = (int)Math.round(yCoords[i] * mLineHeight);
            }

            //Calculate size of matrix text block in canvas
            for (int i = 0; i < lines.length; ++i) {
                mTextWidth = Math.max(mTextWidth, Math.round(enabledPaint.measureText(lines[i])));
            }

            mTextHeight = (int)Math.round(lines.length * mLineHeight);
        }

        /**
         * Setup Notification UI mechanism. Does not render in simulator?
         */
        private void setupSystemUI() {

            setWatchFaceStyle(new WatchFaceStyle.Builder(GDGFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle
                            .BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setStatusBarGravity(Gravity.END | Gravity.TOP)
                    .setHotwordIndicatorGravity(Gravity.END | Gravity.BOTTOM)
                    .setShowSystemUiTime(false)
                    .build());
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            mLowBitAmbient = inAmbientMode;

            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                enabledPaint.setAntiAlias(antiAlias);
                disabledPaint.setAntiAlias(antiAlias);
            }
            if (mBurnInProtection) {
                boolean fill = !inAmbientMode;
                enabledPaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
                disabledPaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
            }
            invalidate();
            updateTimer();
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

            // After this, mTime.minute will be 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, or 55.
            // 0-4 is mapped to 4, etc. That seems better than rounding, because if you're
            // wondering if something that starts 10:00 has already started, then "10:00" will
            // mean that it's at least ten. With rounding (57.5-2.5 -> 0) this wouldn't work.
            mTime.minute -= mTime.minute % 5;

            int mask = IT | IS;

            if (mTime.minute > 30) {
                mTime.minute = 60 - mTime.minute;
                mask |= TO;
                mTime.hour++;
            } else if (mTime.minute != 0)
                mask |= PAST;
            switch (mTime.minute) {
                case 5:
                    mask |= FIVE_H | MINUTES;
                    break;
                case 10:
                    mask |= TEN_H | MINUTES;
                    break;
                case 15:
                    mask |= QUARTER;
                    break;
                case 20:
                    mask |= TWENTY_H | MINUTES;
                    break;
                case 25:
                    mask |= TWENTY_H | FIVE_H | MINUTES;
                    break;
                case 30:
                    mask |= HALF;
                    break;
            }

            mask |= mTime.hour < 12 ? AM : PM;
            mTime.hour %= 12;
            if (mTime.hour == 0)
                mask |= TWELVE;
            else if (mTime.hour == 2)
                mask |= TWO;
            else if (mTime.hour == 3)
                mask |= THREE;
            else
                mask |= ONE << (mTime.hour - 1);

            // Clear background
            if (mLowBitAmbient) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawBitmap(mBackgroundBitmap, 0, 0, disabledPaint);
            }

            // Draw the background first, and then the active time on top of it.
            int x = (canvas.getWidth() - mTextWidth) / 2;
            int y = (canvas.getHeight() - mTextHeight) / 2
                    + (int) Math.round(mLineHeight);
            if (!isInAmbientMode()) {
                for (int i = 0; i < words.length; i++) {
                    if ((mask & (1 << i)) != 0) continue;

                    canvas.drawText(words[i], x + xCoords[i], y + yCoords[i], disabledPaint);
                }
            }

            for (int i = 0; i < words.length; i++) {
                if ((mask & (1 << i)) == 0) continue;

                //Light paint must be colored with google colors
                if (mLowBitAmbient) {
                    enabledPaint.setColor(Color.WHITE);
                } else {
                    enabledPaint.setColor(getColorForIndex(i));
                }

                canvas.drawText(words[i], x + xCoords[i], y + yCoords[i], enabledPaint);
            }
        }
    }
}