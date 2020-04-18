package com.asd412id.bukutamu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HistoryFragment extends Fragment {

    private JSONArray dataHistory;
    private SimpleDateFormat format;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        ListView listHistory = root.findViewById(R.id.list_history);

        SharedPreferences getHistory = getActivity().getApplicationContext().getSharedPreferences("history", Context.MODE_PRIVATE);

        try {
            dataHistory = new JSONArray(getHistory.getString("list", String.valueOf(new JSONArray())));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<String> data = new ArrayList<>();

        dataHistory = sortJsonArray(dataHistory);

        if (dataHistory.length()>0){
            for (int i = 0; i < dataHistory.length();i++){
                try {
                    JSONObject item = (JSONObject) dataHistory.get(i);
                    String[] waktu = item.getString("start_visit").split(" ");
                    String tgl = waktu[0]+" "+waktu[1]+" "+waktu[2];
                    data.add(item.getJSONObject("instansi").getString("nama") + "\n"
                            + tgl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            listHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        JSONObject item = (JSONObject) dataHistory.get(position);
                        String bersama = "";
                        if (item.has("anggota") && !item.isNull("anggota")){
                            bersama = "<br><br><strong><u>Bersama dengan:</u></strong><br>" +
                                    item.getJSONArray("anggota").join("<br>").replace("\"","");
                        }
                        String[] waktu = item.getString("start_visit").split(" ");
                        String tgl = waktu[0]+" "+waktu[1]+" "+waktu[2];
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(item.getJSONObject("instansi").getString("nama"))
                                .setMessage(Html.fromHtml("Tanggal: <strong>"+tgl+"</strong><br>" +
                                        "Pukul: <strong>"+waktu[3]+"</strong><br>" +
                                        "Tujuan: <strong>"+item.getString(("tujuan"))+"</strong>" + bersama))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else {
            data.add("Tidak ada riwayat kunjungan!");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, data);
        listHistory.setAdapter(adapter);

        return root;
    }

    private static JSONArray sortJsonArray(JSONArray array) {
        List<JSONObject> jsons = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                jsons.add(array.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(jsons, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                String lid = null;
                String rid = null;
                try {
                    lid = lhs.getString("id");
                    rid = rhs.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                assert lid != null;
                assert rid != null;
                return rid.compareTo(lid);
            }
        });
        return new JSONArray(jsons);
    }
}
