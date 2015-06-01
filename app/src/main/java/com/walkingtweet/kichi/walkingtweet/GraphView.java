package com.walkingtweet.kichi.walkingtweet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GraphView extends View {
    public GraphView(Context context) {
        super(context);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();
        int base = height/2;
        Paint paint = new Paint();
        paint.setColor(Color.argb(75, 255, 255, 255));
        paint.setStrokeWidth(1);
        for (int y = 0; y < height; y = y + 10) {
            canvas.drawLine(0, y, width, y, paint);
        }
        for (int x = 0; x < width; x = x + 10) {
            canvas.drawLine(x, 0, x, height, paint);
        }
        paint.setColor(Color.RED);
        canvas.drawLine(0, base, width, base, paint);


        // グラフを緑色線で表示
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(4);
        for ( int i = 0; i < counterhistory.length - 1 ; i++ ){
            canvas.drawLine(i, base + counterhistory[i], i + 1,
                    base + counterhistory[i + 1], paint);
        }

        // 現在を赤色縦線で表示
        paint.setColor(Color.RED);
        canvas.drawLine(counter, 0, counter, height, paint);

        Log.i("GraphView","counter:"+counter);
    }

    float div =0;
    int counter = 0;
    int[] counterhistory = new int[480];

    public void setDiv(float div){
        this.div = div;
        this.counter++;
        if (counterhistory.length <= counter){
            counter = 0;
        }
        counterhistory[counter] = (int)div * 10;
    }
}
