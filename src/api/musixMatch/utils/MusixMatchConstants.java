package api.musixMatch.utils;

public class MusixMatchConstants {
	
	////// ======= KEY ========  //////
	public static final String API_KEY_NAME = "apikey";
	public static final String API_KEY_1 = "f29172a320a83fa2eae8802fa44cbb01";
	public static final String API_KEY_2 = "3f74bd630e548103cf652fdc2bef1d5f";
	public static final String API_KEY_3 = "1c70c058fef4efd302f75f721a3d588d";
	public static final String API_KEY_4 = "eedaada00cbdddc234a0d48b1615230a";
	public static final String API_KEY_REGXP = "API_KEY_TO_REPLACE";
	public static final String[] API_KEYS = {API_KEY_3, API_KEY_2, API_KEY_4, API_KEY_1};
	
    public static final String API_URL = "http://api.musixmatch.com/ws/1.1/";
    
    public static final String F_HAS_LYRICS = "f_has_lyrics";
    public static final String PAGE = "page";
    public static final String PAGE_SIZE = "page_size";

    // FROM MuzixMatch API
	public static final String COUNTRY = "country";
	public static final String ARTIST_LIST = "artist_list";
	public static final String ARTIST = "artist";
	public static final String ARTIST_ID = "artist_id";
	public static final String RELEASE_DATE= "s_release_date";
	public static final String RELEASE_DATE_DESC= "desc";
	public static final String RELEASE_DATE_ASC= "asc";
	public static final Integer MAX_PAGE = 99;
	
	public static final String ALBUM_LIST = "album_list";
	public static final String ALBUM = "album";
	public static final String ALBUM_ID = "album_id";

	public static final String TRACK_LIST = "track_list";
	public static final String TRACK = "track";
	public static final String TRACK_ID = "track_id";
	
	public static final Object NO_LYRICS = "0";

	public static final String LYRICS = "lyrics";
	
	/// METHODS ///
    public static final String ALBUM_GET = "album.get";
    public static final String ALBUM_TRACKS_GET = "album.tracks.get";
    public static final String ARTIST_ALBUMS_GET = "artist.albums.get";
    public static final String ARTIST_CHART_GET = "artist.chart.get";
    public static final String ARTIST_GET = "artist.get";
    public static final String ARTIST_SEARCH = "artist.search";
    public static final String MATCHER_TRACK_GET = "matcher.track.get";
    public static final String TRACKING_URL_GET = "tracking.url.get";
    public static final String TRACK_CHART_GET = "track.chart.get";
    public static final String TRACK_GET = "track.get";
    public static final String TRACK_LYRICS_FEEDBACK_POST="track.lyrics.feedback.post";
    public static final String TRACK_LYRICS_GET = "track.lyrics.get";
    public static final String TRACK_SNIPPET_GET = "track.snippet.get";
    public static final String TRACK_SUBTITLE_GET = "track.subtitle.get";
    public static final String TRACK_SEARCH = "track.search";
    public static final String TRACK_SUBTITLE = "track.subtitle.get";
}
