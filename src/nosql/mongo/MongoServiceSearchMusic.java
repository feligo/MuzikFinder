package nosql.mongo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import interfaces.MFMusic;
import server.dto.MusicDTO;
import utils.IdMusicScore;
import utils.MuzikFinderPreferences;

public class MongoServiceSearchMusic {
	
	public static List<MFMusic> filterByExistingMusics(List<MFMusic> musics, MongoService ms) {
		List<String> idAlbumAlreadyExist = ms.getAllAlbumIds();

		if(idAlbumAlreadyExist==null) return musics;

		List<MFMusic> listReduce = new ArrayList<MFMusic>();
		for(MFMusic mfm : musics){
			if(!idAlbumAlreadyExist.contains(mfm.getAlbumId())){
				listReduce.add(mfm);
			}
		}
		return listReduce;
	}

	public static List<MusicDTO> searchMusics(List<String> tags, MongoService ms, String idRecherche){
		
		Instant start = Instant.now();
		List<MusicDTO> resultSearchMusicsByTagsInLyrics = ms.searchMusicsByTagsInLyrics(tags, idRecherche);
		Instant end = Instant.now();
		System.out.println("searchMusicsByTagsInLyrics =="+Duration.between(start, end));
		System.out.println("resultSearchMusicsByTagsInLyrics size =="+resultSearchMusicsByTagsInLyrics.size());
		if(resultSearchMusicsByTagsInLyrics.isEmpty()){
			start = Instant.now();
			List<MusicDTO> resultInTagsByTags = ms.searchMusicsByTagsInTags(tags, idRecherche);
			end = Instant.now();
			System.out.println("searchMusicByTagsInTags =="+Duration.between(start, end)); // prints PT1M3.553S
			System.out.println("resultInTagsByTags size = "+resultInTagsByTags.size());
			return resultInTagsByTags;
		}
		return resultSearchMusicsByTagsInLyrics;
	}
	
	public static List<MusicDTO> searchMusicsByTagsInTags(List<String> tags, MongoService ms, String idRecherche){
		List<IdMusicScore> idMusicScore = new ArrayList<>();
		List<String> listIdMusics = new ArrayList<>();
		boolean presentInList = false;

		for(String tag : tags){
			listIdMusics = ms.getIdMusicsByTag(tag);
			for(String idMusic : listIdMusics){
				for(IdMusicScore musicScore : idMusicScore){
					if(musicScore.getIdMusic().equals(idMusic)){
						musicScore.incrementScore();
						presentInList=true;
						break;
					} 
				}
				if(!presentInList){
					IdMusicScore ims = new IdMusicScore(idMusic, 1);
					idMusicScore.add(ims);
				}else{
					presentInList=false;
				}
			}
		}
		listIdMusics.removeAll(listIdMusics);
		
		Collections.sort(idMusicScore);
		
		for(IdMusicScore iMS: idMusicScore){
			listIdMusics.add(iMS.getIdMusic());
		}
		
		return generateListMusicDTOWithListId(listIdMusics, ms, idRecherche);
	}
	

	/**
	 * Cette méthode permet de chercher les musics correspondantes au tags entrés par
	 * l'utilisateur en traitant les tags comme une phrase complète
	 * @param tags
	 * @return
	 */
	public static List<String> matchMusicsWithTags(List<String> tags, MongoService ms){
		ArrayList<String> result = new ArrayList<String>();
		MongoCollection<Document> collection_Musics = ms.getCollection(MongoCollections.MUSICS);
		MongoCursor<Document> cursor_Music = ms.findBy(collection_Musics, ms.formRegexForSearch(tags));
		
		// On sors les variables temporaires du for pour utiliser efficacement l'espace mémoire
		Document doc_lyrics;
		String idMusic;
		while(cursor_Music.hasNext()){
			doc_lyrics = cursor_Music.next();
			idMusic = doc_lyrics.getString("idMusic");
			result.add(idMusic);
		}
		return result;
	}

	public static List<MusicDTO> searchMusicsByTagsInLyrics(List<String> tags, MongoService ms, String idRecherche){
		List<MusicDTO> listMDTO = new ArrayList<MusicDTO>();
		if(tags.size()<3){	//Si il y a moins de 3 tags on ne fait pas de recherche by tags in lyrics
			return listMDTO;
		}
		int i=0;
		int k=0;
		List<String> tag = new ArrayList<String>();
		List<String> tmp = new ArrayList<String>();
		while(tags.size()-i>=3){
			k=0;
			while(k<=i){
				for(int j=k;j<tags.size()-i+k;j++){
					tag.add(tags.get(j));
				}
				tmp.addAll(ms.matchMusicsWithTags(tag));
				tag.removeAll(tag);
				k++;
			}
			if(!tmp.isEmpty()){
				break;
			}
			i++;
		}
	
		return generateListMusicDTOWithListId(tmp, ms, idRecherche);
	}

	public static List<MusicDTO> generateListMusicDTOWithListId(List<String> idMusics, MongoService ms, String idRecherche){
		
		List<String> copyIdMusics= new ArrayList<String>();
		copyIdMusics.addAll(idMusics);
		List<Integer> listIndexToDelete=new ArrayList<Integer>();
		List<MusicDTO> listMDTO = new ArrayList<MusicDTO>();
		for(int i=0; i<idMusics.size() && i < MuzikFinderPreferences.LIMITACCEPTABLETEMPS ;i++){
			MusicDTO msDTO;
			MongoCollection<Document> collection_Musics = ms.getCollection(MongoCollections.MUSICS);
			
			MongoCursor<Document> cursor_Musics;
			Document doc_Musics, findQuery_MusicByIdMusic;
			findQuery_MusicByIdMusic = new Document("idMusic", new Document("$eq", idMusics.get(i)));
			cursor_Musics = ms.findBy(collection_Musics, findQuery_MusicByIdMusic);
			if(cursor_Musics.hasNext()){ // On récupere l'ensemble du document dans Musics faisant 
				doc_Musics = cursor_Musics.next(); // reference a la musique avec l'id ms.getIdMusic
				String nameArtist = "";
				String albumId = "";
			
				msDTO = new MusicDTO(idMusics.get(i), doc_Musics.getString("nameMusic"), doc_Musics.getString("idArtist"),
						nameArtist, albumId, doc_Musics.getString("spotifyId"), doc_Musics.getString("soundcloudId"));
				listMDTO.add(msDTO);
				
			}
			listIndexToDelete.add(0,i);
		}
		for(int i=0;i<listIndexToDelete.size();i++){
			copyIdMusics.remove(listIndexToDelete.get(i).intValue());
		}
		if(copyIdMusics != null && !copyIdMusics.isEmpty()){
			ms.insertCacheSearchUser(copyIdMusics, idRecherche);
		}
		
		return listMDTO;
	}
}