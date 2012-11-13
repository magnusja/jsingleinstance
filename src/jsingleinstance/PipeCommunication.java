package jsingleinstance;

import java.io.File;


public class PipeCommunication extends Communication {
		
	private String uniqueIdentifier;
	private boolean isAlreadyRunning;
	
	public PipeCommunication(String uniqueIdentifier) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if(tmpDir == null)
			tmpDir = "";
		this.uniqueIdentifier = tmpDir + File.separator + uniqueIdentifier + ".jsingle";
	}

	private native int nativeInit(String identifier);
	
	private void loadLibrary() {
		String os = System.getProperty("os.name").toLowerCase();
		
		String libraryName = os.contains("win") ? "pipe.dll" : "libpipe.so";
		
		File f;
		if((f = new File(libraryName)).exists()) {
			System.load(f.getAbsolutePath());
		}
	}
	
	@Override
	void init() {
		loadLibrary();
		
		int res = nativeInit(uniqueIdentifier);
		
		if(res == 0) {
			isAlreadyRunning = false;
			return;
		} else if(res == 1) {
			isAlreadyRunning = true;
			return;
		}
		
		// error
		System.out.println("eerrror");
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
