### Simple view with change image animation.
![](https://github.com/StudentNSK/AnimatedView/raw/master/screen.gif)

### Usage
    <com.serega.animatedview.AnimatedContainer
        xmlns:animatedContainer="http://schemas.android.com/apk/res-auto"
        android:layout_width="250dp"
        android:layout_height="wrap_content"
        android:id="@+id/test_view"
        android:layout_centerHorizontal="true"
        animatedContainer:backgroundColor="@android:color/white"
        animatedContainer:flipSpeed="fast"
        animatedContainer:animateBitmapChange="true"
        animatedContainer:maxDelayValueInMillis="1000"
        animatedContainer:squaresCountHorizontal="4" />

### Set bitmaps
### in xml:
        animatedContainer:drawableFront="@drawable/bitmap"
        animatedContainer:drawableBack="@drawable/bitmap"

### in code:
        nextBitmap(int resourceId);
    or
        nextBitmap(Bitmap bitmap);

### To swap only two images (if images set in XML):
        flip();

### To clear all:
        clear();
