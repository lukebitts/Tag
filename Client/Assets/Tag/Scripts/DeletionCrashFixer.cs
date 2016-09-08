using System.Collections;
using UnityEngine;
using Sfs2X;

class DeletionCrashFixer : MonoBehaviour
{
    SmartFox sfs;
    static DeletionCrashFixer instance = null;

    void Start()
    {
        if(instance != null)
        {
            GameObject.DestroyImmediate(gameObject);
        }
        else
        {
            GameObject.DontDestroyOnLoad(gameObject);
            instance = this;
        }
    }
    
    public void OnApplicationQuit()
    {
        try
        {
            sfs = Connection.Instance().Sfs();
            if (sfs != null && sfs.IsConnected)
            {
                Application.CancelQuit();
                StartCoroutine(coroutineDisconect());
            }
        }
        catch(System.Exception)
        {

        }
    }

    IEnumerator coroutineDisconect()
    {
        sfs.Disconnect();
        while (sfs.IsConnected)
        {
            yield return new WaitForEndOfFrame();
        }
        Application.Quit();
    }
}
