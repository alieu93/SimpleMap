package adamlieu.simplemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by 100451790 on 04/11/2015.
 */
public class CanvasView extends View {

    public CanvasView(Context context){
        super(context);
    }

    public CanvasView(Context context, AttributeSet attribs){
        super(context, attribs);
    }

    @Override
    protected void onDraw(Canvas canvas){
        //super.onDraw(canvas);
        //draw in black
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);

        //bottom left to top right
        int top = 100, left = 100, width = 200, height = 150;
        canvas.drawLine(left, top + height, left + width, top, paint);
        invalidate();
    }

    public static Bitmap getBitmapFromView(View view){
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }


}
