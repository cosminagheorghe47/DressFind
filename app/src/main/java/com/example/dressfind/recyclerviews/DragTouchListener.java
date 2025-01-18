package com.example.dressfind.recyclerviews;

import android.view.MotionEvent;
import android.view.View;

public class DragTouchListener implements View.OnTouchListener{

    private final View imageView;
    private final View deleteButton;
    private float dX, dY;

    public DragTouchListener(View imageView, View deleteButton) {
        this.imageView = imageView;
        this.deleteButton = deleteButton;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                // Mutăm imaginea
                v.setX(event.getRawX() + dX);
                v.setY(event.getRawY() + dY);

                // Sincronizăm poziția butonului "X"
//                deleteButton.setX(v.getX() + v.getWidth() - deleteButton.getWidth() - 10); // Poziție corectată
//                deleteButton.setY(v.getY() - deleteButton.getHeight() / 2);
                break;
            case MotionEvent.ACTION_UP:
               break;
            default:
                return false;
        }
        return true;
    }
}
