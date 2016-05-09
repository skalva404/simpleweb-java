/*

Simple Web Server example taken from:
http://www.java2s.com/Code/Java/Network-Protocol/ASimpleWebServer.htm

Slightly modified to return the server's IP address for Load-Balancing tests.
Also modified to look for variable $PORT0 and start in that port if exists, which
is useful for deployment in Marathon.

*/

///A Simple Web Server (WebServer.java)

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class WebServer {

  /**
   * WebServer constructor.
   */
  protected void start() {
    ServerSocket s;

    //Detect if the variable $PORT0 is defined. Start the server in that port if so
    String port0 = System.getenv("PORT0");
    System.out.println("DEBUG: PORT0 is " + port0);
    if ((port0 == null) || port0.isEmpty()){
      port0 = "80";
    }

    System.out.println("Webserver starting up on port " + port0);
    System.out.println("(press ctrl-c to exit)");
    try {
      // create the main server socket
      s = new ServerSocket(Integer.parseInt(port0));
    } catch (Exception e) {
      System.out.println("Error: " + e);
      return;
    }

    // Detect my IP address so that I can return it as part of the response
    // print to stdout all system's IP addresses, keep the last value as "ip"
    String ip="";

    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
          NetworkInterface iface = interfaces.nextElement();
          // filters out 127.0.0.1 and inactive interfaces
          if (iface.isLoopback() || !iface.isUp())
            continue;

          Enumeration<InetAddress> addresses = iface.getInetAddresses();
          while(addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            ip = addr.getHostAddress();
            System.out.println(iface.getDisplayName() + " " + ip);
          }
      }
    } catch (SocketException e) {
      throw new RuntimeException(e);
    }

    System.out.println("Waiting for connection");
    for (;;) {
      try {
        // wait for a connection
        Socket remote = s.accept();
        // remote is now the connected socket
        System.out.println("Connection, sending data.");
        BufferedReader in = new BufferedReader(new InputStreamReader(
            remote.getInputStream()));
        PrintWriter out = new PrintWriter(remote.getOutputStream());

        // read the data sent. We basically ignore it,
        // stop reading once a blank line is hit. This
        // blank line signals the end of the client HTTP
        // headers.
        String str = ".";
        while (!str.equals(""))
          str = in.readLine();

        // Send the response
        // Send the headers
        out.println("HTTP/1.0 200 OK");
        out.println("Content-Type: text/html");
        out.println("Server: Bot");
        // this blank line signals the end of the headers
        out.println("");
        // Send the HTML page
        out.println("<H1>Welcome to the Ultra Mini-WebServer</H2>");
        // Send the IP address
        out.println("This server's higher interface's IP address is:");
        out.println("<font color=\"red\">" + ip + "</font>");
        out.println("<br />"); //newline
        out.println("This server is listening on port: ");
        out.println("<font color=\"red\">" + port0 + "</font>");
        out.flush();
        remote.close();
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
    }
  }

  /**
   * Start the application.
   *
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
