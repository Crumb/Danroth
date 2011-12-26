package com.crumbdev.chris.Danroth;


import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import org.apache.maven.project.MavenProject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Danroth {

    private PrintWriter writer;
    private Boolean usenickserv = false;
    private String nickserv = "";
    private String server = "irc.esper.net";
    private int port = 6667;
    private final List channels = new ArrayList();
    private String nick = "Danroth";
    private final String ident = "Danroth";
    public static void main(String[] args)
    {
        (new Danroth()).DanrothStart(args);
    }

    void DanrothStart(String[] args)
    {
        //If --help is found, show help window and exit
        for (String arg1 : args) {
            if (arg1.toLowerCase().equalsIgnoreCase("--help")) {
                System.out.println("Arguments list:");
                System.out.println("\t--help                        Shows this page");
                System.out.println("\t--nickserv=[Password]         Identifies the bot to nickserv upon connect using the specified password. Default: No identification");
                System.out.println("\t--server=[IRC Server Address] The address of the IRC server. Default: irc.esper.net");
                System.out.println("\t--port=[Port]                 The port to connect to the server using. Default: 6667");
                System.out.println("\t--nick=[Nick]                 The nickname for the bot to use. Default: Danroth");
                System.out.println("\t--ident=[Ident]               The ident/username for the bot to use. Default: Danroth");
                System.out.println("\t--channel=[#channelname]      Join #channel on connect. Default: No Channels");
                System.exit(0);
                return;
            }
        }
        //Otherwise, read the arguments and store them in the appropriate variables.
        for (String arg : args) {
            //System.out.println(args[i]);
            if (arg.toLowerCase().startsWith("--nickserv=")) {
                usenickserv = true;
                nickserv = arg.substring("--nickserv=".length(), arg.length());
                System.out.println("Using NickServ authentication.");
            } else if (arg.toLowerCase().startsWith("--server=")) {
                server = arg.toLowerCase().substring("--server=".length(), arg.length());
                System.out.println("Using " + server + " as the server.");
            } else if (arg.toLowerCase().startsWith("--port=")) {
                port = Integer.parseInt(arg.substring("--port=".length(), arg.length()));
                System.out.println("Using port " + port);
            } else if (arg.toLowerCase().startsWith("--nick=")) {
                nick = arg.substring("--nick=".length(), arg.length());
                System.out.println("Using nick " + nick);
            } else if (arg.toLowerCase().startsWith("--ident=")) {
                nick = arg.substring("--ident=".length(), arg.length());
                System.out.println("Using ident " + ident);
            } else if (arg.startsWith("--channel=")) {
                channels.add(arg.substring("--channel=".length()));
            }

            //Unknown command
            else {
                System.out.println("Unknown argument: " + arg);
                System.exit(1);
                return;
            }
        }
        //Print the channel list
        System.out.println("Channel List");
        for(int i = 0; i < channels.toArray().length; i++)
        {
            System.out.println("\t" + channels.toArray()[i]);
        }

        while( true )
        {
            try {
                Socket connection = new Socket(server, port);
                writer = new PrintWriter(connection.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                writeline("NICK " + nick, writer);
                writeline("USER " + ident + " 0 * :" + ident, writer);
                while(true)
                {
                    if(reader.ready())
                    {
                        String read = reader.readLine();
                        System.out.println(read);
                        if(read.startsWith("PING"))
                            writeline("PONG " + read.split(" ") [1], writer);
                        else if(read.startsWith(":NickServ") && usenickserv)
                        {
                            writeline("PRIVMSG NickServ :IDENTIFY " + nickserv, writer);
                            usenickserv = false;
                        }

                        try{
                            switch(Integer.parseInt(read.split(" ")[1]))
                            {
                                case 1:
                                    //Join all the channels. This is the first message to be sent out from the server to state that you are connected.
                                    for(int i = 0; i < channels.toArray().length; i++)
                                    {
                                        writeline("JOIN " + channels.get(i), writer);
                                    }
                                    break;
                            }
                        }
                        catch (Exception e)
                        {
                              interpret(read);
                        }
                    }
                    //Thread.sleep();
                }
            }
            catch(UnknownHostException e)
            {
                System.out.println("Oops, your hostname was invalid. Make sure you're connected to the internet and try again. ("+ server + ")");
                System.exit(1);
            }
            catch (Exception e)
            {
                System.out.println("Oops, an error occured. Please report it to chris@crumbdev.com.\nError stack: ");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    void interpret(String read)
    {
        synchronized (this)
        {
            if(read.split(" ")[1].equalsIgnoreCase("NICK") && read.split(" ")[0].startsWith(":" + nick + "!~" + ident))
            {
                writeline("PRIVMSG NickServ :REGAIN " + nick + " " + nickserv, writer);
                writeline("PRIVMSG NickServ :IDENTIFY " + nickserv, writer);
                writeline("NICK " + nick, writer);
            }
            if(read.split(" ")[1].equalsIgnoreCase("PRIVMSG"))
            {
                String responsePrefix;
                String noChannelPrefix;
                String command;
                if(read.split(" ")[2].equalsIgnoreCase(nick))
                {
                    noChannelPrefix = "PRIVMSG " + read.split(" ")[0].split("!")[0].substring(1) + " :";
                    responsePrefix = "PRIVMSG " + read.split(" ")[0].split("!")[0].substring(1) + " :";
                    command = read.split(" ")[3].substring(1);
                }
                else if(read.split(" ")[3].startsWith(":^"))
                {
                    noChannelPrefix = "NOTICE " + read.split(" ")[0].split("!")[0].substring(1) + " :";
                    responsePrefix = "PRIVMSG " + read.split(" ")[2] + " :";
                    command = read.split(" ")[3].substring(2);
                }
                else if(read.split(" ")[3].startsWith(":!"))
                {
                    noChannelPrefix = "NOTICE " + read.split(" ")[0].split("!")[0].substring(1) + " :";
                    responsePrefix = "NOTICE " + read.split(" ")[0].split("!")[0].substring(1) + " :";
                    command = read.split(" ")[3].substring(2);
                }
                else
                {
                    return;
                }

                //
                // Commands
                //
                if(command.equalsIgnoreCase("bwiki"))
                {
                    writeline(responsePrefix + "bwiki command was used.", writer);
                    throw new NotImplementedException();
                    //TODO: To be implemented
                }
                else if(command.equalsIgnoreCase("build"))
                {
                    writeline(responsePrefix + "build command was used", writer);
                    //TODO: To be implemented
                    throw new NotImplementedException();
                }
                else if(command.equalsIgnoreCase("latest"))
                {
                    writeline(responsePrefix + "latest command was used", writer);
                    //TODO: To be implemented
                    throw new NotImplementedException();
                }
                else if(command.equalsIgnoreCase("notch"))
                {
                    writeline(responsePrefix + "notch command was used", writer);
                    //TODO: To be implemented
                    throw new NotImplementedException();
                }
                else if(command.equalsIgnoreCase("rules"))
                {
                    writeline(noChannelPrefix + "\u0002==IRC Rules==", writer);
                    writeline(noChannelPrefix + "IRC Rules can be found at http://wiki.bukkit.org/IRC", writer);
                }
                else if(command.equalsIgnoreCase("rule"))
                {
                    int rulenum = -1;

                    try{               //Trys to get the rule number, surrounded by a try/catch just in case they leave an empty rule number, or other non-integer.
                        rulenum = Integer.parseInt(read.split(" ")[4]);
                        //int rulenum = (int)rulenum;
                    }
                    catch (Exception e){}
                    switch(rulenum)
                    {
                        case 1:
                            writeline(responsePrefix + "==Rule " + rulenum + ": ALWAYS READ THE TOPIC==", writer);
                            writeline(responsePrefix + "It's the first thing you see when you connect to or join the channel and it is usually at the top of the", writer);
                            writeline(responsePrefix + "screen at all times. Read it when you connect, when you join, when you come back from being away. Always.", writer);
                            break;
                        case 2:
                            writeline(responsePrefix + "==Rule " + rulenum + ": We are volunteers!==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 3:
                            writeline(responsePrefix + "==Rule " + rulenum + ": This is not designed as a support channel!==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 4:
                            writeline(responsePrefix + "==Rule " + rulenum + ": Ignorance isn't a valid defense.==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 5:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No excessive use of profanity==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 6:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No racism, discrimination, threats, harrasment or personal attacks of any kind are permitted.==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 7:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No vulgarity or obscenity.==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 8:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No spamming is permitted, whatsoever.==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 9:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No flaming, inciting hatred or instigating flame bait is permitted.==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 10:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No selling of products or services, unless approved by a member of channel staff.==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 11:
                            writeline(responsePrefix + "==Rule " + rulenum + ": Do not ask for a position on staff.==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 12:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No advertising.==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 13:
                            writeline(responsePrefix + "==Rule " + rulenum + ": Disrespect and intolerance towards other people is NOT acceptable==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 14:
                            writeline(responsePrefix + "==Rule " + rulenum + ": BE PATIENT and no excessive repeating==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 15:
                            writeline(responsePrefix + "==Rule " + rulenum + ": Pastebin logs, code snippets, anything longer than 3 lines!==", writer);
                            //TODO: Finish off the rule
                            break;
                        case 16:
                            writeline(responsePrefix + "==Rule " + rulenum + ": This is an English only channel==", writer);
                            //TODO: Finish off the rule
                            break;
                        default:
                            writeline(noChannelPrefix + "==Error==", writer);
                            writeline(noChannelPrefix + "Please make sure you typed a valid number between 1 and 16. ", writer);
                            break;
                    }
                    try{
                        Thread.sleep(100);
                    } catch (Exception e) {}

                }
                else if(command.equalsIgnoreCase("version"))
                {
                    //writeline(noChannelPrefix + "Running Danroth build " + new MavenProject().getProperties().getProperty("build.number"), writer);
                }
                else if(command.equalsIgnoreCase("help"))
                {
                    writeline(noChannelPrefix + "\u0002==Danroth Commands==", writer);
                    writeline(noChannelPrefix + "IRC Commands can be found at http://wiki.bukkit.org/IRC/Bots/Danroth", writer);
                }
            }
        }
    }

    private void writeline(String towrite, PrintWriter writer)
    {
        writer.write(towrite + "\n");
        writer.flush();
    }
}
