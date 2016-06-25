import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Variables {
	public static List<Integer> timestamp=null;
	public static boolean active=false, passive=true, alive, cansend=true,snapshotActive=true,snapshotPassive=false,server=true;
	public static int maxnumber;
	public static int sentmsgs=0,receivedmsgs=0,markercount=0,totalsent=0,totalreceived=0;
	public static int portNo[];
	public static String hostName[];
	public static List<Integer> neighbours = new ArrayList<Integer>();
	public static int min_per_active, max_per_active, min_send_delay,snapshot_delay;
	public static int[][] matrix;
	public static ArrayList<ArrayList<Integer>> parent;
	public static ArrayList<ArrayList<Integer>> children;
	public static List<Integer> visited;
	public static String nodeState="PASSIVE", MAPNodeState="PASSIVE"; 
	public static String[] globalstate;
	public static boolean[] channelarray;
	public static int nodeCounter=0, childackcounter=0;
	public static File file; 
	public static int[] transitcount;
}
