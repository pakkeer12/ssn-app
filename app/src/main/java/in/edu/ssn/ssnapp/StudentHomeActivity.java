package in.edu.ssn.ssnapp;

import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.suke.widget.SwitchButton;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import in.edu.ssn.ssnapp.adapters.DrawerAdapter;
import in.edu.ssn.ssnapp.adapters.ViewPagerAdapter;
import in.edu.ssn.ssnapp.database.DataBaseHelper;
import in.edu.ssn.ssnapp.fragments.BusAlertsFragment;
import in.edu.ssn.ssnapp.fragments.ClubFragment;
import in.edu.ssn.ssnapp.fragments.ExamCellFragment;
import in.edu.ssn.ssnapp.fragments.PlacementFragment;
import in.edu.ssn.ssnapp.fragments.StudentFeedFragment;
import in.edu.ssn.ssnapp.fragments.EventFragment;
import in.edu.ssn.ssnapp.models.Drawer;
import in.edu.ssn.ssnapp.utils.CommonUtils;
import in.edu.ssn.ssnapp.utils.Constants;
import in.edu.ssn.ssnapp.utils.FCMHelper;
import in.edu.ssn.ssnapp.utils.SharedPref;
import spencerstudios.com.bungeelib.Bungee;

public class StudentHomeActivity extends BaseActivity {
    private static final String TAG = "StudentHomeActivity";
    ImageView notifUI;
    CircleImageView userImageIV, iv_profile;
    DrawerLayout drawerLayout;
    ViewPager viewPager;
    TextView tv_name, tv_email;
    RelativeLayout layout_alum_notif;
    SwitchButton darkModeSwitch, notifSwitch;

    ListView lv_items;
    DrawerAdapter adapter;

    static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (darkModeEnabled) {
            setContentView(R.layout.activity_student_home_dark);
            clearLightStatusBar(this);
        }
        else
            setContentView(R.layout.activity_student_home);

        initUI();

        /******************************************************************/
        //Darkmode handle

        if (darkModeEnabled)
            darkModeSwitch.setChecked(true);
        else
            darkModeSwitch.setChecked(false);

        darkModeSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    darkModeEnabled = true;
                    SharedPref.putBoolean(getApplicationContext(), "dark_mode", darkModeEnabled);
                    finish();
                    startActivity(getIntent());
                    Bungee.fade(StudentHomeActivity.this);
                }
                else {
                    darkModeEnabled = false;
                    SharedPref.putBoolean(getApplicationContext(), "dark_mode", darkModeEnabled);
                    finish();
                    startActivity(getIntent());
                    Bungee.fade(StudentHomeActivity.this);
                }
            }
        });

        /******************************************************************/
        //Notification handle for Alumni

        if(SharedPref.getInt(getApplicationContext(),"clearance") == 2){
            layout_alum_notif.setVisibility(View.VISIBLE);
            notifSwitch.setChecked(SharedPref.getBoolean(getApplicationContext(), "switch_event"));
        }
        else
            layout_alum_notif.setVisibility(View.GONE);

        notifSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                SharedPref.putBoolean(getApplicationContext(),"switch_event", isChecked);
                notifSwitch.setChecked(isChecked);

                if(isChecked)
                    FCMHelper.SubscribeToTopic(StudentHomeActivity.this, Constants.Event);
                else
                    FCMHelper.UnSubscribeToTopic(StudentHomeActivity.this, Constants.Event);
            }
        });

        /******************************************************************/
        //What's new

        if(BuildConfig.VERSION_CODE > SharedPref.getInt(getApplicationContext(),"dont_delete","current_version_code")){
            SharedPref.putInt(getApplicationContext(),"dont_delete","current_version_code", BuildConfig.VERSION_CODE);
            CommonUtils.showWhatsNewDialog(this,darkModeEnabled);
        }

        //Remove on next update
        SharedPref.putBoolean(getApplicationContext(),"is_update_logout", true);

        /******************************************************************/

        userImageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        lv_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Drawer rs = (Drawer) parent.getItemAtPosition(position);
                switch (rs.getTitle()) {
                    case "News Feed":
                    case "Club Feed":
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case "Favourites":
                        startActivity(new Intent(getApplicationContext(), SavedPostActivity.class));
                        Bungee.slideLeft(StudentHomeActivity.this);
                        break;
                    case "Calendar":
                        if (!CommonUtils.alerter(getApplicationContext())) {
                            Intent i = new Intent(getApplicationContext(), PdfViewerActivity.class);
                            i.putExtra(Constants.PDF_URL, Constants.calendar);
                            startActivity(i);
                            Bungee.fade(StudentHomeActivity.this);
                        }
                        else {
                            Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
                            intent.putExtra("key", "home");
                            startActivity(intent);
                            Bungee.fade(StudentHomeActivity.this);
                        }
                        break;
                    case "Library Renewals":
                        if (CommonUtils.checkWifiOnAndConnected(getApplicationContext(), "ssn")) {
                            SharedPref.putString(getApplicationContext(), "url", "http://opac.ssn.net:8081/");
                            startActivity(new Intent(getApplicationContext(), WebViewActivity.class));
                            Bungee.slideLeft(StudentHomeActivity.this);
                        } else {
                            Toast toast = Toast.makeText(StudentHomeActivity.this, "Please connect to SSN wifi ", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        break;
                    case "AlmaConnect":
                        if (!CommonUtils.alerter(getApplicationContext())) {
                            SharedPref.putString(getApplicationContext(), "url", "https://ssn.almaconnect.com");
                            startActivity(new Intent(getApplicationContext(), WebViewActivity.class));
                            Bungee.slideLeft(StudentHomeActivity.this);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
                            intent.putExtra("key", "home");
                            startActivity(intent);
                            Bungee.fade(StudentHomeActivity.this);
                        }
                        break;
                    case "Notification Settings":
                        startActivity(new Intent(getApplicationContext(), NotificationSettings.class));
                        Bungee.slideLeft(StudentHomeActivity.this);
                        break;
                    case "Invite Friends":
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "Hello! Manage your internals, results & exam schedule with ease and Find your bus routes on the go! Click here to stay updated on department and club feeds: https://play.google.com/store/apps/details?id=" + StudentHomeActivity.this.getPackageName();
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                        break;
                    case "Rate Our App":
                        if (!CommonUtils.alerter(getApplicationContext())) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                        } else {
                            Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
                            intent.putExtra("key", "home");
                            startActivity(intent);
                            Bungee.fade(StudentHomeActivity.this);
                        }
                        break;
                    case "Make a Suggestion":
                        startActivity(new Intent(getApplicationContext(), FeedbackActivity.class));
                        Bungee.slideLeft(StudentHomeActivity.this);
                        break;
                    case "App Info":
                        startActivity(new Intent(getApplicationContext(), AppInfoActivity.class));
                        Bungee.slideLeft(StudentHomeActivity.this);
                        break;
                    case "Privacy Policy":
                        if (!CommonUtils.alerter(getApplicationContext())) {
                            SharedPref.putString(getApplicationContext(), "url", Constants.termsfeed);
                            startActivity(new Intent(getApplicationContext(), WebViewActivity.class));
                            Bungee.slideLeft(StudentHomeActivity.this);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
                            intent.putExtra("key", "home");
                            startActivity(intent);
                            Bungee.fade(StudentHomeActivity.this);
                        }
                        break;
                    case "Logout":
                        FirebaseAuth.getInstance().signOut();
                        CommonUtils.UnSubscribeToAlerts(getApplicationContext());
                        DataBaseHelper dbHelper = DataBaseHelper.getInstance(StudentHomeActivity.this);
                        dbHelper.dropAllTables();
                        SharedPref.removeAll(getApplicationContext());
                        SharedPref.putInt(getApplicationContext(), "dont_delete", "is_logged_in", 1);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                        Bungee.slideLeft(StudentHomeActivity.this);

                        break;
                }
            }
        });
    }

    /*********************************************************/

    void initUI() {
        notifUI = findViewById(R.id.notifUI);
        userImageIV = findViewById(R.id.userImageIV);
        iv_profile = findViewById(R.id.iv_profile);

        drawerLayout = findViewById(R.id.drawerLayout);
        viewPager = findViewById(R.id.viewPager);

        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);

        layout_alum_notif = findViewById(R.id.layout_alum_notif);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        notifSwitch = findViewById(R.id.notifSwitch);

        lv_items = findViewById(R.id.lv_items);
        adapter = new DrawerAdapter(this, new ArrayList<Drawer>());

        tv_name.setText(SharedPref.getString(getApplicationContext(), "name"));
        tv_email.setText(SharedPref.getString(getApplicationContext(), "email"));

        try {
            Glide.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString()).placeholder(R.drawable.ic_user_white).into(userImageIV);
            Glide.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString()).placeholder(R.drawable.ic_user_white).into(iv_profile);
        } catch (Exception e) {
            e.printStackTrace();
            Glide.with(this).load(SharedPref.getString(getApplicationContext(), "dp_url")).placeholder(R.drawable.ic_user_white).into(userImageIV);
            Glide.with(this).load(SharedPref.getString(getApplicationContext(), "dp_url")).placeholder(R.drawable.ic_user_white).into(iv_profile);
        }

        setUpDrawer();
        setupViewPager();
    }

    void setUpDrawer() {
        if (SharedPref.getInt(getApplicationContext(), "clearance") == 0) {
            adapter.add(new Drawer("News Feed", R.drawable.ic_feeds));
            adapter.add(new Drawer("Favourites", R.drawable.ic_fav));
        }
        else
            adapter.add(new Drawer("Club Feed", R.drawable.ic_feeds));

        adapter.add(new Drawer("AlmaConnect", R.drawable.ic_alumni));

        if (SharedPref.getInt(getApplicationContext(), "clearance") != 2) {
            adapter.add(new Drawer("Library Renewals", R.drawable.ic_book));
            adapter.add(new Drawer("Notification Settings", R.drawable.ic_notify_grey));
            adapter.add(new Drawer("Calendar", R.drawable.ic_calendar));
        }

        adapter.add(new Drawer("Make a Suggestion", R.drawable.ic_feedback));
        adapter.add(new Drawer("Invite Friends", R.drawable.ic_invite));
        adapter.add(new Drawer("Rate Our App", R.drawable.ic_star));
        adapter.add(new Drawer("Privacy Policy", R.drawable.ic_feedback));
        adapter.add(new Drawer("App Info", R.drawable.ic_info));
        adapter.add(new Drawer("Logout", R.drawable.ic_logout));
        lv_items.setAdapter(adapter);
    }

    void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if(SharedPref.getInt(getApplicationContext(),"clearance") == 0)
            adapter.addFragment(new StudentFeedFragment(), "News feed");
        adapter.addFragment(new ClubFragment(), "Club");
        if (SharedPref.getInt(getApplicationContext(), "year") == Integer.parseInt(Constants.fourth))
            adapter.addFragment(new PlacementFragment(), "Placement");
        if(SharedPref.getInt(getApplicationContext(),"clearance") != 2)
            adapter.addFragment(new BusAlertsFragment(), "Bus alert");
        if(SharedPref.getInt(getApplicationContext(),"clearance") == 0)
            adapter.addFragment(new ExamCellFragment(), "Exam cell");
        adapter.addFragment(new EventFragment(), "Event");
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);
    }

    /*********************************************************/

    @Override
    protected void onResume() {
        super.onResume();

        if (CommonUtils.alerter(getApplicationContext())) {
            Intent intent = new Intent(getApplicationContext(), NoNetworkActivity.class);
            intent.putExtra("key", "home");
            startActivity(intent);
            Bungee.fade(StudentHomeActivity.this);
        }
    }


    /*********************************************************/

    @Override
    public void onBackPressed() {
        if (count > 0) {
            count = 0;
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(startMain);
            finish();
        } else {
            count++;
            Toast toast = Toast.makeText(getApplicationContext(), "Press back once again to exit!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
