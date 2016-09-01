using UnityEngine;
using System.Collections;

public class Popup : MonoBehaviour
{
    public UnityEngine.UI.Text title;
    public UnityEngine.UI.Text content;
    public UnityEngine.UI.Button cancel;

    System.Action onCancel;

    void Start()
    {
        Hide();
    }

    public void Show(string title, string content, bool cancelable, float fadeTime, System.Action onCancel)
    {
        this.title.text = title;
        this.content.text = content;
        this.onCancel = onCancel;

        cancel.gameObject.SetActive(true);

        if (!cancelable)
        {
            cancel.gameObject.SetActive(false);

            if (fadeTime > 0)
            {
                StartCoroutine(FadeAfter(fadeTime));
            }
        }

        gameObject.SetActive(true);
    }

    public void Hide()
    {
        gameObject.SetActive(false);
    }

    IEnumerator FadeAfter(float seconds)
    {
        yield return new WaitForSeconds(seconds);

        Hide();
    }

    public void OnCancelClick()
    {
        if(onCancel != null)
        {
            onCancel();
        }
    }
}