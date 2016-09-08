package tag;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

import tag.game.Game;
import tag.user.InputRequestHandler;
import tag.user.UserJoinRoomHandler;
import tag.user.UserLeaveRoomHandler;

public class MainRoomExtension extends SFSExtension {

	public Game game = null;
	
	@Override
	public void init() {
		trace("MainRoomExtension: initializing...");
		
		addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomHandler.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveRoomHandler.class);
		
		addRequestHandler("input", InputRequestHandler.class);
		
		trace("MainRoomExtension: initialization completed.");
	}
	
	@Override
	public void destroy() {
		trace("MainRoomExtension: destroying...");
		super.destroy();
		
		if(game != null) {
			game.interrupt();
			try {
				game.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		trace("MainRoomExtension: destruction completed.");
	}

}
