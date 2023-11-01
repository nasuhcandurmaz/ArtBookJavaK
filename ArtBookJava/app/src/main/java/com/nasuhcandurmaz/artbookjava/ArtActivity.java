package com.nasuhcandurmaz.artbookjava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.nasuhcandurmaz.artbookjava.databinding.ActivityArtBinding;
import com.nasuhcandurmaz.artbookjava.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher; //neden string çünkü izinlerde String ile uğraşıyoruz
    //bunları oncreate altında dökümantasyonu yapmamız gerekiyor.

    //aktivite sonucu başlatıcı. Aktivite açıp galeriye gidip görsel seçince ne olacağını yazmak mı istiytoruz?
    //izin isteyip o iznin verildiğinde ne olduğuynu yazmak mı istiyoruz. Bu kodu kullanıcaz.

    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();

        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")) {
            //new art
            binding.nameText.setText("");
            binding.artistName.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);

            binding.imageView.setImageResource(R.drawable.selectimage);

        } else {
            int artId = intent.getIntExtra("artId",0);
            binding.button.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artId)}); //burdaki artid id deki soru işareti yerine geçicek

                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()) {
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistName.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte [] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);





                }
                cursor.close();

            }catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    public void save(View view) {

        String name = binding.nameText.getText().toString();
        String artistName = binding.artistName.getText().toString();
        String year = binding.yearText.getText().toString();

        //selected imagi küçültmemiz lazım. SQL tabanında kaudetmek için çünkü boyut sınırı var.

        Bitmap smallImage = makeSmallerImage(selectedImage,300); //bu image'in size kısmıyla ilgili yani seçtiğimiz resmin boyutu w,h ayarladık ya ondan sonraki
        //aşamada oluşturacağımız şey.

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //bunu da byte dizisine çevirmek için kullanıyoruz. 1e 0a çevirmek için.
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream); //yukarıda yaptığımız smallImage'i çağırıyoruz ve compress liyoruz., 1lere 0 lara çevirecek şey outputstream
        byte[] byteArray = outputStream.toByteArray(); //byte array oluşturmuştuk yukarıda. bu dizini çağırmak için, yani görseli 1 lere 0 lara çevirmiş oluyoruz.

        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");


            String sqlString ="INSERT INTO arts (artname, paintername, year, image) VALUES(?, ?, ?, ?)"; //değerlerni ne gireceğiz? values? değer yazmayacağız. değer yerine daha sonradan çalışacak bir şey oluşturucaz.
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString); //binding işlemlerini bağlama işlemlerini kolaylaştıran yapı veri için.
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4,byteArray);

            sqLiteStatement.execute();




        }catch (Exception e ){
            e.printStackTrace();
        }


        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //bundan önceki bütün aktivileri kapat nere gideceksem orayı aç
        startActivity(intent);




    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            //landscape image yatay görsel
            width = maximumSize;
            height =(int) (width / bitmapRatio);


        } else {
            //portrait image - dikey görsel

            height = maximumSize;
            width =(int) (height * bitmapRatio);
        }


        return image.createScaledBitmap(image, width,height,true);
    }


    public void selectImage (View view){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //Android +33 üstü için geçerli kod.-> READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){

                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission(izin isteme işlemi) izin verildi mi verilmedi mi verildiyse tamam verilemdiyse izin isticez.
                            //izin verilmemişse bu işlem oluyor.
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();;

                } else {
                    //request permission(izin isteme işlemi) izin verildi mi verilmedi mi verildiyse tamam verilemdiyse izin isticez.
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                }

        } else {
            //Android 32-->READ_EXTERNAL_STROGAE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission(izin isteme işlemi) izin verildi mi verilmedi mi verildiyse tamam verilemdiyse izin isticez.
                            //izin verilmemişse bu işlem oluyor.
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();

                } else {
                    //request permission(izin isteme işlemi) izin verildi mi verilmedi mi verildiyse tamam verilemdiyse izin isticez.
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                }

        }


        }

        //andoride galariye git demeden önce izin var mı diye kontrol etmemiz gerekiyor bir defaya mahsus.
        //kullanıcı izin verirse otomatik kaydedecek.




        } else {
            //galeriye gitme işlemi
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //BEN GALERİYE GİDİCEM ORDAN BİR GÖRSEL ALICAM PİCK EDİCEM VE GERİ GELİCEM DEMEK.
            //burada daha öncede izin verilmiş.
            activityResultLauncher.launch(intentToGallery);



        }


    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) { //kullanıcının galerisinde gittik. bana cevap veriyor bu kod sayesinde
                if (result.getResultCode() == RESULT_OK){
                    //Kullanıcı galeriden bir şeyler seçmiştir.
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null){
                        Uri imageData = intentFromResult.getData();  //kullanıcının verdiği görselin nerede kayıtlı old. gösteriyor
                        //binding.imageView.setImageURI(imageData); //kullanıcının seçtiği şeyi bunun içerisinde göstermek.
                        //uri kullancının telefonundaki resim ama bu benim için data, veri değil. en nihayetinde bunu kaydetmem gerekecek.
                        //bu resmi bit mape çevirmem lazım.

                        try { //uygulamayı çökertme gel şurdaki kodu çalıştır. Uygulamanın çökmesini engellemek için.

                            if (Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }






                        }catch (Exception e) {
                            e.printStackTrace();

                        }


                    }
                }

            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //permission granted - izin verildi
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);


                } else {
                    //permission denied - izin verilmedi
                    Toast.makeText(ArtActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


}