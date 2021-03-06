package nosql.mongo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import interfaces.MFMusic;
import utils.IdMusicScore;
import utils.MuzikFinderPreferences;
import utils.MuzikFinderUtils;

public class MongoServiceSearchUser {
	
	/* A LIRE POUR COMPRENDRE PLUS FACILEMENT LE CODE
	 * range : correspondant aux différentes tranches d'âge (se référer à MongoCollectionsAndKeys pour comprendre)
	 * De même pour tout les noms de clés utilisés dans le code et les collections.
	 * Les méthodes sont légérement expliquées pour en comprendre l'idée générale. 
	 * */
	
	private static MongoService ms = MongoService.getInstance();

	/**
	 * Méthode retournant la tranche d'âge correspondante à l'âge de l'utilisateur dans la collection STATS
	 * @param age
	 * @return la range correspondante dans la collection STATS
	 */ 
	static String getRangeByAge(int age){
		if(age<18)
			return MongoCollectionsAndKeys.MINUSEIGHTEEN_STATS;
		if(age<25)
			return MongoCollectionsAndKeys.MINUSTWENTYFIVE_STATS;
		if(age<50)
			return MongoCollectionsAndKeys.MINUSFIFTY_STATS;
		else
			return MongoCollectionsAndKeys.PLUSFIFTY_STATS;
	}
	
	
	///////////**********************  PARTIE COLLECTION MONGO STATS ****************///////////////////////////
	/**
	 * Méthode permettant de rajouter dans la collection Stats le résultat de la recherche de l'utilisateur.
	 * Il faudra gérer les différents cas dans le parcours de la collection. Créer un nouveau document par 
	 * nouvel weeks ( C'est à dire que chaque document offrira des informations des tops pour chaque semaines).
	 * Il faut également savoir où écrire la nouvelle information. Si la range dans lequel l'âge de l'utilisateur
	 * existe déjà, il faut écrire dedans et ne pas créer une nouvelle (clef,valeur). 
	 * @param l'id de la musique sélectionné par l'utilisateur
	 * @param la date de naissance de l'utilisateur (pour savoir dans quel range stocké l'id et son score)
	 */
	@SuppressWarnings("unchecked")
	static void addNewSearch(String idMusic, LocalDate userBirth){

		int age = MuzikFinderUtils.calculateAge(userBirth, LocalDate.now());

		List<Document> list_id_score;
		List<Document> list;
		List<Document> list_range;
		String range= getRangeByAge(age);
		boolean test = false;
		Document fin = new Document();
		Document old = new Document();
		Document age_range;
		GregorianCalendar gc = new GregorianCalendar(Locale.US);
		String week=(gc.get(Calendar.WEEK_OF_YEAR)+"-"+gc.get(Calendar.YEAR));

		MongoCollection<Document> collection = ms.getCollection(MongoCollectionsAndKeys.STATS);

		Document doc = new Document(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS,new Document("$eq",week));
		MongoCursor<Document> cursor = ms.findBy(collection, doc);
		if(cursor.hasNext()){
			Document doc1;
			fin.put(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS, week);
			old.put(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS, week);
			Document age_range_doc = cursor.next();
			List<Document> list_age_range =  (List<Document>) age_range_doc.get(MongoCollectionsAndKeys.AGERANGE_STATS);
			old.append(MongoCollectionsAndKeys.AGERANGE_STATS, list_age_range);	
			for(Document doc_range : list_age_range){
				if(doc_range.getString(MongoCollectionsAndKeys.AGE_STATS).equals(range)){
					list_id_score = (List<Document>) doc_range.get(MongoCollectionsAndKeys.MUSICS_STATS);
					list = new ArrayList<Document>();
					for(Document d : list_id_score){
						if(d.get(MongoCollectionsAndKeys.IDMUSIC_STATS).equals(idMusic)){
							int val = d.getInteger(MongoCollectionsAndKeys.SCOREMUSIC_STATS);
							val = val + 1;
							test = true;
							doc1 = new Document();
							doc1.put(MongoCollectionsAndKeys.IDMUSIC_STATS, idMusic);
							doc1.append(MongoCollectionsAndKeys.SCOREMUSIC_STATS, val);
							list.add(doc1);
						}
						else
							list.add(d);
					}
					if(test){		
						age_range = new Document();
						age_range.put(MongoCollectionsAndKeys.AGE_STATS,range);
						age_range.put(MongoCollectionsAndKeys.MUSICS_STATS,list);
						list_range = new ArrayList<Document>();
						list_range.add(age_range);
						for(Document doc_tmp : (ArrayList<Document>)old.get(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE))
							if(!doc_tmp.getString(MongoCollectionsAndKeys.AGE_STATS_CACHE).equals(range))
								list_range.add(doc_tmp);
						fin.append(MongoCollectionsAndKeys.AGERANGE_STATS,list_range);
						ms.replaceOne(collection, old, fin);
						return;
					}
					else{
						Document tmp = new Document();
						tmp.put(MongoCollectionsAndKeys.IDMUSIC_STATS, idMusic);
						tmp.append(MongoCollectionsAndKeys.SCOREMUSIC_STATS, 1);
						list.add(tmp);
						age_range = new Document();
						age_range.put(MongoCollectionsAndKeys.AGE_STATS,range);
						age_range.put(MongoCollectionsAndKeys.MUSICS_STATS,list);
						list_range = new ArrayList<Document>();
						list_range.add(age_range);
						for(Document doc_tmp : (ArrayList<Document>)old.get(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE))
							if(!doc_tmp.getString(MongoCollectionsAndKeys.AGE_STATS_CACHE).equals(range))
								list_range.add(doc_tmp);
						fin.append(MongoCollectionsAndKeys.AGERANGE_STATS,list_range);
						ms.replaceOne(collection, old, fin);
						return;
					}
				}
			}

			age_range = new Document();
			Document id_score = new Document();
			id_score.put(MongoCollectionsAndKeys.IDMUSIC_STATS,idMusic);
			id_score.put(MongoCollectionsAndKeys.SCOREMUSIC_STATS,1);
			list_id_score = new ArrayList<Document>();
			list_id_score.add(id_score);
			age_range.put(MongoCollectionsAndKeys.AGE_STATS,range);
			age_range.put(MongoCollectionsAndKeys.MUSICS_STATS,list_id_score);
			List<Document> l_age_range = new ArrayList<Document>();
			l_age_range.add(age_range);
			l_age_range.addAll((ArrayList<Document>) old.get(MongoCollectionsAndKeys.AGERANGE_STATS));
			fin.append(MongoCollectionsAndKeys.AGERANGE_STATS,l_age_range);
			ms.replaceOne(collection,old,fin);
			return;

		}else{
			Document doc_stat = new Document();
			doc_stat.put(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS, week);
			Document id_score = new Document();
			id_score.put(MongoCollectionsAndKeys.IDMUSIC_STATS,idMusic);
			id_score.put(MongoCollectionsAndKeys.SCOREMUSIC_STATS,1);
			list_id_score = new ArrayList<Document>();
			list_id_score.add(id_score);
			age_range = new Document();
			age_range.put(MongoCollectionsAndKeys.AGE_STATS,range);
			age_range.put(MongoCollectionsAndKeys.MUSICS_STATS,list_id_score);
			list_range = new ArrayList<Document>();
			list_range.add(age_range);
			doc_stat.put(MongoCollectionsAndKeys.AGERANGE_STATS,list_range);
			ms.insertOne(collection, doc_stat);
			return;
		}

	}

