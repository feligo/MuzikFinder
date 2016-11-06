package nosql;

import java.util.Date;
import java.util.List;
import java.util.Map;

import interfaces.MFMusic;
import nosql.mongo.MongoService;
import server.dto.MusicDTO;

/**
 * Appel les fonctions de la BD choisies (MongoDB, DynamoDB, etc ...)
 * @author JulienM
 */
public class NoSQLDB {

	private MongoService mongo;
	// éventuellement private CassandraService cassandra;
	// éventuellement private DynamoDBService dynamo;

	public NoSQLDB() {
		mongo = MongoService.getInstance();
		// éventuellement cassandra = new CassandraService() ;
		// éventuellement dynamo = new DynamoDBService();
	}

	//////////////PARTIE INSERT///////////////
	public boolean insertLyricsIfNotExists(String words, String musicId, String artistId, String artistName,
			String nameMusic, String langue, String spotifyId, String soundCloudId){
		return mongo.insertLyricsIfNotExists(words, musicId, artistId, artistName, nameMusic, langue, 
				spotifyId, soundCloudId);
	}

	public boolean insertTagIfNotExists(String tag, String musicId){
		return mongo.insertTagIfNotExists(tag, musicId);
	}

	public boolean insertIdAlbumIfNotExist(String idAlbum){
		return mongo.insertIdAlbumIfNotExist(idAlbum);
	}

	public void insertNewMusics(Map<String, List<MFMusic>> mapAlbumIdWithAlbum) {
		try {
			mongo.insertNewMusics(mapAlbumIdWithAlbum);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void insertCacheSearchUser(List<String> idMusics, String idRecherche){
		mongo.insertCacheSearchUser(idMusics, idRecherche);
	}

	///////////////PARTIE CONTAINS//////////////////
	public boolean containsLyrics(String musicId){
		return mongo.containsLyrics(musicId);
	}
	
	public boolean containsTag(String tag) {
		return mongo.containsTag(tag);
	}

	public boolean containsIdMusicInTag(String tag, String idMusic){
		return mongo.containsIdMusicInTag(tag, idMusic);
	}
	
	public boolean containsIdAlbum(String idAlbum) {
		return mongo.containsIdAlbum(idAlbum);
	}

	public boolean containsIdRecherche(String idRecherche){
		return mongo.containsIdRecherche(idRecherche);
	}

	//////////////PARTIE GETTER///////////////////////
	public List<String> getIdMusicsByTag(String tag) {
		return mongo.getIdMusicsByTag(tag);
	}

	public List<String> getIdMusicsByIdArtist(String idArtist){
		return mongo.getIdMusicsByIdArtist(idArtist);
	}

	public List<String> getIdMusicsByChainWords(String chainWords){
		return mongo.getIdMusicsByChainWords(chainWords);
	}

	public List<String> getAllAlbumIds(){
		return mongo.getAllAlbumIds();
	}

	//////////////PARTIE SEARCH///////////////////////
	/**
	 * Reduced the given list in parameter if one of these musics exists in Mongo 
	 * @param musics
	 * @return the reduced list
	 */
	public List<MFMusic> filterByExistingMusics(List<MFMusic> musics) {
		return mongo.filterByExistingMusics(musics);
	}

	public List<MusicDTO> searchMusics(List<String> tags, String idRecherche){
		return mongo.searchMusics(tags, idRecherche);
	}
	
	public List<MusicDTO> searchMusicsByTagsInTags(List<String> tags, String idRecherche){
		return mongo.searchMusicsByTagsInTags(tags, idRecherche);
	}

	/**
	 * Cette méthode permet de chercher les musics correspondantes au tags entrés par
	 * l'utilisateur en traitant les tags comme une phrase complète
	 * @param tags
	 * @return
	 */
	public List<String> matchMusicsWithTags(List<String> tags){
		return mongo.matchMusicsWithTags(tags);
	}

	public List<MusicDTO> searchMusicsByTagsInLyrics(List<String> tags, String idRecherche){
		return mongo.searchMusicsByTagsInLyrics(tags, idRecherche);
	}

	///////////////PARTIE SEARCH USER/////////////////////////
	public void addNewSearch(String idMusic, Date userBirth){
		mongo.addNewSearch(idMusic, userBirth);
	}
	
	public List<MusicDTO> getTopMusicSearchThisWeek(){
		return mongo.getTopMusicSearchThisWeek();
	}
	
	public List<MusicDTO> getTopMusicSearchThisMonth(){
		return mongo.getTopMusicSearchThisMonth();
	}

	//////////////PARTIE PREFERENCE//////////////////////////
	/** Get a pref in nosql database by name
	 * @param param
	 * @return
	 */
	public String getPref(String prefName) {
		return mongo.getPref(prefName);
	}

	/**
	 * Set a pref in nosql database by name
	 * @param prefName
	 * @param param
	 */
	public void setPref(String prefName, String param) {
		mongo.setPref(prefName, param);
	}

}
