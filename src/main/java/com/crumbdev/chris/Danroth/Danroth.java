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
            //System.out.println(args[i]);
            if(args[i].toLowerCase().startsWith("--nickserv="))
            {
                if(args[i].substring(11, args[i].length()).equalsIgnoreCase(("true")) || args[i].substring(11, args[i].length()).equalsIgnoreCase("yes"))
                {
                    usenickserv = true;
                }
                else if(args[i].substring(11, args[i].length()).equalsIgnoreCase(("false")) || args[i].substring(11, args[i].length()).equalsIgnoreCase("no"))
                {

                }
                else
                {
                    System.out.println("Malformed NickServ parameter. Use true or false. You provided: " + args[i].substring(11, args[i].length()));
                }
            }
        }
    }
}
