package com.whytefarms.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.whytefarms.R;

import java.util.List;

public class BannerSliderAdapter extends RecyclerView.Adapter<BannerSliderAdapter.BannerSliderViewHolder> {
    private final List<ViewPagerItemImage> sliderImages;

    public BannerSliderAdapter(List<ViewPagerItemImage> images) {
        this.sliderImages = images;
    }

    @NonNull
    @Override
    public BannerSliderAdapter.BannerSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BannerSliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_banner, parent, false
                ));
    }

    @Override
    public void onBindViewHolder(@NonNull BannerSliderAdapter.BannerSliderViewHolder holder, int position) {
        holder.setImage(sliderImages.get(position));
    }

    @Override
    public int getItemCount() {
        return sliderImages.size();
    }

    static class BannerSliderViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageView imageView;

        BannerSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.banner_image);
        }

        void setImage(ViewPagerItemImage sliderItems) {
            Glide.with(imageView.getContext())
                    .load(sliderItems.getImage())
                    .skipMemoryCache(false)
                    .centerCrop()
                    .dontAnimate()
                    .dontTransform()
                    .placeholder(R.drawable.ic_picture_placeholder)
                    .priority(Priority.IMMEDIATE)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(imageView);
        }
    }
}