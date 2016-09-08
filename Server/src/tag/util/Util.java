package tag.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class Util {

	public static final boolean DEBUG = true;

	public static boolean CheckParameters(ISFSObject data, String[] parameterNames, Class<?>[] expectedTypes) {
		assert(parameterNames.length == expectedTypes.length);

		for (int i = 0; i < parameterNames.length; ++i) {
			String parameterName = parameterNames[i];
			if (!data.containsKey(parameterName))
				return false;
			try {
				expectedTypes[i].cast(data.get(parameterName).getObject());
			} catch (ClassCastException ex) {
				return false;
			}
		}
		return true;
	}

	public static class Lock {

		boolean isLocked = false;
		Thread lockedBy = null;
		int lockedCount = 0;

		public synchronized void lock() throws InterruptedException {
			Thread callingThread = Thread.currentThread();
			while (isLocked && lockedBy != callingThread) {
				wait();
			}
			isLocked = true;
			lockedCount++;
			lockedBy = callingThread;
		}

		public synchronized void unlock() {
			if (Thread.currentThread() == this.lockedBy) {
				lockedCount--;

				if (lockedCount == 0) {
					isLocked = false;
					notify();
				}
			}
		}
	}
	
	public interface ICompare<T> {
		public boolean call(T a);
	}
	
	public static <T> T filter(Collection<T> collection, ICompare<T> predicate) {
		T ret = null;
		
		for(T t : collection) {
			if(predicate.call(t)) {
				ret = t;
				break;
			}
		}
		
		return ret;
	}
	
	public static void mulVec(Vector3f vec, float v) {
		vec.x *= v;
		vec.y *= v;
		vec.z *= v;
	}
	
	public static Vector3f mulQuatVec(Quat4f q, Vector3f rhs) {
		
		Vector3f quatVector = new Vector3f(q.x, q.y, q.z);
		
		Vector3f uv = new Vector3f();
		uv.cross(quatVector, rhs);
		
		Vector3f uuv = new Vector3f();
		uuv.cross(quatVector, uv);
		
		mulVec(uv, q.w);
		uv.add(uuv);
		mulVec(uv, 2.f);
		rhs.add(uv);
		
		return rhs;
	}
	
	public static int[] convertIntegers(List<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    Iterator<Integer> iterator = integers.iterator();
	    for (int i = 0; i < ret.length; i++)
	    {
	        ret[i] = iterator.next().intValue();
	    }
	    return ret;
	}
	
	public static float[] convertFloats(List<Float> floats)
	{
	    float[] ret = new float[floats.size()];
	    Iterator<Float> iterator = floats.iterator();
	    for (int i = 0; i < ret.length; i++)
	    {
	        ret[i] = iterator.next().floatValue();
	    }
	    return ret;
	}

}
