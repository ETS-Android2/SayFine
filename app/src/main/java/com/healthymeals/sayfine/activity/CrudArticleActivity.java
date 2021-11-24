package com.healthymeals.sayfine.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.healthymeals.sayfine.R;
import com.healthymeals.sayfine.adapter.crud.CrudArticleAdapter;
import com.healthymeals.sayfine.model.Article;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CrudArticleActivity extends AppCompatActivity {
    private TextInputLayout inputTitle;
    private TextInputLayout inputDesctiption;
    private ImageButton imgArticleThumb;
    private Button btnUpload;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;

    private Uri thumbUrl;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private CrudArticleAdapter adapter;
    private ArrayList<Article> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_article);
        inputTitle = findViewById(R.id.inputTitle);
        inputDesctiption = findViewById(R.id.inputDescription);
        imgArticleThumb = findViewById(R.id.imgArticleThumb);
        btnUpload = findViewById(R.id.btnUpload);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mendaftarkan akun Anda...");

        firebaseFirestore = FirebaseFirestore.getInstance();
        list = new ArrayList<>();
        getArticles();
        adapter = new CrudArticleAdapter(this, this , list);
        recyclerView.setAdapter(adapter);

        imgArticleThumb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ImagePicker.with(CrudArticleActivity.this)
                        .crop(3f, 2f)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFireStore(
                        inputTitle.getEditText().getText().toString(),
                        inputDesctiption.getEditText().getText().toString(),
                        thumbUrl
                );
            }
        });
    }

    private void saveToFireStore(String title, String description, Uri thumbUrl){
        if (!title.isEmpty() && !description.isEmpty() && thumbUrl != null){
            String articleId = firebaseFirestore.collection("Articles").document().getId();
            storageReference = FirebaseStorage.getInstance().getReference().child("article/" + articleId + ".jpg");
            storageReference.putFile(thumbUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Timestamp timestamp = Timestamp.now();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("title", title);
                            map.put("description", description);
                            map.put("thumbUrl", uri.toString());
                            map.put("timestamp", timestamp);
                            firebaseFirestore.collection("Articles").document(articleId).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Article article = new Article(articleId, title, description, uri.toString(), timestamp);
                                        Toast.makeText(CrudArticleActivity.this, articleId, Toast.LENGTH_LONG).show();
                                        adapter.notifyDataSetChanged();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    progressDialog.show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(CrudArticleActivity.this, "Uploading Failed !!", Toast.LENGTH_SHORT).show();
                }
            });
        } else Toast.makeText(this, "Empty Fields not Allowed", Toast.LENGTH_SHORT).show();
    }

    /*private void asaveToFireStore(String title, String description, Uri thumbUrl){
        if (!title.isEmpty() && !description.isEmpty() && thumbUrl != null){
            HashMap<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("description", description);
            firebaseFirestore.collection("Articles").add(map)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(CrudArticleActivity.this, "Data Saved !!", Toast.LENGTH_SHORT).show();
                                storageReference = FirebaseStorage.getInstance().getReference().child("article/" + task.getResult().getId() + ".jpg");
                                storageReference.putFile(thumbUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                map.put("thumbUrl", uri.toString());
                                                map.put("timestamp", FieldValue.serverTimestamp());
                                                firebaseFirestore.collection("Articles").document(task.getResult().getId()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });

                                                Article article = new Article(task.getResult().getId(), title, description, uri.toString(), Timestamp.now());
                                                list.add(article);
                                                adapter.notifyDataSetChanged();
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }
                                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                        progressDialog.show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CrudArticleActivity.this, "Uploading Failed !!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CrudArticleActivity.this, "Failed !!", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else Toast.makeText(this, "Empty Fields not Allowed", Toast.LENGTH_SHORT).show();
    }*/

    public void getArticles() {
        firebaseFirestore.collection("Articles")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            if (progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            Toast.makeText(CrudArticleActivity.this, "Gagal mendapatkan data artikel!", Toast.LENGTH_SHORT).show();
                            Log.e("Firestore error",error.getMessage());
                            return;
                        }

                        for (DocumentChange dc : value.getDocumentChanges()){
                            Article article;
                            article = new Article(dc.getDocument().getId(), dc.getDocument().getString("title"), dc.getDocument().getString("description"), dc.getDocument().getString("thumbUrl"), dc.getDocument().getTimestamp("timestamp"));
                            switch (dc.getType()) {
                                case ADDED:
                                    list.add(article);
                                    break;
                                case MODIFIED:

                                    break;
                                case REMOVED:

                                    break;
                            }
                            adapter.notifyDataSetChanged();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            thumbUrl = data.getData();
            imgArticleThumb.setImageURI(thumbUrl);
        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }

    }
}