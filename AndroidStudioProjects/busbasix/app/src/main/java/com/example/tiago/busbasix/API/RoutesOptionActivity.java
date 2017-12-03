package com.example.tiago.busbasix.API;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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
import android.widget.TextView;

import com.example.tiago.busbasix.API.googleDirection.Route;
import com.example.tiago.busbasix.R;
import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RoutesOptionActivity extends AppCompatActivity {

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_option);


        Bundle extras = getIntent().getExtras();
        //ArrayList<Route> routesList  = (ArrayList<Route>) extras.getSerializable("routesList");
        String teste = extras.getString("json");
        ArrayList<Route> rotas = new ArrayList<>();
        Type listType = new TypeToken<ArrayList<Route>>(){}.getType();
        rotas = new Gson().fromJson(teste, listType);
        if (teste != null){
            Log.d("Teste", teste);
        }

        for (int i=0; i<rotas.size(); i++){
            Log.d("Teste", rotas.get(i).fare.text);
        }

        list = (ListView) findViewById(R.id.routesView);

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
        Route route = routes.get(position);

        //pegando as referências das Views
        TextView nome = (TextView)
                view.findViewById(R.id.nome_lista_rota);
        TextView descricao = (TextView)
                view.findViewById(R.id.preco_lista_rota);
//        ImageView imagem = (ImageView)
//                view.findViewById(R.id.imagem_lista_rota);

        //populando as Views
        String title ="";
        for (int j=0; j<route.legs.size(); j++){
            for (int k=0; k<route.legs.get(j).steps.size(); k++){
                if ( route.legs.get(j).steps.get(k).transit_details!=null){
                    title += route.legs.get(j).steps.get(k).transit_details.line.short_name;
                }
                else{
                    //TODO
                  //  title += route.legs.get(j).steps.get(k).transit_details.line.short_name;
                }
                if (k != route.legs.get(j).steps.size()){
                    title += " - ";
                }
            }
        }

        nome.setText(title);
        descricao.setText(route.fare.text);
//        imagem.setImageResource(R.drawable.com_facebook_button_icon);

        return view;
    }
}