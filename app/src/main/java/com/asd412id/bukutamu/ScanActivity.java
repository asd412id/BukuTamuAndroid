package com.asd412id.bukutamu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    SharedPreferences configs;
    SharedPreferences.Editor editor;
    SharedPreferences getHistory;
    SharedPreferences.Editor history_editor;
    JSONArray listHistory;
    String api = "/api/v1/";
    String instansi;
    ProgressDialog progressDialog;
    EditText tujuan;
    EditText anggota;
    String url;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if( ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},5);
            }
        }

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

        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
    }

    @Override
    public void handleResult(Result rawResult) {
        progressDialog.setMessage("Silahkan Tunggu ...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String result = rawResult.getText();
        StringTokenizer split = new StringTokenizer(result,".");
        if (split.countTokens()!=4){
            serverNotFound("Kode QR tidak dikenali!");
            return;
        }
        String u1 = split.nextToken();
        instansi = split.nextToken();
        String u3 = split.nextToken();
        String u4 = split.nextToken();

        String merge = u4+u1;
        merge = merge.replace(u3,"=");

        byte[] data = Base64.decode(merge, Base64.DEFAULT);
        try {
            url = new String(data, "UTF-8");
            if (Patterns.WEB_URL.matcher(url).matches()){
                checkServer();
            }else {
                serverNotFound("Kode QR tidak dikenali!");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void checkServer() {
        String uri = url+api+"connect";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    if (response.getString("status").equals("connected")){
                        JSONObject instansi = response.getJSONObject("data");
                        editor.putString("url",url);
                        editor.putString("instansi", String.valueOf(instansi));
                        editor.commit();
                        serverFound(instansi);
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
                    serverNotFound("Server tidak ditemukan!");
                }else if (error.networkResponse!=null && error.networkResponse.data!=null){
                    VolleyError volleyError = new VolleyError(new String(error.networkResponse.data));
                    try {
                        JSONObject errorJson = new JSONObject(Objects.requireNonNull(volleyError.getMessage()));
                        serverNotFound(errorJson.getString("message"));
                    } catch (JSONException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
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
                params.put("Instansi", instansi);
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }

    private void serverFound(JSONObject instansi) {
        setContentView(R.layout.activity_scan);
        TextView nama_instansi = findViewById(R.id.nama_instansi);
        tujuan = findViewById(R.id.tujuan);
        anggota = findViewById(R.id.anggota);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        try {
            nama_instansi.setText(Html.fromHtml("Selamat Datang di<br><strong>"+instansi.getString("nama")+"</strong>"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject validate = validateForm();
                try {
                    if (validate.getString("status").equals("ok")){
                        String bersama = "";
                        if (anggota.getText().toString().trim().length()>1){
                            bersama = "<br><br><strong><u>Bersama dengan:</u></strong><br>" +
                                    anggota.getText().toString().replace("\n","<br>");
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                        builder.setTitle("Apakah Data Anda Sudah Benar?")
                                .setMessage(Html.fromHtml("Tujuan: <strong>"+tujuan.getText()+"</strong>" + bersama))
                                .setPositiveButton("Benar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        submitData();
                                    }
                                })
                                .setNegativeButton("Perbaiki", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                        builder.setTitle("Kesalahan!")
                                .setMessage(validate.getString("msg"))
                                .setPositiveButton("OK, Saya Mengerti!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        if (validate.getString("status").equals("tujuan")){
                            tujuan.requestFocus();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void submitData() {
        progressDialog.show();
        String uri = url+api+"visit";
        RequestQueue queue = Volley.newRequestQueue(this);

        String angg = null;
        if (anggota.getText().toString().trim().length()>0){
            angg = anggota.getText().toString().trim();
        }

        final JSONObject user_data = new JSONObject();
        try {
            user_data.put("nama",configs.getString("nama",null));
            user_data.put("alamat",configs.getString("alamat",null));
            user_data.put("telp",configs.getString("telp",null));
            user_data.put("pekerjaan",configs.getString("pekerjaan",null));
            user_data.put("_token",configs.getString("_token",null));
            user_data.put("tujuan",tujuan.getText());
            user_data.put("anggota",angg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    if (response.getString("status").equals("success")){
                        JSONObject guest = response.getJSONObject("data");
                        listHistory.put(guest);
                        history_editor.putString("list", String.valueOf(listHistory));
                        history_editor.commit();
                        editor.putString("guest", String.valueOf(guest));
                        editor.commit();
                        Intent intent = new Intent(ScanActivity.this,RatingActivity.class);
                        startActivity(intent);
                        finishAffinity();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
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
                params.put("Instansi", instansi);
                params.put("User-Data", String.valueOf(user_data));
                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }

    private JSONObject validateForm() {
        JSONObject status = new JSONObject();
        try {
            status.put("status","ok");
            if (tujuan.getText().toString().trim().length()<1){
                status.put("status","tujuan");
                status.put("msg","Tujuan kunjungan tidak boleh kosong!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }

    private void serverNotFound(String msg) {
        Intent intent = new Intent(this,HomeActivity.class);
        intent.putExtra("err",msg);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}
