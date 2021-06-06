package at.htlAnich.stockUpdater;

import java.io.*;
import java.util.Locale;

import static at.htlAnich.tools.BaumbartLogger.errf;
import static at.htlAnich.tools.BaumbartLogger.errlnf;

public class CredentialLoader {
	private static BufferedReader reader = null;
	public static final String csvSeparator = ",";
	public static final String lineSeparator = System.lineSeparator();
	protected CredentialLoader(){System.err.println("How did you call this? Go away!!"); System.exit(-1);}

	public static class DatabaseCredentials{
		private String mUser;
		private String mPassword;
		private String mDatabase;
		private String mHost;
		public DatabaseCredentials(String host, String user, String pass, String database){
			mUser = user;
			mPassword = pass;
			mDatabase = database;
			mHost = host;
		}
		public String user(){return mUser;}
		public String password(){return mPassword;}
		public String database(){return mDatabase;}
		public String host(){return mHost;}
	}

	public static class ApiCredentials{
		private String mKey;
		public ApiCredentials(String apiKey){
			mKey = apiKey;
		}
		public String apiKey(){return mKey;}
	}

	private static BufferedReader initReader(String file){
		if(reader != null)
			return reader;
		BufferedReader out = null;
		try {
			out = new BufferedReader(new FileReader(file));
		}catch(FileNotFoundException e){
			errlnf("File \"%s\" not found", new File(file).getAbsolutePath());
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		return out;
	}

	public static DatabaseCredentials loadDatabase(boolean inProduction){
		var file = inProduction ? "" : "database.csv";
		return loadDatabase(file);
	}
	public static ApiCredentials loadApi(boolean inProduction){
		var file = inProduction ? "" : "api.csv";
		return loadApi(file);
	}

	public static DatabaseCredentials loadDatabase(String file){
		var user = "";
		var pass = "";
		var database = "";
		var host = "";

		var out = new DatabaseCredentials("","","","");

		try{
			reader = initReader(file);
			if(!reader.readLine().contains("hostname,user,password,database")){
				throw new IOException("Corrupted database file!");
			}
			var splittedLine = reader.readLine().trim().split(csvSeparator);
			if(splittedLine.length != 4){
				throw new IOException("Corrupted database file!");
			}
			out = new DatabaseCredentials(splittedLine[0], splittedLine[1], splittedLine[2], splittedLine[3]);
		}catch(FileNotFoundException e){
			errlnf("File \"%s\" not found", new File(file).getAbsolutePath());
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch (NullPointerException e){
			errf("Corrupted database file!");
			e.printStackTrace();
		}
		return out;
	}

	public static ApiCredentials loadApi(String file){
		var apiKey = "";

		var out = new ApiCredentials("");

		try{
			reader = initReader(file);
			if(!reader.readLine().contains("apiKey")){
				throw new IOException("Corrupted api file!");
			}
			var line = reader.readLine().trim();
			out = new ApiCredentials(line);
		}catch(FileNotFoundException e){
			errlnf("File \"%s\" not found", new File(file).getAbsolutePath());
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(NullPointerException e){
			errf("Corrupted api file!");
			e.printStackTrace();
		}
		return out;
	}
}
