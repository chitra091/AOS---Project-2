import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Node implements Runnable {
	int totalNodes;
	int minperactive;
	int maxperactive;
	int minsenddelay;
	List<Integer> neighbours;
	int portNumbers[];
	String hostName[];
	int snapshotdelay;
	int nodeId;
	static ConcurrentLinkedQueue<String> queue = null;
	String filename=null; 

	public Node(int totalNodes,int minperactive,int maxperactive,int minsenddelay,int snapshotdelay,List<Integer> neighbours,int portNo[],String hostName[],int nodeId)
	{
		this.totalNodes = totalNodes;
		this.minperactive = minperactive;
		this.maxperactive = maxperactive;
		this.neighbours = neighbours;
		this.minsenddelay=minsenddelay;
		this.snapshotdelay = snapshotdelay;
		this.portNumbers=portNo;
		this.hostName=hostName;
		this.nodeId=nodeId;
	}

	public Node()
	{

	}

	public static void main(String[] args) throws FileNotFoundException
	{
		String ip = args[0];
		String[] tokens=null;
		int no_of_nodes,maxNo=0;
		List<String> lines = new ArrayList<String>();
		Variables.timestamp = null;
		try
		{
			File file = new File(args[1]);
			String gfile = file.getName();
			String[] config1 = gfile.split("\\.(?=[^\\.]+$)");
			String config = config1[0];
			FileReader fileReader = new FileReader(file);
			String filename1 = config+"-"+Integer.parseInt(ip)+".out";
			Variables.file = new File(filename1);
			Variables.file.createNewFile();
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line=null;
			while ((line = bufferedReader.readLine()) != null) {
				if(line.startsWith("#")||line.length()==0)
				{
					continue;
				}
				else
				{
					line = line.trim();
					lines.add(line);
				}
			}
			fileReader.close();
			String str = lines.get(0);
			String[] first_line = str.split("\\s+");
			no_of_nodes = Integer.parseInt(first_line[0]);
			Variables.min_per_active = Integer.parseInt(first_line[1]);
			Variables.max_per_active = Integer.parseInt(first_line[2]);
			Variables.min_send_delay = Integer.parseInt(first_line[3]);
			Variables.snapshot_delay = Integer.parseInt(first_line[4]);
			maxNo = Integer.parseInt(first_line[5]);
			Variables.portNo = new int[no_of_nodes];
			Variables.hostName = new String[no_of_nodes];
			Variables.timestamp= new ArrayList<Integer>(no_of_nodes);
			Variables.maxnumber = maxNo;
			Variables.alive = true;
			Variables.matrix = new int[no_of_nodes][no_of_nodes];
			Variables.visited = new ArrayList<Integer>();
			Variables.parent = new ArrayList<ArrayList<Integer>>(no_of_nodes);
			Variables.children = new ArrayList<ArrayList<Integer>>(no_of_nodes);
			Variables.globalstate = new String[no_of_nodes];
			for(int i=0;i<no_of_nodes;i++)
			{
				Variables.timestamp.add(0);
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(0);
				Variables.parent.add(i, temp);
			}
			for(int i=1;i<=no_of_nodes;i++)
			{
				tokens = lines.get(i).split("\\s+");
				Variables.portNo[i-1]=Integer.parseInt(tokens[2]);
				Variables.hostName[i-1]=tokens[1];
				Variables.visited.add(i-1);
			}

			int counter=0;

			for(int i=no_of_nodes+1;i<lines.size();i++)
			{
				String[] str1 = lines.get(i).split("#");
				String str2 = str1[0].trim();
				String[] val = str2.split("\\s+");
				for(int k=0;k<val.length;k++)
				{
					Variables.matrix[counter][Integer.parseInt(val[k])]=Variables.matrix[Integer.parseInt(val[k])][counter]=1;
				}
				counter++;
			}
			
			//get neighbors
			for(int i=0;i<Variables.matrix.length;i++)
			{
				if(i==Integer.parseInt(ip))
				{
					for(int j=0;j<Variables.matrix.length;j++)
					{
						if(Variables.matrix[i][j]==1)
						{
							Variables.neighbours.add(j);
						}
					}
				}
			}
			
			Variables.transitcount = new int[no_of_nodes];
			Variables.channelarray = new boolean[no_of_nodes];
			for(int i=0;i<Variables.globalstate.length;i++){
				Variables.globalstate[i]="PASSIVE";
			}

			if(Integer.parseInt(ip)==0){
		//		Variables.active=true;
		//		Variables.passive=false;
				Variables.nodeState="ACTIVE";
				Variables.MAPNodeState="ACTIVE";
			}
			else{
				Random random = new Random();
				boolean n = random.nextBoolean();
				if(n==true)
				{
					Variables.nodeState="ACTIVE";
					Variables.MAPNodeState="ACTIVE";
				}
			}
			//get children -- spanning tree
			boolean visited=false;
			Variables.visited.remove(0);
			for(int i=0;i<Variables.matrix.length;i++){
				ArrayList<Integer> temp = new ArrayList<Integer>();
				for(int j=0; j<Variables.matrix.length;j++){
					if(!Variables.visited.isEmpty()){
						if(Variables.matrix[i][j]==1){
							for(int k=0; k<Variables.visited.size();k++){
								if(j==Variables.visited.get(k)){
									visited=true;
									Variables.visited.remove(k);
									break;
								}
							}
							if(visited==true){
								temp.add(j);
								visited=false;
							}
						}
					}
					else{
						break;
					}
				}
				Variables.children.add(i, temp);
				
			}
			
			//get parent
			for(int i=0; i<Variables.children.size();i++){
				if(!Variables.children.get(i).isEmpty()){
					for(int j=0; j<Variables.children.get(i).size();j++){
						if(Variables.children.get(i).get(j) > 0){
							ArrayList<Integer> temp = new ArrayList<Integer>();
							temp.add(i);
							Variables.parent.set(Variables.children.get(i).get(j), temp);
						}
					}
				}
			}

			//create Node
			Node node = new Node(no_of_nodes, Variables.min_per_active, Variables.max_per_active, Variables.min_send_delay, Variables.snapshot_delay, Variables.neighbours,Variables.portNo,Variables.hostName,Integer.parseInt(ip));

			//run server
			Thread serverThread = new Thread(node);
			serverThread.start();
			Thread.sleep(5000);
			if(Integer.parseInt(ip)!=0){
				Thread.sleep(1000);
			}
			//start client 
			System.out.println("Starting client for " + Integer.parseInt(ip));
			Thread clientThread = new Thread(new Client(Variables.hostName, Variables.portNo,Variables.neighbours,ip,Variables.min_per_active,Variables.max_per_active));
			clientThread.start();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void run() {
		//create a server socket
		try {
			ServerSocket serverSocket = new ServerSocket(portNumbers[nodeId]);
			while(Variables.server)
			{
				try
				{
					Socket sock = serverSocket.accept();
					Thread serviceThread = new Thread(new WorkerThread(sock,nodeId));
					serviceThread.start();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			System.out.println("Closing server ");
		//	serverSocket.close();

		} catch (IOException e) 
		{
			e.printStackTrace();
			e.printStackTrace();
		}
	}
}

