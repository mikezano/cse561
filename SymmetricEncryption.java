package cse561;

public class SymmetricEncryption extends GenericCryptoEngine {
 
	public static final double MilliWattsPerByte = 0.2;
	
	public SymmetricEncryption(){
		super("Symm Encryption", CryptoEngineType.SYMMETRIC, MilliWattsPerByte);
	}

	public SymmetricEncryption(String name){
		super(name, CryptoEngineType.SYMMETRIC, MilliWattsPerByte);
	}
	
	public SymmetricEncryption(String name, double mjPerByte){
		super(name, CryptoEngineType.SYMMETRIC, mjPerByte);
	}
	
	public void initialize()
	{
		super.initialize();
	}
} 