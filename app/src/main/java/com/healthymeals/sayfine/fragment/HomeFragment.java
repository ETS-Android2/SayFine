package com.healthymeals.sayfine.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.healthymeals.sayfine.R;
import com.healthymeals.sayfine.activity.ArticleListActivity;
import com.healthymeals.sayfine.activity.BmiActivity;
import com.healthymeals.sayfine.activity.MenuListActivity;

public class HomeFragment extends Fragment {
    private Button btnMenu;
    private Button btnBmi;
    private Button btnArtikel;
    private RecyclerView recyclerViewPacket;
    private RecyclerView recyclerViewPromo;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnMenu = view.findViewById(R.id.btnMenu);
        btnBmi = view.findViewById(R.id.btnBmi);
        btnArtikel = view.findViewById(R.id.btnArticle);
        recyclerViewPacket = view.findViewById(R.id.recyclerViewPacket);
        recyclerViewPromo = view.findViewById(R.id.recyclerViewPromo);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MenuListActivity.class);
                startActivity(intent);
            }
        });

        btnBmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BmiActivity.class);
                startActivity(intent);
            }
        });

        btnArtikel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ArticleListActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
