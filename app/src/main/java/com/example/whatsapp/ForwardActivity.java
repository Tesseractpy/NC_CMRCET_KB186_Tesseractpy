package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ForwardActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;

    private FirebaseAuth mAuth;

    private ForwardTabsAccessorAdapter myForwardTabsAccessorAdapter;

    private DatabaseReference RootRef;

    private Button OkButton;
    private String Id, messageSenderID, category, msg;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.forward_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Forward");

        myViewPager = (ViewPager) findViewById(R.id.forward_tabs_pager);
        myForwardTabsAccessorAdapter = new ForwardTabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myForwardTabsAccessorAdapter);


        myTabLayout = (TabLayout) findViewById(R.id.forward_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        Bundle bundle = getIntent().getExtras();
        Id = Objects.requireNonNull(bundle).getString("Id");
        messageSenderID = Objects.requireNonNull(bundle).getString("messageSenderID");
        msg = Objects.requireNonNull(bundle).getString("msg");
        category = Objects.requireNonNull(bundle).getString("category");


    }

    public String getMyData() {
        return Id;
    }
    public String getMessageSenderID(){
        return messageSenderID;
    }
    public String getMsg(){
        return msg;
    }
    public String getCategory(){
    return category;
    }
}
