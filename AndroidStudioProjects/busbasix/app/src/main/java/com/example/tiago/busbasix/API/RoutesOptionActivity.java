package com.example.tiago.busbasix.API;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.tiago.busbasix.API.googleDirection.Route;
import com.example.tiago.busbasix.R;
import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoutesOptionActivity extends AppCompatActivity {

    ListView list;
    Spinner spinner;
    private String[] arraySpinner;
    ArrayList<Route> rotas;
    RouteAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_option);


        Bundle extras = getIntent().getExtras();
        //ArrayList<Route> routesList  = (ArrayList<Route>) extras.getSerializable("routesList");
        String teste = extras.getString("json");
        rotas = new ArrayList<>();

        Type listType = new TypeToken<ArrayList<Route>>(){}.getType();
        rotas = new Gson().fromJson(teste, listType);
        if (teste != null){
            Log.d("Teste", teste);
        }

        for (int i=0; i<rotas.size(); i++){
            Log.d("Teste", rotas.get(i).fare.text);
        }

        list = (ListView) findViewById(R.id.routesView);
        this.arraySpinner = new String[] {
                "Preço", "Tempo", "Acessibilidade", "Comunicação", "Confiança", "Conforto", "Conveniência", "Segurança"
        };
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_selectable_list_item, arraySpinner);
        spinner.setAdapter(spinner_adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String items = spinner.getSelectedItem().toString();
                if (items.equals("Preço")){
                    Log.d("Select", "Ordenando Itens por " + items);
                    Collections.sort(rotas, new Comparator<Route>() {
                        @Override
                        public int compare(Route o1, Route o2) {
                            return Double.compare(o1.fare.value, o2.fare.value);
                        }
                    });
                }
                if (items.equals("Tempo")){
                    Log.d("Select", "Ordenando Itens por " + items);
                    Collections.sort(rotas, new Comparator<Route>() {
                        @Override
                        public int compare(Route o1, Route o2) {
                            return Double.compare(o1.getRouteDuration().value, o2.getRouteDuration().value);
                        }
                    });
                }


                adapter = new RouteAdapter(rotas, RoutesOptionActivity.this);
                list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        RouteAdapter adapter =
                new RouteAdapter(rotas, this);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent intent = new Intent();
                                            intent.putExtra("listposition", position);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                    });
//        if (routesList == null){
//            Log.d("routeList", "null");
//        }
//        else{
//            Log.d("routeList size", String.valueOf(routesList.size()));
//        }

    }
}

class RouteAdapter extends BaseAdapter{

    private final List<Route> routes;
    private final Activity act;


    public RouteAdapter(List<Route> routes, Activity act){
        this.routes = routes;
        this.act = act;

    }

    @Override
    public int getCount() {
        return routes.size();
    }

    @Override
    public Object getItem(int position) {
        return routes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = act.getLayoutInflater()
                .inflate(R.layout.routelist, parent, false);
        if (position % 2 ==0){
            view.setBackgroundColor(Color.rgb(62, 116, 86)); //#3e7456 rgb(62, 116, 86) hsl(147, 30%, 35%)
        }
        else{
            view.setBackgroundColor(Color.rgb(90, 166, 123)); //#5aa67b hsl(146, 30%, 50%)
        }
        Route route = routes.get(position);

        TextView titulo = (TextView)
                view.findViewById(R.id.titulo_rota);
        //pegando as referências das Views
        TextView nome = (TextView)
                view.findViewById(R.id.nome_lista_rota);
        TextView descricao = (TextView)
                view.findViewById(R.id.preco_lista_rota);
//        ImageView imagem = (ImageView)
//                view.findViewById(R.id.imagem_lista_rota);
        TextView tempo = (TextView)
                view.findViewById(R.id.tempo_lista_rota);

        titulo.setText("Rota " + String.valueOf(position + 1));
        //populando as Views
        String title ="";
        for (int j=0; j<route.legs.size(); j++){
            for (int k=0; k<route.legs.get(j).steps.size(); k++){
                if ( route.legs.get(j).steps.get(k).transit_details!=null){
                    title += "Ônibus " + route.legs.get(j).steps.get(k).transit_details.line.short_name;
                    title += "\n";
                    //TODO: Tratar o caso da barca
                }
                else{
                    //TODO
                    //title += "Andar "+ route.legs.get(j).steps.get(k).duration.text;
                    title += route.legs.get(j).steps.get(k).html_instructions + " ("+ route.legs.get(j).steps.get(k).duration.text + ")";
                    title += "\n";
                }
//                if (k +1 != route.legs.get(j).steps.size()){
//                    title += " - ";
//                }
//                title += route.legs.get(j).steps.get(k).html_instructions;
//                title += "\n";
            }
        }

        nome.setText(title);
        descricao.setText(route.fare.text);
        tempo.setText(route.getRouteDuration().text);
//        imagem.setImageResource(R.drawable.com_facebook_button_icon);

        return view;
    }


}

//
//public class RateSearchTask extends AsyncTask<String, String, List<Rate>> {
//
//    //TODO
//
//}