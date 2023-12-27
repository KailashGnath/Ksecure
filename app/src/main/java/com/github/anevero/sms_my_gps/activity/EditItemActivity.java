package com.github.anevero.sms_my_gps.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.github.anevero.sms_my_gps.R;
import com.github.anevero.sms_my_gps.data.Constants;
import com.github.anevero.sms_my_gps.data.Preferences;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class EditItemActivity extends AppCompatActivity {
  private EditText senderNameInput;
  private EditText senderNumInput;
  private EditText prefixInput;
  private TextInputLayout senderInputLayout;
  private TextInputLayout prefixInputLayout;
  private CheckBox ignoreCheckBox;

  private Button pickContactButton;
  private Button deleteButton;
  private Button saveButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppCompatDelegate.setDefaultNightMode(Preferences.getTheme(this));
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_item);
    Objects.requireNonNull(getSupportActionBar())
           .setDisplayHomeAsUpEnabled(true);

    senderNameInput = findViewById(R.id.sender_name_input);
    senderNumInput = findViewById(R.id.sender_num_input);
    prefixInput = findViewById(R.id.prefix_input);
    senderInputLayout = findViewById(R.id.sender_num_input_layout);
    prefixInputLayout = findViewById(R.id.prefix_input_layout);
    ignoreCheckBox = findViewById(R.id.ignore_requests_checkbox);
    pickContactButton = findViewById(R.id.pick_contact_button);

    deleteButton = findViewById(R.id.delete_button);
    saveButton = findViewById(R.id.save_button);

    if (!isContactsPermissionGranted()) {
      pickContactButton.setEnabled(false);
      requestContactsPermission();
    }
    pickContactButton.setOnClickListener(v -> startContactPickerActivity());

    int itemId = getIntent().getIntExtra(Constants.ITEM_ID_KEY, -1);
    if (itemId == -1) {
      // We're adding a new item, not editing existing.
      setTitle(R.string.add_item);
      deleteButton.setText(R.string.cancel_button);
    } else {
      // We're editing existing item and must fill the fields with current info.
      String senderName = getIntent().getStringExtra(Constants.SENDER_NAME_KEY);
      String senderNum = getIntent().getStringExtra(Constants.SENDER_NUM_KEY);
      String prefix = getIntent().getStringExtra(Constants.MESSAGE_KEY);
      boolean ignore = getIntent().getBooleanExtra(Constants.IGNORE_KEY, false);

      if (senderName != null) {
        // We guard against setting to null in case we're importing
        // ListItems from legacy versions which don't include the sender
        // name.
        senderNameInput.setText(senderName);
      }
      senderNumInput.setText(senderNum);
      prefixInput.setText(prefix);
      ignoreCheckBox.setChecked(ignore);

      // Only changing the prefix or the name is allowed.
      senderNumInput.setEnabled(false);
      pickContactButton.setEnabled(false);
      pickContactButton.setVisibility(View.GONE);
    }

    senderNumInput.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) {
        senderInputLayout.setError(null);
      }
    });

    prefixInput.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) {
        prefixInputLayout.setError(null);
      }
    });

    deleteButton.setOnClickListener(v -> {
      Intent result = new Intent(this, MainActivity.class);
      result.putExtra(Constants.ITEM_ID_KEY, getIntent().getIntExtra(
              Constants.ITEM_ID_KEY, -1));
      setResult(Constants.EDIT_ITEM_REMOVE_RESULT_CODE, result);
      finish();
    });

    saveButton.setOnClickListener(v -> {
      if (senderNumInput.getText().toString().isEmpty() ||
          prefixInput.getText().toString().isEmpty()) {
        if (senderNumInput.getText().toString().isEmpty()) {
          senderInputLayout.setError(getString(R.string.field_empty_label));
        }
        if (prefixInput.getText().toString().isEmpty()) {
          prefixInputLayout.setError(getString(R.string.field_empty_label));
        }
        return;
      }

      Intent result = new Intent(this, MainActivity.class);
      result.putExtra(Constants.ITEM_ID_KEY, getIntent().getIntExtra(
              Constants.ITEM_ID_KEY, -1));
      result.putExtra(Constants.SENDER_NAME_KEY,
                      senderNameInput.getText().toString().trim());
      result.putExtra(Constants.SENDER_NUM_KEY,
                      senderNumInput.getText().toString().trim());
      result.putExtra(Constants.MESSAGE_KEY,
                      prefixInput.getText().toString().trim());
      result.putExtra(Constants.IGNORE_KEY,
                      ignoreCheckBox.isChecked());
      setResult(Constants.EDIT_ITEM_ADD_RESULT_CODE, result);
      finish();
    });
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private boolean isContactsPermissionGranted() {
    return checkSelfPermission(Constants.CONTACTS_PERMISSION[0]) ==
           PackageManager.PERMISSION_GRANTED;
  }

  private void requestContactsPermission() {
    if (!isContactsPermissionGranted()) {
      requestPermissions(Constants.CONTACTS_PERMISSION,
                         Constants.CONTACTS_PERMISSION_REQUEST_CODE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == Constants.CONTACTS_PERMISSION_REQUEST_CODE) {
      if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        return;
      }
      recreate();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,
                                  @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.CONTACT_PICKER_REQUEST_CODE) {
      processContactPickerResult(resultCode, data);
    }
  }

  private void startContactPickerActivity() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
    startActivityForResult(intent, Constants.CONTACT_PICKER_REQUEST_CODE);
  }

  private void processContactPickerResult(int resultCode,
                                          @Nullable Intent data) {
    if (resultCode != RESULT_OK || data == null) {
      return;
    }

    Uri contactUri = data.getData();
    if (contactUri == null) {
      return;
    }

    String[] projection = new String[]{
            ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY};
    Cursor cursor = getContentResolver().query(
            contactUri, projection, null, null, null);
    if (cursor == null) {
      return;
    }

int rows = cursor.getCount();
String columnNames[] = cursor.getColumnNames();

    if (cursor.moveToFirst()) {
java.util.ArrayList<String> columnValues = new java.util.ArrayList<String>();
for (int i = 0; i < columnNames.length; ++i) {
    columnValues.add(cursor.getString(i));
}

      int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
      String number = cursor.getString(numberIndex);
      number = number.replaceAll("[^+0-9]", "");
      senderNumInput.setText(number);

      numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
      senderNameInput.setText(cursor.getString(numberIndex));
    }

    cursor.close();
  }
}
