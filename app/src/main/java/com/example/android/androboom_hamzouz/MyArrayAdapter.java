package com.example.android.androboom_hamzouz;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by android on 20/12/2017.
 */

class MyArrayAdapter extends ArrayAdapter<Profil> {
    List<Profil> liste;

    private MyArrayAdapter(Context context, int ressource, List<Profil> liste) {
        super(context, ressource, liste);
        this.liste = liste;
    }

    @Override
    public View getView (int position, View converView, ViewGroup parent) {
        TextView tv = new TextView(getContext());
        tv.setText(liste.get(position).getEmail());
        return tv;
    }

    @Override
    public int getCount() {
        return liste.size();
    }
}
