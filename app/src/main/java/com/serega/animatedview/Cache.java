package com.serega.animatedview;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.LruCache;

import java.util.List;

public class Cache {
	private final LruCache<Integer, Bitmap> cache;
	private static Cache instance;

	private Cache(){
		cache = new LruCache<>(5 * 1024 * 1024);
	}

	public static Cache getInstance(){
		if(instance == null){
			instance = new Cache();
		}

		return instance;
	}

	public void put(int key, Bitmap bitmap){
		cache.put(key, bitmap);
	}

	@Nullable
	public Bitmap get(int key){
		return cache.get(key);
	}
}
