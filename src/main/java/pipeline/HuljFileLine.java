package pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import bean.SpiderResult;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

/**
 * Created by sshss on 2017/5/5.
 *
 *
 * 结果文件处理类
 */
public class HuljFileLine extends FilePersistentBase implements Pipeline {


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * create a FilePipeline with default path"/data/webmagic/"
     */
    public HuljFileLine() {
        setPath("/data/webmagic/");
    }

    public HuljFileLine(String path) {
        setPath(path);
    }

    public void process(ResultItems resultItems, Task task) {
        String path = this.path + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;

        try {
            for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
                SpiderResult value = (SpiderResult) entry.getValue();
                //小区名称作为 文件名
                String villageName = URLDecoder.decode(value.getVillageName(),"utf-8");

                String s1 = path + villageName + ".txt";
                System.out.println("#########=" + s1);

                //该小区的搜索结果为文件内容
                List<String> resultList = value.getResultList();
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(getFile(path + villageName + ".txt"), true), "UTF-8"));
                for (String s : resultList) printWriter.println(s);

                printWriter.close();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
    }
}