	////////////////******************* PARTIE COLLECTION MONGO STATS_CACHE ***************////////////////////	
	/**
	 * Méthode permettant d'ajouter dans la collection Stats_Cache les musiques les plus populaires
	 * Elle doit traiter tout les cas possibles. Par exemple, si il existe déjà un champ week, il ne faudra
	 * pas créer un autre document. Il faudra écrire dans ce même document. De même pour chaque clé de la collection. 
	 * @param la range
	 */
	@SuppressWarnings("unchecked")
	static void addListIdMusicMostPopularByRange(String range){
		System.out.println("addList : "+range);
		List<String> list_id_music = new ArrayList<String>();
		if(range.equals(MongoCollectionsAndKeys.GENERAL_STATS_CACHE))
			list_id_music= getListIdMostPopularAllRangeInStats(range);
		else
			list_id_music = getListIdMostPopularByRangeInStats(range);

		if(list_id_music.isEmpty()) return; // Aucune musique correspondant au range dans la Collection Stats 
		MongoCollection<Document> collection = ms.getCollection(MongoCollectionsAndKeys.STATS_CACHE);
		GregorianCalendar gc = new GregorianCalendar(Locale.US);
		String week=(gc.get(Calendar.WEEK_OF_YEAR)+"-"+gc.get(Calendar.YEAR));
		Document doc = new Document(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS_CACHE,new Document("$eq",week));
		MongoCursor<Document> cursor = ms.findBy(collection, doc);
		Document old = new Document();
		Document fin = new Document();
		if(cursor.hasNext()){// Si jamais le champ week existe. C'est à dire s'il y a déjà de musique entrée cette semaine.
			fin.put(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS_CACHE, week);
			old.put(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS_CACHE, week);
			Document age_range_doc = cursor.next();
			List<Document> list_age_range =  (List<Document>) age_range_doc.get(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE);
			old.append(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE, list_age_range);
			for(Document doc_range : list_age_range){
				// Si on a déjà un champ range. C'est à dire si jamais un autre utilisateur du même range que celui qui a envoyé
				// sélectionné la musique à déjà été entré dans notre collection
				if(doc_range.getString(MongoCollectionsAndKeys.AGE_STATS_CACHE).equals(range)){
					Document age_range = new Document();
					age_range.put(MongoCollectionsAndKeys.AGE_STATS_CACHE,range);
					List<Document> list_id_music_doc = new ArrayList<Document>();
					for(String id : list_id_music){
						Document doc_id = new Document();
						doc_id.put(MongoCollectionsAndKeys.MUSIC_ID_STATS_CACHE, id);
						list_id_music_doc.add(doc_id);
					}
					age_range.put(MongoCollectionsAndKeys.MUSICS_STATS_CACHE,list_id_music_doc);
					List<Document> list_range = new ArrayList<Document>();
					list_range.add(age_range);
					for(Document doc_tmp : (ArrayList<Document>)old.get(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE))
						if(!doc_range.getString(MongoCollectionsAndKeys.AGE_STATS_CACHE).equals(range))
							list_range.add(doc_tmp);
					fin.append(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE,list_range);
					//System.out.println(old);
					ms.replaceOne(collection,old,fin);
					return;
				}
			}

			Document age_range = new Document();
			age_range.put(MongoCollectionsAndKeys.AGE_STATS_CACHE,range);
			List<Document> list_id_music_doc = new ArrayList<Document>();
			for(String id : list_id_music){
				Document doc_id = new Document();
				doc_id.put(MongoCollectionsAndKeys.MUSIC_ID_STATS_CACHE, id);
				list_id_music_doc.add(doc_id);
			}
			age_range.append(MongoCollectionsAndKeys.MUSICS_STATS_CACHE,list_id_music_doc);
			List<Document> list_range = new ArrayList<Document>();
			list_range.add(age_range);
			for(Document doc_tmp : (ArrayList<Document>)old.get(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE))
				if(!doc_tmp.getString(MongoCollectionsAndKeys.AGE_STATS_CACHE).equals(range))
					list_range.add(doc_tmp);
			fin.append(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE,list_range);
			ms.replaceOne(collection, old, fin);
			return;
		}
		else{
			Document new_doc = new Document();
			new_doc.put(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS_CACHE, week);
			Document age_range = new Document();
			age_range.put(MongoCollectionsAndKeys.AGE_STATS_CACHE,range);
			List<Document> list_id_music_doc = new ArrayList<Document>();
			for(String id : list_id_music){
				Document doc_id = new Document();
				doc_id.put(MongoCollectionsAndKeys.MUSIC_ID_STATS_CACHE, id);
				list_id_music_doc.add(doc_id);
			}
			age_range.put(MongoCollectionsAndKeys.MUSICS_STATS_CACHE,list_id_music_doc);
			List<Document> list_range = new ArrayList<Document>();
			list_range.add(age_range);
			new_doc.put(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE,list_range);
			ms.insertOne(collection, new_doc);
			return;
		}
	}

