package kg.prosoft.anticorruption.service;

/**
 * Created by ProsoftPC on 7/19/2017.
 */

public class Endpoints {
    public static final String SCHEME = "http";
    public static final String AUTHORITY = "anticorruption.cf";

    public static final String BASE_URL = "http://anticorruption.cf";
    public static final String API_URL = BASE_URL +"/api";
    public static final String LOGIN = API_URL + "/accounts/login";
    public static final String USERS = API_URL + "/accounts";
    public static final String CTGS = API_URL + "/categories";
    public static final String CTG_DEPEND = API_URL + "/categories/depend";
    public static final String IMG = BASE_URL +"/images";
    public static final String PRODUCT_IMG = IMG + "/product/";
    public static final String BANNER_IMG = IMG + "/banner/";
    public static final String BANNERS = API_URL + "/banners";
    public static final String PAGES = API_URL + "/pages";
    public static final String LOOKUPS = API_URL + "/lookups";
    public static final String VOCABULARIES = API_URL + "/vocabularies";
    public static final String LOOKUP_DEPEND = API_URL + "/lookups/depend";
}
