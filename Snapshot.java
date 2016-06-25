import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class Snapshot implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int counter=0;
		synchronized(this){
			while(Variables.alive==true){
				int nodeCounter=0;
				boolean terminate = false;
					if(Variables.snapshotActive==true){
						counter++;
						System.out.println("Taking snapshot: " + counter);
						Variables.snapshotActive=false;
						Variables.globalstate[0]=Variables.nodeState;
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
							System.out.println("File not found");
						}
						Variables.markercount++;
						System.out.println("Sending marker messages to neighbors from node 0 " + Variables.markercount);
						for(int i=0;i<Variables.neighbours.size();i++){
							Socket clientSock;
							try {
								clientSock = new Socket(Variables.hostName[Variables.neighbours.get(i)],Variables.portNo[Variables.neighbours.get(i)]);
								BufferedWriter brwriter = new BufferedWriter(new OutputStreamWriter(clientSock.getOutputStream()));
								String out_messg = "Marker-0";
								brwriter.write(out_messg);
								Variables.transitcount[i]=0;
								Variables.channelarray[i]=true;
								brwriter.flush();
								brwriter.close();
								clientSock.close();	
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						System.out.println("Snapshot child ack received " + Variables.childackcounter + " and children size is " + Variables.children.get(0).size());	
					}
					while(Variables.childackcounter != Variables.children.get(0).size()){
						System.out.println("*******************");
					//	System.out.println("waiting for all ack");
					}
					if(Variables.childackcounter == Variables.children.get(0).size()){
						Variables.markercount=0;
						System.out.println("Received ack from all processes...going to sleep");
						Variables.childackcounter=0;
						try {
							Thread.sleep(Variables.snapshot_delay);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("Active again");
						Variables.snapshotActive=true;
					}
					
					for(int i=0;i<Variables.children.get(0).size();i++){
							System.out.println("Current state: "+i+" is " + Variables.globalstate[i]);
						if(Variables.globalstate[i].equals("PASSIVE")){
							nodeCounter++;		
						}
					}
					if((nodeCounter==Variables.children.get(0).size()) && (Variables.totalsent==Variables.totalreceived)){
						terminate=true;
					}
					else{
						Variables.totalsent =0;
						Variables.totalreceived =0;
					}
					if(terminate==true){
						Variables.alive=false;
						System.out.println("Stop snapshot");
						System.out.println("Total sent: " + Variables.totalsent);
						System.out.println("Total received: " + Variables.totalreceived);
						System.out.println("Children are: " + Variables.children.get(0).toString());
						for(int i=0;i<Variables.children.get(0).size();i++){
							Socket clientSock;
							try {
								clientSock = new Socket(Variables.hostName[Variables.children.get(0).get(i)],Variables.portNo[Variables.children.get(0).get(i)]);
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
			System.out.println("out of snapshot");
		}
	}

}
