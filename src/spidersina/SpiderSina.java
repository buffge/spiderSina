package spidersina;

import java.sql.SQLException;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.hamcrest.core.CombinableMatcher;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * selenium爬虫获取新浪用户所有微博
 *
 * @author buff
 */
public class SpiderSina extends SpiderBase {

    public static DBhelper db;
    public static JSONObject jsObj;
    public static String myqslTable;
    public static Common c;
    public static Date beginTime;
    public static Date lastOPerationTime;

    public static void main(String[] args) throws Exception {
        c = new Common();
        SpiderSina ss = new SpiderSina();
        ss.initBrowser();
        try {
            ss.goIndex();
        } catch (Exception e) {
            e.printStackTrace();
            ss.captureScreenshot("访问自己的首页");
        }
        try {
            ss.goSearchIndex();
        } catch (Exception e) {
            e.printStackTrace();
            ss.captureScreenshot("访问要搜索的用户的首页");
        }
        ss.getPage();
        db.conn.close();
        ss.tearDown();
    }

    public SpiderSina() throws JSONException {
        beginTime = new Date();
        lastOPerationTime = beginTime;
        initJson();
        initMysql();
    }

    /**
     * 设置浏览器
     *
     * @param browserName 浏览器名称
     * @throws JSONException
     */
    @Override
    protected void setBrowser(String browserName) throws JSONException {
        switch (browserName) {
            case "ie":
                String ieDriverPath = jsObj.getJSONObject("browserDrivers").getString("ie");
                System.setProperty("webdriver.ie.driver", ieDriverPath);
                this.driver = new InternetExplorerDriver();
                break;
            case "chrome":
                String chromeDriverPath = jsObj.getJSONObject("browserDrivers").getString("chrome");
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                ChromeOptions cps = new ChromeOptions();
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("profile.default_content_setting_values.notifications", 2);
                prefs.put("profile.managed_default_content_settings.images", 2);
                cps.setExperimentalOption("prefs", prefs);
                this.driver = new ChromeDriver(cps);
                break;
            case "firefox":
                String firefoxDriverPath = jsObj.getJSONObject("browserDrivers").getString("firefox");
                System.setProperty("webdriver.gecko.driver", firefoxDriverPath);
                this.driver = new FirefoxDriver();
                break;
            case "phantomjs":
                String phantomjsDriverPath = jsObj.getJSONObject("browserDrivers").getString("phantomjs");
                DesiredCapabilities caps = new DesiredCapabilities();
                caps.setJavascriptEnabled(true);
                caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsDriverPath);
                caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX, "Y");
                caps.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
                caps.setCapability("phantomjs.page.settings.cssSelectorsEnabled", true);
                caps.setCapability("phantomjs.page.settings.loadImages", false);
                caps.setCapability("phantomjs.page.settings.browserConnectionEnabled", true);
                caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[]{
                    "--web-security=false",
                    "--ssl-protocol=any",
                    "--ignore-ssl-errors=true", //                    "--webdriver-loglevel=DEBUG"
                });
                //截屏支持
                caps.setCapability("takesScreenshot", true);
                driver = new PhantomJSDriver(caps);
                break;
            default:
                throw new ExceptionInInitializerError("浏览器设置错误,请检查名称和路径");
        }
    }

    private void goIndex() throws JSONException {
        driver.get(baseUrl);
        driver.findElement(By.cssSelector("#loginname")).clear();
        String sinaUsername = jsObj.getJSONObject("sina").getString("username");
        String sinaPassword = jsObj.getJSONObject("sina").getString("password");
        driver.findElement(By.cssSelector("#loginname")).sendKeys(sinaUsername);
        driver.findElement(By.cssSelector("#pl_login_form .info_list.password input")).clear();
        driver.findElement(By.cssSelector("#pl_login_form .info_list.password input")).sendKeys(sinaPassword);
        driver.findElement(By.cssSelector("#pl_login_form.login_box div.login_innerwrap div.W_login_form .login_btn")).click();
        if (debug) {
            Date now = new Date();
            c.log("进入新浪微博自己的首页共用时"
                    + (now.getTime() - lastOPerationTime.getTime()) + "毫秒\n");
            lastOPerationTime = now;
        }
    }

    private void goSearchIndex() throws JSONException {
        String searchUser = jsObj.getString("searchUser");
        driver.navigate().to(searchUser);
        if (debug) {
            Date now = new Date();
            c.log("进入搜索用户的首页共用时"
                    + (now.getTime() - lastOPerationTime.getTime()) + "毫秒\n");
            lastOPerationTime = now;
        }
    }

    @Override
    public void getPage() {
        pageNum++;
        System.out.println("##################正在进入第" + this.pageNum + "页#######################\n");
        try {
            ajaxLoadAllWeibo();
        } catch (Exception e) {
            e.printStackTrace();
            captureScreenshot("ajax载入微博");
        }
        try {
            getAllWeibo();
        } catch (Exception e) {
            e.printStackTrace();
            captureScreenshot("获取微博");
        }
        driver.manage().timeouts().implicitlyWait(50L, TimeUnit.MILLISECONDS);
        try {
            WebElement nextPage = driver.findElement(By.cssSelector(".WB_frame_c > div > .WB_feed.WB_feed_v3.WB_feed_v4 .WB_cardwrap.S_bg2>div>a:last-of-type"));
            String strNext = nextPage.getText();
            driver.manage().timeouts().implicitlyWait(10L, TimeUnit.SECONDS);
            if ("下一页".equals(strNext)) {
                nextPage.click();
                getPage();
            }
        } catch (Exception e) {
            driver.manage().timeouts().implicitlyWait(10L, TimeUnit.SECONDS);
            captureScreenshot("进入下一页失败");
            refresh();
        }

    }

    private void ajaxLoadAllWeibo() {
        JavascriptExecutor jse = (JavascriptExecutor) this.driver;
        this.driver.manage().timeouts().implicitlyWait(100L, TimeUnit.MILLISECONDS);
        for (int i = 0, scrollY = 5000; i < 100; i++, scrollY += 5000) {
            if (isElementPresent(By.cssSelector(".page.next.S_txt1.S_line1"))) {
                jse.executeScript("window.scrollTo(0," + scrollY + ")");
                break;
            }
            String setscroll = "window.scrollTo(0," + scrollY + ")";
            jse.executeScript(setscroll);
        }
        if (debug) {
            Date now = new Date();
            c.log("ajax加载页面所有微博共用时"
                    + (now.getTime() - lastOPerationTime.getTime()) + "毫秒\n");
            lastOPerationTime = now;
        }
    }

    private void getAllWeibo() {
        this.driver.manage().timeouts().implicitlyWait(10L, TimeUnit.MILLISECONDS);
        List<WebElement> divs = driver.findElements(By.cssSelector(".WB_frame_c > div > .WB_feed.WB_feed_v3.WB_feed_v4 >.WB_cardwrap.WB_feed_type"));
        divs.forEach(this::getOneWeibo);
        if (debug) {
            Date now = new Date();
            c.log("获取所有微博共用时"
                    + (now.getTime() - lastOPerationTime.getTime()) + "毫秒\n");
            lastOPerationTime = now;
        }
    }

    private void getOneWeibo(WebElement ele) {
        Date tempLastOPerationTime = new Date();
        driver.manage().timeouts().implicitlyWait(10L, TimeUnit.MILLISECONDS);
        String isZanForOthers = ele.findElement(By.cssSelector("div:first-of-type")).getAttribute("class");
        if (!"WB_cardtitle_b S_line2".equals(isZanForOthers)) {
            String weiboContent;
            if (this.isElementPresent(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_text.W_f14 a"), ele) && "展开全文".equals(ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_text.W_f14 a")).getText().trim())) {
                driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
                ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_text.W_f14 a")).click();
                weiboContent = ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>div:last-of-type")).getText();
            } else {
                weiboContent = ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_text.W_f14")).getText();
            }
            driver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
            String writeTime = ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_from>a:first-of-type")).getText();
            insertToMysql(weiboContent, writeTime);
            // System.out.println(weiboContent + "\n");
        }
        if (debug) {
            Date now = new Date();
            c.log("获取一条微博共用时"
                    + (now.getTime() - tempLastOPerationTime.getTime()) + "毫秒\n");
        }
    }

    /**
     * 执行一些善后工作　关闭驱动和报错运行途中产生的错误
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = this.verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            Assert.fail(verificationErrorString);
        }

    }

    /**
     * 将微博插入到数据库中
     *
     * @param str　微博内容
     * @param time　发布微博时间
     * @return
     */
    public int insertToMysql(String str, String time) {
        String sql = "insert into " + myqslTable + " (content,release_time) values(?,?)";
        int i = 0;
        try {
            db.pst = db.conn.prepareStatement(sql);
            db.pst.setString(1, str);
            db.pst.setString(2, time.trim());
            i = db.pst.executeUpdate();
            db.pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * 判断driver元素下的元素是否存在
     *
     * @param by　路径查找对象
     * @return
     */
    private boolean isElementPresent(By by) {
        try {
            this.driver.findElement(by);
            return true;
        } catch (NoSuchElementException var3) {
            return false;
        }
    }

    /**
     * 判断ele元素下的元素是否存在
     *
     * @param by　路径查找对象
     * @param ele　父元素
     * @return
     */
    private boolean isElementPresent(By by, WebElement ele) {
        try {
            ele.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

}
