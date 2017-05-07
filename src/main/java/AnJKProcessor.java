import com.virjar.dungproxy.client.webmagic.DungProxyDownloader;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import bean.SpiderResult;
import bean.VillageName;
import pipeline.HuljFileLine;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * Created by sshss on 2017/5/6.
 *
 *
 * 爬取安居客--租房信息
 */
public class AnJKProcessor implements PageProcessor {

    private Site site = Site.me()// .setHttpProxy(new HttpHost("127.0.0.1",8888))
            .setRetryTimes(3) // 就我的经验,这个重试一般用处不大,他是httpclient内部重试
            .setTimeOut(30000)// 在使用代理的情况下,这个需要设置,可以考虑调大线程数目
            .setSleepTime(1000)// 使用代理了之后,代理会通过切换IP来防止反扒。同时,使用代理本身qps降低了,所以这个可以小一些
            .setCycleRetryTimes(3)// 这个重试会换IP重试,是setRetryTimes的上一层的重试,不要怕三次重试解决一切问题。。
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");

    public AnJKProcessor() {
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        List<String> nameList = VillageName.getNameList();
        final String prefix = "https://bj.zu.anjuke.com/fangyuan/px7/?t=1&from=0&comm_exist=on&kw={0}&cw={1}";
        List<String> urlList = new ArrayList<String>();
        for (String name : nameList) {
            String encode = URLEncoder.encode(name, "UTF-8");
            String format = MessageFormat.format(prefix, encode, encode);
            urlList.add(format);
        }

        //按价格升序排列
        Spider.create(new AnJKProcessor())
                .setUUID("安居客final")
                .setDownloader(new DungProxyDownloader())
                .startUrls(urlList)
                .addPipeline(new HuljFileLine("d:\\temp"))
                .thread(10)
                .run();
    }

    public void process(Page page) {
        //当前页面可以点击的 页码, 排除 上一页、下一页
        List<String> list = page.getHtml().$(".multi-page :not(.aPre, .aNxt)").links().all();
        page.addTargetRequests(list);

        //详情页地址
        List<String> detailUrlList = page.getHtml().$(".zu-itemmod", "link").all();
        //价格
        List<String> priceList = page.getHtml().$(".zu-itemmod > .zu-side strong", "text").all();
        //地址
        List<String> locationNameList = page.getHtml().$(".zu-info >h3 a", "title").all();

        SpiderResult spiderResult = new SpiderResult();
        List<String> lineList = new ArrayList<String>();

        for (int i = 0; i < detailUrlList.size(); i++) {
            String price = priceList.get(i);
            String locationName = StringUtils.deleteWhitespace(locationNameList.get(i));
            String url = detailUrlList.get(i);
            lineList.add(new SpiderResult.ResultLine(price, locationName, url).toString());
        }

        String villageName = StringUtils.substringAfterLast(page.getRequest().getUrl(), "=");
        spiderResult.setVillageName(villageName);
        spiderResult.setResultList(lineList);

        if (lineList.isEmpty()) {
            page.setSkip(true);
        }

        page.putField("result", spiderResult);
    }

    public Site getSite() {
        return site;
    }
}
