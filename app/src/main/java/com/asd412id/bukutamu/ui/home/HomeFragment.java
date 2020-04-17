package com.asd412id.bukutamu.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.asd412id.bukutamu.R;
import com.asd412id.bukutamu.ScanActivity;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    Bundle bundle;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences configs = this.getActivity().getSharedPreferences("configs", MODE_PRIVATE);
        bundle = getActivity().getIntent().getExtras();

        TextView nama = root.findViewById(R.id.nama);
        TextView hints = root.findViewById(R.id.hints);
        ImageView homeImg = root.findViewById(R.id.home_img);

        if (bundle!=null){
            String err = bundle.getString("err",null);
            if (err!=null){
                hints.setText(err);
                hints.setTextColor(Color.RED);
                homeImg.setImageResource(R.drawable.error);
            }
        }

        nama.setText(configs.getString("nama",null).toUpperCase());
        Button btnScan = root.findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }
}
