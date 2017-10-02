/*
 *  * 本程序由益鸽网络出品,未经授权请不要在网络传播.
 * Copyright (c) 2015~2017 <http://buffge.com> All rights reserved.
 * Author: buff <admin@buffge.com>
 * Created on : 2017-10-2, 6:20:20
 * QQ:1378504650
 */
package spidersina;

import java.text.SimpleDateFormat;
import java.util.Date;
import static spidersina.SpiderSina.c;
import static spidersina.SpiderSina.lastOPerationTime;

/**
 *
 * @author Administrator
 */
public class Common {

    public void log(String str) {
        System.out.println(str + "\n");
    }

    public void logTime(String prefixStr, Date nowTime) {
        SimpleDateFormat formatDate1 = new SimpleDateFormat("y-M-d H:m:s:S");
        System.out.println(prefixStr + " " + formatDate1.format(nowTime) + "\n");
    }
   
}
