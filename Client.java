import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;


public class Client implements Runnable {
	int toNodeId;
	String ipAddress[];
	int port[];
	List<Integer> neighbours;
	String ip;
	int minperactive;
	int maxperactive;

	public Client(String hostName[],int portNo[],List<Integer> neighbours, String ip, int minperactive, int maxperactive) {
		this.ipAddress = hostName;
		this.port = portNo;
		this.neighbours = neighbours;
		this.ip = ip;
		this.minperactive = minperactive;
		this.maxperactive = maxperactive; 
	}

	@Override
	public void run() {
		synchronized (this)
		{
			BufferedWriter brwriter=null;	
			Random random = new Random();
			int send = random.nextInt(maxperactive - minperactive + 1) + minperactive;
			//Node 0 will be the initiator
			if(Integer.parseInt(ip)==0){
				Thread snapshot = new Thread(new Snapshot());
				snapshot.start();
			}
			while(Variables.alive==true){
				//if node has sent max number of msgs then the node should remain passive
				if(Variables.sentmsgs >= Variables.maxnumber){
				//	Variables.active=false;
					Variables.cansend=false;
					synchronized(this){
					Variables.nodeState="PASSIVE";
					Variables.MAPNodeState="PASSIVE";}
				}
				//send msgs based on random number to neighbor if the node is active and it has not sent less  than max number of msgs
				if(Variables.MAPNodeState.equals("ACTIVE")){
					if(Variables.cansend==true){
						System.out.println("Node: "+Integer.parseInt(ip) +" Messages sent: " + Variables.sentmsgs + " and can send " + send);
						try {
							for(int j=0;j<send;j++)
							{
									Variables.sentmsgs++;
									int k = random.nextInt(neighbours.size());
									int ipval = Integer.parseInt(ip);
									int value = Variables.timestamp.get(ipval);
									value++;
									Variables.timestamp.set(Integer.parseInt(ip),value);
									Socket clientSock = new Socket(ipAddress[neighbours.get(k)],port[neighbours.get(k)]);
									brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
									String out_messg = "Application-"+ Variables.timestamp.toString()+"-"+Integer.parseInt(ip);
									brwriter.write(out_messg);
									brwriter.flush();
									brwriter.close();
									clientSock.close();	
									try {
										Thread.sleep(Variables.min_send_delay);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							}
						//	Variables.passive=true;
							synchronized(this){
							Variables.nodeState="PASSIVE";
							Variables.MAPNodeState="PASSIVE";}
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			System.out.println("Terminating Node " + Integer.parseInt(ip));
			Variables.server=false;
		}
	}
}
