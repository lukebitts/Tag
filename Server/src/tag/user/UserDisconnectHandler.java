package tag.user;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import tag.MainZoneExtension;

public class UserDisconnectHandler extends BaseServerEventHandler {

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		MainZoneExtension ext = (MainZoneExtension)getParentExtension();
		User user = (User)event.getParameter(SFSEventParam.USER);
		
		trace(String.format("User '%s' (%s) disconnected.",user.getName(), user.getId()));
	
		ext.userQueue.remove((Integer)user.getId());
	}

}
