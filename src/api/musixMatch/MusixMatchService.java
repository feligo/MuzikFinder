package api.musixMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.musixMatch.metier.Album;
import api.musixMatch.utils.MusixMatchAPIHelper;
import api.musixMatch.utils.MusixMatchConstants;
import api.musixMatch.utils.MusixMatchUtils;
import api.musixMatch.utils.RequestHelper;
import interfaces.MFArtist;
import interfaces.MFMusic;

public class MusixMatchService {

	public MusixMatchService() {
		// TODO SINGLETON
	}

	public List<String> getAllAlbumIds(String artistId) {
		Map<String, String> params = new HashMap<>();
		int page=1;
		List<String> allIds = new ArrayList<>();
		List<Album> acceptedAlbums = new ArrayList<>();
		List<Album> albumsFromAPI = null;
		
		do {
			params.put(MusixMatchConstants.ARTIST_ID, artistId);
			params.put(MusixMatchConstants.PAGE, Integer.toString(page));
			params.put(MusixMatchConstants.PAGE_SIZE, Integer.toString(MusixMatchConstants.MAX_PAGE) );
			params.put(MusixMatchConstants.RELEASE_DATE, MusixMatchConstants.RELEASE_DATE_DESC);
			String request = RequestHelper.createRequest(MusixMatchConstants.ARTIST_ALBUMS_GET, params);
			
//			System.out.println("Requête in MusixMatch : "+request);

			String response = RequestHelper.sendRequest(request);
			
			albumsFromAPI = MusixMatchAPIHelper.getAlbumList(response);
			for(Album alb : albumsFromAPI){
				if( MusixMatchUtils.isAnAlbum(alb) && !MusixMatchUtils.containsSameAlbum(alb, acceptedAlbums)) {
					acceptedAlbums.add(alb);
				}
			}
		} while ( albumsFromAPI.size()>MusixMatchConstants.MAX_PAGE );
		
		acceptedAlbums.forEach(alb -> allIds.add(alb.getAlbumId()));
		return allIds;
	}
	
	/**
	 * Récupère la liste des musiques (avec paroles) d'un album
	 * API url example : http://api.musixmatch.com/ws/1.1/album.musics.get?apikey=f29172a320a83fa2eae8802fa44cbb01&album_id=16742456&page=1&page_size=100
	 * @param albumId
	 * @return
	 */
	public List<MFMusic> getMusicsInAlbum(String albumId) {
		Map<String, String> params = new HashMap<>();

		params.put(MusixMatchConstants.PAGE_SIZE, Integer.toString(MusixMatchConstants.MAX_PAGE) );
		params.put(MusixMatchConstants.ALBUM_ID, albumId);
		String request = RequestHelper.createRequest(MusixMatchConstants.ALBUM_TRACKS_GET, params);
		
//		System.out.println("Requête in MusixMatch : "+request);

		String response = RequestHelper.sendRequest(request);
		
		List<MFMusic> musics = MusixMatchAPIHelper.getMusicsList(response);
		musics.forEach(music -> addLyricsToMusic(music)); // add Lyrics from API
		
		return musics;
	}

	private void addLyricsToMusic(MFMusic music) {
		if( !music.getHasLyrics().equals(MusixMatchConstants.NO_LYRICS) ){
			Map<String, String> params = new HashMap<>();

			params.put(MusixMatchConstants.TRACK_ID, music.getTrackId());
			String request = RequestHelper.createRequest(MusixMatchConstants.TRACK_LYRICS_GET, params);
			
//			System.out.println("Requête in MusixMatch : "+request);

			String response = RequestHelper.sendRequest(request);
			
			music.setLyrics( MusixMatchAPIHelper.getLyrics(response) );
		}
	}

	public List<MFMusic> getTopMusics(int from, int to, String country) {
		Map<String, String> params = new HashMap<>();
		params.put(MusixMatchConstants.PAGE_SIZE, Integer.toString(to));
		params.put(MusixMatchConstants.PAGE, Integer.toString(from));
		params.put(MusixMatchConstants.COUNTRY, country);
		String request = RequestHelper.createRequest(MusixMatchConstants.TRACK_CHART_GET, params);
//		System.out.println("Requête in MusixMatch : "+request);

		String response = RequestHelper.sendRequest(request);
		return MusixMatchAPIHelper.getMusicsList(response);
	}
	
	public List<MFArtist> getTopArtists(int pos, int nbArtistsToGet, String country) {
		Map<String, String> params = new HashMap<>();

		params.put(MusixMatchConstants.PAGE_SIZE, Integer.toString(nbArtistsToGet));
		params.put(MusixMatchConstants.PAGE, Integer.toString(pos));
		params.put(MusixMatchConstants.COUNTRY, country);
		String request = RequestHelper.createRequest(MusixMatchConstants.ARTIST_CHART_GET, params);
		
//		System.out.println("Requête in MusixMatch : "+request);

		String response = RequestHelper.sendRequest(request);
		
		return MusixMatchAPIHelper.getArtistsList(response);
	}

}
