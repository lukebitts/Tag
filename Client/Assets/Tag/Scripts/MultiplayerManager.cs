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

        ISFSArray pos = sfs.MySelf.GetVariable("pos").GetSFSArrayValue();

        GameObject playerGo = Instantiate(playerPrefab, new Vector3(pos.GetFloat(0), pos.GetFloat(1), pos.GetFloat(2)), Quaternion.identity) as GameObject;
        sfs.MySelf.Properties.Add("GameObject", playerGo);
        users.Add(sfs.MySelf.Id, sfs.MySelf);
    }

    void Update()
    {

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
            newPosition.y = pos.GetFloat(1);
            newPosition.z = pos.GetFloat(2);

            playerGo.transform.position = newPosition;
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
