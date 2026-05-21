package com.codeconnect.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ResumeBuilderActivity - Enter your own info, select a template, toggle sections,
 * preview and export as PDF.
 */
public class ResumeBuilderActivity extends AppCompatActivity {

    private int selectedTemplate = 1;
    private MaterialCardView cardTemplate1, cardTemplate2, cardTemplate3;
    private View radio1, radio2, radio3;
    private SwitchMaterial switchSkills, switchEducation, switchProjects;
    private LinearLayout layoutSkillsPreview, layoutEducationPreview;
    private TextView tvResumeName, tvResumeRole, tvResumeSubtitle;
    private CircleImageView ivResumePhoto;

    // Input fields
    private EditText etFullName, etRole, etLocation, etPhone, etEmail;
    private EditText etSkills, etExperience1Title, etExperience1Desc;
    private EditText etExperience2Title, etExperience2Desc;
    private EditText etEduDegree, etEduInstitute, etEduYear;
    private EditText etProject1, etProject2;

    // Preview content views
    private TextView tvPreviewSkills, tvPreviewExp1Title, tvPreviewExp1Desc;
    private TextView tvPreviewExp2Title, tvPreviewExp2Desc;
    private TextView tvPreviewEduDegree, tvPreviewEduInstitute;
    private TextView tvPreviewProject1, tvPreviewProject2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_builder);

        initViews();
        loadUserData();
        setupInputListeners();
        setupTemplateSelection();
        setupVisibilityToggles();
        setupExportButton();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        cardTemplate1 = findViewById(R.id.cardTemplate1);
        cardTemplate2 = findViewById(R.id.cardTemplate2);
        cardTemplate3 = findViewById(R.id.cardTemplate3);
        radio1 = findViewById(R.id.radio1);
        radio2 = findViewById(R.id.radio2);
        radio3 = findViewById(R.id.radio3);
        switchSkills = findViewById(R.id.switchSkills);
        switchEducation = findViewById(R.id.switchEducation);
        switchProjects = findViewById(R.id.switchProjects);
        layoutSkillsPreview = findViewById(R.id.layoutSkillsPreview);
        layoutEducationPreview = findViewById(R.id.layoutEducationPreview);
        tvResumeName = findViewById(R.id.tvResumeName);
        tvResumeRole = findViewById(R.id.tvResumeRole);
        tvResumeSubtitle = findViewById(R.id.tvResumeSubtitle);
        ivResumePhoto = findViewById(R.id.ivResumePhoto);

        // Input fields
        etFullName = findViewById(R.id.etFullName);
        etRole = findViewById(R.id.etRole);
        etLocation = findViewById(R.id.etLocation);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etSkills = findViewById(R.id.etSkills);
        etExperience1Title = findViewById(R.id.etExperience1Title);
        etExperience1Desc = findViewById(R.id.etExperience1Desc);
        etExperience2Title = findViewById(R.id.etExperience2Title);
        etExperience2Desc = findViewById(R.id.etExperience2Desc);
        etEduDegree = findViewById(R.id.etEduDegree);
        etEduInstitute = findViewById(R.id.etEduInstitute);
        etEduYear = findViewById(R.id.etEduYear);
        etProject1 = findViewById(R.id.etProject1);
        etProject2 = findViewById(R.id.etProject2);

        // Preview views
        tvPreviewSkills = findViewById(R.id.tvPreviewSkills);
        tvPreviewExp1Title = findViewById(R.id.tvPreviewExp1Title);
        tvPreviewExp1Desc = findViewById(R.id.tvPreviewExp1Desc);
        tvPreviewExp2Title = findViewById(R.id.tvPreviewExp2Title);
        tvPreviewExp2Desc = findViewById(R.id.tvPreviewExp2Desc);
        tvPreviewEduDegree = findViewById(R.id.tvPreviewEduDegree);
        tvPreviewEduInstitute = findViewById(R.id.tvPreviewEduInstitute);
        tvPreviewProject1 = findViewById(R.id.tvPreviewProject1);
        tvPreviewProject2 = findViewById(R.id.tvPreviewProject2);
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();
            if (displayName != null && !displayName.isEmpty()) {
                etFullName.setText(displayName);
                tvResumeName.setText(displayName);
            }
            if (email != null && !email.isEmpty()) {
                etEmail.setText(email);
            }
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl())
                        .placeholder(android.R.drawable.ic_menu_myplaces)
                        .into(ivResumePhoto);
            }
        }
    }

    private void setupInputListeners() {
        // Live update the preview as user types
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { updatePreview(); }
        };

        etFullName.addTextChangedListener(watcher);
        etRole.addTextChangedListener(watcher);
        etLocation.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);
        etEmail.addTextChangedListener(watcher);
        etSkills.addTextChangedListener(watcher);
        etExperience1Title.addTextChangedListener(watcher);
        etExperience1Desc.addTextChangedListener(watcher);
        etExperience2Title.addTextChangedListener(watcher);
        etExperience2Desc.addTextChangedListener(watcher);
        etEduDegree.addTextChangedListener(watcher);
        etEduInstitute.addTextChangedListener(watcher);
        etEduYear.addTextChangedListener(watcher);
        etProject1.addTextChangedListener(watcher);
        etProject2.addTextChangedListener(watcher);
    }

    private void updatePreview() {
        String name = etFullName.getText().toString().trim();
        String role = etRole.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        tvResumeName.setText(name.isEmpty() ? "Your Name" : name);
        tvResumeRole.setText(role.isEmpty() ? "Your Role / Title" : role);

        String contactLine = "";
        if (!location.isEmpty()) contactLine += location;
        if (!phone.isEmpty()) contactLine += (contactLine.isEmpty() ? "" : " · ") + phone;
        if (!email.isEmpty()) contactLine += (contactLine.isEmpty() ? "" : " · ") + email;
        tvResumeSubtitle.setText(contactLine.isEmpty() ? "Location · Phone · Email" : contactLine);

        // Skills preview
        String skills = etSkills.getText().toString().trim();
        tvPreviewSkills.setText(skills.isEmpty() ? "e.g. Java, Python, Android, SQL" : skills);

        // Experience preview
        String exp1Title = etExperience1Title.getText().toString().trim();
        String exp1Desc = etExperience1Desc.getText().toString().trim();
        tvPreviewExp1Title.setText(exp1Title.isEmpty() ? "Company · Job Title" : exp1Title);
        tvPreviewExp1Desc.setText(exp1Desc.isEmpty() ? "Describe your role and achievements" : exp1Desc);

        String exp2Title = etExperience2Title.getText().toString().trim();
        String exp2Desc = etExperience2Desc.getText().toString().trim();
        tvPreviewExp2Title.setText(exp2Title.isEmpty() ? "Company · Job Title (optional)" : exp2Title);
        tvPreviewExp2Desc.setText(exp2Desc.isEmpty() ? "" : exp2Desc);

        // Education preview
        String degree = etEduDegree.getText().toString().trim();
        String institute = etEduInstitute.getText().toString().trim();
        String year = etEduYear.getText().toString().trim();
        tvPreviewEduDegree.setText(degree.isEmpty() ? "Degree / Course" : degree);
        String eduLine = institute;
        if (!year.isEmpty()) eduLine += (eduLine.isEmpty() ? "" : " · ") + year;
        tvPreviewEduInstitute.setText(eduLine.isEmpty() ? "Institution · Year" : eduLine);

        // Projects preview
        tvPreviewProject1.setText(etProject1.getText().toString().trim().isEmpty() ? "Project 1 name & description" : etProject1.getText().toString().trim());
        tvPreviewProject2.setText(etProject2.getText().toString().trim().isEmpty() ? "" : etProject2.getText().toString().trim());
    }

    private void setupTemplateSelection() {
        cardTemplate1.setOnClickListener(v -> selectTemplate(1));
        cardTemplate2.setOnClickListener(v -> selectTemplate(2));
        cardTemplate3.setOnClickListener(v -> selectTemplate(3));
    }

    private void selectTemplate(int template) {
        selectedTemplate = template;
        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        int dividerColor = ContextCompat.getColor(this, R.color.divider);
        int cardBg = ContextCompat.getColor(this, R.color.bg_card);
        int cardBgElevated = ContextCompat.getColor(this, R.color.bg_card_elevated);

        cardTemplate1.setStrokeColor(dividerColor); cardTemplate1.setStrokeWidth(1);
        cardTemplate1.setCardBackgroundColor(cardBg);
        radio1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dividerColor));

        cardTemplate2.setStrokeColor(dividerColor); cardTemplate2.setStrokeWidth(1);
        cardTemplate2.setCardBackgroundColor(cardBg);
        radio2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dividerColor));

        cardTemplate3.setStrokeColor(dividerColor); cardTemplate3.setStrokeWidth(1);
        cardTemplate3.setCardBackgroundColor(cardBg);
        radio3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dividerColor));

        MaterialCardView selected;
        View selectedRadio;
        switch (template) {
            case 2: selected = cardTemplate2; selectedRadio = radio2; break;
            case 3: selected = cardTemplate3; selectedRadio = radio3; break;
            default: selected = cardTemplate1; selectedRadio = radio1; break;
        }
        selected.setStrokeColor(primaryColor); selected.setStrokeWidth(2);
        selected.setCardBackgroundColor(cardBgElevated);
        selectedRadio.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
    }

    private void setupVisibilityToggles() {
        switchSkills.setOnCheckedChangeListener((b, checked) ->
                layoutSkillsPreview.setVisibility(checked ? View.VISIBLE : View.GONE));
        switchEducation.setOnCheckedChangeListener((b, checked) ->
                layoutEducationPreview.setVisibility(checked ? View.VISIBLE : View.GONE));
        switchProjects.setOnCheckedChangeListener((b, checked) -> {
            View projectsLayout = findViewById(R.id.layoutProjectsPreview);
            if (projectsLayout != null) projectsLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (etProject1 != null) etProject1.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (etProject2 != null) etProject2.setVisibility(checked ? View.VISIBLE : View.GONE);
        });
    }

    private void setupExportButton() {
        findViewById(R.id.btnExportPdf).setOnClickListener(v -> generatePdf());
    }

    private void generatePdf() {
        String name = etFullName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name before exporting", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(22);
        titlePaint.setColor(0xFF1A1A2E);
        titlePaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint();
        bodyPaint.setTextSize(11);
        bodyPaint.setColor(0xFF333333);

        Paint accentPaint = new Paint();
        accentPaint.setTextSize(13);
        accentPaint.setColor(0xFF6C63FF);
        accentPaint.setFakeBoldText(true);

        Paint subPaint = new Paint();
        subPaint.setTextSize(10);
        subPaint.setColor(0xFF666666);

        Paint linePaint = new Paint();
        linePaint.setColor(0xFFCCCCCC);
        linePaint.setStrokeWidth(1);

        String role = etRole.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Header
        canvas.drawText(name, 40, 55, titlePaint);
        if (!role.isEmpty()) canvas.drawText(role, 40, 75, accentPaint);

        String contactInfo = "";
        if (!location.isEmpty()) contactInfo += location;
        if (!phone.isEmpty()) contactInfo += (contactInfo.isEmpty() ? "" : "  |  ") + phone;
        if (!email.isEmpty()) contactInfo += (contactInfo.isEmpty() ? "" : "  |  ") + email;
        if (!contactInfo.isEmpty()) canvas.drawText(contactInfo, 40, 93, subPaint);

        canvas.drawLine(40, 105, 555, 105, linePaint);

        int y = 128;

        // Skills section
        if (switchSkills.isChecked()) {
            String skills = etSkills.getText().toString().trim();
            if (!skills.isEmpty()) {
                canvas.drawText("SKILLS", 40, y, accentPaint);
                y += 18;
                // Word wrap for skills
                String[] words = skills.split(",");
                StringBuilder line = new StringBuilder();
                for (String word : words) {
                    String w = word.trim();
                    if (line.length() + w.length() > 70) {
                        canvas.drawText(line.toString().trim(), 40, y, bodyPaint);
                        y += 16;
                        line = new StringBuilder(w + ", ");
                    } else {
                        line.append(w).append(", ");
                    }
                }
                if (line.length() > 0) {
                    String last = line.toString().trim();
                    if (last.endsWith(",")) last = last.substring(0, last.length() - 1);
                    canvas.drawText(last, 40, y, bodyPaint);
                    y += 16;
                }
                y += 10;
            }

            // Experience
            String exp1Title = etExperience1Title.getText().toString().trim();
            String exp1Desc = etExperience1Desc.getText().toString().trim();
            if (!exp1Title.isEmpty()) {
                canvas.drawText("EXPERIENCE", 40, y, accentPaint);
                y += 18;
                canvas.drawText(exp1Title, 40, y, bodyPaint);
                y += 15;
                if (!exp1Desc.isEmpty()) {
                    canvas.drawText(exp1Desc, 40, y, subPaint);
                    y += 15;
                }
            }
            String exp2Title = etExperience2Title.getText().toString().trim();
            String exp2Desc = etExperience2Desc.getText().toString().trim();
            if (!exp2Title.isEmpty()) {
                canvas.drawText(exp2Title, 40, y, bodyPaint);
                y += 15;
                if (!exp2Desc.isEmpty()) {
                    canvas.drawText(exp2Desc, 40, y, subPaint);
                    y += 15;
                }
            }
            y += 10;
        }

        // Education section
        if (switchEducation.isChecked()) {
            String degree = etEduDegree.getText().toString().trim();
            String institute = etEduInstitute.getText().toString().trim();
            String year = etEduYear.getText().toString().trim();
            if (!degree.isEmpty()) {
                canvas.drawText("EDUCATION", 40, y, accentPaint);
                y += 18;
                canvas.drawText(degree, 40, y, bodyPaint);
                y += 15;
                String eduLine = institute;
                if (!year.isEmpty()) eduLine += (eduLine.isEmpty() ? "" : " · ") + year;
                if (!eduLine.isEmpty()) { canvas.drawText(eduLine, 40, y, subPaint); y += 15; }
                y += 10;
            }
        }

        // Projects section
        if (switchProjects.isChecked()) {
            String p1 = etProject1.getText().toString().trim();
            String p2 = etProject2.getText().toString().trim();
            if (!p1.isEmpty()) {
                canvas.drawText("PROJECTS", 40, y, accentPaint);
                y += 18;
                canvas.drawText(p1, 40, y, bodyPaint);
                y += 15;
                if (!p2.isEmpty()) { canvas.drawText(p2, 40, y, bodyPaint); y += 15; }
            }
        }

        canvas.drawText("Template: " + getTemplateName(), 40, y + 10, subPaint);

        document.finishPage(page);

        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "CodeConnect");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "Resume_" + name.replace(" ", "_") + ".pdf");
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Could not save PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private String getTemplateName() {
        switch (selectedTemplate) {
            case 2: return "Minimalist Classic";
            case 3: return "Two-Column Split";
            default: return "Technical Modern";
        }
    }
}