	/**
	 * Méthode permettant de récupérer la list d'id des musiques en se servant de la méthode 
	 * getListIdMusicScoreMostPopularByRange pour avoir un tri correct par score.
	 * @param la range
	 * @return la liste des id des musiques
	 */
	static List<String> getListIdMostPopularByRangeInStats(String range){
		List<IdMusicScore> list_music_score = getListIdMusicScoreMostPopularByRange(range);
		if(list_music_score.size() > MuzikFinderPreferences.LIMITACCEPTABLE_NB_MUSICS)
			list_music_score.subList(0, MuzikFinderPreferences.LIMITACCEPTABLE_NB_MUSICS);

		List<String> list_id = new ArrayList<String>();
		for(IdMusicScore mscore : list_music_score){
			list_id.add(mscore.getIdMusic());
		}
		System.out.println("taille list_id : "+range+" = "+list_id.size());
		return list_id;
	}


	@SuppressWarnings("unchecked")
	/**
	 * Récupère la list d'IdMusicScore pour chaque range. Ceci est fait afin de pouvoir trier proprement
	 * les musiques suivant leur score. 
	 * @param la range
	 * @return la liste des musiques et de leur score dans la collection STATS par range
	 */
	static List<IdMusicScore> getListIdMusicScoreMostPopularByRange(String range){
		System.out.println("Début getListIdMUsicScoreMostPopularByRange : "+range);
		MongoCollection<Document> collection = ms.getCollection(MongoCollectionsAndKeys.STATS);
		GregorianCalendar gc = new GregorianCalendar(Locale.US);
		String week=(gc.get(Calendar.WEEK_OF_YEAR)+"-"+gc.get(Calendar.YEAR));
		System.out.println(week);
		Document doc = new Document(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS,new Document("$eq",week));
		MongoCursor<Document> cursor = ms.findBy(collection, doc);
		List<IdMusicScore> list_music_score = new ArrayList<IdMusicScore>();
		if(cursor.hasNext()){
			Document doc_weeks = cursor.next();
			System.out.println("doc_weeks : "+doc_weeks);
			List<Document> list_age_range = (List<Document>) doc_weeks.get(MongoCollectionsAndKeys.AGERANGE_STATS);
			for(Document doc_range : list_age_range){
				if(doc_range.getString(MongoCollectionsAndKeys.AGE_STATS).equals(range)){
					List<Document> doc_musics = (List<Document>) doc_range.get(MongoCollectionsAndKeys.MUSICS_STATS);
					for(Document d : doc_musics){
						IdMusicScore mscore= new IdMusicScore(d.getString(MongoCollectionsAndKeys.IDMUSIC_STATS)
								,d.getInteger(MongoCollectionsAndKeys.SCOREMUSIC_STATS));
						System.out.println("mscore : "+mscore);
						list_music_score.add(mscore);
					}
				}
			}
		}
		Collections.sort(list_music_score);
		System.out.println(list_music_score);
		System.out.println("Fin getListIdMUsicScoreMostPopularByRange : "+range);
		return list_music_score;
	}

