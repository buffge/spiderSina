# spiderSina
## selenium 是一个浏览器自动化测试框架.可以模拟用户的所有操作. ##

很久以前就想把李敖大师的所有微博爬取下来.一直没空,前天看见群里有人推荐selenium和phantomjs<br>
就学了selenium做了个demo.你们想爬别人的只要把李敖大师的地址换成你要的就行了.<br>
phantomjs我还没学,不过我猜他就是去掉ui功能的浏览器.这样可以让爬虫更快.如果专业做爬虫的可以看看.我做这个是玩的.

package.json 是配置文件 必须要先配置好才能运行

> 学习地址: [慕课网 selenium 教程][1]
> 项目地址: [spiderSina][2]
> 环境:
> ide:Netbeans
> java包:非常多(在lib文件夹中)
> 浏览器驱动:ie,火狐,谷歌随便选(在res文件夹内)

## 爬虫思路: ##
    
>  1. 打开新浪微博首页并登录
>  2. 跳转到李敖大师主页
>  3. 触发ajax将一个页面全部显示出来
>  4. 对每一个微博进行判断解析 只获取大师本人的微博 
>     如果有展开全文就点击点击一下.然后将微博内容插入数据库
>  5. 判断是否有下一页,如果有就到下一页然后进入第四步

## 打开微博并登录 ##
### 这里的css选择器就当jQuery用 ###

    baseUrl = "https://weibo.com";
    //打开微博主页面
    driver.get(baseUrl);
    //设置窗口最大化
    driver.manage().window().maximize();
     //输入用户名
     driver.findElement(By.cssSelector("#loginname")).clear();
     driver.findElement(By.cssSelector("#loginname")).sendKeys(sinaUsername);
     //输出密码
     driver.findElement(By.cssSelector("#pl_login_form .info_list.password input")).clear();
     driver.findElement(By.cssSelector("#pl_login_form .info_list.password input")).sendKeys(password);
     //点击登录
     driver.findElement(By.cssSelector("#pl_login_form.login_box div.login_innerwrap div.W_login_form .login_btn")).click();
     
## 触发ajax将一个页面全部显示出来 ## 
   
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

## 对每一个微博进行判断解析 只获取大师本人的微博 ##

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
   
## 判断是否有下一页 ##
    //查看按钮的文本是否为"下一页" 如果是那就还有下一页
     WebElement nextPage = driver.findElement(By.cssSelector("#Pl_Official_MyProfileFeed__23 .WB_cardwrap.S_bg2>div>a:last-of-type"));
     String strNext = nextPage.getText();
     if ("下一页".equals(strNext)) {
          nextPage.click();
          this.getPage();
       }

没有了... 其实selenium普通玩家应该很快就能上手.高端玩法没试过.主要就是看一下他重要的几个api
如果不知道api 就百度还有写demo猜api用法.
本来想用php写的,但是php写得话可能比较麻烦.
我本人对李敖大师十分敬仰,一直就想把他的语录记下来.待他百年之后,有人在网上吹牛李敖说"xxx"
我能有勇气发这个图给他.   
![pic](https://github.com/buffge/spiderSina/blob/master/src/liao.png "我没说过这话")<br/>

开个玩笑,我是为了学习.
注:我今早准备上传到github,不小心按成提取.导致本地被覆盖了,现在的代码是我用之前构建的反编译回来的.  
但是我测试了,一点毛病都没有. 
    


  [1]: http://www.imooc.com/video/13952
  [2]: https://github.com/buffge/spiderSina
