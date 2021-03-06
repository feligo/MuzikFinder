package utils;

import java.util.Date;

public final class MuzikFinderPreferences {

///////////////////======= Preferences ======= ////////////
	public static final boolean LOGS = false;
	
	// MusixMatch et daemons
	public static final int MAX_TOP_TRACKS = 200;
	public static final int PAGE_MAX = 100;
	public static final String MUSIXMATCH_KEY_PREF = "musixmatch_pos";
	public static final String POS_COUNTRY_PREF = "posCountry";
	public static final String[] COUNTRY_ORDER = {"us", "gb", "au", "fr", "ca", "be"}; // norme ISO
	
	// Client
	public static final int MIN_SIZE_OF_USERNAME = 5;
	public static final int MIN_SIZE_OF_PASSWORD = 5;
	public static final int SIZE_OF_TYPEAHEAD = 6; // Proposition max de l'auto-complétion de recherche
	public static final int LIMITACCEPTABLE_NB_MUSICS = 15;
	public static final int MAX_SIZE_OF_TAGS_FOR_SEARCH = 10;
	public static final int MIN_SIZE_OF_TAGS_FOR_SEARCH = 3;
	public static final int NB_MAX_TOPS_TO_DISPLAY = 10;
	
	///// COOKIES
	public static final int COOKIE_DURATION = 60*60*2; // 2 hour
	public static final String COOKIE_LOGIN = "MUZIKFINDERLOGIN";
	public static final String COOKIE_BIRTH = "MUZIKFINDERBIRTH";
	public static final String COOKIE_USERID = "MUZIKFINDERUSERID";
	public static final String COOKIE_PATH = "/";

	
	private MuzikFinderPreferences(){ }

	public static String getCountry(int pos) {
		if(pos>(COUNTRY_ORDER.length-1)){
			return COUNTRY_ORDER[0];
		}
		return COUNTRY_ORDER[pos];
	}
	
	public static int getNextPosition(int lastPos){
		return (lastPos+1) % COUNTRY_ORDER.length;
	}
	
	public static long getTimeTopCache(){
		return new Date().getTime() - TimeInMilliSeconds.HOUR.value;
	}
	
}
