package com.example.parcial2;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class mapas extends AppCompatActivity implements OnMapReadyCallback {
    Double nor;
    Double sur;
    Double este;
    Double oeste;
    String url;
    String name;
    TextView paisName;
    TextView capital;
    TextView prefijo;
    String cap,pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);
        name = getIntent().getExtras().getString("Nombre");
        cap= getIntent().getExtras().getString("Capital");
        pref= getIntent().getExtras().getString("TelPref");
        url = getIntent().getExtras().getString("Url");
        nor = Double.valueOf(getIntent().getExtras().getString("Norte"));
        sur = Double.valueOf(getIntent().getExtras().getString("Sur"));
        este = Double.valueOf(getIntent().getExtras().getString("Este"));
        oeste = Double.valueOf(getIntent().getExtras().getString("Oeste"));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ImageView imageView = (ImageView) findViewById(R.id.imabandera);
        Glide.with(this.getApplicationContext()).load(url).into(imageView);
        paisName = findViewById(R.id.pais);
        capital = findViewById(R.id.capital);
        prefijo = findViewById(R.id.Pref);
        paisName.setText(name);
        capital.setText(cap);
        prefijo.setText(pref);
    }

    GoogleMap mapa;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);
        CameraUpdate camUpd1 = CameraUpdateFactory.newLatLngZoom(new LatLng(nor, oeste), 5);
        mapa.moveCamera(camUpd1);
        PolylineOptions lineas = new PolylineOptions()
                .add(new LatLng(nor, oeste))
                .add(new LatLng(nor, este))
                .add(new LatLng(sur, este))
                .add(new LatLng(sur, oeste))
                .add(new LatLng(nor, oeste));
        lineas.width(8);
        lineas.color(Color.RED);
        mapa.addPolyline(lineas);
    }
}