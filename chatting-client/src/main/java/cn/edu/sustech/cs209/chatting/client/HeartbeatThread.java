package cn.edu.sustech.cs209.chatting.client;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class HeartbeatThread implements Runnable{
  private final Controller controller;
  private final Scanner sc;
  private final PrintWriter pw;
  public HeartbeatThread(Controller controller, Scanner sc, PrintWriter pw) {
    this.controller = controller;
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
    } catch (Exception e) {
      controller.exitDueToServer();
    }
  }
}
