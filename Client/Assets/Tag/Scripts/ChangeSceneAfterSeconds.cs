using UnityEngine;
using System.Collections;
using UnityEngine.SceneManagement;

public class ChangeSceneAfterSeconds : MonoBehaviour
{
    public float seconds;
    public string sceneName;

    void Start()
    {
        StartCoroutine(Change());
    }

    IEnumerator Change()
    {
        yield return new WaitForSeconds(seconds);

        SceneManager.LoadScene(sceneName);
    }
}