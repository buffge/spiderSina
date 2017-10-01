package spidersina;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
/**
 * selenium爬虫获取新浪用户所有微博
 * @author buff
 */
public class SpiderSina {
    private WebDriver driver;
    private String baseUrl;
    private final StringBuffer verificationErrors = new StringBuffer();
    public DBhelper db;
    private int pageNum = 0;
    public static JSONObject jsObj;
    public static String myqslTable;

    public static void main(String[] args) throws Exception {
        //解析json文件
        jsObj = JsonHelper.ParseJson("./src/package.json");
        String browserName = jsObj.getString("browser");
        String sinaUsername = jsObj.getJSONObject("sina").getString("username");
        String sinaPassword = jsObj.getJSONObject("sina").getString("password");
        String mysqlHost = jsObj.getJSONObject("mysql").getString("host");
        String mysqlPort = jsObj.getJSONObject("mysql").getString("port");
        String mysqlUser = jsObj.getJSONObject("mysql").getString("username");
        String mysqlPwd = jsObj.getJSONObject("mysql").getString("password");
        String mysqlDb = jsObj.getJSONObject("mysql").getString("dbname");
        myqslTable = jsObj.getJSONObject("mysql").getString("tableName");
        SpiderSina ss = new SpiderSina();
        //初始化数据库连接
        ss.db = new DBhelper(mysqlHost, mysqlPort, mysqlUser, mysqlPwd, mysqlDb);
        //初始化浏览器
        ss.setUp(browserName);
        //爬虫执行
        ss.test(sinaUsername, sinaPassword);
        //关闭数据库连接
        ss.db.conn.close();
        //执行一些善后工作比如关闭浏览器以及驱动程序
        ss.tearDown();
    }
    /**
     * 初始化驱动和浏览器
     * @param browserName　浏览器名称
     * @throws Exception 
     */
    @Before
    public void setUp(String browserName) throws Exception {
        this.setBrowser(browserName);
        this.baseUrl = "https://weibo.com";
        this.driver.get(this.baseUrl);
        this.driver.manage().window().maximize();
        this.driver.manage().timeouts().implicitlyWait(10L, TimeUnit.SECONDS);
    }
    /**
     * 登录新浪微博并到李大师微博进行获取数据
     * @param sinaUsername
     * @param password
     * @throws InterruptedException 
     */
    @Test
    public void test(String sinaUsername, String password) throws InterruptedException {
        this.driver.findElement(By.cssSelector("#loginname")).clear();
        this.driver.findElement(By.cssSelector("#loginname")).sendKeys(new CharSequence[]{sinaUsername});
        this.driver.findElement(By.cssSelector("#pl_login_form .info_list.password input")).clear();
        this.driver.findElement(By.cssSelector("#pl_login_form .info_list.password input")).sendKeys(new CharSequence[]{password});
        this.driver.findElement(By.cssSelector("#pl_login_form.login_box div.login_innerwrap div.W_login_form .login_btn")).click();
        this.driver.navigate().to("https://weibo.com/u/2134671703?from=myfollow_all&is_all=1");
        this.getPage();
    }
    /**
     * 执行一些善后工作　关闭驱动和报错运行途中产生的错误
     * @throws Exception 
     */
    @After
    public void tearDown() throws Exception {
        //调试模式下可以不关　但是自己记得要关闭驱动程序
        //cmd 命令为　taskkill -f -t -im chromedriver.exe　||ie||firefox
        //driver.quit();
        String verificationErrorString = this.verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            Assert.fail(verificationErrorString);
        }

    }
    /**
     * 获取当前页面的微博　并进行判断然后插入数据库　再判断是否有下一页　有就进入
     */
    public void getPage() {
        ++this.pageNum;
        System.out.println("##################正在进入第" + this.pageNum + "页#######################\n");
        JavascriptExecutor jse = (JavascriptExecutor) this.driver;
        this.driver.manage().timeouts().implicitlyWait(150L, TimeUnit.MILLISECONDS);
        //判断是否有下一页那个按钮 没的就向下拉
        for (int i = 0, scrollY = 5000; i < 20; i++, scrollY += 5000) {
            //判断是否有下一页那个div 如果有就表示到底了
            if (this.isElementPresent(By.cssSelector(".page.next.S_txt1.S_line1"))) {
                break;
            }
            String setscroll = "window.scrollTo(0," + scrollY + ")";
            //执行js代码　将页面向下拉
            jse.executeScript(setscroll);
        }
        this.driver.manage().timeouts().implicitlyWait(10L, TimeUnit.SECONDS);
        //获取李大师所有的微博
        List<WebElement> divs = driver.findElements(By.cssSelector("#Pl_Official_MyProfileFeed__23 div>.WB_cardwrap.WB_feed_type"));
//对每一条微博进行判断是否为正常微博(非别人的点赞) 并进行展开全文 然后插入数据库
        divs.forEach((WebElement ele) -> {
            String isZan = ele.findElement(By.cssSelector("div:first-of-type")).getAttribute("class");
            if (!("WB_cardtitle_b S_line2".equals(isZan))) {
                String weiboContent;
                //如果要运行的快一点就把数值改小，数值越小等待的时间越少
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
                //判断是否微博中是否有超链接并且超链接内容为展开全文 如果是就点击
                if (this.isElementPresent(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_text.W_f14 a"), ele)
                        && "展开全文".equals(ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_text.W_f14 a")).getText().trim())) {
                    //这里获取全文应该是300ms之内 设置太久会浪费时间
                    driver.manage().timeouts().implicitlyWait(300, TimeUnit.MILLISECONDS);
                    //点击 展开全文按钮
                    ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_text.W_f14 a")).click();
                    //获取微博内容
                    weiboContent = ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>div:last-of-type")).getText();
                } else {
                    //如果没有展开全文 获取微博 这两种内容不在一个div里
                    weiboContent = ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_text.W_f14")).getText();
                }
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                //获取大师发微博的时间
                String writeTime = ele.findElement(By.cssSelector(".WB_feed_detail>.WB_detail>.WB_from>a:first-of-type")).getText();
                //这里我想把微博中偶尔出现的零宽字符去掉但是失败了,可能是版本问题.不想搞
                //weiboContent = weiboContent.replaceAll("\\u200b", "");
                //将微博插入数据库
                this.insertToMysql(weiboContent, writeTime);
                //这个是获取到的微博内容调试时候可以打开看看在哪里出来问题
                // System.out.println(weiboContent + "\n");
            }
        });
//查看按钮的文本是否为"下一页" 如果是那就还有下一页
        WebElement nextPage = driver.findElement(By.cssSelector("#Pl_Official_MyProfileFeed__23 .WB_cardwrap.S_bg2>div>a:last-of-type"));
        String strNext = nextPage.getText();
        if ("下一页".equals(strNext)) {
            nextPage.click();
            this.getPage();
        }
    }
    /**
     * 将微博插入到数据库中
     * @param str　微博内容
     * @param time　发布微博时间
     * @return 
     */
    public int insertToMysql(String str, String time) {
        String sql = "insert into " + myqslTable + " (content,release_time) values(?,?)";
        int i = 0;
        try {
            this.db.pst = this.db.conn.prepareStatement(sql);
            this.db.pst.setString(1, str);
            this.db.pst.setString(2, time.trim());
            i = this.db.pst.executeUpdate();
            this.db.pst.close();
        } catch (SQLException var6) {
            var6.printStackTrace();
        }
        return i;
    }
    /**
     * 设置浏览器
     * @param browserName 浏览器名称
     * @throws JSONException 
     */
    private void setBrowser(String browserName) throws JSONException {
        byte var3 = -1;
        switch (browserName.hashCode()) {
            case -1361128838:
                if (browserName.equals("chrome")) {
                    var3 = 1;
                }
                break;
            case -849452327:
                if (browserName.equals("firefox")) {
                    var3 = 2;
                }
                break;
            case 3356:
                if (browserName.equals("ie")) {
                    var3 = 0;
                }
        }

        switch (var3) {
            case 0:
                String ieDriverPath = jsObj.getJSONObject("browserDrivers").getString("ie");
                System.setProperty("webdriver.ie.driver", ieDriverPath);
                this.driver = new InternetExplorerDriver();
                break;
            case 1:
                String chromeDriverPath = jsObj.getJSONObject("browserDrivers").getString("chrome");
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                this.driver = new ChromeDriver();
                break;
            case 2:
                String firefoxDriverPath = jsObj.getJSONObject("browserDrivers").getString("firefox");
                System.setProperty("webdriver.gecko.driver", firefoxDriverPath);
                this.driver = new FirefoxDriver();
                break;
            default:
                throw new ExceptionInInitializerError("浏览器设置错误,请检查名称和路径");
        }

    }
   /**
     * 判断driver元素下的元素是否存在
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
     * @param by　路径查找对象
     * @param ele　父元素
     * @return 
     */
    private boolean isElementPresent(By by, WebElement ele) {
        try {
            ele.findElement(by);
            return true;
        } catch (NoSuchElementException var4) {
            return false;
        }
    }
}
