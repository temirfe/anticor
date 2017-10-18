package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 7/19/2017.
 */

public class Endpoints {
    public static final String SCHEME = "http";
    public static final String AUTHORITY = "anticorruption.cf";
    public static final String API = "api";

    public static final String BASE_URL = "http://anticorruption.cf";
    public static final String API_URL = BASE_URL +"/api";
    public static final String LOGIN = API_URL + "/accounts/login";
    public static final String USERS = API_URL + "/accounts";
    public static final String VOC_DEPEND = API_URL + "/vocabularies/depend";
    public static final String IMG = BASE_URL +"/images";
    public static final String NEWS_IMG = IMG + "/news/";
    public static final String AUTHORITY_IMG = IMG + "/authority/";
    public static final String REPORT_IMG = IMG + "/report/";
    public static final String PAGES = API_URL + "/pages";
    public static final String VOCABULARIES = API_URL + "/vocabularies";
    public static final String AUTHORITIES = API_URL + "/authorities";
    public static final String AUTH_DEPEND = API_URL + "/authorities/depend";
    public static final String COMMENTS = API_URL + "/comments";
    public static final String REPORTS = API_URL + "/reports";
    public static final String REPORT_DEPEND = API_URL + "/reports/depend";
    public static final String NEWS = API_URL + "/news";
    public static final String NEWS_DEPEND = API_URL + "/news/depend";
    public static final String AUTHORITY_RATE = AUTHORITIES + "/rate";
    public static final String AUTHORITY_USER_RATE = AUTHORITIES + "/userrate";
    public static final String EDUCATION = API_URL + "/educations";
    public static final String ANALYTICS = API_URL + "/analytics";
    public static final String EDU_IMG = IMG + "/education/";
}
