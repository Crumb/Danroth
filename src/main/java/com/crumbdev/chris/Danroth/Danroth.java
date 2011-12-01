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
            System.out.println(args[i]);
            if(args[i].startsWith("--nickserv="))
            {
                args[i].substring(10, args[i].length());
                System.out.println(args[i].substring(10, args[i].length()));
            }
        }
    }
}
