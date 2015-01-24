package com.serega.example;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import com.serega.animatedview.AnimatedContainer;
import com.serega.animatedview.R;


public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final AnimatedContainer animatedContainer = (AnimatedContainer) findViewById(R.id.test_view);

		findViewById(R.id.btn_rotate).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				animatedContainer.flip();
			}
		});

		findViewById(R.id.btn_add_front).setOnClickListener(new View.OnClickListener() {
			private int front = R.mipmap.image_marilyn_1;

			@Override
			public void onClick(View v) {
				switch (front) {
					case R.mipmap.image_marilyn_1:
						front = R.mipmap.image_marilyn_2;
						break;

					case R.mipmap.image_marilyn_2:
						front = R.mipmap.image_marilyn_3;
						break;

					case R.mipmap.image_marilyn_3:
						front = R.mipmap.image_marilyn_4;
						break;

					case R.mipmap.image_marilyn_4:
						front = R.mipmap.image_marilyn_5;
						break;

					case R.mipmap.image_marilyn_5:
						front = R.mipmap.image_marilyn_1;
						break;
				}
				animatedContainer.nextBitmap(front);
			}
		});

		findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				animatedContainer.clear();
			}
		});
	}
}
