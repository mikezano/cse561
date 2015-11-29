package cse561;

public class Hash extends GenericCryptoEngine {
 
	public static final double MilliWattsPerByte = 0.1;
	
	public Hash(){
		super("Hash", CryptoEngineType.HASH, MilliWattsPerByte);
	}

	public Hash(String name){
		super(name, CryptoEngineType.HASH, MilliWattsPerByte);
	}
	
	public Hash(String name, double mwPerByte){
		super(name, CryptoEngineType.HASH, mwPerByte);
	}
	
	public void initialize()
	{
		super.initialize();
	}
} 