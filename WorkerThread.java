import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class WorkerThread implements Runnable{
	Socket myClientSocket;
	int nodeId; 

	public WorkerThread(Socket myClientSocket,int nodeId)
	{
		this.myClientSocket = myClientSocket;
		this.nodeId = nodeId;
	}

	@Override
	public void run() {
		synchronized (this)
		{
			BufferedReader in = null;
			String clientCommand="";
			//if the node has sent more than max number of msgs make the node passive
			if(Variables.sentmsgs >= Variables.maxnumber)
			{
				synchronized(this){
				Variables.nodeState= "PASSIVE";
				Variables.MAPNodeState="PASSIVE";}
			}
			try
			{
				in = new BufferedReader(new InputStreamReader(this.myClientSocket.getInputStream()));
				clientCommand = in.readLine();
				System.out.println("Received :" + clientCommand + " on node " + nodeId);
				String[] val = clientCommand.split("-");
				//if the received message is application message
				if(val[0].startsWith("Application")){
					synchronized(this){
						Variables.receivedmsgs++;
					}
					if(Variables.MAPNodeState.equals("PASSIVE")){
						if(Variables.sentmsgs >= Variables.maxnumber)
						{
							synchronized(this){
							Variables.nodeState= "PASSIVE";
							Variables.MAPNodeState="PASSIVE";}
						}
						else
						{
						//	Variables.active = true;
						//	Variables.passive=false;
							synchronized(this){
							Variables.nodeState="ACTIVE";
							Variables.MAPNodeState="ACTIVE";}
						}
					}
					
					val[1]=val[1].substring(1, val[1].length()-1);
					String[] vector= val[1].trim().split(",");	
					List<Integer> piggyback = new ArrayList<Integer>(vector.length);
					for(int i=0;i<vector.length;i++)
					{
						piggyback.add(0);
					}
					for(int i=0;i<vector.length;i++)
					{
						int value =Integer.parseInt(vector[i].trim());
						piggyback.set(i,value);

					}
					if(Variables.channelarray[Integer.parseInt(val[2])]==true){
						Variables.transitcount[Integer.parseInt(val[2])]++;
					}
					//update clock value on receive
					Variables.timestamp=receive(piggyback, Variables.timestamp, nodeId);

					in.close();
					myClientSocket.close();
				}
				//if the message is received is marker message
				else if(val[0].startsWith("Marker")){
					synchronized(this){
						Variables.markercount++;}
					//any node other than node 0 receives the first marker, take snapshot
					if(Variables.markercount==1 && nodeId!=0){
						if(Variables.file.exists()){
							try {
								BufferedWriter w = new BufferedWriter(new FileWriter(Variables.file,true));
								String temp = Variables.timestamp.toString();
								temp = temp.substring(1, temp.length()-1);
								temp = temp.trim();
								temp = temp.replace(","," ");
								w.write(temp + "\n");
								w.flush();
								w.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else{
							System.out.println("file not found");
						}
						//send marker messages to neighbors on receiving first marker message
						if(Variables.neighbours.size()>0){
							System.out.println("Sending marker messages to neighbor" + nodeId);
							for(int i=0;i<Variables.neighbours.size();i++){
								Socket clientSock;
								try {
									clientSock = new Socket(Variables.hostName[Variables.neighbours.get(i)],Variables.portNo[Variables.neighbours.get(i)]);
									BufferedWriter brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
									String out_messg = "Marker-"+nodeId;
									Variables.transitcount[i]=0;
									if(i!=Integer.parseInt(val[1])){
									Variables.channelarray[i]=true;
									}
									brwriter.write(out_messg);
									brwriter.flush();
									brwriter.close();
									clientSock.close();	
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
						if((Variables.children.get(nodeId).size()==0) && (Variables.childackcounter==0)){
							Socket clientSock = new Socket(Variables.hostName[Variables.parent.get(nodeId).get(0)],Variables.portNo[Variables.parent.get(nodeId).get(0)]);
							BufferedWriter brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
							String out_messg = "ACK-"+nodeId+"-"+Variables.nodeState+"-"+Variables.sentmsgs+"-"+Variables.receivedmsgs;
							brwriter.write(out_messg);
							brwriter.flush();
							brwriter.close();
							clientSock.close();	
						}
					}
					if(Variables.markercount>1){
						Variables.channelarray[Integer.parseInt(val[1])]=false;
					}
					in.close();
					myClientSocket.close();

					//writing acknowledgement
					if(nodeId!=0){
						if(Variables.markercount==Variables.neighbours.size()){
							Variables.markercount=0;
						}
					}
				}
				else if(val[0].startsWith("ACK"))
				{
					if(Variables.children.size()>0){
						Variables.childackcounter++;
						if(Variables.transitcount[Integer.parseInt(val[1])]>0 || val[2]=="ACTIVE"){
							Variables.nodeState = "ACTIVE";
							System.out.println("intransit");
						}
						Variables.globalstate[Integer.parseInt(val[1])]=Variables.nodeState;
						Variables.totalsent += Integer.parseInt(val[3]);
						Variables.totalreceived += Integer.parseInt(val[4]);
					}
					if(nodeId!=0){
						if((Variables.children.get(nodeId).size()>0) && (Variables.childackcounter==Variables.children.get(nodeId).size())){
							Variables.childackcounter=0;
							Variables.totalsent += Variables.sentmsgs;
							Variables.totalreceived += Variables.receivedmsgs;
							Socket clientSock = new Socket(Variables.hostName[Variables.parent.get(nodeId).get(0)],Variables.portNo[Variables.parent.get(nodeId).get(0)]);
							BufferedWriter brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
							String out_messg = "ACK-"+nodeId+"-"+Variables.nodeState+"-"+Variables.totalsent+"-"+Variables.totalreceived;
							System.out.println("Client wrote:"+out_messg);
							brwriter.write(out_messg);
							Variables.totalsent =0;
							Variables.totalreceived =0;
							brwriter.flush();
							brwriter.close();
							clientSock.close();	
						}
					}
					else{
						if((Variables.children.get(nodeId).size()>0) && (Variables.childackcounter==Variables.children.get(nodeId).size())){
							Variables.totalsent += Variables.sentmsgs;
							Variables.totalreceived += Variables.receivedmsgs;
						}
					}
					in.close();
					myClientSocket.close();
				}
				else if(val[0].startsWith("FINISH")){
					Variables.alive=false;
					//System.out.println("Terminate node " + nodeId);
					in.close();
					myClientSocket.close();
					//send finish to its children if any
					System.out.println("Children are: " + Variables.children.get(nodeId).toString());
					if(Variables.children.get(nodeId).size()>0)
						for(int i=0;i<Variables.children.get(nodeId).size();i++){
							Socket clientSock;
							try {
								clientSock = new Socket(Variables.hostName[Variables.children.get(nodeId).get(i)],Variables.portNo[Variables.children.get(nodeId).get(i)]);
								BufferedWriter brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
								String out_messg = "FINISH-";
								brwriter.write(out_messg);
								brwriter.flush();
								brwriter.close();
								clientSock.close();	
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}

				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}


	public static List<Integer> receive(List<Integer> piggyback, List<Integer> timestamp, int nodeid){
		List<Integer> max = new ArrayList<Integer>(timestamp.size());
		for(int i=0; i<timestamp.size();i++){
			if(timestamp.get(i)	>= piggyback.get(i)){
				max.add(timestamp.get(i));
			}
			else{
				max.add(piggyback.get(i));
			}
		}
		max.set(nodeid, (max.get(nodeid) +1));
		return max;
	}

}
