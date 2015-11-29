package cse561;

public class AsymmetricEncryption extends GenericCryptoEngine {
 
	public static final double MilliWattsPerByte = 0.2;
	
	public AsymmetricEncryption(){
		super("Asymm Encryption", CryptoEngineType.ASYMMETRIC, MilliWattsPerByte);
	}

	public AsymmetricEncryption(String name){
		super(name, CryptoEngineType.ASYMMETRIC, MilliWattsPerByte);
	}
	
	public AsymmetricEncryption(String name, double mwPerByte){
		super(name, CryptoEngineType.ASYMMETRIC, mwPerByte);
	}
	
	public void initialize()
	{
		super.initialize();
	}
} 