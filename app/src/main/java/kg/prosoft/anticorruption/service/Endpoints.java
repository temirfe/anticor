package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 7/19/2017.
 */

public class Endpoints {
    public static final String SCHEME = "https";
    //public static final String AUTHORITY = "anticorruption.kg";
    public static final String AUTHORITY = "anticor.ml";
    public static final String API = "api";

    //public static final String BASE_URL = "https://anticorruption.kg";
    public static final String BASE_URL = "https://anticor.ml";
    public static final String API_URL = BASE_URL +"/api";
    public static final String USERS = API_URL + "/accounts";
    public static final String LOGIN = USERS + "/login";
    public static final String SOCIAL = USERS + "/social";
    public static final String IMG = BASE_URL +"/images";
    public static final String NEWS_IMG = IMG + "/news/";
    public static final String AUTHORITY_IMG = IMG + "/authority/";
    public static final String REPORT_IMG = IMG + "/report/";
    public static final String PAGES = API_URL + "/pages";
    public static final String PAGE_DEPEND = PAGES + "/depend";
    public static final String VOCABULARIES = API_URL + "/vocabularies";
    public static final String VOC_DEPEND = VOCABULARIES + "/depend";
    public static final String AUTHORITIES = API_URL + "/authorities";
    public static final String AUTH_DEPEND = AUTHORITIES + "/depend";
    public static final String COMMENTS = API_URL + "/comments";
    public static final String REPORTS = API_URL + "/reports";
    public static final String REPORT_DEPEND = REPORTS + "/depend";
    public static final String NEWS = API_URL + "/news";
    public static final String NEWS_DEPEND = API_URL + "/news/depend";
    public static final String AUTHORITY_RATE = AUTHORITIES + "/rate";
    public static final String AUTHORITY_USER_RATE = AUTHORITIES + "/userrate";
    public static final String EDUCATION = API_URL + "/educations";
    public static final String ANALYTICS = API_URL + "/analytics";
    public static final String EDU_IMG = IMG + "/education/";
}
