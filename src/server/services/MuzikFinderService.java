package server.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import api.API;
import api.musixMatch.metier.Artist;
import api.musixMatch.metier.Music;
import nosql.NoSQLDB;
import sql.SQLDB;
import utils.MuzikFinderPreferences;

/**
 * Aucun appel direct aux BD ou Api ici !!
 * @author JulienM
 *
 */
public class MuzikFinderService {
	
	private NoSQLDB nosql;
	private SQLDB sql;
	private API api;
	
	public MuzikFinderService(){
		 nosql = new NoSQLDB(); // (va instancier ou récupérer le singleton du NoSQL, Mongo ou autre) 
		 sql = new SQLDB(); // (va instancier ou récupérer le singleton du SQL, MySQL ou autre) 
		 api = new API(); // (va instancier ou récupérer le singleton de l'API, MusixMatch ou autre) 
	}
	
	
	////====== API PART ====== ////
	
	public List<Artist> getTopArtistsFromAPI(int pos, int nbArtistsToGet, String country) {
		return api.getTopArtists(pos, nbArtistsToGet, country);
	}

	public List<String> getAllAlbumIdsFromAPI(String artistId) {
		return api.getAllAlbumIds(artistId);
	}

	public List<Music> getAllMusicsFromAPI(String albumId) {
		return api.getMusicsInAlbum(albumId);
	}
	
	private List<Music> getTopMusicsFromAPI(int from, int to, String country){
		return api.getTopMusics(from, to, country);
	}
	
	
	//// ====== NOSQL PART ====== ////
	
	private boolean containsArtistsInNoSQL(String artist){
		return nosql.presentArtist(artist);
	}
	
	public Set<String> getMusicByTagInNoSQL(String tag){
		if(!nosql.presentTag(tag))
			return null;
		else
			return nosql.getMusicsByTag(tag);
	}
	
	public Set<String> getMusicByIdArtistInNoSQL(String artist){
		if(!nosql.presentArtist(artist))
			return null;
		else
			return nosql.getMusicsByIdArtist(artist);
	}
	
	public Set<String> getMusicsByLyricsInNoSQL(String lyrics){
		return nosql.getMusicsByLyrics(lyrics);
	}
	
	
	////====== SQL PART ====== ////
	
	
	////====== DAEMON PART ====== ////
	
	private List<String> extractImportantWords(String lyrics){
		//TODO: utiliser TextParser du package utils.textMining;
		return null;
	}

	public void startFilingDatabaseProcess() {
		List<Music> musics = getTopMusicsFromAPI(0, MuzikFinderPreferences.MAX_TOP_TRACKS, MuzikFinderPreferences.COUNTRY_ORDER[0]);
		System.out.println(musics.size()+" musiques");
		//TODO: remove if exists in mongo
		Map<String, List<Music>> fromSameAlbum = new HashMap<>();
		int cpt = 0;
		for(Music music : musics){
			if(music!=null && music.getAlbumId()!=null){
				fromSameAlbum.put( music.getAlbumId(), getAllMusicsFromAPI(music.getAlbumId()) );
			}
			System.out.println("Album "+music.getAlbumId()+" = "+fromSameAlbum.get(music.getAlbumId()).size()+" musics...");
			cpt+=fromSameAlbum.get(music.getAlbumId()).size();
		}
		System.out.println("\nNombre de musiques récupérées au final : "+cpt);
		// insert in mongo
	}
	
}
