import com.virjar.dungproxy.client.webmagic.DungProxyDownloader;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import bean.SpiderResult;
import bean.VillageName;
import pipeline.HuljFileLine;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * Created by sshss on 2017/5/6.
 */
public class ZiRuProcessor implements PageProcessor {

    private Site site = Site.me()// .setHttpProxy(new HttpHost("127.0.0.1",8888))
            .setRetryTimes(3) // 就我的经验,这个重试一般用处不大,他是httpclient内部重试
            .setTimeOut(30000)// 在使用代理的情况下,这个需要设置,可以考虑调大线程数目
            .setSleepTime(1000)// 使用代理了之后,代理会通过切换IP来防止反扒。同时,使用代理本身qps降低了,所以这个可以小一些
            .setCycleRetryTimes(3)// 这个重试会换IP重试,是setRetryTimes的上一层的重试,不要怕三次重试解决一切问题。。
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");

    public static void main(String[] args) throws UnsupportedEncodingException {

//        String searchName = "天通苑";
//        searchName = URLEncoder.encode(searchName,"utf-8");
//
//        String prefix = "http://www.ziroom.com/z/nl/r2000TO4000-z3-o1.html?qwd={0}";
//        String url = MessageFormat.format(prefix, searchName);
//
//        Spider.create(new ZiRuProcessor())
//                .setUUID("自如")
//                .addUrl(url)
//                .addPipeline(new HuljFileLine("d:\\temp"))
//                .thread(1)
//                .run();


        List<String> nameList = VillageName.getNameList();

        // 价格区间 2000~4000, 按价格升序输出
        final String prefix = "http://www.ziroom.com/z/nl/r2000TO4000-z3-o1.html?qwd={0}";
        List<String> urlList = new ArrayList<String>();
        for (String name : nameList) {
            String encode = URLEncoder.encode(name, "UTF-8");
            String format = MessageFormat.format(prefix, encode);
            urlList.add(format);
        }

        Spider.create(new ZiRuProcessor())
                .setUUID("自如")
                .setDownloader(new DungProxyDownloader())
                .startUrls(urlList)
                .addPipeline(new HuljFileLine("d:\\temp"))
                .thread(10)
                .run();
    }

    public void process(Page page) {

        List<String> pageList = page.getHtml().$("#page > a:not(.prev):not(.active):not(.next)").links().all();

        page.addTargetRequests(pageList);

        //标题
        List<String> locationNameList = page.getHtml().$("#houseList .t1", "text").all();
        //详情页
        List<String> detailUrlList = page.getHtml().$("#houseList .t1", "href").all();

        // "¥ 2295"每月    "¥ 69"每天
        List<String> priceList = page.getHtml().$("#houseList .price", "text").all();

        SpiderResult spiderResult = new SpiderResult();
        List<String> lineList = new ArrayList<String>();

        String exp = "[^0-9]";
        Pattern pattern = Pattern.compile(exp);

        for (int i = 0; i < priceList.size(); i++) {

            String price = priceList.get(i);
            String trim = pattern.matcher(price).replaceAll("").trim();

            Integer priceVal = Integer.valueOf(trim);
            if (priceVal != null && priceVal < 2000) {
                priceVal *= 30;
            }

            String locationName = StringUtils.deleteWhitespace(locationNameList.get(i));
            String url = detailUrlList.get(i);
            lineList.add(new SpiderResult.ResultLine(priceVal.toString(), locationName, url).toString());
        }

        //fixme 不是很完善
        String requestUrl = page.getRequest().getUrl();

        String villageName = requestUrl.indexOf("&p") > 0
                ? StringUtils.substringBetween(page.getRequest().getUrl(), "qwd=", "&")
                : StringUtils.substringAfterLast(requestUrl, "qwd=");

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
