using UnityEngine;
using Sfs2X;
using Sfs2X.Core;
using Sfs2X.Entities.Data;
using System.Collections;

public class QuickPlayButton : MonoBehaviour {

    SmartFox sfs;

    public Popup popup;

    void SetupListeners()
    {
        sfs.AddEventListener(SFSEvent.CONNECTION, OnConnection);
        sfs.AddEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);
        sfs.AddEventListener(SFSEvent.LOGIN, OnLogin);
        sfs.AddEventListener(SFSEvent.LOGIN_ERROR, OnLoginError);

        sfs.AddEventListener(SFSEvent.ROOM_JOIN, OnRoomJoin);
        sfs.AddEventListener(SFSEvent.EXTENSION_RESPONSE, OnExtensionResponse);
    }

    void Reset()
    {
        sfs = Connection.Instance().Reset();
        SetupListeners();
    }

    void Start()
    {
        Reset();
    }

    public void OnClick()
    {
        popup.Show("CONNECTING TO SERVER", "Please wait while we try to find a match for you", true, 0, CancelConnection);

        sfs.Connect("127.0.0.1", 9933);
    }

    void CancelConnection()
    {
        popup.Hide();
        sfs.KillConnection();
    }

    void OnConnection(BaseEvent evt)
    {
        bool connectionSuccess = (bool)evt.Params["success"];

        Debug.Log("OnConn:" + connectionSuccess);

        if (connectionSuccess)
        {
            popup.Show("LOGIN AS GUEST REQUESTED", "Please wait while we try to find a match for you", true, 0, CancelConnection);

            sfs.Send(new Sfs2X.Requests.LoginRequest("", "", "Lobby"));
        }
        else
        {
            popup.Show("ERROR CONNECTING TO SERVER", "Something went wrong while we tried to connect you, please try again.", true, 0, popup.Hide);

            Reset();
        }
    }

    void OnConnectionLost(BaseEvent evt)
    {
        string reason = evt.Params["reason"] as string;
        print("Connection lost: " + reason);

        Reset();
    }

    void OnLogin(BaseEvent evt)
    {
        popup.Show("WAITING IN QUEUE FOR A MATCH", "Please wait while we try to find a match for you", true, 0, CancelConnection);

        var user = (Sfs2X.Entities.User)evt.Params["user"];
        Debug.Log("Login success. " + user.Name);
    }

    void OnLoginError(BaseEvent evt)
    {
        popup.Show("ERROR IN GUEST LOGIN", "Something went wrong while we tried to login you as a guest, please try again.", true, 0, popup.Hide);

        Debug.Log("Login error: " + evt.Params["errorMessage"] + " - Code: " + evt.Params["errorCode"]);
        sfs.Disconnect();
    }

    void OnExtensionResponse(BaseEvent evt)
    {
        string cmd = evt.Params["cmd"] as string;

        if(cmd == "game_found")
        {
            SFSObject message = evt.Params["params"] as SFSObject;
            string roomName = message.GetUtfString("room_name");

            sfs.Send(new Sfs2X.Requests.JoinRoomRequest(roomName));
        }
        else
        {
            Debug.LogWarning("Unrecognized command received: " + cmd);
        }
    }

    void OnRoomJoin(BaseEvent evt)
    {
        popup.Show("MATCH FOUND", "Please wait as your game starts.", false, -1, null);

        sfs.RemoveEventListener(SFSEvent.CONNECTION, OnConnection);
        sfs.RemoveEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);
        sfs.RemoveEventListener(SFSEvent.LOGIN, OnLogin);
        sfs.RemoveEventListener(SFSEvent.LOGIN_ERROR, OnLoginError);

        sfs.RemoveEventListener(SFSEvent.ROOM_JOIN, OnRoomJoin);
        sfs.RemoveEventListener(SFSEvent.EXTENSION_RESPONSE, OnExtensionResponse);

        ChangeSceneAfterSeconds c = gameObject.AddComponent<ChangeSceneAfterSeconds>();
        c.seconds = 0;
        c.sceneName = "Game";
    }
}
