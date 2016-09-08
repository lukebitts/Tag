using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using Sfs2X;
using Sfs2X.Core;
using Sfs2X.Logging;
using Sfs2X.Entities;
using Sfs2X.Entities.Data;
using Sfs2X.Entities.Variables;
using System;
using UnityEngine.SceneManagement;

public class MultiplayerManager : MonoBehaviour
{
    public GameObject playerPrefab;
    public GameObject networkPlayerPrefab;

    public ChaserCamera chaserCamera;
    public RuntimeAnimatorController animatorController;

    SmartFox sfs;
    Dictionary<int, User> users = new Dictionary<int, User>();

    void Start()
    {
#if DEBUG
        try
        {
            sfs = Connection.Instance().Sfs();
        }
        catch (System.NullReferenceException)
        {
            SceneManager.LoadScene(0);
            return;
        }
#else
        sfs = Connection.Instance().Sfs();
#endif
        sfs.AddEventListener(SFSEvent.USER_VARIABLES_UPDATE, OnUserVariablesUpdate);
        sfs.AddEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);
        sfs.AddEventListener(SFSEvent.USER_ENTER_ROOM, OnUserEnterRoom);
        sfs.AddEventListener(SFSEvent.EXTENSION_RESPONSE, OnExtensionResponse);

        ISFSArray pos = sfs.MySelf.GetVariable("pos").GetSFSArrayValue();
        ISFSArray ori = sfs.MySelf.GetVariable("ori").GetSFSArrayValue();

        GameObject playerGo = Instantiate(playerPrefab, 
            new Vector3(pos.GetFloat(0), pos.GetFloat(1) - 1.0f, pos.GetFloat(2)), 
            new Quaternion(ori.GetFloat(0), ori.GetFloat(2), ori.GetFloat(2), ori.GetFloat(3)))
            as GameObject;
        playerGo.AddComponent<PlayerInput>();
        playerGo.AddComponent<PlayerInterpolation>();

        playerGo.GetComponent<Animator>().runtimeAnimatorController = animatorController;

        sfs.MySelf.Properties.Add("GameObject", playerGo);
        users.Add(sfs.MySelf.Id, sfs.MySelf);

        chaserCamera.target = playerGo.transform;

        foreach(SFSUser u in sfs.LastJoinedRoom.UserList)
        {
            if(!u.IsItMe)
                UserEnterRoom(u);
        }
    }
    void Update()
    {

    }

    private void OnUserEnterRoom(BaseEvent evt)
    {
        SFSUser user = (SFSUser)evt.Params["user"];
        UserEnterRoom(user);
    }

    private void UserEnterRoom(SFSUser user)
    {       
        ISFSArray pos = user.GetVariable("pos").GetSFSArrayValue();
        ISFSArray ori = user.GetVariable("ori").GetSFSArrayValue();

        GameObject playerGo = Instantiate(playerPrefab,
            new Vector3(pos.GetFloat(0), pos.GetFloat(1) - 1.0f, pos.GetFloat(2)),
            new Quaternion(ori.GetFloat(0), ori.GetFloat(2), ori.GetFloat(2), ori.GetFloat(3)))
            as GameObject;
        //playerGo.AddComponent<PlayerInput>();
        playerGo.AddComponent<PlayerInterpolation>();

        playerGo.GetComponent<Animator>().runtimeAnimatorController = animatorController;

        user.Properties.Add("GameObject", playerGo);
        users.Add(user.Id, user);

        //chaserCamera.target = playerGo.transform;
    }

    private void OnUserVariablesUpdate(BaseEvent evt)
    {
        ArrayList changedVars = (ArrayList)evt.Params["changedVars"];

        SFSUser user = (SFSUser)evt.Params["user"];

        if (!users.ContainsKey(user.Id))
        {
            Debug.LogWarning("Got variable update for an user that is not in the users list.");
            return;
        }

        if (changedVars.Contains("pos"))
        {
            GameObject playerGo = user.Properties["GameObject"] as GameObject;

            Vector3 newPosition = new Vector3();
            ISFSArray pos = user.GetVariable("pos").GetSFSArrayValue();
            newPosition.x = pos.GetFloat(0);
            newPosition.y = pos.GetFloat(1) - 1.0f;
            newPosition.z = pos.GetFloat(2);

            var pi = playerGo.GetComponent<PlayerInterpolation>();
            pi.latestPosition = newPosition;
        }

        if (changedVars.Contains("ori"))
        {
            GameObject playerGo = user.Properties["GameObject"] as GameObject;

            Quaternion newOrientation = new Quaternion();
            ISFSArray ori = user.GetVariable("ori").GetSFSArrayValue();
            newOrientation.x = ori.GetFloat(0);
            newOrientation.y = ori.GetFloat(1);
            newOrientation.z = ori.GetFloat(2);
            newOrientation.w = ori.GetFloat(3);

            var pi = playerGo.GetComponent<PlayerInterpolation>();
            pi.latestOrientation = newOrientation;
        }

        if (changedVars.Contains("turning"))
        {
            GameObject playerGo = user.Properties["GameObject"] as GameObject;

            Boolean turning = user.GetVariable("turning").GetBoolValue();

            var pi = playerGo.GetComponent<PlayerInterpolation>();
            pi.turning = turning;
        }
    }

    private void OnExtensionResponse(BaseEvent evt)
    {
        string cmd = evt.Params["cmd"] as string;

        if (cmd == "game_start")
        {
            Debug.LogWarning("GAME STARTED");
        }
        else
        {
            Debug.LogWarning("Unrecognized command received: " + cmd);
        }
    }

    void OnConnectionLost(BaseEvent evt)
    {
        sfs.RemoveEventListener(SFSEvent.USER_VARIABLES_UPDATE, OnUserVariablesUpdate);
        sfs.RemoveEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);

        Destroy(Connection.Instance().gameObject);
        SceneManager.LoadScene(0);
    }

    public void OnDisconnectClick()
    {
        if (sfs.LastJoinedRoom != null)
        {
            sfs.Send(new Sfs2X.Requests.LeaveRoomRequest(sfs.LastJoinedRoom));
        }
        else
            Debug.LogError("No room was found to leave");

        if (sfs.IsConnected)
            sfs.Disconnect();
    }
}
