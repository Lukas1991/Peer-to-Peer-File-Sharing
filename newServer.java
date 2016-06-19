//package p2p;
           
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

/* this is the file owner Server */

public class newServer {

	    public final int BUFFER_SIZE=102400;   //set buffer size to 100kb
	    
	    public static HashMap<Integer, byte[]> chunkList = new HashMap<Integer, byte[]>();
	    
	    public static final int Peersnum = 5;
	    private  int SERVER_PORT = 0; 
	    
	    private ServerSocket listener;
	    private String FileName;
	    private String path="File/";
	    File f = new File("File/");
	    
	    
		public int peerid = 1;
		public static int chunktotal = 0;   //total chunk number
		
		
		public newServer(int serverport){		
			SERVER_PORT = serverport;
			
		}

		protected void breakchunkFile(){
			    ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
			    FileName = names.get(0);
			    try{
			         
			        FileInputStream fis = new FileInputStream(path+names.get(0));
			    	byte[] buffer = new byte[BUFFER_SIZE];
			    	while(fis.read(buffer)!=-1){
			    		chunkList.put(chunktotal, buffer);
			    		buffer = new byte[BUFFER_SIZE];
			    		chunktotal++;
			    	}
			    	System.out.println("The Server has in total "+chunktotal+" chunks.");
			    	fis.close();
			    }
			    catch(IOException e){
			    	e.printStackTrace();
			    }
			    }
	
		 
		public void Start() {
			try {
		        listener = new ServerSocket(SERVER_PORT);
	        } catch (IOException e) {
	            e.printStackTrace();
	            System.exit(1);
	        }
	        this.breakchunkFile();
	        try {
	            while(true) {
	                System.out.println("Server is waiting clients...");
	                Socket p = listener.accept();
	                Handler st = new Handler(p,peerid);
	                st.setChunk(chunkList);
	                st.run();
	                peerid++;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	    }
		
	
		
		public static void main(String[] args) {

			int ServerPort=0;
			try (BufferedReader br = new BufferedReader (new FileReader ("config.txt"))){
				String line;
				while ((line = br.readLine()) != null){
					String tepStr[] = line.split(" ");
					int index = Integer.parseInt(tepStr[0]);
					if(index ==0)
						ServerPort = Integer.parseInt(tepStr[1]);
				}
				br.close();
			}
			catch(IOException e){
		    	e.printStackTrace();
			}

	        new newServer(ServerPort).Start();
	    }
		

	
         class Handler extends Thread{
	    	
	     protected int clientIndex;
	     private Socket connection;
	     private ObjectOutputStream out;
	     private ObjectInputStream in;
	     protected HashMap<Integer, byte[]> chunk_active = new HashMap<Integer, byte[]>();
	     
	     
	     public Handler(Socket connection, int no)
	     {
	    	 this.connection = connection;
	    	 this.clientIndex = no;
	     }
	     
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
	     

	     
	     public void setChunk(HashMap<Integer, byte[]> Chunk)
	        {
	            this.chunk_active = Chunk;
	        }
	         
	     
	     public void run() {
	           try{
	        	   out = new ObjectOutputStream(connection.getOutputStream());
	        	   in = new ObjectInputStream(connection.getInputStream());
	               while (true) {
	                   try {
	                       System.out.println("Server is listening...........:");
	                       String message = (String)in.readObject();
	                       System.out.println("Server Received message (" + message + ") from " + clientIndex);
	                       int t = -1;
	                       switch (message) {
	                           case "fileName":
	                        	   send(FileName);
	                        	   break;
	                           case "sendChunkNUM":
	                        	   t = chunktotal;
	                               send(t);
	                               break;
	                           case "sendChunkDATA":
	                        	   t = in.readInt();
	                        	   send(chunk_active.get(t));
	                               break;
	                           case "Close":
	                        	   in.close();
	                               out.close(); 
	                               connection.close();
	                               return;
	                       }
	                   } catch (ClassNotFoundException | IOException e) {
	                       e.printStackTrace();
	                   }
	               }   
	           } catch (IOException e) {
	                e.printStackTrace();
	            } 
	        } 
	
	
}
}
