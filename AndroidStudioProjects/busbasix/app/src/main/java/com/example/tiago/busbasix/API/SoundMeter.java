package com.example.tiago.busbasix.API;

import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

/**
 * Created by tiago on 20/11/2017.
 */

public class SoundMeter {

    private MediaRecorder mRecorder = null;
    private File mFile = null;

    public void start() throws IOException {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //mFile = new File("/dev/null");
            //mRecorder.setOutputFile(mFile.getAbsolutePath());
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();
        }
    }

    public void stop() {

        /*
        if (mRecorder != null) {

            try {
                mRecorder.stop();
            } catch(RuntimeException e) {
                mFile.delete();  //you must delete the outputfile when the recorder stop failed.
            } finally {
                mRecorder.release();
                mRecorder = null;
            }

        }
        */
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            return 0;

    }
}