	/**
	 * Méthode permettant de récupérer la list d'IdMusicScore pour toutes les ranges. Ceci afin de pouvoir les
	 * stocker dans notre collection Stats_Cache. L'idée est de mettre en relation les différents id des musiques
	 * présentes dans toutes les ranges existe pour notre semaine en particulier.
	 * Ainsi, si une musique est présente dans plusieurs range différentes, il faudra additioner les différents scores.
	 * Ceci afin d'obtenir un score général entre toutes les ranges.
	 * @return la liste des musiques et de leur score toutes ranges confondu (correspond à general dans la collection STATS_CACHE)
	 */
	static List<IdMusicScore> concatListIdMusicScore(List<IdMusicScore> list1, List<IdMusicScore> list2 ){
		int cpt = 0;
		List<IdMusicScore> list_tmp = new ArrayList<IdMusicScore>();
		for(IdMusicScore id_score_tmp : list2){
			cpt = 0;
			for(IdMusicScore id_score : list1){
				if(id_score_tmp.getIdMusic().equals(id_score.getIdMusic())){
					id_score.setScore(id_score_tmp.getScore()+id_score.getScore());
				}
				else
					cpt++;
			}
			if(cpt==list1.size())
				list_tmp.add(id_score_tmp);
		}
		list1.addAll(list_tmp);
		return list1;
	}

