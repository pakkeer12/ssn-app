package in.edu.ssn.ssnapp;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import android.animation.LayoutTransition;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import in.edu.ssn.ssnapp.adapters.ImageAdapter;
import in.edu.ssn.ssnapp.models.Post;
import in.edu.ssn.ssnapp.utils.SharedPref;

public class PostDetailsActivity extends BaseActivity {

    Post post;
    ImageView backIV, userImageIV, shareIV;
    ViewPager imageViewPager;
    TextView tv_author, tv_position, tv_time, tv_title, tv_current_image,tv_attachments;
    SocialTextView tv_description;
    ChipGroup attachmentsChipGroup, yearChipGroup, deptChipGroup;
    RelativeLayout textGroupRL, layout_receive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        initUI();

        post = getIntent().getParcelableExtra("post");
        String time = getIntent().getStringExtra("time");

        tv_title.setText(post.getTitle().trim());
        tv_description.setText(post.getDescription().trim());
        tv_author.setText(post.getAuthor().trim());
        tv_position.setText(post.getPosition().trim());
        Picasso.get().load(post.getAuthor_image_url()).placeholder(R.drawable.ic_user_white).into(userImageIV);

        if(post.getImageUrl()!=null && post.getImageUrl().size()!=0){
            imageViewPager.setVisibility(View.VISIBLE);
            tv_current_image.setVisibility(View.VISIBLE);
            final ImageAdapter imageAdapter = new ImageAdapter(getApplicationContext(), post.getImageUrl(),false);
            imageViewPager.setAdapter(imageAdapter);

            if(post.getImageUrl().size()==1)
                tv_current_image.setVisibility(View.GONE);
            else {
                tv_current_image.setVisibility(View.VISIBLE);
                tv_current_image.setText(String.valueOf(1)+" / "+String.valueOf(post.getImageUrl().size()));
                imageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int pos) {
                        tv_current_image.setText(String.valueOf(pos+1)+" / "+String.valueOf(post.getImageUrl().size()));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
        }
        else {
            imageViewPager.setVisibility(View.GONE);
            tv_current_image.setVisibility(View.GONE);
        }

        ArrayList<String> fileName = post.getFileName();
        ArrayList<String> fileUrl = post.getFileUrl();

        if(fileName != null && fileName.size() > 0){
            tv_attachments.setVisibility(View.VISIBLE);
            attachmentsChipGroup.setVisibility(View.VISIBLE);

            for(int i=0; i<fileName.size(); i++){
                Chip chip = getFilesChip(attachmentsChipGroup, fileName.get(i), fileUrl.get(i));
                attachmentsChipGroup.addView(chip);
            }
        }
        else {
            tv_attachments.setVisibility(View.GONE);
            attachmentsChipGroup.setVisibility(View.GONE);
        }

        List<String> depts = post.getDept();
        List<String> year = post.getYear();

        if(SharedPref.getInt(getApplicationContext(),"clearance") == 1){
            if(depts != null && depts.size() != 0){
                layout_receive.setVisibility(View.VISIBLE);

                for(int i=0; i<depts.size(); i++){
                    Chip dept_chip = getDataChip(deptChipGroup, depts.get(i).toUpperCase());
                    deptChipGroup.addView(dept_chip);
                }

                for(int i=0; i<year.size(); i++){
                    Chip year_chip = getDataChip(yearChipGroup, year.get(i));
                    yearChipGroup.addView(year_chip);
                }
            }
            else
                layout_receive.setVisibility(View.GONE);
        }
        else
            layout_receive.setVisibility(View.GONE);

        tv_time.setText(time);

        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tv_description.setOnHyperlinkClickListener(new SocialView.OnClickListener() {
            @Override
            public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {
                String url = text.toString();
                if (!url.startsWith("http") && !url.startsWith("https")) {
                    url = "http://" + url;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        shareIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Hello! New posts from " + post.getAuthor().trim() + ". Check it out: http://ssnportal.cf/" + post.getId();
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
    }

    void initUI(){
        backIV = findViewById(R.id.backIV);
        userImageIV = findViewById(R.id.userImageIV);
        tv_author = findViewById(R.id.tv_author);
        tv_position = findViewById(R.id.tv_position);
        tv_time = findViewById(R.id.tv_time);
        tv_title = findViewById(R.id.tv_title);
        tv_description = findViewById(R.id.tv_description);
        shareIV = findViewById(R.id.shareIV);
        tv_current_image = findViewById(R.id.currentImageTV);
        tv_attachments = findViewById(R.id.tv_attachment);
        imageViewPager = findViewById(R.id.viewPager);
        attachmentsChipGroup = findViewById(R.id.attachmentsGroup);
        yearChipGroup = findViewById(R.id.yearGroup);
        deptChipGroup = findViewById(R.id.deptGroup);
        textGroupRL = findViewById(R.id.textGroupRL);
        layout_receive = findViewById(R.id.layout_receive);
        textGroupRL.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    /*****************************************************************/
    //Files

    private Chip getFilesChip(final ChipGroup entryChipGroup, final String file_name, final String url) {
        final Chip chip = new Chip(this);
        chip.setChipDrawable(ChipDrawable.createFromResource(this, R.xml.file_name_chip));
        chip.setText(file_name);
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostDetailsActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();

                try {
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    Uri downloadUri = Uri.parse(url);
                    DownloadManager.Request request = new DownloadManager.Request(downloadUri);

                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/SSN App/", file_name)
                            .setTitle(file_name).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                    dm.enqueue(request);
                }
                catch (Exception ex) {
                    Toast.makeText(getApplicationContext(),"Download failed!", Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            }
        });

        return chip;
    }

    /*****************************************************************/
    //Year & Dept

    private Chip getDataChip(final ChipGroup entryChipGroup, final String data) {
        final Chip chip = new Chip(this);
        chip.setChipDrawable(ChipDrawable.createFromResource(this, R.xml.year_chip));
        chip.setChipCornerRadius(30f);
        chip.setText(data);
        return chip;
    }
}
