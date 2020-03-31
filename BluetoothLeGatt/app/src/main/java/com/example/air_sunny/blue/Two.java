package com.example.air_sunny.blue;

/**
 * Created by jerry123 on 2016/11/19.
 */

import java.util.ArrayList;

public class Two {
    //ratio 用来保存ibeacon的比例
    private Double ratio;
    //a 表示两个ibeacon之间的距离
    private Double a = 6.0;
    private ArrayList<Double> res = new ArrayList<Double>();

    public ArrayList<Double> Change(Double b1, Double b2, Double x) {
        setA(x);
        res = change(b1, b2);
        return res;
    }

    public ArrayList<Double> getRes() {
        return res;
    }

    public void setA(Double x) {
        a = x;
    }

    /**
     * @param x1 为第一个ibeacon的值
     * @param x2 为第二个ibeacon的值
     * @return 返回经过修改后的ibeacon的值
     */
    private ArrayList<Double> change(Double x1, Double x2) {
        ArrayList<Double> arr = new ArrayList<Double>();
        ArrayList<Double> res = new ArrayList<Double>();
        if (x1 != 0 && x2 != 0) {
            ratio = x2 / x1;//ratio 是x1 和  x2的比值
            res = dealing(x1, x2, a);
        } else {
            //如果期中某个值为0时，另一个值为两个ibeacon之间直线的值
            if (x1 == 0) {
                x2 = a;
            } else {
                x1 = a;
            }
            res.add(x1);
            res.add(x2);
        }
        return res;
    }

    //按顺序返回Ibeacon1 的值和Ibeacon2 的值
    private ArrayList<Double> dealing(Double x1, Double x2, Double a) {
//		ArrayList<Double> m = new ArrayList<Double>();
        ArrayList<Double> temp = new ArrayList<Double>();
        int flag = 0;
        Double m;
        m = max(a, x1, x2);
        //开始处理
        if (a == m) {
            // a 是最大值
            if ((x1 + x2) < a) {
                x1 = x1 + 0.1;
                x2 = x2 + 0.1 * ratio;
                flag = 1;
            }
        } else if (x1 == m) {
            //x1 是最大值
            if ((x2 + a) < x1) {
                x2 += 0.1 * ratio;
                x1 -= 0.1;
                flag = 1;
            }
        } else {
            //x2是最大值
            if ((x1 + a) < x2) {
                x1 += 0.1;
                x2 -= 0.1 * ratio;
                flag = 1;
            }
        }
        if (flag == 0) {
            temp.add(x1);
            temp.add(x2);
            return temp;
        } else {
            return dealing(x1, x2, a);
        }
    }

    // 返回最大值
    private Double max(Double a, Double b, Double c) {
        Double mmax = a;
        if (mmax < b) {
            mmax = b;
        }
        if (mmax < c) {
            mmax = c;
        }
        return mmax;
    }
}
