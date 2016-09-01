using UnityEngine;
using System.Collections;
using System.Linq;
using Sfs2X;
using Sfs2X.Entities.Data;

public class PlayerInput : MonoBehaviour
{

    public static class INPUT_TYPE
    {
        public static int FORWARD = 0;
        public static int BACKWARD = 1;
        public static int LEFT = 2;
        public static int RIGHT = 3;
        public static int JUMP = 4;
    }

    SmartFox sfs;
    bool[] input = new bool[] { false, false, false, false, false };

    void Start()
    {
        sfs = Connection.Instance().Sfs();
    }

    void Update()
    {

        bool[] oldInput = (bool[])input.Clone();

        input[INPUT_TYPE.FORWARD] = Input.GetKey(KeyCode.W);
        input[INPUT_TYPE.BACKWARD] = Input.GetKey(KeyCode.S);
        input[INPUT_TYPE.LEFT] = Input.GetKey(KeyCode.A);
        input[INPUT_TYPE.RIGHT] = Input.GetKey(KeyCode.D);
        input[INPUT_TYPE.JUMP] = Input.GetKey(KeyCode.Space);

        if (!oldInput.SequenceEqual(input))
        {
            ISFSObject param = new SFSObject();
            param.PutBoolArray("inputData", input);
            sfs.Send(new Sfs2X.Requests.ExtensionRequest("input.setInput", param, sfs.LastJoinedRoom));
        }

        /*Vector3 pos = transform.position;

        int x = input[INPUT_TYPE.LEFT] ? -1 : input[INPUT_TYPE.RIGHT] ? 1 : 0;
        int z = input[INPUT_TYPE.FORWARD] ? 1 : input[INPUT_TYPE.BACKWARD] ? -1 : 0;

        float speed = 8.0f * Time.deltaTime;
        float div = 1.0f;
        if (x != 0 && z != 0)
            div = 1.41f;

        pos.x += x / div * speed;
        pos.z += z / div * speed;

        transform.position = pos;*/

    }
}
