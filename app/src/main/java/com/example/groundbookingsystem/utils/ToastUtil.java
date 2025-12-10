package com.example.groundbookingsystem.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groundbookingsystem.R;
import com.google.android.material.card.MaterialCardView;

public class ToastUtil {
    
    public static void showSuccess(Context context, String message) {
        showCustomToast(context, message, R.color.success);
    }
    
    public static void showError(Context context, String message) {
        showCustomToast(context, message, R.color.error);
    }
    
    public static void showInfo(Context context, String message) {
        showCustomToast(context, message, R.color.primary);
    }
    
    public static void showWarning(Context context, String message) {
        showCustomToast(context, message, R.color.accent);
    }
    
    private static void showCustomToast(Context context, String message, int backgroundColor) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View toastView = inflater.inflate(R.layout.custom_toast, null);
        
        MaterialCardView cardView = toastView.findViewById(R.id.toastCard);
        TextView textView = toastView.findViewById(R.id.toastMessage);
        
        textView.setText(message);
        cardView.setCardBackgroundColor(context.getResources().getColor(backgroundColor));
        
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setView(toastView);
        toast.show();
    }
}

