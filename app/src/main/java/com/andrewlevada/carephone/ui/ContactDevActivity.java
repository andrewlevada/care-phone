package com.andrewlevada.carephone.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.andrewlevada.carephone.R;

public class ContactDevActivity extends AppCompatActivity {
    AutoCompleteTextView subjectTextView;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_dev);

        // Find views by ids
        subjectTextView = findViewById(R.id.text_subject);
        editText = findViewById(R.id.edit_text);

        // Setup auto complete text view
        String[] subjects = getResources().getStringArray(R.array.contact_dev_subjects);
        ArrayAdapter<String> subjectsAdapter =
                new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, subjects);
        subjectTextView.setAdapter(subjectsAdapter);

        // Process send onclick
        findViewById(R.id.send_button).setOnClickListener(view -> trySend());
    }

    private void trySend() {
        if (subjectTextView.getText().length() == 0) {
            subjectTextView.setError(getString(R.string.contact_dev_select_subject));
            return;
        }

        if (editText.getText().length() == 0) {
            editText.setError(getString(R.string.contact_dev_enter_text));
            return;
        }
    }
}
