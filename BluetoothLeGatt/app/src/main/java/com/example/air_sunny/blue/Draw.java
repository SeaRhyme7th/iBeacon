package com.example.air_sunny.blue;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.View;
import android.util.Log;
import android.view.ViewDebug;
import android.view.WindowManager;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by jerry123 on 2016/11/3.
 */
public class Draw extends View {

    private Canvas canvas = new Canvas();
    private Paint paint = new Paint();
    private float me_x;
    private float me_y;
    private float friend_x;
    private float friend_y;
    private float[] dis = new float[3];
    //两个ibeacon间的距离：单位：米
    private float ibeaconDistance = 6;
    //标识：以判断画不画friend
    private String flagStr = "me";

    public float getMyX() {
        return me_x;
    }

    public float getMyY() {
        return me_y;
    }

    //x,y以毫米为单位
    public void setMyX(float x) {
        this.me_x = x;
    }

    public void setMyY(float y) {
        this.me_y = y;
    }

    public float getFriendX() {
        return friend_x;
    }

    public float getFriendY() {
        return friend_y;
    }

    public void setFriendX(float x) {
        this.friend_x = x;
    }

    public void setFriendY(float y) {
        this.friend_y = y;
    }

    public void setFlagStr(String str) {
        this.flagStr = str;
    }

    public String getFlagStr() {
        return flagStr;
    }

    public Draw(Context context) {
        super(context);
    }

    /**
     * drawRect:矩形
     * drawCircle:圆形
     * drawOval:椭圆
     * drawPath:绘制任意多边形
     * drawLine:直线
     * drawPoint:点
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        switch (flagStr) {
            case "me":
                DrawMyPic();
                break;
            case "friend":
                DrawFriendPic();
                break;
            default:
                break;
        }
    }

    //画ibeacon位置
    private Resources drawIBeacon() {
        //创建画笔
        float width = (float) getWindowWidth();
        //设置空心STROKE,实心FILL
        paint.setStyle(Paint.Style.FILL);
        //是否去锯齿
        SetAntiAlias(true);
        //画iBeacon位置点
        Resources r = this.getContext().getResources();
        Bitmap bmp_ibeacon = BitmapFactory.decodeResource(r, R.drawable.ibeacon_device);
        DrawBitmap(bmp_ibeacon, width / 3, 20);
        DrawBitmap(bmp_ibeacon, width * 2 / 3, 20);
        return r;
    }

    //画my位置
    private void drawMyPosition(Resources r, float x, float y, String str) {
        //画手机位置点
        //dis1为手机距E-Beacon_0EC0B6的距离,dis2为手机距E-Beacon_E4C5E0的距离
        float dis1 = (float) (x / 1000 * getScale());
        float dis2 = (float) (y / 1000 * getScale());
        //从小到大三边长排序
        divSort(dis1, dis2);
        //可以构成三角形
        if (judgeTriangle()) {
            drawPos(r, dis1, dis2, str);
        } else {
            Double meter_1 = floatToDouble(x);
            Double meter_2 = floatToDouble(y);
            Double ibeacon_meter = floatToDouble(ibeaconDistance);
            Two two = new Two();
            ArrayList<Double> resultDistance = two.Change(meter_1 / 1000, meter_2 / 1000, ibeacon_meter);
            dis1 = (float) (resultDistance.get(0) * getScale());
            dis2 = (float) (resultDistance.get(1) * getScale());
            //从小到大三边长排序
            divSort(dis1, dis2);
            drawPos(r, dis1, dis2, str);
        }
    }

    private Double floatToDouble(float f) {
        BigDecimal bd = new BigDecimal(String.valueOf(f));
        return bd.doubleValue();
    }

    private void drawPos(Resources r, float dis1, float dis2, String str) {
        float PosY = (float) (getPositionY(dis1, dis2));
        float PosX = (float) (getPositionX(dis1, dis2, PosY));
        //画图片
        Bitmap bmp_me;
        if (str.equals("me")) {
            bmp_me = BitmapFactory.decodeResource(r, R.drawable.your_position);
        } else {
            bmp_me = BitmapFactory.decodeResource(r, R.drawable.friend_position);
        }
        DrawBitmap(bmp_me, PosX, PosY);
    }

    //画包含friend的示意图
    private void DrawFriendPic() {
        paint.setStyle(Paint.Style.STROKE);
        DrawRect(0, 0, (float) getWindowWidth() - 25, (float) (getWindowHight() * 2 / 5 - 145));
        Resources r = drawIBeacon();
        drawMyPosition(r, me_x, me_y, "me");
        drawMyPosition(r, friend_x, friend_y, "friend");
    }

    //画无friend的示意图
    private void DrawMyPic() {
        paint.setStyle(Paint.Style.STROKE);
        DrawRect(0, 0, (float) getWindowWidth() - 25, (float) (getWindowHight() * 2 / 5 - 145));
        Resources r = drawIBeacon();
        drawMyPosition(r, me_x, me_y, "me");
    }

    /*获取横坐标
    * @param a 为到第一个ibeacon的距离
     * @param b 为到第二个ibeacon的距离
     * @param high 为用户的纵坐标
     * @return 返回用户位置的横坐标
     */
    private double getPositionX(float a, float b, float high) {
        if (getCostheta(a, getWindowWidth() / 3, b) < 0) {
            return (getWindowWidth() * 2 / 3 - Math.sqrt(b * b - high * high));
        }
        if (getCostheta(b, getWindowWidth() / 3, a) < 0) {
            return (getWindowWidth() * 2 / 3 + Math.sqrt(b * b - high * high));
        }
        return (getWindowWidth() / 3 + Math.sqrt(a * a - high * high));
    }

