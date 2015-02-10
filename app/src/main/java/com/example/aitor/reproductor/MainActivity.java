package com.example.aitor.reproductor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends ActionBarActivity {
  private Cursor listadeCanciones;
  private ListView lista;
  private List<String>canciones;
    private ImageButton btnplay,btnpause,btnstop,btnprev,btnnext;
  private AdaptadorCanciones adaptador;
    private static String TIEMPO="tiempo";
    private static int GRABAR=0;
    private SeekBar tiempo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       btnplay=(ImageButton)findViewById(R.id.play);
       btnpause=(ImageButton)findViewById(R.id.pausa);
       btnstop=(ImageButton)findViewById(R.id.stop);
       btnprev=(ImageButton)findViewById(R.id.detras);
       btnnext=(ImageButton)findViewById(R.id.delante);
        tiempo=(SeekBar)findViewById(R.id.seekBar);
       listadeCanciones=getContentResolver().query( android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
       lista=(ListView)findViewById(R.id.lvCanciones);
       adaptador=new AdaptadorCanciones(this,listadeCanciones);
       lista.setAdapter(adaptador);
       listadeCanciones.moveToFirst();

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), Servicio.class);
                intent.putExtra("cancion", listadeCanciones.getString(listadeCanciones.getColumnIndex(MediaStore.Audio.Media.DATA)));
                tiempo.setMax(Integer.parseInt(listadeCanciones.getString(listadeCanciones.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                intent.setAction(Servicio.ADD);
                startService(intent);
                btnplay.setVisibility(View.INVISIBLE);
                btnpause.setVisibility(View.VISIBLE);
                btnstop.setVisibility(View.VISIBLE);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.grabar){
            Intent intent = new Intent(
                    MediaStore.Audio.Media.
                            RECORD_SOUND_ACTION);
            startActivityForResult(intent, GRABAR);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receptor,new IntentFilter(TIEMPO));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receptor);

    }
    @Override
    public void onBackPressed() {
        Intent backtoHome = new Intent(Intent.ACTION_MAIN);
        backtoHome.addCategory(Intent.CATEGORY_HOME);
        backtoHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(backtoHome);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         stopService(new Intent(MainActivity.this,
               Servicio.class));

    }

    BroadcastReceiver receptor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b=intent.getExtras();
            if(b.getString("accion").compareTo("completo")==0){
                siguiente();
            }else {
                tiempo.setProgress(Integer.parseInt(intent.getExtras().getString("accion")));
            }
        }
    };
    public void play(View v){
        btnplay.setVisibility(View.INVISIBLE);
        btnpause.setVisibility(View.VISIBLE);
        btnstop.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, Servicio.class);
        intent.setAction(Servicio.PLAY);
        startService(intent);
    }

    public void pause(View v){
        btnpause.setVisibility(View.INVISIBLE);
        btnplay.setVisibility(View.VISIBLE);
        btnstop.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, Servicio.class);
        intent.setAction(Servicio.PAUSE);
        startService(intent);
    }

    public void stop(View v){
        btnpause.setVisibility(View.INVISIBLE);
        btnplay.setVisibility(View.VISIBLE);
        btnstop.setVisibility(View.GONE);
        Intent intent = new Intent(this, Servicio.class);
        intent.setAction(Servicio.STOP);
        startService(intent);
    }

    public void next(View v){
        siguiente();
    }
    public void siguiente(){
        listadeCanciones.moveToNext();
        if(listadeCanciones.getPosition()<=listadeCanciones.getCount()-1) {
            tiempo.setMax(Integer.parseInt(listadeCanciones.getString(listadeCanciones.getColumnIndex(MediaStore.Audio.Media.DURATION))));
            Intent intent = new Intent(getApplicationContext(), Servicio.class);
            intent.putExtra("cancion", listadeCanciones.getString(listadeCanciones.getColumnIndex(MediaStore.Audio.Media.DATA)));
            intent.setAction(Servicio.ADD);
            startService(intent);
            btnplay.setVisibility(View.INVISIBLE);
            btnpause.setVisibility(View.VISIBLE);
            btnstop.setVisibility(View.VISIBLE);
        }else {
            tostada(getResources().getString(R.string.action_ultima));
        }
    }
    public void prev(View v){
        if(listadeCanciones.getPosition()!=0) {
            listadeCanciones.moveToPrevious();
            tiempo.setMax(Integer.parseInt(listadeCanciones.getString(listadeCanciones.getColumnIndex(MediaStore.Audio.Media.DURATION))));
            Intent intent = new Intent(getApplicationContext(), Servicio.class);
            intent.putExtra("cancion", listadeCanciones.getString(listadeCanciones.getColumnIndex(MediaStore.Audio.Media.DATA)));
            intent.setAction(Servicio.ADD);
            startService(intent);
            btnplay.setVisibility(View.INVISIBLE);
            btnpause.setVisibility(View.VISIBLE);
            btnstop.setVisibility(View.VISIBLE);
        }else{
            tostada(getResources().getString(R.string.action_primera));
        }

    }

    public void tostada(String s){
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }

}
