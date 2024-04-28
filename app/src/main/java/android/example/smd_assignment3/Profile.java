package android.example.smd_assignment3;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;

public class Profile extends Activity {

    private EditText editUsername;
    private EditText editPassword;
    private EditText editUrl;
    private Button btnAdd;
    private ListView listViewEntries;
    private ArrayList<String> entryList;
    private ArrayList<Long> entryIds;
    private ArrayAdapter<String> adapter;
    private String user;
    private PasswordManagerDB db;
    private int selectedPosition = -1;
    private Button btnRecycleBin;

    // Boolean to track whether to show entries from the recycle bin or not
    private boolean showRecycleBin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editUrl = findViewById(R.id.editUrl);
        btnAdd = findViewById(R.id.btnAdd);
        listViewEntries = findViewById(R.id.listViewEntries);
        btnRecycleBin = findViewById(R.id.btnRecycle);

        db = new PasswordManagerDB(this);

        entryList = new ArrayList<>();
        entryIds = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, entryList);
        listViewEntries.setAdapter(adapter);

        Intent intent = getIntent();
        user = intent.getStringExtra("uname");

        btnRecycleBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEntries();
                showRecycleBin = !showRecycleBin;

            }
        });

        loadEntries();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editUsername.getText().toString();
                String password = editPassword.getText().toString();
                String url = editUrl.getText().toString();

                String entry = "Username: " + username + "\nPassword: " + password + "\nURL: " + url;

                entryList.add(entry);

                db.open();
                long entryId = db.addEntry(url, username, password, user);
                db.close();

                entryIds.add(entryId);

                adapter.notifyDataSetChanged();

                editUsername.setText("");
                editPassword.setText("");
                editUrl.setText("");
            }
        });

        listViewEntries.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            showOptionsDialog();
        });
    }

    private void loadEntries() {
        entryList.clear();
        entryIds.clear();

        db.open();
        Cursor cursor;
        if (showRecycleBin) {
            // Load entries from the recycle bin
            cursor = db.getRecycleBinEntries(user);
        } else {
            // Load user entries
            cursor = db.getUserEntries(user);
        }

        if (cursor.moveToFirst()) {
            do {
                long entryId = cursor.getLong(cursor.getColumnIndex(PasswordManagerDB.ROW_ENTRY_ID));
                String username = cursor.getString(cursor.getColumnIndex(PasswordManagerDB.ROW_ID_NAME));
                String password = cursor.getString(cursor.getColumnIndex(PasswordManagerDB.ROW_PASSCODE));
                String url = cursor.getString(cursor.getColumnIndex(PasswordManagerDB.ROW_WEBSITE_URL));
                String entry = "Username: " + username + "\nPassword: " + password + "\nURL: " + url;
                entryList.add(entry);
                entryIds.add(entryId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        adapter.notifyDataSetChanged();
    }

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options")
                .setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            editEntry();
                        } else if (which == 1) {
                            confirmDelete();
                        }
                    }
                });
        builder.create().show();
    }

    private void editEntry() {
        String selectedEntry = entryList.get(selectedPosition);
        long entryId = entryIds.get(selectedPosition);

        String existingUsername, existingPassword, existingUrl;
        String[] lines = selectedEntry.split("\\n");
        existingUsername = lines[0].replace("Username: ", "");
        existingPassword = lines[1].replace("Password: ", "");
        existingUrl = lines[2].replace("URL: ", "");

        editUsername.setText(existingUsername);
        editPassword.setText(existingPassword);
        editUrl.setText(existingUrl);

        btnAdd.setOnClickListener(v -> {
            String updatedUsername = editUsername.getText().toString();
            String updatedPassword = editPassword.getText().toString();
            String updatedUrl = editUrl.getText().toString();
            String updatedEntry = "Username: " + updatedUsername + "\nPassword: " + updatedPassword + "\nURL: " + updatedUrl;

            entryList.set(selectedPosition, updatedEntry);
            adapter.notifyDataSetChanged();

            db.open();
            db.updateEntry(entryId, updatedUrl, updatedUsername, updatedPassword);
            db.close();

            editUsername.setText("");
            editPassword.setText("");
            editUrl.setText("");
        });
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedPosition >= 0 && selectedPosition < entryList.size()) {
                            long entryId = entryIds.get(selectedPosition);

                            db.open();
                            if (showRecycleBin) {
                                // Permanently delete from recycle bin
                                db.removeEntryFromRecycleBin(entryId);
                            } else {
                                // Move to recycle bin
                                db.moveToRecycleBin(entryId);
                            }
                            db.close();

                            entryList.remove(selectedPosition);
                            entryIds.remove(selectedPosition);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);
        builder.create().show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear recycle bin when activity is destroyed
        if (!showRecycleBin) {
            db.open();
            db.clearRecycleBin();
            db.close();
        }
    }
}
