using UnityEngine;
using Sfs2X;
using Sfs2X.Core;
using Sfs2X.Entities.Data;

public class QuickPlayButton : MonoBehaviour {

    SmartFox sfs;

    void SetupListeners()
    {
        sfs.AddEventListener(SFSEvent.CONNECTION, OnConnection);
        sfs.AddEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);
        sfs.AddEventListener(SFSEvent.LOGIN, OnLogin);
        sfs.AddEventListener(SFSEvent.LOGIN_ERROR, OnLoginError);

        sfs.AddEventListener(SFSEvent.ROOM_JOIN, OnRoomJoin);
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
        sfs.Connect("127.0.0.1", 9933);
    }

    void OnConnection(BaseEvent evt)
    {
        bool connectionSuccess = (bool)evt.Params["success"];

        Debug.Log("OnConn:" + connectionSuccess);

        if (connectionSuccess)
        {
            sfs.Send(new Sfs2X.Requests.LoginRequest("", "", "Lobby"));
        }
        else
        {
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
        var user = (Sfs2X.Entities.User)evt.Params["user"];
        Debug.Log("Login success. " + user.Name);
    }

    void OnLoginError(BaseEvent evt)
    {
        Debug.Log("Login error: " + evt.Params["errorMessage"] + " - Code: " + evt.Params["errorCode"]);
        sfs.Disconnect();
    }

    void OnRoomJoin(BaseEvent evt)
    {
        Debug.Log("Joined room!");
    }
}
