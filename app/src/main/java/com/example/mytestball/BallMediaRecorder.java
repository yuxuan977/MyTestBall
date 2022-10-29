package com.example.mytestball;

import android.media.MediaRecorder;

import java.io.File;

public class BallMediaRecorder {
    public  File ballAudioFile;
    private MediaRecorder recorder;
    public boolean isTape = false ;

    public float getMaxAmplitude() {
        if(recorder==null){
            return 10;
        }else {
            try {
                return recorder.getMaxAmplitude();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }


    public void setBallAudioFile(File ballAudioFile) {
        this.ballAudioFile = ballAudioFile;
    }

    public boolean startRecorder(){
        if (ballAudioFile == null) return false;
        try {
            System.out.println("----------333");
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(ballAudioFile.getAbsolutePath());

            recorder.prepare();
            recorder.start();
            isTape = true;
            return true;
        } catch(Exception e) {
            System.out.println("------------1");
            recorder.reset();
            recorder.release();
            recorder = null;
            isTape = false ;
            e.printStackTrace();
            System.out.println("------------2");
        }
        return false;
    }

}