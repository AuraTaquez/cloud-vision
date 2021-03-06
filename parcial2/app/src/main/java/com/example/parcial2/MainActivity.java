package com.example.parcial2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import WebServices.Asynchtask;
import WebServices.WebService;


public class MainActivity extends AppCompatActivity implements Asynchtask {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imagen;
    ArrayList<Pais> Listapais;
    TextView infoPais;

    public Vision vision;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagen = (ImageView) findViewById(R.id.imvImagen);
        Map<String, String> datos = new HashMap<String, String>();
        WebService ws = new WebService("http://www.geognos.com/api/en/countries/info/all.json", datos, MainActivity.this, MainActivity.this);
        ws.execute("");
        infoPais = findViewById(R.id.textbuscar);
        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(), new AndroidJsonFactory(), null);
        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyCkh1NM7L46ORcJjjEBfbo_netCD5JZMzA"));
        vision = visionBuilder.build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 10:
                    Uri MIpath = data.getData();
                    imagen.setImageURI(MIpath);
                    break;
                case 1:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imagen.setImageBitmap(imageBitmap);

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + requestCode);
            }

        }

    }

    public void selecImg(View v) {

        final CharSequence[] opciones = {"Tomar Foto", "Buscar Imagen"};
        final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(MainActivity.this);
        alertOpciones.setTitle("Seleccione...");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (opciones[i].toString()) {
                    case "Tomar Foto":
                        tomarFoto();
                        break;
                    case "Buscar Imagen":
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent, "Seleccione la app"), 10);
                        break;
                }
            }
        });
        alertOpciones.show();
    }

    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void processFinish(String result) throws JSONException {
        Listapais = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(result);
        JSONObject jresults = jsonObject.getJSONObject("Results");
        Iterator<?> iterator = jresults.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            JSONObject paisJson = jresults.getJSONObject(key);
            Pais pais = new Pais();
            pais.setNombre(paisJson.getString("Name"));
            pais.setPrefijo(paisJson.getString("TelPref"));
            JSONObject georectangle = paisJson.getJSONObject("GeoRectangle");
            try {
                JSONObject capital = paisJson.getJSONObject("Capital");
                pais.setCapital(capital.getString("Name"));
            }catch (Exception e){
                pais.setCapital("Sin datos de Capital");
            }
            pais.setNorte(georectangle.getString("North"));
            pais.setSur(georectangle.getString("South"));
            pais.setEste(georectangle.getString("East"));
            pais.setOeste(georectangle.getString("West"));
            JSONObject countryCodes = paisJson.getJSONObject("CountryCodes");
            pais.setUrl("http://www.geognos.com/api/en/countries/flag/" + countryCodes.getString("iso2") + ".png");
            Listapais.add(pais);
        }

    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    public void ejecutarOpcion(View view) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                bitmap = scaleBitmapDown(bitmap, 1200);
                ByteArrayOutputStream stream = new ByteArrayOutputStream(); //2da de la api
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();
                Image inputImage = new Image(); //googlevision
                inputImage.encodeContent(imageInByte);
                //Armo mi listado de solicitudes
                List<Feature> desiredFeaturelst = new ArrayList<>();
                //Realizar la solicitud de cualquier tipo de los servicio que ofrece la API
                Feature desiredFeatureitem;
                //Recorro mi listado de solicitudes seleccionadas
                desiredFeatureitem = new Feature();
                desiredFeatureitem.setType("TEXT_DETECTION");
                //Cargo a mi lista la solicitud
                desiredFeaturelst.add(desiredFeatureitem);


                //Armamos la solicitud o las solicitudes .- FaceDetection solo o facedeteccion,textdetection,etc..
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(desiredFeaturelst);
                BatchAnnotateImagesRequest batchRequest = new
                        BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));
                //Asignamos al control VisionBuilder la solicitud
                BatchAnnotateImagesResponse batchResponse = null;
                try {
                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchRequest);
                    //Enviamos la solicitud
                    annotateRequest.setDisableGZipContent(true);
                    batchResponse = annotateRequest.execute();
                } catch (IOException ex) {
                    Toast.makeText(MainActivity.this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
                if (batchResponse != null) {
                    TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();
                    if (text != null) {
                        message = text.getText();

                    } else {
                        Toast.makeText(MainActivity.this, "No hay texto", Toast.LENGTH_SHORT).show();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infoPais.setText(message.trim());
                    }
                });
            }
        });
    }


    public void mostrarInfo(View view) {
        for (Pais p : Listapais) {
            String name = infoPais.getText().toString();
            if (p.nombre.equals(name)) {
                Intent miIntent = new Intent(MainActivity.this, mapas.class);
                miIntent.putExtra("Nombre", p.getNombre());
                miIntent.putExtra("Capital", p.getCapital());
                miIntent.putExtra("TelPref", p.getPrefijo());
                miIntent.putExtra("Norte", p.getNorte());
                miIntent.putExtra("Sur", p.getSur());
                miIntent.putExtra("Este", p.getEste());
                miIntent.putExtra("Oeste", p.getOeste());
                miIntent.putExtra("Url", p.getUrl());
                startActivityForResult(miIntent, 0);
            }
        }
        Toast.makeText(MainActivity.this, "Ingrese un pais valido", Toast.LENGTH_SHORT).show();
    }
}