	static List<IdMusicScore> getListIdMusicScoreMostPopularAllRange(){
		List<IdMusicScore> list_music_score = getListIdMusicScoreMostPopularByRange(MongoCollectionsAndKeys.MINUSEIGHTEEN_STATS);
		List<IdMusicScore> list_music_score_min_twenty_five = getListIdMusicScoreMostPopularByRange(MongoCollectionsAndKeys.MINUSTWENTYFIVE_STATS);
		List<IdMusicScore> list_music_score_min_fifty = getListIdMusicScoreMostPopularByRange(MongoCollectionsAndKeys.MINUSFIFTY_STATS);
		List<IdMusicScore> list_music_score_plus_fifty = getListIdMusicScoreMostPopularByRange(MongoCollectionsAndKeys.PLUSFIFTY_STATS);

		list_music_score = concatListIdMusicScore(list_music_score, list_music_score_min_twenty_five);
		list_music_score = concatListIdMusicScore(list_music_score, list_music_score_min_fifty);
		list_music_score = concatListIdMusicScore(list_music_score, list_music_score_plus_fifty);
		
		Collections.sort(list_music_score);
		return list_music_score;
	}
	
	/**
	 * récupère la liste d'id de musique pour toutes les ranges confondus dans l'ordre décroissant suivant le score 
	 * @param range
	 * @return la liste des id des musiques
	 */
	static List<String> getListIdMostPopularAllRangeInStats(String range){
		List<IdMusicScore> list_music_score = getListIdMusicScoreMostPopularAllRange();
		if(list_music_score.size()> MuzikFinderPreferences.LIMITACCEPTABLE_NB_MUSICS)
			list_music_score.subList(0, MuzikFinderPreferences.LIMITACCEPTABLE_NB_MUSICS);
		List<String> list_id = new ArrayList<String>();
		for(IdMusicScore mscore : list_music_score){
			list_id.add(mscore.getIdMusic());
		}
		System.out.println(list_id);
		return list_id;

	}

