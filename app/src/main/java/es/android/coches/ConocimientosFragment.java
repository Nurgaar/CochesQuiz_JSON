package es.android.coches;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;


import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import es.android.coches.databinding.FragmentConocimientosBinding;

public class ConocimientosFragment extends Fragment {

    private FragmentConocimientosBinding binding;

    List<Pregunta> todasLasPreguntas;
    List<String> todasLasRespuestas;

    List<Pregunta> preguntas;
    int respuestaCorrecta;

    int puntuacion;
    int puntuacionMax;
    JSONObject objJson;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        FileInputStream fis = null;
        try {


            if (existeFichero(getContext(),"puntuaciones.json")){


                fis = getContext().openFileInput("puntuaciones.json");


                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String fileContent = br.readLine();
                while (fileContent != null) {
                    sb.append(fileContent);
                    fileContent = br.readLine();
                }

                objJson = new JSONObject(sb.toString());
                String puntuacionMaxima = objJson.getString("puntuacionMaxima");
                puntuacionMax = Integer.parseInt(puntuacionMaxima);


            }else {

                objJson = new JSONObject();
                objJson = objJson.put("puntuacionMaxima",puntuacionMax);
                objJson = objJson.put("ultima puntuacion",puntuacion);
                salvarFichero("puntuaciones.json",objJson.toString());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        super.onCreate(savedInstanceState);
        if(todasLasPreguntas == null) {
            try {
                generarPreguntas("coches"); //CON RAW SE QUITA LA EXTENSION .XML
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.shuffle(todasLasPreguntas);
        preguntas = new ArrayList<>(todasLasPreguntas);
    }


    public boolean existeFichero(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConocimientosBinding.inflate(inflater,container,false);

        presentarPregunta();

        binding.botonRespuesta.setOnClickListener(v -> {
            int seleccionado = binding.radioGroup.getCheckedRadioButtonId();
            CharSequence mensaje = seleccionado == respuestaCorrecta ? "¡Acertaste!" : "Fallaste";

            //Para que me sume las acertadas y me las muestre
            if(mensaje.equals("¡Acertaste!")){
                puntuacion++;
            }
            Snackbar.make(v, mensaje, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Siguiente", v1 -> presentarPregunta())
                    .show();
            v.setEnabled(false);
        });

        return binding.getRoot();
    }

    private List<String> generarRespuestasPosibles(String respuestaCorrecta) {
        List<String> respuestasPosibles = new ArrayList<>();
        respuestasPosibles.add(respuestaCorrecta);

        List<String> respuestasIncorrectas = new ArrayList<>(todasLasRespuestas);
        respuestasIncorrectas.remove(respuestaCorrecta);

        for(int i=0; i<binding.radioGroup.getChildCount()-1; i++) {
            int indiceRespuesta = new Random().nextInt(respuestasIncorrectas.size());
            respuestasPosibles.add(respuestasIncorrectas.remove(indiceRespuesta));

        }
        Collections.shuffle(respuestasPosibles);
        return respuestasPosibles;
    }

    private void presentarPregunta() {
        if(preguntas.size() > 0) {
            binding.botonRespuesta.setEnabled(true);

            int pregunta = new Random().nextInt(preguntas.size());

            Pregunta preguntaActual = preguntas.remove(pregunta);
            preguntaActual.setRespuetas(generarRespuestasPosibles(preguntaActual.respuestaCorrecta));

            InputStream bandera = null;
            try {
                int idElemento = getResources().getIdentifier(preguntaActual.modelo , "raw", getContext().getPackageName());
                bandera = getContext().getResources().openRawResource(idElemento);
                binding.bandera.setImageBitmap(BitmapFactory.decodeStream(bandera));
            } catch (Exception e) {
                e.printStackTrace();
            }
            binding.radioGroup.clearCheck();

            for (int i = 0; i < binding.radioGroup.getChildCount(); i++) {
                RadioButton radio = (RadioButton) binding.radioGroup.getChildAt(i);

                CharSequence respuesta = preguntaActual.getRespuetas().get(i);
                if (respuesta.equals(preguntaActual.respuestaCorrecta))
                    respuestaCorrecta = radio.getId();

                radio.setText(respuesta);
            }
        } else {
            objJson = new JSONObject();
            if (puntuacionMax<puntuacion){


                try {
                    objJson = objJson.put("puntuacionMaxima",puntuacion);
                    objJson = objJson.put("ultima puntuacion",puntuacion);
                    salvarFichero("puntuaciones.json",objJson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                binding.bandera.setVisibility(View.GONE);
                binding.radioGroup.setVisibility(View.GONE);
                binding.botonRespuesta.setVisibility(View.GONE);
                binding.textView.setText("Has batido tu récord \n"+"Puntuación máxima conseguida: "+puntuacion);
            }else{
                try {
                    objJson = objJson.put("puntuacionMax",puntuacionMax);
                    objJson = objJson.put("ultima puntuacion",puntuacion);
                    salvarFichero("puntuaciones.json",objJson.toString());


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                binding.bandera.setVisibility(View.GONE);
                binding.radioGroup.setVisibility(View.GONE);
                binding.botonRespuesta.setVisibility(View.GONE);
                binding.textView.setText("No has conseguido batir el récord \n"+"Puntuación conseguida: "+puntuacion);
            }


        }
    }
        private void salvarFichero( String fichero,String texto) {
            FileOutputStream fos;
                try {
                      fos = getContext().openFileOutput(fichero, Context.MODE_PRIVATE);
                      fos.write(texto.getBytes());
                      fos.close();
                  } catch (Exception e) {
                      e.printStackTrace();
                     }
    }


    class Pregunta {
        private String nombre;
        private String modelo;
        private String respuestaCorrecta;
        private List<String> respuetas;

        public Pregunta(String nombre, String modelo, String respuestaCorrecta) {
            this.nombre = nombre;
            this.modelo = modelo;
            this.respuestaCorrecta = respuestaCorrecta;
        }

        public List<String> getRespuetas() {
            return respuetas;
        }

        public void setRespuetas(List<String> respuetas) {
            this.respuetas = respuetas;
        }
    }

    private Document leerXML(String fichero) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder constructor = factory.newDocumentBuilder();
        int idElemento = getResources().getIdentifier(fichero, "raw", getContext().getPackageName());
        Document doc = constructor.parse(getContext().getResources().openRawResource(idElemento));
        doc.getDocumentElement().normalize();
        return doc;
    }


    //AQUÍ SE PROCESA EL XML
    private void generarPreguntas(String fichero) throws Exception {
        todasLasPreguntas = new LinkedList<>();
        todasLasRespuestas = new LinkedList<>();
        Document doc = leerXML(fichero);
        Element documentElement = doc.getDocumentElement();
        NodeList paises = documentElement.getChildNodes();
        for(int i=0; i<paises.getLength(); i++) {
            if(paises.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element pais = (Element) paises.item(i);
                String nombre = pais.getAttribute("nombre");
                String nombreMostrar = pais.getElementsByTagName("nombre_mostrar").item(0).getTextContent();
                String modelo = pais.getElementsByTagName("modelo").item(0).getTextContent();
                todasLasPreguntas.add(new Pregunta(nombre, modelo, nombreMostrar));
                todasLasRespuestas.add(nombreMostrar);
            }
        }
    }
}