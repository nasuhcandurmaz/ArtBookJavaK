package com.nasuhcandurmaz.artbookjava;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nasuhcandurmaz.artbookjava.databinding.RecylerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> { //art adapter oluşuturulduğunda benden bir array list istemesi için alttaki kodu girdik.

    ArrayList<Art> artArrayList;

    public ArtAdapter(ArrayList<Art> artArrayList){

        this.artArrayList = artArrayList;
    }


    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //burda bir artHolder oluşturmamız ve layoutla birbirine bağlmamız bekleniyor.
        RecylerRowBinding recylerRowBinding = RecylerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);
        return new ArtHolder(recylerRowBinding);


    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) { //bütün işlemler birbirine bağlanınca holder veriyor burası.
        holder.binding.recylerViewTextView.setText(artArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
                intent.putExtra("info", "old");
                intent.putExtra("artId", artArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return artArrayList.size(); //arraylistin içinde kaç eleman varsa onu göstericez.
    }

    public class ArtHolder extends RecyclerView.ViewHolder {
        private  RecylerRowBinding binding;
        public ArtHolder(RecylerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
