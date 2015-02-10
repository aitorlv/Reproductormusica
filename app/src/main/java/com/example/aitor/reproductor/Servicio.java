package com.example.aitor.reproductor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Servicio extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mp;
    private String TIEMPO="tiempo";
    private enum Estados {
        idle,
        initialized,
        prepairing,
        prepared,
        started,
        paused,
        completed,
        sttoped,
        end,
        error
    };
    private Estados estado;
    public static final String PLAY = "play";
    public static final String STOP = "stop";
    public static final String ADD = "add";
    public static final String PAUSE = "pause";
    private String rutaCancion = null;
    private List<String> canciones;
    private boolean reproducir;

    /* ******************************************************* */
    // METODOS SOBREESCRITOS //
    /* ****************************************************** */

    @Override
    public void onCreate() {
        super.onCreate();
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int r = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
           iniciar();
        } else {
            stopSelf();
        }

    }

    public void iniciar(){
        mp = new MediaPlayer();
        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mp.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        estado = Estados.idle;
    }
    @Override
    public void onDestroy() {
        //mp.reset();
        mp.release();
        mp = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String dato = intent.getStringExtra("cancion");

        if (action.equals(PLAY)) {
            play();
            notificaion();
        } else if (action.equals(ADD)) {
            add(dato);
        } else if (action.equals(STOP)) {
            stop();
        } else if (action.equals(PAUSE)) {
            pause();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /* ******************************************************* */
    // INTERFAZ PREPARED LISTENER //
    /* ****************************************************** */

    @Override
    public void onPrepared(MediaPlayer mp) {
        estado = Estados.prepared;
        if (reproducir) {
            mp.start();
            estado = Estados.started;
            handler.post(updateThread);
        }
    }

    /* ******************************************************* */
    // INTERFAZ COMPLETED LISTENER //
    /* ****************************************************** */

    @Override
    public void onCompletion(MediaPlayer mp) {
        estado = Estados.completed;
        Intent intent = new Intent(TIEMPO);
        intent.putExtra("accion","completo");
        sendBroadcast(intent);
    }

    /* ******************************************************* */
    // INTERFAZ AUDIO FOCUS CHANGED //
    /* ****************************************************** */

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                play();
                mp.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mp.setVolume(0.1f, 0.1f);
                break;
        }
    }

    /* ******************************************************* */
    // METODOS DE AUDIO //
    /* ****************************************************** */
    Handler handler = new Handler();
    Runnable updateThread = new Runnable(){
        public void run() {
            Intent intent = new Intent(TIEMPO);
            intent.putExtra("accion",mp.getCurrentPosition()+"");
            //intent.putExtra("total",mp.getDuration()+"");
            sendBroadcast(intent);


            //100
            handler.postDelayed(updateThread, 300);
        }
    };
    private void play() {
        if (rutaCancion != null) {
            if (estado == Estados.error) {
                estado = Estados.idle;
            }
            if (estado == Estados.idle) {
                reproducir = true;
                try {
                    mp.setDataSource(rutaCancion);
                    estado = Estados.initialized;
                } catch (IOException e) {
                    estado = Estados.error;
                }
            }
            if (estado == Estados.initialized || estado==Estados.sttoped) {
                reproducir = true;
                mp.prepareAsync();
                estado = Estados.prepairing;
            } else if (estado == Estados.prepairing) {
                reproducir = true;
            }
            if (estado == Estados.prepared ||
                    estado == Estados.paused ||
                    estado == Estados.completed ||
                    estado == Estados.started) {
                mp.start();
                estado = Estados.started;
            }
           
        }
    }

    private void stop() {
        if (estado == Estados.prepared ||
                estado == Estados.started ||
                estado == Estados.paused ||
                estado == Estados.completed) {
            mp.seekTo(0); // Para volver al principio sino comentar para pause
            mp.stop();
            handler.removeCallbacks(updateThread);
            estado = Estados.sttoped;
            stopForeground(true);

        }
        reproducir = false;
    }

    private void pause() {

            mp.pause();
            estado = Estados.paused;

    }



    private void add(String cancion) {
        this.rutaCancion = cancion;
        Log.v("ruta", this.rutaCancion);
        stop();
        iniciar();
        play();
        notificaion();
    }

    private void notificaion(){

        Notification note=new Notification(R.drawable.ic_launcher,
                "Can you hear the music?",
                System.currentTimeMillis());
        Intent i=new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi=PendingIntent.getActivity(this, 0,
                i, 0);

        note.setLatestEventInfo(this, "Fake Player",
                "Now Playing: \"Ummmm, Nothing\"",
                pi);
        note.flags|=Notification.FLAG_NO_CLEAR;

        startForeground(1337, note);
  }

}
