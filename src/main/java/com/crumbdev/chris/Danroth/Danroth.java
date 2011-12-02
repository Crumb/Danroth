package com.crumbdev.chris.Danroth;


/**
 * Created by IntelliJ IDEA.
 * User: chris
 * Date: 1/12/11
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class Danroth {
    public static void main(String[] args)
    {
        Boolean usenickserv = false;
        String nickserv = "";
        String server = "irc.esper.net";
        int port = 6667;
        String channels = "";
        String nick = "Danroth";
        String ident = "Danroth";
        for(int i = 0; i < args.length; i++)
        {
            if(args[i].toLowerCase().equalsIgnoreCase("--help"))
            {
                System.out.println("Help page here :P");
                System.out.println("Arguments list:");
                System.out.println("\t--help                        Shows this page");
                System.out.println("\t--nickserv=[true|false]       Identifies the bot to nickserv upon connect");
                System.out.println("");
                System.exit(0);
                return;
            }
        }
        for(int i = 0; i < args.length; i++)
        {
            //System.out.println(args[i]);
            if(args[i].toLowerCase().startsWith("--nickserv="))
            {
                if(args[i].substring(11, args[i].length()).equalsIgnoreCase(("true")) || args[i].substring(11, args[i].length()).equalsIgnoreCase("yes"))
                {
                    usenickserv = true;
                    System.out.println("Using NickServ authentication.");
                }
                else if(args[i].substring(11, args[i].length()).equalsIgnoreCase(("false")) || args[i].substring(11, args[i].length()).equalsIgnoreCase("no"))
                {
                    System.out.println("Not using NickServ authentication.");
                }
                else
                {
                    System.out.println("Malformed NickServ parameter. Use true or false. You provided: " + args[i].substring(11, args[i].length()));
                    System.exit(1);
                    return;
                }
            }
            else if(args[i].toLowerCase().startsWith("--server="))
            {
                server = args[i].toLowerCase().substring("--server=".length(), args[i].length());
                System.out.println("Using " + server + " as the server.");
            }
        }
    }
}
