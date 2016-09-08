using UnityEngine;

public class ChaserCamera : MonoBehaviour
{
    public Transform target;
    public float lerpSpeed = 10;

    private bool lastLeft = false;

    //The relative position the camera should aim to be at during different player actions
    private static readonly Vector3 CAMERA_POSITION_NORMAL        = new Vector3( 0, 4.5f, -3);
    private static readonly Vector3 CAMERA_POSITION_TURNING_LEFT  = new Vector3( 4, 2.5f, -3);
    private static readonly Vector3 CAMERA_POSITION_TURNING_RIGHT = new Vector3(-4, 2.5f, -3);
    //The relative direction the camera should aim to look at during the game
    private static readonly Vector3 CAMERA_LOOK_AT = new Vector3(0, 1.5f, 2);

    void Start()
    {
        
    }

    void Update()
    {
        if(Input.GetKey(KeyCode.A)) lastLeft = true;
        if(Input.GetKey(KeyCode.D)) lastLeft = false;

#if DEBUG
        if (target == null) return;
#endif

        Vector3 positionToMove;
        if (!target.GetComponent<PlayerInterpolation>().turning)
            positionToMove = (target.position + target.localRotation * CAMERA_POSITION_NORMAL);
        else
            positionToMove = (target.position + target.localRotation * (lastLeft ? CAMERA_POSITION_TURNING_LEFT : CAMERA_POSITION_TURNING_RIGHT));

        Quaternion neededRotation = Quaternion.LookRotation(target.position - transform.position + target.localRotation * CAMERA_LOOK_AT);
        
        transform.position = Vector3.Slerp(transform.position, positionToMove, Time.smoothDeltaTime * lerpSpeed);
        transform.localRotation = Quaternion.Slerp(transform.localRotation, neededRotation, Time.smoothDeltaTime * lerpSpeed);
    }
}

