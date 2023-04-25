package cn.edu.sustech.cs209.chatting.server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class HeartbeatThread implements Runnable {
  private final Scanner sc;
  private final PrintWriter pw;
  
  public HeartbeatThread(Scanner sc, PrintWriter pw) {
    this.sc = sc;
    this.pw = pw;
  }
  @Override
  public void run() {
    try {
      while (true) {
        pw.println("!");
        pw.flush();
        sc.nextLine();
      }
    } catch (Exception ignored) {
    }
  }
}
