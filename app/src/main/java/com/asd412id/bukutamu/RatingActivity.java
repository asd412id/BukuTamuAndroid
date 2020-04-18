package com.asd412id.bukutamu;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RatingActivity extends AppCompatActivity {
    SharedPreferences configs;
    SharedPreferences.Editor editor;
    SharedPreferences getHistory;
    SharedPreferences.Editor history_editor;
    JSONArray listHistory;
    String api = "/api/v1/";
    ProgressDialog progressDialog;
    RatingBar ratingBar;
    TextView instansi, ratingBarText;
    EditText kesan;
    Button btnSubmit, btnSnooze, btnDismiss;
    JSONObject _instansi;
    String ratingCount, kesanText;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        progressDialog = new ProgressDialog(this,ProgressDialog.THEME_HOLO_LIGHT);
        configs = getApplicationContext().getSharedPreferences("configs", Context.MODE_PRIVATE);
        editor = configs.edit();

        getHistory = getApplicationContext().getSharedPreferences("history", Context.MODE_PRIVATE);
        history_editor = getHistory.edit();

        try {
            if (getHistory.getString("list",null)!=null){
                listHistory = new JSONArray(getHistory.getString("list",null));
            }else{
                listHistory = new JSONArray();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        instansi = findViewById(R.id.instansi);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBarText = findViewById(R.id.ratingBarText);
        kesan = findViewById(R.id.kesan);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSnooze = findViewById(R.id.btnSnooze);
        btnDismiss = findViewById(R.id.btnDismiss);

        progressDialog.setMessage("Silahkan Tunggu ...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        String getInstansi = configs.getString("instansi",null);

        if (getInstansi!=null){
            try {
                _instansi = new JSONObject(getInstansi);
                instansi.setText(_instansi.getString("nama"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        btnSubmit.setEnabled(false);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if ((int) rating > 0){
                    btnSubmit.setEnabled(true);
                }else {
                    btnSubmit.setEnabled(false);
                }
                String rt;
                switch ((int) rating){
                    case 5:
                        rt = "Memuaskan";
                        break;
                    case 4:
                        rt = "Sangat Baik";
                        break;
                    case 3:
                        rt = "Baik";
                        break;
                    case 2:
                        rt = "Buruk";
                        break;
                    case 1:
                        rt = "Mengecewakan";
                        break;
                    default:
                        rt = "Rating Belum Dipilih";
                }
                ratingBarText.setText(rt);
            }
        });

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
                builder.setTitle("Konfirmasi!")
                        .setMessage("Yakin tidak ingin memberikan rating?")
                        .setPositiveButton("Tidak Ingin", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismissRating();
                            }
                        })
                        .setNegativeButton("Beri Rating", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRating();
            }
        });
    }

    private void dismissRating() {
        try {
            editor.remove("instansi");
            editor.remove("guest");
            editor.apply();
            AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
            builder.setMessage("Terima Kasih telah berkunjung di "+_instansi.getString("nama")+" ^_^")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(RatingActivity.this,HomeActivity.class));
                            finishAffinity();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendRating() {
        progressDialog.show();
        String url = configs.getString("url",null);
        assert url != null;
        if (Patterns.WEB_URL.matcher(url).matches()){
            ratingCount = null;
            kesanText = null;
            if (ratingBar.getRating()>0){
                ratingCount = String.valueOf(ratingBar.getRating());
            }
            if (kesan.getText().toString().trim().length()>0){
                kesanText = kesan.getText().toString().trim();
            }
            try {
                final JSONObject guest = new JSONObject(configs.getString("guest",null));
                String uri = url+api+"visit";
                RequestQueue queue = Volley.newRequestQueue(this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, uri, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        try {
                            if (response.getString("status").equals("success")){
                                JSONObject guest = response.getJSONObject("data");
                                listHistory.put(listHistory.length()-1,guest);
                                history_editor.putString("list", String.valueOf(listHistory));
                                history_editor.commit();
                                editor.remove("instansi");
                                editor.remove("guest");
                                editor.apply();
                                AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
                                builder.setTitle("Berhasil!")
                                        .setMessage("Terima Kasih telah berkunjung di "+_instansi.getString("nama")+" ^_^")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                startActivity(new Intent(RatingActivity.this,HomeActivity.class));
                                                finishAffinity();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (error instanceof NoConnectionError){
                            AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
                            builder.setTitle("Error")
                                    .setMessage("Jaringan Bermasalah!")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }else if (error.networkResponse!=null && error.networkResponse.data!=null){
                            VolleyError volleyError = new VolleyError(new String(error.networkResponse.data));
                            try {
                                JSONObject errorJson = new JSONObject(Objects.requireNonNull(volleyError.getMessage()));
                                AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
                                builder.setTitle("Error")
                                        .setMessage(errorJson.getString("message"))
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } catch (JSONException e) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RatingActivity.this);
                                builder.setTitle("Error")
                                        .setMessage("Jaringan Bermasalah!")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                e.printStackTrace();
                            }
                        }
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("Accept", "application/json");
                        try {
                            params.put("VisitID", guest.getString("uuid"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        params.put("Rating", ratingCount);
                        params.put("Kesan", kesanText);
                        return params;
                    }
                };
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonObjectRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
