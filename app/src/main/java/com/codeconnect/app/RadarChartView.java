package com.codeconnect.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

/**
 * RadarChartView - Custom View that draws a radar/spider chart.
 * Supports up to 2 data series for comparison overlay.
 */
public class RadarChartView extends View {

    private static final int NUM_AXES = 5;
    private static final int NUM_LEVELS = 4;

    private Paint gridPaint, labelPaint;
    private Paint fillPaint1, strokePaint1;
    private Paint fillPaint2, strokePaint2;

    private float[] data1 = {0.85f, 0.65f, 0.45f, 0.70f, 0.55f};
    private float[] data2 = null;
    private String[] labels = {"DSA", "Web Dev", "Sys Design", "ML/AI", "DevOps"};

    private float animProgress = 0f;
    private float centerX, centerY, radius;

    public RadarChartView(Context context) {
        super(context);
        init();
    }

    public RadarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.chart_grid));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        fillPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint1.setColor(ContextCompat.getColor(getContext(), R.color.radar_fill_1));
        fillPaint1.setStyle(Paint.Style.FILL);

        strokePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint1.setColor(ContextCompat.getColor(getContext(), R.color.radar_stroke_1));
        strokePaint1.setStyle(Paint.Style.STROKE);
        strokePaint1.setStrokeWidth(2.5f);

        fillPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint2.setColor(ContextCompat.getColor(getContext(), R.color.radar_fill_2));
        fillPaint2.setStyle(Paint.Style.FILL);

        strokePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint2.setColor(ContextCompat.getColor(getContext(), R.color.radar_stroke_2));
        strokePaint2.setStyle(Paint.Style.STROKE);
        strokePaint2.setStrokeWidth(2.5f);
    }

    /**
     * Set the primary data series (values 0.0 to 1.0)
     */
    public void setData1(float[] data) {
        this.data1 = data;
        animateChart();
    }

    /**
     * Set a second data series for comparison overlay
     */
    public void setData2(float[] data) {
        this.data2 = data;
        animateChart();
    }

    /**
     * Set axis labels
     */
    public void setLabels(String[] labels) {
        this.labels = labels;
        invalidate();
    }

    private void animateChart() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(800);
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
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - 40f;
        animateChart();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (radius <= 0) return;

        drawGrid(canvas);
        drawAxes(canvas);
        drawDataSeries(canvas, data1, fillPaint1, strokePaint1);
        if (data2 != null) {
            drawDataSeries(canvas, data2, fillPaint2, strokePaint2);
        }
        drawDots(canvas, data1, strokePaint1);
        if (data2 != null) {
            drawDots(canvas, data2, strokePaint2);
        }
    }

    private void drawGrid(Canvas canvas) {
        for (int level = 1; level <= NUM_LEVELS; level++) {
            float levelRadius = radius * level / NUM_LEVELS;
            Path path = new Path();
            for (int i = 0; i < NUM_AXES; i++) {
                double angle = getAngle(i);
                float x = centerX + (float) (levelRadius * Math.cos(angle));
                float y = centerY + (float) (levelRadius * Math.sin(angle));
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }
            path.close();
            canvas.drawPath(path, gridPaint);
        }
    }

    private void drawAxes(Canvas canvas) {
        for (int i = 0; i < NUM_AXES; i++) {
            double angle = getAngle(i);
            float endX = centerX + (float) (radius * Math.cos(angle));
            float endY = centerY + (float) (radius * Math.sin(angle));
            canvas.drawLine(centerX, centerY, endX, endY, gridPaint);

            // Draw labels
            float labelX = centerX + (float) ((radius + 24) * Math.cos(angle));
            float labelY = centerY + (float) ((radius + 24) * Math.sin(angle)) + 10;
            if (labels != null && i < labels.length) {
                canvas.drawText(labels[i], labelX, labelY, labelPaint);
            }
        }
    }

    private void drawDataSeries(Canvas canvas, float[] data, Paint fill, Paint stroke) {
        Path path = new Path();
        for (int i = 0; i < NUM_AXES; i++) {
            double angle = getAngle(i);
            float value = data[i] * animProgress;
            float x = centerX + (float) (radius * value * Math.cos(angle));
            float y = centerY + (float) (radius * value * Math.sin(angle));
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, fill);
        canvas.drawPath(path, stroke);
    }

    private void drawDots(Canvas canvas, float[] data, Paint paint) {
        Paint dotPaint = new Paint(paint);
        dotPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < NUM_AXES; i++) {
            double angle = getAngle(i);
            float value = data[i] * animProgress;
            float x = centerX + (float) (radius * value * Math.cos(angle));
            float y = centerY + (float) (radius * value * Math.sin(angle));
            canvas.drawCircle(x, y, 5f, dotPaint);
        }
    }

    private double getAngle(int index) {
        return Math.toRadians(-90 + 360.0 * index / NUM_AXES);
    }
}
