package com.example.ivan.proyectosdm.CreacionNotas;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.ContextCompat.checkSelfPermission;

import com.example.ivan.proyectosdm.DataBase.Save;
import com.example.ivan.proyectosdm.MainActivity;
import com.example.ivan.proyectosdm.Notas.ArchivoAdapter;
import com.example.ivan.proyectosdm.Notas.Imagen;
import com.example.ivan.proyectosdm.Notas.Nota;
import com.example.ivan.proyectosdm.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAdjuntos extends Fragment {
    private boolean fabExpanded = false;
    private FloatingActionButton fabSettings;
    private LinearLayout layoutFabFoto;
    private LinearLayout layoutFabVideo;
    private LinearLayout layoutFabUbi;
    private final String CARPETA_RAIZ = "misImagenesPrueba/";
    private final String RUTA_IMAGEN = CARPETA_RAIZ + "misFotos";
    String path;
    final int COD_FOTO_SELECCION=10;
    final int COD_FOTO_CAPTURA=20;
    final int COD_VIDEO_SELECCION=30;
    final int COD_VIDEO_CAPTURA=40;
    private boolean permisos;
    private Save save = new Save();
    private Nota nota;


    private List<Imagen> imagenes;
    private RecyclerView mRVImagen;
    private ArchivoAdapter adapter;
    private GridLayoutManager glm;

    public FragmentAdjuntos() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            nota = (Nota) getArguments().getSerializable(MainActivity.OBJETO_NOTA);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fabFoto = inflater.inflate(R.layout.layout_fab_foto, container, false);
        View fabVideo = inflater.inflate(R.layout.layout_fab_video, container, false);
        View fabUbi = inflater.inflate(R.layout.layout_fab_ubicacion, container, false);
        container.addView(fabFoto);
        container.addView(fabVideo);
        container.addView(fabUbi);
        View v = inflater.inflate(R.layout.fragment_fragment_adjuntos, container, false);
        fabSettings = (FloatingActionButton) v.findViewById(R.id.fabAdjuntos);
        layoutFabFoto = (LinearLayout) fabFoto.findViewById(R.id.layoutFabFoto);
        layoutFabVideo = (LinearLayout) fabVideo.findViewById(R.id.layoutFabVideo);
        layoutFabUbi = (LinearLayout) fabUbi.findViewById(R.id.layoutFabUbi);
        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded){
                    closeSubMenusFab();
                } else {
                    openSubMenusFab();
                }
            }
        });
        closeSubMenusFab();
        layoutFabFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validaPermisos(0);
            }
        });
        layoutFabVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validaPermisos(1);
            }
        });
        mRVImagen = (RecyclerView) v.findViewById(R.id.rvImagenes);
        return v;
    }

    private boolean validaPermisos(int seleccion) {

        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            if(seleccion == 0) {
                cargarImagen();
            }else{
                cargarVideo();
            }
            return true;
        }

        if((checkSelfPermission(getContext(),CAMERA)==PackageManager.PERMISSION_GRANTED)&&
                (checkSelfPermission(getContext(),WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)){
            if(seleccion == 0) {
                cargarImagen();
            }else{
                cargarVideo();
            }
            return true;
        }
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
        if(permisos){
            if(seleccion == 0) {
                cargarImagen();
            }else{
                cargarVideo();
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED
                    && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                permisos = true;
            }else{
                permisos = false;
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Aviso: Permisos Desactivados");
                dialog.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");
                dialog.create().show();
            }
        }
    }

    private void cargarVideo(){
        final CharSequence[] opciones={"Grabar Vídeo","Cargar Vídeo","Cancelar"};
        final AlertDialog.Builder alertOpciones=new AlertDialog.Builder(getContext());
        alertOpciones.setTitle("Seleccione una Opción");
        alertOpciones.setItems(opciones,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(opciones[which].equals("Grabar Vídeo")){
                            closeSubMenusFab();
                            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            startActivityForResult(Intent.createChooser(intent, ""), COD_VIDEO_CAPTURA);
                        }
                        else if(opciones[which].equals("Cargar Vídeo")){
                            Intent intent = new Intent();
                            intent.setType("video/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, ""), COD_VIDEO_SELECCION);
                        }else{
                            dialog.dismiss();
                        }
                    }
                });
        alertOpciones.create().show();
    }

    private void cargarImagen() {
        final CharSequence[] opciones={"Tomar Foto","Cargar Imagen","Cancelar"};
        final AlertDialog.Builder alertOpciones=new AlertDialog.Builder(getContext());
        alertOpciones.setTitle("Seleccione una Opción");
        alertOpciones.setItems(opciones,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(opciones[which].equals("Tomar Foto")){
                            closeSubMenusFab();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(Intent.createChooser(intent, ""), COD_FOTO_CAPTURA);
                        }
                        else if(opciones[which].equals("Cargar Imagen")){
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, ""), COD_FOTO_SELECCION);
                        }else{
                            dialog.dismiss();
                        }
                    }
                });
        alertOpciones.create().show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            if(requestCode == COD_FOTO_SELECCION){
                Uri miPath=data.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),miPath);
                } catch (IOException e) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setTitle("Aviso: Error");
                    dialog.setMessage("Vuelve a seleccionar la foto");
                    dialog.create().show();
                }

                String fileName = save.SaveImage(getContext(),bitmap);
                Imagen imagen = new Imagen(nota.getId(), fileName);
                nota.addImagen(imagen);
                cargarImagenes();

            }else if(requestCode == COD_FOTO_CAPTURA){
                MediaScannerConnection.scanFile(getContext(), new String[]{path}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("Ruta de almacenamiento","Path: "+path);
                            }
                        });
                Bitmap bitmap= BitmapFactory.decodeFile(path);
                String fileName = save.SaveImage(getContext(),bitmap);
                Imagen imagen = new Imagen(nota.getId(),fileName);
                nota.addImagen(imagen);
                cargarImagenes();
            }else if(requestCode == COD_VIDEO_CAPTURA){
                MediaScannerConnection.scanFile(getContext(), new String[]{path}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("Ruta de almacenamiento","Path: "+path);
                            }
                });
                Uri uri = Uri.parse(path);
            }else if(requestCode == COD_VIDEO_SELECCION){
                Uri miPath=data.getData();
                if(miPath == null) {
                    AlertDialog.Builder dialog1 = new AlertDialog.Builder(getContext());
                    dialog1.setTitle("Aviso: Error");
                    dialog1.setMessage("Vuelve a seleccionar el vídeo");
                    dialog1.create().show();
                }
            }
        }
    }

    public void cargarImagenes(){
        glm = new GridLayoutManager(getContext(), 1);
        mRVImagen.setLayoutManager(glm);
        adapter = new ArchivoAdapter(nota.getImagenes());
        mRVImagen.setAdapter(adapter);
    }

    private void closeSubMenusFab(){
        layoutFabFoto.setVisibility(View.GONE);
        layoutFabVideo.setVisibility(View.GONE);
        layoutFabUbi.setVisibility(View.GONE);
        fabSettings.setImageResource(R.drawable.ic_add_white_24dp);
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        layoutFabFoto.setVisibility(View.VISIBLE);
        layoutFabVideo.setVisibility(View.VISIBLE);
        layoutFabUbi.setVisibility(View.VISIBLE);
        //Change settings icon to 'X' icon
        fabSettings.setImageResource(R.drawable.ic_close_black_24dp);
        fabExpanded = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        closeSubMenusFab();
    }

    @Override
    public void onResume() {
        super.onResume();
        closeSubMenusFab();
    }

    public List getImagenes(){
        return nota.getImagenes().size() == 0? null:nota.getImagenes();
    }
}
