package com.codeconnect.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

/**
 * BarChartView - Custom View that draws an animated bar chart.
 * Used in Skill Analytics to show weekly growth.
 */
public class BarChartView extends View {

    private Paint barPaint, inactivePaint, labelPaint, valuePaint;
    private float[] data = {12, 8, 15, 22, 18, 25, 20};
    private String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private float animProgress = 0f;
    private int gradientStart, gradientEnd;
    private int highlightIndex = 5; // Saturday highlighted

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gradientStart = ContextCompat.getColor(getContext(), R.color.bar_gradient_start);
        gradientEnd = ContextCompat.getColor(getContext(), R.color.bar_gradient_end);

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        inactivePaint.setColor(ContextCompat.getColor(getContext(), R.color.bar_inactive));
        inactivePaint.setStyle(Paint.Style.FILL);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        labelPaint.setTextSize(26f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary));
        valuePaint.setTextSize(24f);
        valuePaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(float[] data, String[] labels) {
        this.data = data;
        this.labels = labels;
        animateChart();
    }

    public void setHighlightIndex(int index) {
        this.highlightIndex = index;
        invalidate();
    }

    private void animateChart() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            animProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        animateChart();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.length == 0) return;

        int w = getWidth();
        int h = getHeight();
        float bottomPadding = 40f;
        float topPadding = 30f;
        float barAreaHeight = h - bottomPadding - topPadding;
        float barWidth = (w - 20f) / data.length * 0.6f;
        float gap = (w - 20f) / data.length;

        float maxVal = 0;
        for (float v : data) maxVal = Math.max(maxVal, v);
        if (maxVal == 0) maxVal = 1;

        for (int i = 0; i < data.length; i++) {
            float barHeight = (data[i] / maxVal) * barAreaHeight * animProgress;
            float left = 10f + i * gap + (gap - barWidth) / 2f;
            float top = h - bottomPadding - barHeight;
            float right = left + barWidth;
            float bottom = h - bottomPadding;

            RectF rect = new RectF(left, top, right, bottom);

            if (i == highlightIndex) {
                barPaint.setShader(new LinearGradient(left, top, left, bottom,
                        gradientStart, gradientEnd, Shader.TileMode.CLAMP));
            } else {
                barPaint.setShader(null);
                barPaint.setColor(ContextCompat.getColor(getContext(), R.color.bar_inactive));
            }

            canvas.drawRoundRect(rect, 6f, 6f, i == highlightIndex ? barPaint : inactivePaint);

            // Value on top of highlighted bar
            if (i == highlightIndex && animProgress > 0.5f) {
                canvas.drawText(String.valueOf((int) data[i]),
                        left + barWidth / 2f, top - 8f, valuePaint);
            }

            // Label below
            canvas.drawText(labels[i], left + barWidth / 2f,
                    h - 10f, labelPaint);
        }
    }
}
