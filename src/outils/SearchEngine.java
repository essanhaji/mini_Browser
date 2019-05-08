package outils;

public class SearchEngine {

    public String urlStr;
    public String engineName;

    public String formatUrl() {
        if (engineName.equalsIgnoreCase("google")) {
            urlStr = urlStr.replace(" ", "+");
            return "https://www.google.com/?gws_rd=cr&ei=YpHvV47aK8vWvATsl5X4CQ#q=" + urlStr;
        } else if (engineName.equalsIgnoreCase("bing")) {
            return "http://www.bing.com/search?q=" + urlStr;
        } else {
            System.out.println("No search eninge by " + engineName + " found.");
            return null;
        }
    }

    public SearchEngine(String engineName, String urlStr) {
        this.engineName = engineName;
        this.urlStr = urlStr;
        formatUrl();
    }

    public void setEngine(String engineName) {
        this.engineName = engineName;
    }

    public void setUrlStr(String urlStr) {
        this.urlStr = urlStr;
    }

    public String getEngine() {
        return engineName;
    }

    public String getUrl() {
        return urlStr;
    }

    public String getEngineSpecificUrl() {
        return formatUrl();
    }
}
