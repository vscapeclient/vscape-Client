package vscape;
import java.math.BigInteger;


public class ClientSettings {
	public final static String CLIENT_VERSION = "7.3.3"; //Semantic versioning
	public final static int REVISION_ID = 349;
	
	public final static String SERVER_IP = "vidyascape.org";
	public final static int SERVER_PORT = 43594;
	
	public final static boolean DevMode = true; // * dev only
	public final static boolean CACHE_DEV_BRANCH = false; // * dev only
	
	public static final BigInteger RSA_MODULUS = new BigInteger("97393834724571344361186284399555017463580643733241989343933569906289479857232734673227031705609928525310184026797877413554557219989611148829616886456704465162251001906477482898484065493478668868970961148259263092398658927214055858754052796796469311460233657015626986772706121763446786121463117547757663156849");
	public static final BigInteger RSA_EXPONENT = new BigInteger("65537");
}
