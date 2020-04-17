package com.asd412id.bukutamu;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationActivity extends AppCompatActivity {
    private EditText nama,pekerjaan,telp,alamat;
    private ProgressDialog progressDialog;
    private SharedPreferences.Editor editor;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        SharedPreferences configs = getApplicationContext().getSharedPreferences("configs", MODE_PRIVATE);
        editor = configs.edit();

        nama = findViewById(R.id.nama);
        pekerjaan = findViewById(R.id.pekerjaan);
        telp = findViewById(R.id.telp);
        alamat = findViewById(R.id.alamat);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject validate = validateForm();
                try {
                    if (!validate.getString("status").equals("ok")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
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

                        switch (validate.getString("status")){
                            case "pekerjaan":
                                pekerjaan.requestFocus();
                                break;
                            case "telp":
                                telp.requestFocus();
                                break;
                            case "alamat":
                                alamat.requestFocus();
                                break;
                            default:
                                nama.requestFocus();
                        }
                    }else {
                        progressDialog = new ProgressDialog(RegistrationActivity.this,ProgressDialog.THEME_HOLO_LIGHT);
                        progressDialog.setMessage("Menyimpan data ...");
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        saveData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveData() {
        String _token = Helper.randomString(100);
        editor.putString("nama",nama.getText().toString());
        editor.putString("pekerjaan",pekerjaan.getText().toString());
        editor.putString("telp",telp.getText().toString());
        editor.putString("alamat",alamat.getText().toString());
        editor.putString("_token",_token);
        editor.commit();
        progressDialog.dismiss();
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private JSONObject validateForm() {
        JSONObject status = new JSONObject();
        try {
            status.put("status","ok");
            if (nama.getText().toString().trim().length()<1){
                status.put("status","nama");
                status.put("msg","Nama tidak boleh kosong!");
            }else if (pekerjaan.getText().toString().trim().length()<1){
                status.put("status","pekerjaan");
                status.put("msg","Pekerjaan tidak boleh kosong!");
            }else if (telp.getText().toString().trim().length()<1){
                status.put("status","telp");
                status.put("msg","Nomor Handphone tidak boleh kosong!");
            }else if (alamat.getText().toString().trim().length()<1){
                status.put("status","alamat");
                status.put("msg","Alamat tidak boleh kosong!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }
}
