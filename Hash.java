package cse561;

public class Hash extends GenericCryptoEngine {
 
	public static final double MilliJoulesPerByte = 0.1;
	
	public Hash(){
		super("Hash", CryptoEngineType.HASH, MilliJoulesPerByte);
	}

	public Hash(String name){
		super(name, CryptoEngineType.HASH, MilliJoulesPerByte);
	}
	
	public Hash(String name, double mjPerByte){
		super(name, CryptoEngineType.HASH, mjPerByte);
	}
	
	public void initialize()
	{
		super.initialize();
	}
} 