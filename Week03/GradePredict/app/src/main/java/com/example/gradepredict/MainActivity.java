package com.example.gradepredict;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    EditText cgpaEditText;
    EditText iqEditText;
    EditText profileScoreEditText;
    Button predictButton;
    Button retrofitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intializeID();

        intializeAction();

    }

    private void intializeAction() {

        String url = "http://10.0.2.2:5000/predict";

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String data = jsonObject.getString("result");
                                    if(data.equals("1")){
                                        Toast.makeText(getApplicationContext(), "Passed", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }){

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String,String> params = new HashMap<String,String>();
                        params.put("cgpa", cgpaEditText.getText().toString());
                        params.put("iq", iqEditText.getText().toString());
                        params.put("profile_score", profileScoreEditText.getText().toString());
                        return params;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(stringRequest);

            }
        });


        retrofitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GradeService gradeService = DemoRetrofit.getInstance().create(GradeService.class);
                gradeService.getGrade(cgpaEditText.getText().toString(), iqEditText.getText().toString(), profileScoreEditText.getText().toString())
                        .enqueue(new Callback<Result>() {
                            @Override
                            public void onResponse(Call<Result> call, retrofit2.Response<Result> response) {
                                if(response.body().getResult().equals("1")){
                                    Toast.makeText(getApplicationContext(), "Passed", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();

                                }
                            }

                            @Override
                            public void onFailure(Call<Result> call, Throwable t) {

                            }
                        });
            }
        });
    }

    private void intializeID() {
        cgpaEditText = findViewById(R.id.cpgaID);
        iqEditText = findViewById(R.id.iqID);
        profileScoreEditText = findViewById(R.id.profile_score_id);
        predictButton = findViewById(R.id.predictId);
        retrofitButton = findViewById(R.id.retrofitButton);
    }
}