package com.example.patientdemo;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorSchedule extends AppCompatActivity {

    DrawerLayout drawerLayout;
    CircleImageView image;
    TextView nav_name;
    public static String UserName;

    TextView timer1, timer2;
    Button btn;

    int t1Hr, t1Min, t2Hr, t2Min;
    SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_schedule);

        UserName = getIntent().getStringExtra("UserName");

        drawerLayout = findViewById(R.id.drawer_layout);
        nav_name = findViewById(R.id.nav_name);
        nav_name.setText("Dr."+UserName);
        image = findViewById(R.id.nav_image);
        image.setImageResource(R.drawable.doctor_image);

        timer1 = findViewById(R.id.timer1);
        timer2 = findViewById(R.id.timer2);
        btn = findViewById(R.id.confirm_btn);

        timer1.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(DoctorSchedule.this,
                    (view, hourOfDay, minute) -> {
                        t1Hr = hourOfDay;
                        t1Min = minute;

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hr, t1Min);

                        String dateAndroid = android.text.format.DateFormat.format(
                                "kk:mm aa", calendar).toString();

                        timer1.setText(dateAndroid);
                    }, 12, 0, false);
            timePickerDialog.updateTime(t1Hr, t1Min);
            timePickerDialog.show();
        });

        timer2.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(DoctorSchedule.this,
                    (view, hourOfDay, minute) -> {
                        t2Hr = hourOfDay;
                        t2Min = minute;

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t2Hr, t2Min);

                        String dateAndroid = android.text.format.DateFormat.format(
                                "kk:mm aa", calendar).toString();

                        timer2.setText(dateAndroid);
                    }, 12, 0, false);
            timePickerDialog.updateTime(t2Hr, t2Min);
            timePickerDialog.show();
        });

        btn.setOnClickListener(v -> {
            int min = findMinutes();

            Calendar calendar = Calendar.getInstance();
            ArrayList<String> results = new ArrayList<>();
            sdf = new SimpleDateFormat("hh:mm aaa");

            calendar.set(0, 0, 0, t1Hr, t1Min);

            for (int i = 0; i < min; i++) {
                String  day1 = sdf.format(calendar.getTime());

                // add 15 minutes to the current time; the hour adjusts automatically!
                calendar.add(Calendar.MINUTE, 15);
                String day2 = sdf.format(calendar.getTime());

                //String day = day1 + " - " + day2;
                results.add(day1);
            }

            if(results.size() != 0) {
                for(String slot: results) {
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    DoctorDashboard.redirectActivity(DoctorSchedule.this, DoctorDashboard.class);
                    finish();
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Please select valid Time slot", Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressLint("SimpleDateFormat")
    private int findMinutes() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(0, 0, 0, t1Hr, t1Min);
        String d1 = android.text.format.DateFormat.format(
                "kk:mm", calendar).toString();

        calendar.set(0, 0, 0, t2Hr, t2Min);
        String d2 = android.text.format.DateFormat.format(
                "kk:mm", calendar).toString();

        sdf = new SimpleDateFormat("hh:mm");

        Date date1 = null;
        try {
            date1 = sdf.parse(d1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date2 = null;
        try {
            date2 = sdf.parse(d2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = date2.getTime() - date1.getTime();
        int min = (int) (diff / (1000 * 60));
        min = min/15;
        return (min);
    }

    public void ClickMenu(View view) {
        DoctorDashboard.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view) {
        DoctorDashboard.closeDrawer(drawerLayout);
    }

    public void ClickHome(View view) {
        DoctorDashboard.redirectActivity(this, DoctorDashboard.class);
    }

    public void ClickUpdateProfile(View view) {
        DoctorDashboard.redirectActivity(this, com.example.patientdemo.UpdateProfile.class);
    }

    public void ClickLogout(View view) {
        DoctorDashboard.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DoctorDashboard.closeDrawer(drawerLayout);
    }
}