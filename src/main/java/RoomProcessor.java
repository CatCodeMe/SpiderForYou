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
 * @author hulj
 * @since 2017-5-5 23:27:19
 *
 * 抓取 www.107room.com 租房数据
 */
public class RoomProcessor implements PageProcessor {

    private Site site = Site.me()// .setHttpProxy(new HttpHost("127.0.0.1",8888))
            .setRetryTimes(3) // 就我的经验,这个重试一般用处不大,他是httpclient内部重试
            .setTimeOut(30000)// 在使用代理的情况下,这个需要设置,可以考虑调大线程数目
            .setSleepTime(0)// 使用代理了之后,代理会通过切换IP来防止反扒。同时,使用代理本身qps降低了,所以这个可以小一些
            .setCycleRetryTimes(3)// 这个重试会换IP重试,是setRetryTimes的上一层的重试,不要怕三次重试解决一切问题。。
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");


    public void process(Page page) {
        //当前条件下的 分页页面 url
        List<String> hrefList = page.getHtml().$("#kkpager > a").links().all();
        page.addTargetRequests(hrefList);

        List<String> priceList = page.getHtml().xpath("//li[@class='price']/span/text()").all();
        List<String> locationNameList = page.getHtml().xpath("//a[@class='locationname']/text()").all();
        //点击进入详情页的 后缀url (注意有特殊字符, /r)
        List<String> detailUrlList = page.getHtml().$(".locationname", "href").all();

        SpiderResult spiderResult = new SpiderResult();
        List<String> lineList = new ArrayList<String>();
        for (int i = 0; i < priceList.size(); i++) {
            String price = priceList.get(i);
            String locationName = StringUtils.deleteWhitespace(locationNameList.get(i));
            String url = detailUrlList.get(i);
            lineList.add(new SpiderResult.ResultLine(price, locationName, url).toString());
        }

        String villageName = StringUtils.substringAfterLast(page.getRequest().getUrl(), "=");
        spiderResult.setVillageName(villageName);
        spiderResult.setResultList(lineList);

        if(lineList.isEmpty()){
            page.setSkip(true);
        }
        page.putField("result", spiderResult);
    }

    public Site getSite() {
        return site;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        List<String> nameList = VillageName.getNameList();
        final String prefix = "http://www.107room.com/z2_a1_1?query={0}";
        List<String> urlList=  new ArrayList<String>();
        for (String name : nameList) {
            String searchName = URLEncoder.encode(StringUtils.deleteWhitespace(name),"utf-8");
            String finalUrl = MessageFormat.format(prefix, searchName);
            urlList.add(finalUrl);
        }

        Spider.create(new RoomProcessor())
                .setUUID("107间房final")
                .startUrls(urlList)
                .setDownloader(new DungProxyDownloader())
                .addPipeline(new HuljFileLine("d:\\temp"))
                .thread(10).run();
    }
}
