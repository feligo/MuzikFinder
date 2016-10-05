package nosql;

import java.util.List;

import nosql.mongo.MongoService;

/**
 * Appel les fonctions de la BD choisies (MongoDB, DynamoDB, etc ...)
 * @author JulienM
 */
public class NoSQLDB {
	
	private MongoService mongo;

	public NoSQLDB() {
		mongo = new MongoService();
	}
	
	// TODO : to delete
	public void fakeUse(){
		mongo.fakeUse();
	}
	
	public void insertLyricsIfNotExists( List<String> words, String musicId ){
		// mongo....
	}
	
	public boolean insertArtistIfNotExist( String artistName, String artistId ){
		// mongo....
		return true;
	}

}
