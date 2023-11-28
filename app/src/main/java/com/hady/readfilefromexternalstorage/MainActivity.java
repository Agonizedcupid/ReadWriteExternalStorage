package com.hady.readfilefromexternalstorage;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int OPEN_DIRECTORY_REQUEST_CODE = 0xf11e;
    private FloatingActionButton openDirectoryButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openDirectoryButton = findViewById(R.id.fab_open_directory);
        listView = findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);

        listFilesInDirectory(Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AFolderA"));

        openDirectoryButton.setOnClickListener(v -> openDirectory());
    }

    private void openDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri directoryUri = data.getData();
                getContentResolver().takePersistableUriPermission(
                        directoryUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                //listFilesInDirectory(directoryUri);
            }
        }
    }

    private void listFilesInDirectory(Uri directoryUri) {
        Log.d("CHECK_DOCUMENT_URI", "listFilesInDirectory: "+directoryUri.toString());
        fileList.clear();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(directoryUri, DocumentsContract.getTreeDocumentId(directoryUri));

        try (Cursor cursor = getContentResolver().query(
                childrenUri,
                new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                null,
                null,
                null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                do {
                    String fileName = cursor.getString(columnIndex);
                    fileList.add(fileName);
                } while (cursor.moveToNext());
            }
        }
        adapter.notifyDataSetChanged();
    }
}
