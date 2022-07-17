package com.example.patientdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReSchedule extends AppCompatActivity {

    DrawerLayout drawerLayout;
    TextView nav_name;

    ArrayList<String> slotTimes = new ArrayList<>();
    ArrayList<String> docTimings = new ArrayList<>();
    ArrayList<String> docReserved = new ArrayList<>();

    HashMap<String, ArrayList<String>> itemList = new HashMap<>();
    BookAppointmentAdapter adapter;

    RecyclerView slotCards;
    CircleImageView img;
    TextView name, time, price;
    Button btn;
    EditText remarks;

    String dr_name, slot, dr_price;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef, ref1, ref2;
    private ValueEventListener mListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule);

        drawerLayout = findViewById(R.id.drawer_layout);
        nav_name = findViewById(R.id.nav_name);
        nav_name.setText(PatientDashboard.UserName);

        dr_name = getIntent().getExtras().getString("dr_name");
        slot = getIntent().getExtras().getString("slot");

        btn = findViewById(R.id.re_btn);

        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("doctors");

        img = findViewById(R.id.re_dr_image);
        name = findViewById(R.id.re_dr_name);
        remarks = findViewById(R.id.re_remarks);
        time = findViewById(R.id.re_dr_time);
        price = findViewById(R.id.re_dr_price);
        slotCards = findViewById(R.id.reschedule_slots);

        ref1 = FirebaseDatabase.getInstance().getReference("doctors").child(dr_name);

        ref2 = FirebaseDatabase.getInstance().getReference("patients").child(PatientDashboard.UserName).child("active_app");

        name.setText(dr_name);
        Glide.with(this)
                .load(getIntent().getExtras().getString("dr_img"))
                .error(R.drawable.doctor_image).into(img);

        ref2.child(dr_name + slot).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dr_price = snapshot.child("price").getValue(String.class);
                remarks.setText(snapshot.child("remarks").getValue(String.class));
                time.setText(snapshot.child("time").getValue(String.class));
                price.setText(snapshot.child("price").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                slotTimes.clear();
                docTimings.clear();
                docReserved.clear();
                for (DataSnapshot slot: snapshot.getChildren()) {
                    String stand = slot.getValue(String.class);

                    slotTimes.add(slot.getKey());
                    if (stand.equals("Available"))
                        docTimings.add(slot.getKey());
                    else if (stand.equals("Reserved"))
                        docReserved.add(slot.getKey());
                }
                Collections.sort(slotTimes, new Comparator<String>() {
                    @SuppressLint("SimpleDateFormat")
                    @Override
                    public int compare(String o1, String o2) {
                        try {
                            return new SimpleDateFormat("hh:mm a").parse(o1).compareTo(new SimpleDateFormat("hh:mm a").parse(o2));
                        } catch (ParseException e) {
                            return 0;
                        }
                    }
                });
                slotCards(slotTimes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mRef.child(dr_name).child("slots").addValueEventListener(mListener);
    }

    private void slotCards(ArrayList<String> slotTimes) {
        slotCards.setHasFixedSize(true);
        slotCards.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<String> sectionList = new ArrayList<>();
        sectionList.add("Today's Slots");
        itemList.put(sectionList.get(0), slotTimes);
        adapter = new BookAppointmentAdapter(this, sectionList, itemList, docTimings, docReserved, dr_name);
        GridLayoutManager manager = new GridLayoutManager(this, 3);

        slotCards.setLayoutManager(manager);
        adapter.setLayoutManager(manager);
        slotCards.setAdapter(adapter);

        btn.setOnClickListener(v -> {
            String sItem = adapter.getSelected();
            if (sItem != null) {

                ref2.child(dr_name + slot).removeValue();

                ref1.child("slots").child(slot).setValue("Available");
                ref1.child("my_app").child(slot).removeValue();

                ref1.child("slots").child(sItem).setValue("Booked");

                ref2.child(dr_name + sItem).child("name").setValue(dr_name);
                ref2.child(dr_name + sItem).child("time").setValue(sItem);
                ref2.child(dr_name + sItem).child("price").setValue(dr_price);
                ref2.child(dr_name + sItem).child("remarks").setValue(remarks.getText().toString());
                ref2.child(dr_name + sItem).child("image").setValue("https://firebasestorage.googleapis.com/v0/b/my-application-6d359.appspot.com/o/images%2Fkakashi.jpg?alt=media&token=348a2050-641e-44f8-9c3f-80c740c917a3");

                ref1.child("my_app").child(sItem).child("patient").setValue(PatientDashboard.UserName);
                ref1.child("my_app").child(sItem).child("remarks").setValue(remarks.getText().toString());

                finish();
            }
            else
                Toast.makeText(this, "Please select a Slot", Toast.LENGTH_SHORT).show();
        });
    }


    // ---------------------------- NAVIGATION BAR ---------------------------- //
    public void ClickMenu(View view) {
        PatientDashboard.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view) {
        PatientDashboard.closeDrawer(drawerLayout);
    }

    public void ClickHome(View view) {
        PatientDashboard.redirectActivity(this, PatientDashboard.class);
    }

    public void ClickUpdateProfile(View view) {
        PatientDashboard.redirectActivity(this, UpdateProfilePatient.class);
    }

    public void ClickLogout(View view) {
        PatientDashboard.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PatientDashboard.closeDrawer(drawerLayout);
    }
    // ----------------------------------------------------------------------- //
}