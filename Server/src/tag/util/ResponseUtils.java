package tag.util;

import com.smartfoxserver.v2.entities.data.SFSObject;

public class ResponseUtils {

	public static class ResponseException extends Exception {
		
		private static final long serialVersionUID = -954467656569106170L;
		
		public String command;
		public SFSObject message = new SFSObject();
		
		public ResponseException(String command, String message) {
			this.command = command;
			this.message.putUtfString("message", message);
		}
		
	}
	
}
