//package p2p;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

public class newClient5 {

	private final int Peersnum = 5;
    public int SERVER_PORT;
	
    
    static int peerId;
    public String peerName;
    public int peerport;
    
    private String filename;
    
    public int downloadport;
    public int downloadpeer;
    public int uploadpeer;

    public int chunktotal=0;
    
	private HashMap<Integer, byte[]> chunkList;
  

    
    public newClient5(int Id,int serverport,int port,int downport,int downpeer, int uppeer){
    	peerId = Id;
    	SERVER_PORT = serverport;
    	peerport = port;
    	downloadport = downport;
    	downloadpeer = downpeer;
    	uploadpeer = uppeer;
    	chunkList =  new HashMap<Integer, byte[]>();
    	chunktotal = 0;
    }
    
    
    
    public static void write(ObjectOutputStream out, String message) throws IOException {
        out.writeObject(message);
        out.flush();
        out.reset();
    }

    public static void write(ObjectOutputStream out, byte[] load) throws IOException {
        out.writeObject(load);
        out.flush();
        out.reset();
    }

    public static void write(ObjectOutputStream out, int value) throws IOException {
        out.writeInt(value);
        out.flush();
    }
    
    public static void write(ObjectOutputStream out, ArrayList<Integer> obj) throws IOException {
        out.writeObject(obj);
        out.flush();
        out.reset();
    }

    
    
    
    public void run() {
        try {

            Socket s = new Socket("localhost", SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());     
            
            //get file name
        	write(out,"fileName");
        	filename = (String)in.readObject();
            
            
            
            //Get chunk list
            write(out, "sendChunkNUM");
            chunktotal = in.readInt();
            
            
            //Get initial several chunks from server and store them as separate files;
            int startIndex = (int)((1.0*chunktotal / Peersnum) * (peerId-1));
            int endIndex = (int)((1.0*chunktotal / Peersnum)*peerId);
            

            for(int i = startIndex; i < endIndex; i++) {
                write(out, "sendChunkDATA");
                write(out, i);
                byte[] chunk = (byte[]) in.readObject();
                chunkList.put(i, chunk);    
                System.out.println("Peer"+peerId+" Received Chunk #" + i + " from server");
                writefile(chunk,i,peerId);
            }
            // close the connection with Server
            write(out,"Close");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        
        try{ 	  
        PeerDownloader down;
        down= new PeerDownloader(peerId,peerport,downloadport,downloadpeer);
        down.start(); 
        }
        finally{	
        }
        try{
 	    PeerUploader  up;
        up= new PeerUploader(peerId,peerport,peerport,uploadpeer);
        up.start();
        }finally{}




    }
  
    


    public void writefile(byte[] mybytearray, int i,int peerId){
    	int id = peerId;
    	peerName = Integer.toString(id);
    	File FileDirectory = new File(peerName);
    	// Create the directory if it doesn't exist.
    	FileDirectory.mkdir();
    	try{
    		File chunkFile = new File(FileDirectory, Integer.toString(i));
    		OutputStream output = new FileOutputStream(chunkFile);
        	output.write(mybytearray);	
    		output.close();
    	}catch (IOException e) {
            e.printStackTrace();
    	}
    }
    
    

    
    public static void main(String args[])
    {
    	int ServerPort=0;
    	int portNumber [] = new int [6]; 
		int downloadPeerNum [] = new int [6];
		int uploadPeerNum [] =new int [6];
		try (BufferedReader br = new BufferedReader (new FileReader ("config.txt"))){
			String line;
			while ((line = br.readLine()) != null){
				String tepStr[] = line.split(" ");
				int index = Integer.parseInt(tepStr[0]);
				if(index !=0)
				{
					portNumber[index] = Integer.parseInt(tepStr[1]);
					downloadPeerNum [index] = Integer.parseInt(tepStr[2]);
					uploadPeerNum [index] = Integer.parseInt(tepStr[3]);
				}
				else{
					ServerPort = Integer.parseInt(tepStr[1]);
					}
			}
			br.close();
		}
		catch(IOException e){
	    	e.printStackTrace();
		}
    	
        int peerId = 5;
    	newClient5 peer5 = new newClient5(peerId,ServerPort,portNumber[peerId],portNumber[downloadPeerNum[peerId]],downloadPeerNum[peerId],uploadPeerNum[peerId]);
    	peer5.run(); 	
    	
    	
    }
    
   
    

    public boolean checkChunk() {
        for(int i=0;i<chunktotal;i++) {
            if(!chunkList.containsKey(i))
            {
                return false;
            }
        }
        return true;
    }
   
    
    
    public void mergeChunk(int index) {
    	int peerId = index;
        //This means we already had all chunks, combine and write it out
        try {
            File fout = new File(peerId +"/"+filename);
            if(fout.exists())
            {
                fout.delete();
            }
            FileOutputStream fs = new FileOutputStream(fout);
            for(int i = 0; i < chunkList.size(); i++) {
                fs.write(chunkList.get(i));
            }
            fs.flush();
            fs.close();
            System.out.println("[ Peer " + peerId + "] Finished downloading and writing");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    
    
    
    
    
    
    
    
    
    class PeerDownloader extends Thread {
    	
  	  private int Peerindex = -2;
      private int downloaderPort = 0;
      private int pport;
      private int downloaderPeer =0;
      private int receivechunknum = 0;
      
      
      public PeerDownloader(int id, int port, int dport,int dpeer){
    	  Peerindex = id;
    	  pport = port;
    	  downloaderPort = dport;
    	  downloaderPeer = dpeer;
      }
      
  	
      public void run(){
     	    System.out.println("Making download connection...");
     	    boolean scanning = true;
     	    while(scanning){
     	         try
     	          {  
     	        	Socket down = new Socket("localhost",downloaderPort);
     	        	scanning =false;
     	        	System.out.println("Download Connection Made!");
     	         	ObjectOutputStream downout = new ObjectOutputStream(down.getOutputStream());
     	            ObjectInputStream downin = new ObjectInputStream(down.getInputStream());  
     	         	 
     	          
 
     	           int i=0;    
     	           while(chunkList.size()<chunktotal){
     	        	    ArrayList<Integer> needlist = new ArrayList<Integer>(); 
     	        	    for(i=0;i<chunktotal;i++)
     	        	    {
     	        	    	if(chunkList.containsKey(i))
        	            	{
        	            		continue;
        	            	}
     	        	    	write(downout,"ASK");
    	            	    write(downout, i);
    	            	    int  S = downin.readInt();
    	            	    if(S==1)
    	            	    	needlist.add(i);
     	        	    }
     	        	   if(needlist.size()!=0)
    	        	    {
    	        	    	write(downout,"Print");
        	        	    System.out.println("Received ChunkList from Peer " + downloaderPeer);
        	        	    System.out.println("Uploader have Chunk #:" + needlist);	
    	        	    }	
     	        	    for(int j=0;j<needlist.size();j++)
     	        	    {
     	        	    	int pp=needlist.get(j);
     	        	    	write(downout,"Requestchunk");
     	            	    System.out.println("Peer "+Peerindex+" Request Chunk #" + pp + " from Peer " + downloaderPeer);
     	            	    write(downout,pp);
     	            	    byte[] temp = (byte[]) downin.readObject();
     	            	    chunkList.put(pp, temp);
     	            	    System.out.println("Peer "+Peerindex+" Received Chunk #" + pp + " from Peer " + downloaderPeer);
      	            		writefile(temp,pp,Peerindex);
     	        	    	
     	        	    	
     	        	    }
     	        	   
     	        	   
    	            }   
     	            
     	            
     	            
     	            
     	  
     	                 
  
     	            if(chunkList.size()==chunktotal)
     	           {	
     	           	mergeChunk(Peerindex);
     	           }

     	            write(downout,"Close");
    
     	          }
     	          catch(IOException|ClassNotFoundException e)
     	           {
     	             try{
     	                 Thread.sleep(100);//1 seconds
     	                 }
     	            catch( InterruptedException ie){
     	              ie.printStackTrace();
     	                 }
     	            } 
     	         
                }
        }
  
  }
    
    
    
    
    
    
    
  class PeerUploader extends Thread {
    	
    	private int Peerindex = -2;
    	private int pport;
    	private int uploadpeer;
        private int upPort = 0;
        private ServerSocket uploadSock;
        public HashMap<Integer, byte[]> activechunk = new HashMap<Integer, byte[]>();
        private ObjectOutputStream out;
        private ObjectInputStream in;
        Socket connection = null;
        
        public void send(Object message) throws IOException {
            out.writeObject(message);
            out.flush();
            out.reset();
        }

        public void send(int message) throws IOException {
            out.writeInt(message);
            out.flush();
            out.reset();
        }
        
        public void send(ArrayList<Integer> obj) throws IOException {
            out.writeObject(obj);
            out.flush();
            out.reset();
        }
        
        
        
        public PeerUploader(int id, int peerport,int port, int upport){
        	Peerindex = id;
        	pport = peerport;
        	upPort = port;
        	uploadpeer = upport;
        }
    	
        public void run(){	
            try{
           	     ServerSocket uploadSock = new ServerSocket(upPort);
            	 System.out.println("Making upload connection..."); 
            	 connection = uploadSock.accept();
            	 out = new ObjectOutputStream(connection.getOutputStream());
	        	 in = new ObjectInputStream(connection.getInputStream());
                 System.out.println("Upload Connection Made!");  
		             while (true) {
		            	       try{
		                       String message = (String)in.readObject();
		                       int t=-1;
		                       switch (message) {
		                       case "Print":
		                    	   System.out.println("Send ChunkList!");
		                    	   break;
		                       case "Requestchunklist":
		                    	   Set<Integer> cset = chunkList.keySet();
		                    	   ArrayList<Integer> uplist = new ArrayList<Integer>(cset);
		                    	   send(uplist);
		                    	   break;
	                           case "ASK":
	                        	   t = in.readInt();
	                        	   if(chunkList.containsKey(t))
	                        	      {
	                        		   send(1);
	                        		   }
	                        	   else
	                        		   send(0);
	                               break;
	                           case "Requestchunk":
	                        	   t = in.readInt();
	                        	   System.out.println("Peer "+Peerindex+" Received Peer " + uploadpeer + " Request for Chunk #" + t);
	                        	   send(chunkList.get(t));
	                        	   System.out.println("Peer "+Peerindex+" Send Chunk #" + t + " to Peer "+ uploadpeer);
	                               break;
	                           case "Close":
	                        	   in.close();
	                        	   out.close();
	                        	   connection.close();
	                        	   System.out.println("[ Peer " + peerId + "] Finished uploading");
	                        	   return;
	                             }
		            	       }catch(ClassNotFoundException|IOException ex){ 
		                           ex.printStackTrace(); 
		                       }      
		                      
		            }  
            }catch(IOException ex){ 
                ex.printStackTrace(); 
            }      
	      finally{
	    	 try{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				}
     	  
            }   
         
        
      }
  }
   
  
  
  
  
  
}
  

    
    
    
    
    
    
    
    
    
    
    
    
    
    