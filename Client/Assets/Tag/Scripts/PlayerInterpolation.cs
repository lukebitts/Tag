using UnityEngine;

public class PlayerInterpolation : MonoBehaviour
{
    public Vector3 latestPosition;
    public Quaternion latestOrientation;
    public bool turning;

    public float interpolationSpeed = 12;

    void Start()
    {
        
    }

    void Update()
    {
        transform.position = Vector3.Lerp(transform.position, latestPosition, Time.smoothDeltaTime * interpolationSpeed);
        if (!turning)
        {
            transform.localRotation = latestOrientation;
        }
        if (Vector3.Distance(transform.position, latestPosition) > 1.0f)
        {
            transform.position = latestPosition;
        }

        GetComponent<Animator>().SetBool("turning", turning);

        Vector3 comparePosition1 = transform.position;
        Vector3 comparePosition2 = latestPosition;
        comparePosition1.y = comparePosition2.y = 0;

        if (Vector3.Distance(comparePosition1, comparePosition2) > 0.1f)
        {
            GetComponent<Animator>().SetBool("moving", true);
        }
        else
        {
            GetComponent<Animator>().SetBool("moving", false);
        }
    }
}