    //获取三角形余弦值，判断是否钝角:用以分类讨论计算横坐标
    private double getCostheta(float a, double b, float c) {
        return (a * a + b * b - c * c) / (2 * a * b);
    }

    /*获取纵坐标
    * @param a 为到第一个ibeacon的距离
     * @param b 为到第二个ibeacon的距离
     * @return 返回用户位置的纵坐标
     */
    private double getPositionY(float a, float b) {
        double zhouchang_2 = (dis[0] + dis[1] + dis[2]) / 2;
        double mianji = Math.sqrt(zhouchang_2 * (zhouchang_2 - dis[0]) * (zhouchang_2 - dis[1]) * (zhouchang_2 - dis[2]));
        return mianji * 2 / (getWindowWidth() / 3);
    }

    //判断是否构成三角形
    private boolean judgeTriangle() {
        if (dis[0] + dis[1] >= dis[2]) {
            return true;
        }
        return false;
    }

    //距离进行排序
    private void divSort(float a, float b) {
        dis[0] = a;
        dis[1] = b;
        dis[2] = (float) (getWindowWidth() / 3);
        float temp;
        for (int i = 0; i < 2; i++) {
            for (int j = i; j < 2 - i; j++) {
                if (dis[j] > dis[j + 1]) {
                    temp = dis[j];
                    dis[j] = dis[j + 1];
                    dis[j + 1] = temp;
                }
            }
        }
    }

    //两ibeacon间距离
    private double getScale() {
        return (getWindowWidth() / 3) / ibeaconDistance;
    }

    private double getWindowWidth() {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        double width = wm.getDefaultDisplay().getWidth();
        return width;
    }

    private double getWindowHight() {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        double height = wm.getDefaultDisplay().getHeight();
        return height;
    }

    //缩小图片
    private static Bitmap small(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(0.1f, 0.1f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    public void clear() {
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        invalidate();
    }

    //1:红，2:蓝,3:绿,4:黄,5:黑,6:白,7:灰
    public void SetColor(int color) {
        int _color = 0;
        switch (color) {
            case 1:
                _color = Color.RED;
                break;
            case 2:
                _color = Color.BLUE;
                break;
            case 3:
                _color = Color.GREEN;
                break;
            case 4:
                _color = Color.YELLOW;
                break;
            case 5:
                _color = Color.BLACK;
                break;
            case 6:
                _color = Color.WHITE;
                break;
            case 7:
                _color = Color.GRAY;
                break;
            default:
                _color = Color.BLACK;
        }
        this.paint.setColor(_color);
    }

    //写字
    public void DrawText(String string, float x, float y) {
        this.canvas.drawText(string, x, y, this.paint);
    }

    //画圆
    public void DrawCircle(float x, float y, float r) {
        this.canvas.drawCircle(x, y, r, this.paint);
    }

    //设置画笔的锯齿效果。 true是去除
    public void SetAntiAlias(boolean b) {
        this.paint.setAntiAlias(b);
    }

    //画长方形
    public void DrawRect(float FromX, float FromY, float ToX, float ToY) {
        paint.setStyle(Paint.Style.STROKE);
        this.canvas.drawRect(FromX, FromY, ToX, ToY, this.paint);
    }

    //画线
    public void DrawLine(float FromX, float FromY, float ToX, float ToY) {
        this.canvas.drawLine(FromX, FromY, ToX, ToY, this.paint);
    }

    //画圆角矩形
    public void DrawRoundRect(float RectFromX, float RectFromY, float RectToX, float RectToY, float xRadius, float yRadius) {
        RectF oval = new RectF(RectFromX, RectFromY, RectToX, RectToY);
        this.canvas.drawRoundRect(oval, xRadius, yRadius, this.paint);
    }

    //画点
    public void DrawPoint(float x, float y) {
        this.paint.setStyle(Paint.Style.FILL);
        this.canvas.drawPoint(x, y, this.paint);
    }

    //画图片，即贴图
    public void DrawBitmap(Bitmap bitmap, float x, float y) {
        this.canvas.drawBitmap(bitmap, x, y, this.paint);
    }
}
