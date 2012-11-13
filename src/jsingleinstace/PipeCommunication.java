package jsingleinstace;


public class PipeCommunication extends Communication {
	
	static {
		System.loadLibrary("pipe");
	}
	
	private String uniqueIdentifier;
	private boolean isAlreadyRunning;
	
	public PipeCommunication(String uniqueIdentifier) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if(tmpDir == null)
			tmpDir = "";
		this.uniqueIdentifier = tmpDir + uniqueIdentifier + ".jsingle";
	}

	private native int nativeInit(String identifier);
	
	@Override
	void init() {
		int res = nativeInit(uniqueIdentifier);
		
		if(res == 0) {
			isAlreadyRunning = false;
			return;
		}
		
		// error
	}

	@Override
	boolean isAlreadyRunning() {
		return isAlreadyRunning;
	}

	@Override
	native boolean sendCommand(String cmd);

	@Override
	native String waitForCommand();

	@Override
	native void shutdown();

}
