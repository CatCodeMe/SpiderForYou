package bean;

import java.util.List;

/**
 * Created by sshss on 2017/5/5.
 *
 * 爬取得最终结果对象, 对应文件中的每行记录
 */
public class SpiderResult {

    private String villageName;  //小区名称

    private List<String> resultList;

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public List<String> getResultList() {
        return resultList;
    }

    public void setResultList(List<String> resultList) {
        this.resultList = resultList;
    }

    public static class ResultLine{

        private String price;
        private String locationName;
        private String detailUrl;

        public ResultLine() {
        }

        public ResultLine(String price, String locationName, String detailUrl) {
            this.price = price;
            this.locationName = locationName;
            this.detailUrl = detailUrl;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }

        public String getDetailUrl() {
            return detailUrl;
        }

        public void setDetailUrl(String detailUrl) {
            this.detailUrl = detailUrl;
        }

        @Override
        public String toString() {
            return price + "\t" +  detailUrl + "\t" + locationName;
        }
    }
}
