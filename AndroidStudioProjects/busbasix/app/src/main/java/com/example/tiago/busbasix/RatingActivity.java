package com.example.tiago.busbasix;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class RatingActivity extends AppCompatActivity {

    private Button submit;
    private TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        Random random = new Random();
        int randomRate = random.nextInt(6);

        submit = (Button)findViewById(R.id.rate_submit);
        title = (TextView)findViewById(R.id.rate_title);

        switch (randomRate){
                case 0:
                title.setText("Confiança");
                break;
                case 1:
                title.setText("Conforto");
                break;
                case 2:
                title.setText("Conveniência");
                break;
                case 3:
                title.setText("Comunicação");
                break;
                 case 4:
                title.setText("Acessibilidade");
                break;
                 case 5:
                title.setText("Segurança");
                break;

        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Submeter a Avaliação para o servidor
            }
        });
    }
}
