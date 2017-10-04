/*
 *  * 本程序由益鸽网络出品,未经授权请不要在网络传播.
 * Copyright (c) 2015~2017 <http://buffge.com> All rights reserved.
 * Author: buff <admin@buffge.com>
 * Created on : 2017-10-2, 21:40:35
 * QQ:1378504650
 */
package spidersina;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import static spidersina.SpiderSina.jsObj;
import static spidersina.SpiderSina.db;
import static spidersina.SpiderSina.c;
import static spidersina.SpiderSina.lastOPerationTime;

/**
 *
 * @author Administrator
 */
public abstract class SpiderBase {

    protected boolean debug;
    protected String baseUrl;
    protected WebDriver driver;
    protected final StringBuffer verificationErrors = new StringBuffer();
    protected int pageNum = 0;
    protected String myqslTable;

    public final void initJson() throws JSONException {
        jsObj = JsonHelper.ParseJson("./src/config.json");
        debug = jsObj.getBoolean("debug");
        if (debug) {
            Date now = new Date();
            c.log("初始化json共用时"
                    + (now.getTime() - lastOPerationTime.getTime()) + "毫秒\n");
            lastOPerationTime = now;
        }
    }

    public final void initMysql() throws JSONException {
        String mysqlHost = jsObj.getJSONObject("mysql").getString("host");
        String mysqlPort = jsObj.getJSONObject("mysql").getString("port");
        String mysqlUser = jsObj.getJSONObject("mysql").getString("username");
        String mysqlPwd = jsObj.getJSONObject("mysql").getString("password");
        String mysqlDb = jsObj.getJSONObject("mysql").getString("dbname");
        myqslTable = jsObj.getJSONObject("mysql").getString("tableName");
        db = new DBhelper(mysqlHost, mysqlPort, mysqlUser, mysqlPwd, mysqlDb);
        if (debug) {
            Date now = new Date();
            c.log("初始化mysql对象共用时"
                    + (now.getTime() - lastOPerationTime.getTime()) + "毫秒\n");
            lastOPerationTime = now;
        }
    }

    public void initBrowser() throws JSONException {
        String browserName = jsObj.getString("browser");
        setBrowser(browserName);
        baseUrl = "https://weibo.com";
//        driver.manage().window().maximize();
        driver.manage().timeouts().setScriptTimeout(5L, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(10L, TimeUnit.SECONDS);
        driver.get(baseUrl);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        if (debug) {
            Date now = new Date();
            c.log("初始化浏览器共用时"
                    + (now.getTime() - lastOPerationTime.getTime()) + "毫秒\n");
            lastOPerationTime = now;
        }
    }

    protected void setBrowser(String browserName) throws JSONException {
        throw new UnsupportedOperationException("请覆盖此方法");
    }

    protected void refresh() {
        driver.navigate().refresh();
        this.getPage();
    }

    public void getPage() {

    }

    /**
     * 截屏方法
     *
     * @param fileName
     * @param driver
     */
    public void captureScreenshot(String fileName) {
        Date time = new Date();
        SimpleDateFormat formatDate1 = new SimpleDateFormat("M-d H点m分s秒S");
        String dirName = "c:\\projects\\java\\selenium_java\\temp\\chromeDir\\capture\\";
        try {
            //指定了OutputType.FILE做为参数传递给getScreenshotAs()方法，其含义是将截取的屏幕以文件形式返回。
            File scrFile = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE); // 关键代码，执行屏幕截图，默认会把截图保存到temp目录
            System.out.println(dirName + formatDate1.format(time) + fileName + ".png");
            FileUtils.copyFile(scrFile, new File(dirName + formatDate1.format(time) + fileName + ".png"));  //利用FileUtils工具类的copyFile()方法保存getScreenshotAs()返回的文件对象。 
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("该错误可以查看截图：" + dirName + fileName + ".png");
        } catch (WebDriverException e) {
            e.printStackTrace();
        }
    }
}