	@SuppressWarnings("unchecked")
	/**
	 * Méthode permettant de récupérer la liste d'Id dans la collection Stats_Cache à partir d'un range particulier 
	 * @param range
	 * @return
	 */
	static List<String> getListIdStringByRangeInStats_Cache(String range){
		if(range==null || range.isEmpty()) return null;

		List<String> list_id = new ArrayList<String>();
		MongoCollection<Document> collection_stats_cache = ms.getCollection(MongoCollectionsAndKeys.STATS_CACHE);
		if(collection_stats_cache==null) return null;

		GregorianCalendar gc = new GregorianCalendar(Locale.US);
		String week=(gc.get(Calendar.WEEK_OF_YEAR)+"-"+gc.get(Calendar.YEAR));
		Document doc = new Document(MongoCollectionsAndKeys.DATEWEEKSYEARS_STATS_CACHE,new Document("$eq",week));
		MongoCursor<Document> cursor = ms.findBy(collection_stats_cache, doc);
		if(cursor.hasNext()){
			Document doc_weeks = cursor.next();
			List<Document> list_age_range = (List<Document>) doc_weeks.get(MongoCollectionsAndKeys.AGERANGE_STATS_CACHE);
			for(Document doc_range : list_age_range){
				if(doc_range.getString(MongoCollectionsAndKeys.AGE_STATS_CACHE).equals(range)){
					List<Document> doc_musics = (List<Document>) doc_range.get(MongoCollectionsAndKeys.MUSICS_STATS_CACHE);
					for(Document d : doc_musics){
						if(list_id.size()==MuzikFinderPreferences.NB_MAX_TOPS_TO_DISPLAY) break;
						list_id.add(d.getString(MongoCollectionsAndKeys.MUSIC_ID_STATS_CACHE));
					}
				}
			}
		}
		return list_id;
	}

	//////////////************************ PARTIE DEAMON AJOUT DANS STATS_CACHE **************///////////////////

	/**
	 * Méthode appelée par le deamon afin de remplir la collection STATS_CACHE toutes les heures
	 */
	public static void addListIdMusicMostPopularAllRanges(){
		System.out.println("Début appel addList");
		addListIdMusicMostPopularByRange(MongoCollectionsAndKeys.MINUSEIGHTEEN_STATS);
		addListIdMusicMostPopularByRange(MongoCollectionsAndKeys.MINUSTWENTYFIVE_STATS);
		addListIdMusicMostPopularByRange(MongoCollectionsAndKeys.MINUSFIFTY_STATS);
		addListIdMusicMostPopularByRange(MongoCollectionsAndKeys.PLUSFIFTY_STATS);
		addListIdMusicMostPopularByRange(MongoCollectionsAndKeys.GENERAL_STATS_CACHE);
		System.out.println("Fin appel addList");
	}

	//////////////////******************* PARTIE FONCTION RECUPERATION DANS STATS_CACHE POUR PAGE WEB *******///////

	/**
	 * Méthode permettant de récupérer la liste d'id de musique dans la collection STATS_CACHE
	 * et de les convertir en MFMusic afin de les afficher sur la page web.
	 * @param range
	 * @return la liste des musiques (MFMusic)
	 */

	public static List<MFMusic> getListMFMusicMostPopularByRange(String range){
		MongoCollection<Document> collection_musics = ms.getCollection(MongoCollectionsAndKeys.MUSICS);

		List<String> list_id = getListIdStringByRangeInStats_Cache(range); // on récupère la list d'id (voir méthode correspondante)
		List<MFMusic> list_music_dto = new ArrayList<MFMusic>();
		Document findQuery;
		Document music_doc;
		MongoCursor<Document> cursor_music;
		for(String id : list_id){
			findQuery = new Document(MongoCollectionsAndKeys.IDMUSIC_MUSICS, new Document("$eq",id));
			cursor_music = ms.findBy(collection_musics, findQuery);	
			if(cursor_music.hasNext()){
				music_doc = cursor_music.next();
				list_music_dto.add(MongoUtils.transformDocumentIntoMFMusic(music_doc));
			}
		}
		return list_music_dto;
	}	

	//////////////////**************** PARTIE DAEMON *******//////////////////////
	public static void deleteCacheUserExceed(long time){
		Document doc;
		MongoCollection<Document> collection = ms.getCollection(MongoCollectionsAndKeys.SEARCH_CACHE);
		doc = new Document("time", new Document("$lt",time));
		ms.deleteMany(collection, doc);
	}

	public static void main(String[] args){
		addListIdMusicMostPopularAllRanges();
	}
} 