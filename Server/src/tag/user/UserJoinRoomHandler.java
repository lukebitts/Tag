package tag.user;

import java.util.Arrays;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import tag.MainRoomExtension;
import tag.game.Game;

public class UserJoinRoomHandler extends BaseServerEventHandler {

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		User user = (User)event.getParameter(SFSEventParam.USER);
		trace(String.format("User '%s' (%s) joined room.",user.getName(), user.getId()));
		
		if(getParentExtension().getParentRoom().getUserList().size() == 1) {
			
			MainRoomExtension ext = (MainRoomExtension)getParentExtension();
			
			SFSArray posData = new SFSArray();
			posData.addFloat(0.f);
			posData.addFloat(50.f);
			posData.addFloat(0.f);
			
			UserVariable pos = new SFSUserVariable("pos", posData);
			SmartFoxServer.getInstance().getAPIManager().getSFSApi().setUserVariables(user, Arrays.asList(pos));
			
			ext.game = new Game(ext.getParentRoom());
			ext.game.start();
		}
	}
	
}
