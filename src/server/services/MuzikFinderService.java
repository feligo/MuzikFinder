package server.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.API;
import interfaces.MFArtist;
import interfaces.MFMusic;
import nosql.NoSQLDB;
import sql.SQLDB;
import sql.metier.User;
import utils.MuzikFinderPreferences;

public class MuzikFinderService {
	
	private NoSQLDB nosql;
	private SQLDB sql;
	private API api;
	
	/**
	 * @param withSQL Pour instancier uniquement les connexions à la base NoSQL et aux API
	 */
	private MuzikFinderService(boolean withSQL){
		 nosql = new NoSQLDB(); // (va instancier ou récupérer le singleton du NoSQL, MongoDB, Cassandra ou autre) 
		 if(withSQL) sql = new SQLDB(); // (va instancier ou récupérer le singleton du SQL, MySQL, PostgreSQL ou autre) 
		 api = new API(); // (va instancier ou récupérer le singleton de l'API, MusixMatch ou autre) 
	}
	
	/** Instance unique préinitialisée */
	private static MuzikFinderService INSTANCE = getInstance();
 
	/**
	 * Technique du double checking (Singleton)
	 * @return
	 */
	public static MuzikFinderService getInstance(boolean withSQL) {
		if (INSTANCE == null){ 	
			synchronized(MuzikFinderService.class){
				if (INSTANCE == null){
					INSTANCE = new MuzikFinderService(withSQL);
				}
			}
		}
		return INSTANCE;
	}
	
	public static MuzikFinderService getInstance() {
		return getInstance(true);
	}
	
	////====== API PART ====== ////
	public List<MFArtist> getTopArtistsFromAPI(int pos, int nbArtistsToGet, String country) {
		return api.getTopArtists(pos, nbArtistsToGet, country);
	}
	
	public List<MFMusic> getAllMusicsFromAPI(String albumId) {
		return api.getMusicsInAlbum(albumId);
	}
	
	private List<MFMusic> getTopMusicsFromAPI(int from, int to, String country){
		return api.getTopMusics(from, to, country);
	}
	
	
	//// ====== NOSQL PART ====== ////
	public List<String> getIdMusicsByTagInNoSQL(String tag){
		return nosql.getIdMusicsByTag(tag);
	}
	
	public List<String> getIdMusicsByIdArtistInNoSQL(String artist){
		return nosql.getIdMusicsByIdArtist(artist);
	}
	
	public List<String> getIdMusicsByChainWordsInNoSQL(String lyrics){
		return nosql.getIdMusicsByChainWords(lyrics);
	}
	
	public List<MFMusic> searchMusics(List<String> tags, String idRecherche) {
		return nosql.searchMusics(tags, idRecherche);
	}
	
	public MFMusic getMusicById(String idMusic) {
		return nosql.getMusicById(idMusic);
	}
	
	
	////====== SQL PART ====== ////
	
	public User checkConnection(String username, String password) {
		return sql.checkConnexion(username, password);
	}
	
	public User createNewUser(String username, String password, String mail, int year, int month, int day) {
		return sql.createNewUser(username, password, mail, year, month, day);
	}
	
	public boolean checkLogin(String username) {
		return sql.checkLogin(username);
	}
	
	
	////====== DAEMON PART ====== ////
	
	public void startFilingDatabaseProcess() {
		String lastCountry_str = nosql.getPref(MuzikFinderPreferences.POS_COUNTRY_PREF);
		int lastCountry;
		if(lastCountry_str==null) lastCountry = 0;
		else lastCountry = Integer.parseInt(lastCountry_str);
		
		System.out.println("Pays visé : "+MuzikFinderPreferences.getCountry(lastCountry).toUpperCase());
		List<MFMusic> musics = getTopMusicsFromAPI(0, MuzikFinderPreferences.MAX_TOP_TRACKS, MuzikFinderPreferences.getCountry(lastCountry));
		System.out.println(musics.size()+" musiques");
		List<MFMusic> musicsNotInNoSQL = nosql.filterByExistingMusics(musics);
		
		Map<String, List<MFMusic>> mapAlbumIdWithAlbum = new HashMap<>();
		int cpt = 0;
		for(MFMusic music : musicsNotInNoSQL){
			if(music!=null && music.getAlbumId()!=null){
				mapAlbumIdWithAlbum.put( music.getAlbumId(), getAllMusicsFromAPI(music.getAlbumId()) );
			}
			
			System.out.println("Album "+music.getAlbumId()+" = "+mapAlbumIdWithAlbum.get(music.getAlbumId()).size()+" musics...");
			cpt+=mapAlbumIdWithAlbum.get(music.getAlbumId()).size();
		}
		System.out.println("\nNombre de musiques récupérées au final : "+cpt);
		nosql.insertNewMusics(mapAlbumIdWithAlbum);
		nosql.setPref(MuzikFinderPreferences.POS_COUNTRY_PREF, Integer.toString(MuzikFinderPreferences.getNextPosition(lastCountry)) );
	}

}