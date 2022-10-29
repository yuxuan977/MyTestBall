package com.example.mytestball;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    int sHeight,sWidth;
    //The total score
    int score;
    //End mark
    boolean endMark;
    //BALL
    //size of the ball
    static final float bSize = 15;
    float bFall = 4f; //falling speed
    static final float bRise = 90; //rising speed
    float ballX = 222,ballY=sHeight/2; //mid
    //BLOCKS
    //Two blocks, up and down
    float blockHeight1,blockHeight2,blockWidth1,blockWidth2;
    float blockX1,blockX2,blockY1,blockY2;
    float blockSpeed = 4f;
    double speedLevel=1;
    float gap = 300f;

    BallGameView ballGameView;
    Timer timer;
    boolean markFlag;
    int TIME = 1000;

    //voice controller
    float volume = 10000;
    BallMediaRecorder br;
    static final int msgVoice = 0x1001;
    static final int refTime = 100;
    static final int msgCanvas = 0X1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Get Window Manager
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        sHeight = metrics.heightPixels;
        sWidth = metrics.widthPixels;
        //full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        //media recorder
        br = new BallMediaRecorder();

        //Bind the game view
        ballGameView = new BallGameView(this);
        setContentView(ballGameView);

        playGame();
    }

    //game voice ball
    @SuppressLint("ClickableViewAccessibility")
    public void playGame() {
        endMark = false;
        score = 0;
        blockX1 = sWidth;
        blockY1 = 0;
        blockX2 = sWidth;
        blockY2 = sHeight;

        //block From right to left
        System.out.println(sHeight); //1280
        System.out.println("----test09------");
        blockHeight1 = (float)((sHeight/2)*Math.random()+50);
        blockWidth1 = 100+(float)(Math.random()*70%62);
        blockHeight2 = sHeight - blockHeight1 - gap;
        blockWidth2 = blockWidth1;

        //ball y
        ballY = sHeight/2;

        //sendMSG
        handlerPic.sendEmptyMessage(msgCanvas);

        //mark flag
        markFlag = true;

        //func1  click to jump
        ballGameView.setOnTouchListener(clickLis);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                blockX1 = blockX1 - blockSpeed;
                blockX2 = blockX2 - blockSpeed;
                ballY = ballY + bFall;

                //Ball hits the blocks
                if (ballX >= blockX1 && ballX <= blockX1 + blockWidth1) {
                    if (ballY<blockHeight1 || ballY>gap+blockHeight1) {
                        blockSpeed = 4f;
                        speedLevel = 1;
                        endMark = true;
                        timer.cancel();
                    }
                }

                //Ball hits up or down side
                if (ballY >= sHeight || ballY <= 0) {
                    blockSpeed = 4f;
                    speedLevel = 1;
                    endMark = true;
                    timer.cancel();
                }

                if (markFlag) {
                    if ( blockX1 + blockWidth1<ballX) {
                        score++;
                        markFlag = false;
                    }
                }

                if (blockX1 + blockWidth1 <= 0) {
                    blockX1 = sWidth;
                    blockX2 = sWidth;

                    blockHeight1 = (float)((sHeight/2)*Math.random()+50);
                    blockWidth1 = 100+(float)(Math.random()*70%62);
                    blockHeight2 = sHeight - blockHeight1 - gap;
                    blockWidth2 = blockWidth1;
                    markFlag = true;
                    blockSpeed+=0.5;
                    speedLevel = blockSpeed/0.5-8;
                }
                handlerPic.sendEmptyMessage(msgCanvas);
            }
        }, 0, 13);

    }
    //handler msg
    @SuppressLint("HandlerLeak")
    Handler handlerPic = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msgCanvas == msg.what) {
                ballGameView.invalidate();
            }
        }
    };

    View.OnTouchListener clickLis = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                ballY = ballY - bRise;
                handlerPic.sendEmptyMessage(msgCanvas);
            }
            return true;
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (this.hasMessages(msgVoice)) {
                System.out.println("-=-=-=");
                return;
            }
            volume = br.getMaxAmplitude();
            System.out.println("++++++++++++++++++++1");

            System.out.println(String.valueOf(volume));
            if(volume > 0 && volume < 1000000) {
                System.out.println("++++++++++++++++++++");
                super.handleMessage(msg);
                if (volume>=3000 ) {
                    ballY = ballY - bRise;
                    handlerPic.sendEmptyMessage(msgCanvas);
                    volume=0;
                }
            }
            handler.sendEmptyMessageDelayed(msgVoice, refTime);
        }
    };

    private void startListenAudio() {
        handler.sendEmptyMessageDelayed(msgVoice, refTime);

    }

    public void startRecord(File fFile){
        try{
            br.setBallAudioFile(fFile);
            if (br.startRecorder()) {
                System.out.println("==7===");
                startListenAudio();
            }else{
                System.out.println("------------");
                Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(this, "The recorder is occupied ", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    //game view
    class BallGameView extends View {
        Paint pBall = new Paint();
        Paint pBlock = new Paint();
        Paint pText = new Paint();

        public BallGameView(Context context) {
            super(context);
        }

        //canvas
        @SuppressLint("ClickableViewAccessibility")
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            pBall.setStyle(Paint.Style.FILL);
            pText.setStyle(Paint.Style.FILL);
            pText.setTextSize(80);
            pText.setColor(Color.RED);

            if (true==endMark) {
                //game over
                pBall.setColor(Color.RED);
                pBall.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("your score: " + score , sWidth/2, sHeight/2, pText);

                //click to restart
                this.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            playGame();
                        }
                        return true;
                    }
                });
            } else {
                //game running
                //ball
                pBall.setColor(Color.BLACK);
                pBall.setAlpha(80);
                canvas.drawCircle(ballX, ballY, bSize, pBall);

                //block
                pBlock.setColor(Color.RED);
                pBlock.setStyle(Paint.Style.STROKE);
                canvas.drawRect(blockX1, blockY1, blockWidth1 + blockX1, blockHeight1 + blockY1, pBlock);
                canvas.drawRect(blockX2, blockY2 - blockHeight2, blockWidth2 + blockX2, blockHeight2 + blockY2, pBlock);

                //text
                pText.setColor(Color.BLACK);
                pText.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.valueOf(score), sWidth/2, 90, pText);

            }

        }

    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onResume() {
        super.onResume();
        String fileName = "temp"+(int)Math.random()+".amr";
        System.out.println(fileName);
        File file = FileStore.createFile(fileName);
        if (file!=null) {
            System.out.println(file.getAbsolutePath());
            startRecord(file);
        } else {
            Toast.makeText(getApplicationContext(), "Failed to create file", Toast.LENGTH_LONG).show();
        }
    }
}