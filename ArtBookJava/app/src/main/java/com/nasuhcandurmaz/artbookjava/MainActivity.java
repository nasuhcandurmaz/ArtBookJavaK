package com.nasuhcandurmaz.artbookjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.nasuhcandurmaz.artbookjava.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter; //art adapter'imizi burada çağırdık.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        artArrayList = new ArrayList<>();

        binding.recylerView.setLayoutManager(new GridLayoutManager(this));
        artAdapter = new ArtAdapter(artArrayList);
        binding.recylerView.setAdapter(artAdapter);

        getData();
    }


    private void getData() {

        try {

            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts", null);

            int nameIx = cursor.getColumnIndex("artname"); //buradaki art name artactivitydeki yazdığımız CREATE TABLE IF... komutundaki name ile aynı olmak zorunda.

            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                Art art = new Art(name, id); //bu art'ı bir diziye kaydedelim çübkü recylerviewde göstermemiz laızm. onun için ArrayList dizisi oluşturduk. oncreate altında da boş
                //olarak tanımladık.

                artArrayList.add(art);


            }

            artAdapter.notifyDataSetChanged();
            cursor.close();



        }catch ( Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.art_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }



    //menüye tıklanınce ne olacak?


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_art) {
            Intent intent = new Intent(this,ArtActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }
}