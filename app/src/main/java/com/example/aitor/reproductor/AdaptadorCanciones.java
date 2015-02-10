package com.example.aitor.reproductor;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by aitor on 07/02/2015.
 */
public class AdaptadorCanciones extends CursorAdapter {

    public AdaptadorCanciones(Context context, Cursor c) {
        super(context, c,true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater i = LayoutInflater.from(parent.getContext());
        View v = i.inflate(R.layout.detalle_cancion, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv1, tv2;
        tv1 = (TextView)view.findViewById(R.id.tvNombre);
        tv2 = (TextView)view.findViewById(R.id.tvTiempo);
        tv1.setText(cursor.getString(cursor.getColumnIndex("_display_name")));
        tv2.setText(cursor.getString(cursor.getColumnIndex("duration")));
    }
}
