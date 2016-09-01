package tag.user;

import java.util.Collection;

import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;

import tag.game.Player;
import tag.util.ResponseUtils;

@MultiHandler
public class InputRequestHandler extends BaseClientRequestHandler {

	public static class INPUT_TYPE {
		public static int FORWARD = 0;
		public static int BACKWARD = 1;
		public static int LEFT = 2;
		public static int RIGHT = 3;
		public static int JUMP = 4;
	}
	
	//Requests
	private static final String SET_INPUT = "setInput";
	
	//Responses
	//private static final String SET_INPUT_SUCCESS = "input.setSuccess";
	private static final String SET_INPUT_ERROR = "input.setError";
	
	@Override
	public void handleClientRequest(User sender, ISFSObject params) {
		 String command = params.getUtfString(SFSExtension.MULTIHANDLER_REQUEST_ID);
         
		 try {
			if (command.equals(SET_INPUT))
				setInput(sender, params);
		 } catch(ResponseUtils.ResponseException ex) {
			 send(ex.command, ex.message, sender);
		 }
	}

	private void setInput(User sender, ISFSObject params) throws ResponseUtils.ResponseException {
		try {
			if(!sender.containsProperty("player")) {
				throw new ResponseUtils.ResponseException(SET_INPUT_ERROR, "Player is not initialized yet.");
			}
			
			Player player = (Player)sender.getProperty("player");
			Collection<Boolean> inputData = params.getBoolArray("inputData");
			
			//assert(inputData.size() == 5);
			
			player.setInput(inputData.toArray(new Boolean[5]));
		
		}
		catch(ResponseUtils.ResponseException ex) {
			throw ex;
		}
		catch(Throwable ex) {
			throw new ResponseUtils.ResponseException(SET_INPUT_ERROR, "Throwable Caught: "+ex.getMessage());
		}
	}

}
