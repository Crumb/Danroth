package com.crumbdev.chris.Danroth;


import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.yaml.snakeyaml.Yaml;
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
    private Map buildinfo;
    public static void main(String[] args)
    {
        (new Danroth()).DanrothStart(args);
    }

    Runnable r = new Runnable() {
        @Override
        public void run() {
            while(true)
            {
                flood = 0;
                try{
                    Thread.sleep(20000);
                }
                catch (Exception e)
                {
                    writeline("PRIVMSG #Danroth :" + e.getMessage());
                    for(StackTraceElement el : e.getStackTrace())
                        writeline("PRIVMSG #Danroth :" + el.toString());
                }
            }
        }
    };

    void DanrothStart(String[] args)
    {
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getResourceAsStream("/buildinfo.yml");
        Reader strreader = new BufferedReader(new InputStreamReader(inputStream));
        Writer strwriter = new StringWriter();
        char[] buffer = new char[1024];
        try{
            int n;
            while ((n = strreader.read(buffer)) != -1) {
                strwriter.write(buffer, 0, n);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                inputStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }

        buildinfo = (Map)yaml.load(strwriter.toString());

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
                writeline("NICK " + nick);
                writeline("USER " + ident + " 0 * :" + ident);
                new Thread(r).start();
                while(true)
                {
                    if(reader.ready())
                    {
                        String read = reader.readLine();
                        System.out.println(read);
                        if(read.startsWith("PING"))
                            writeline("PONG " + read.split(" ") [1]);
                        else if(read.startsWith(":NickServ") && usenickserv)
                        {
                            writeline("PRIVMSG NickServ :IDENTIFY " + nickserv);
                            usenickserv = false;
                        }

                        try{
                            switch(Integer.parseInt(read.split(" ")[1]))
                            {
                                case 1:
                                    //Join all the channels. This is the first message to be sent out from the server to state that you are connected.
                                    for(int i = 0; i < channels.toArray().length; i++)
                                    {
                                        writeline("JOIN " + channels.get(i));
                                    }
                                    break;
                                case 433:
                                    writeline("NICK " + nick + "_");
                                    writeline("PRIVMSG NickServ :REGAIN " + nick + " " + nickserv);
                                    writeline("PRIVMSG NickServ :IDENTIFY " + nickserv);
                                    writeline("NICK " + nick);
                                    break;
                                default:
                                    interpret(read);
                            }
                        }
                        catch (Exception e)
                        {
                              interpret(read);
                        }
                    }
                    else
                    {
                        Thread.sleep(100);
                    }
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

    public boolean disabled = false;
    public int flood = 0;

    
    void interpret(String read)
    {
        synchronized (this)
        {
            if(read.split(" ")[0].equalsIgnoreCase("ERROR"))
            {
                System.exit(0);
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
                if(++flood >= 4 && command != "disable" && command != "enable")
                {
                    return;
                }
                if(command.equalsIgnoreCase("disable") && (read.toLowerCase().startsWith(":chrisward!") || read.toLowerCase().startsWith(":chris!")))
                {
                    writeline("NICK " + nick + "|Disabled");
                    disabled = true;
                }
                else if(command.equalsIgnoreCase("enable") && (read.toLowerCase().startsWith(":chrisward!") || read.toLowerCase().startsWith(":chris!")))
                {
                    writeline("NICK " + nick);
                    disabled = false;
                    flood = 0;
                }
                else if(disabled)
                    return;




                //
                // Commands
                //
                else if(command.equalsIgnoreCase("bwiki") || command.equalsIgnoreCase("wiki"))
                {
                    try
                    {
                        URL u = new URL("http://wiki.bukkit.org/index.php?title=Special:RecentChanges&feed=atom");
                        java.net.URLConnection c = u.openConnection();
                        c.addRequestProperty("User-Agent", "Mozilla/4.76");
                        c.addRequestProperty("X-I-Am-A-Bot", "Danroth");
                        BufferedReader feedreader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        String feedRead = "";
                        while(feedreader.ready())
                        {
                            feedRead += feedreader.readLine();
                        }
                        feedRead = feedRead.split("<entry>")[1].split("</entry>")[0];
                        String timestamp = feedRead.split("<updated>")[1].split("</updated>")[0];

                        writeline(responsePrefix + "==Latest Wiki Edit==");
                        writeline(responsePrefix + "Page: " + feedRead.split("<id>")[1].split("</id>")[0].replace("&amp;", "&"));
                        writeline(responsePrefix + "Edit summary: " + feedRead.split("<summary type=\"html\">")[1].split("</summary>")[0].replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&").replaceAll("\\<.*?>",""));
                        writeline(responsePrefix + "Author: " + feedRead.split("<author>")[1].split("</author>")[0].split("<name>")[1].split("</name>")[0]);
                        writeline(responsePrefix + "Datestamp: " + timestamp.substring(8, 10) + "/" + timestamp.substring(5, 7) + "/" + timestamp.substring(0, 4) + " " + timestamp.substring(11, 19));

                        

                        System.out.println(feedRead);

                    }
                    catch (Exception e)
                    {
                        writeline(responsePrefix + "An error occurred while performing this command.");
                        writeline("PRIVMSG #Danroth :" + e.getMessage());

                        for(StackTraceElement line : e.getStackTrace())
                        {
                            writeline("PRIVMSG #Danroth :" + line.toString());
                        }
                    }
                }
                else if(command.equalsIgnoreCase("build") || command.equalsIgnoreCase("latest"))
                {
                    writeline(responsePrefix + "ci.bukkit.org is down, and this command will be implemented when it comes back up, or when an alternate does.");
                    //TODO: To be implemented
                }
                else if(command.equalsIgnoreCase("notch"))
                {
                    try
                    {
                        URL u = new URL("http://notch.tumblr.com/rss");
                        java.net.URLConnection c = u.openConnection();
                        c.addRequestProperty("User-Agent", "Mozilla/4.76");
                        c.addRequestProperty("X-I-Am-A-Bot", "Danroth");
                        BufferedReader feedreader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        String feedRead = "";
                        while(feedreader.ready())
                        {
                            feedRead += feedreader.readLine();
                        }
                        feedRead = feedRead.split("<item>")[1].split("</item>")[0];
                        writeline(responsePrefix + "==Notch Stalking Module==");
                        writeline(responsePrefix + "Title: " + feedRead.split("<title>")[1].split("</title>")[0]);
                        writeline(responsePrefix + "Date/Time: " + feedRead.split("<pubDate>")[1].split("</pubDate>")[0]);
                        writeline(responsePrefix + "Link: " + feedRead.split("<link>")[1].split("</link>")[0]);




                        System.out.println(feedRead);

                    }
                    catch (Exception e)
                    {
                        writeline(responsePrefix + "An error occurred while performing this command.");
                        writeline("PRIVMSG #Danroth :" + e.getMessage());

                        for(StackTraceElement line : e.getStackTrace())
                        {
                            writeline("PRIVMSG #Danroth :" + line.toString());
                        }
                    }
                }
                else if(command.equalsIgnoreCase("rules"))
                {
                    writeline(noChannelPrefix + "\u0002==IRC Rules==");
                    writeline(noChannelPrefix + "A full list of IRC Rules can be found at http://wiki.bukkit.org/IRC");
                    writeline(noChannelPrefix + "Use the \"rule <number>\" command for details on each rule.");
                }

                else if(command.equalsIgnoreCase("rule"))
                {
                    int rulenum = -1;

                    try{               //Tries to get the rule number, surrounded by a try/catch just in case they leave an empty rule number, or other non-integer.
                        rulenum = Integer.parseInt(read.split(" ")[4]);
                        //int rulenum = (int)rulenum;
                    }
                    catch (Exception e){}
                    switch(rulenum)
                    {
                        case 1:
                            writeline(responsePrefix + "==Rule " + rulenum + ": ALWAYS READ THE TOPIC==");
                            writeline(responsePrefix + "It's the first thing you see when you connect to or join the channel and it is usually at the top of the screen at all times. Read it when you connect, when you join, when you come back from being away. Always.");
                            break;
                        case 2:
                            writeline(responsePrefix + "==Rule " + rulenum + ": We are volunteers!==");
                            writeline(responsePrefix + "The people helping out in Bukkit's IRC channels are doing so voluntarily and not being compensated in any way. We do not owe you anything, nor are we obligated to respond to you. Please show common courtesy and show your appreciation for everyone giving you their time.");
                            break;
                        case 3:
                            writeline(responsePrefix + "==Rule " + rulenum + ": This is not designed as a support channel!==");
                            writeline(responsePrefix + "Remember, #bukkit is not designed to be a support channel - people just help out when they can. That being said, you're likely to get help faster if you follow our Support Guidelines ( http://bit.ly/qcxKJt )");
                            break;
                        case 4:
                            writeline(responsePrefix + "==Rule " + rulenum + ": Ignorance isn't a valid defense.==");
                            break;
                        case 5:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No excessive use of profanity==");
                            writeline(responsePrefix + "While we have no objection to sharing your opinions and debating a topic - in fact, we encourage it - with other members of this community, we do not tolerate any excessive profanity whatsoever.");
                            writeline(responsePrefix + "A minor or infrequent usage of profanity is permitted. If you are found to be using profanity frequently, your posts will be deemed of a flaming nature and you will be dealt with accordingly.");
                            writeline(responsePrefix + "Since the measure of excessive usage is ultimately decided by channel staff on a case by case basis, you will receive ample warning prior to being banned due to the infringement of this clause.");
                            break;
                        case 6:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No racism, discrimination, threats, harrasment or personal attacks of any kind are permitted.==");
                            writeline(responsePrefix + "This is a friendly community and as such you are expected to be courteous and follow general chatting etiquette. There are times when you may not agree with someone, or you've had a bad day and that's fine, we all have those days. But please, for your own benefit, leave your problems off Bukkit.org and our of the channel. Doing so will ensure that the Bukkit Community is a welcoming, friendly one.");
                            break;
                        case 7:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No vulgarity or obscenity.==");
                            break;
                        case 8:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No spamming is permitted, whatsoever.==");
                            writeline(responsePrefix + "Spam is posting any content that is not within the topic of discussion, or does not contribute anything to the channel - off topic OR on topic. The definition of spam used by this community is determined by the channel staff and is not open to discussion.");
                            writeline(responsePrefix + "Spam also constitutes, but is not limited to, messages that are formatted incorrectly (has excessive colour, bold etc.) or is in all caps, mixed caps or any other formatting deemed intrusive or annoying by the channel staff.");
                            break;
                        case 9:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No flaming, inciting hatred or instigating flame bait is permitted.==");
                            writeline(responsePrefix + "This is a friendly community and as such you are expected to be courteous and follow general chatting etiquette.");
                            writeline(responsePrefix + "In general, any parties involved in a flame-fest - whether they be the instigator or the flamer, will be dealt with accordingly. The best way to stay out of trouble, with regards to flame-fests, is to not join in.");
                            break;
                        case 10:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No selling of products or services, unless approved by a member of channel staff.==");
                            break;
                        case 11:
                            writeline(responsePrefix + "==Rule " + rulenum + ": Do not ask for a position on staff.==");
                            writeline(responsePrefix + "For those of you interested in being a part of the Bukkit Community staff, here's some advice: Since launching we've received requests from people to be a part of our staff - be it IRC, wiki, community, etc. but what is important to note is that the vast majority of those requests have been accompanied by a lack of initiative being shown.");
                            writeline(responsePrefix + "Show some initiative and your work will be noticed, along with your attitude and your passion for the community and the chances of us picking you up to help out will be higher. This should really have gone without saying. ");
                            writeline(responsePrefix + "EvilSeph's policy generally is: if you ask for a position or privileges, you reduce the chances of your getting it. You shouldn't have to ask; you demonstrate that you fit the bill and things should fall into line.");
                            break;
                        case 12:
                            writeline(responsePrefix + "==Rule " + rulenum + ": No advertising.==");
                            writeline(responsePrefix + "Advertising of any form is completely prohibited unless otherwise stated by channel staff.");
                            break;
                        case 13:
                            writeline(responsePrefix + "==Rule " + rulenum + ": Disrespect and intolerance towards other people is NOT acceptable==");
                            break;
                        case 14:
                            writeline(responsePrefix + "==Rule " + rulenum + ": BE PATIENT and no excessive repeating==");
                            break;
                        case 15:
                            writeline(responsePrefix + "==Rule " + rulenum + ": Pastebin logs, code snippets, anything longer than 3 lines!==");
                            writeline(responsePrefix + "Do NOT use Mibpaste, it's an eye-sore. Please use http://pastie.org, http://pastebin.com, http://slexy.org or http://gist.github.com instead.");
                            break;
                        case 16:
                            writeline(responsePrefix + "==Rule " + rulenum + ": This is an English only channel==");
                            writeline(responsePrefix + "Please try and speak English only in our channel as our staff do not understand any other languages, making it difficult to moderate the channel effectively. Please also refrain from bastardising the English language through the use of \"txt speak\" as it is very annoying and has no place on IRC.");
                            break;
                        default:
                            writeline(noChannelPrefix + "==Error==");
                            writeline(noChannelPrefix + "Please make sure you typed a valid number between 1 and 16. ");
                            break;
                    }
                    try{
                        Thread.sleep(100);
                    } catch (Exception e) {}

                }
                else if(command.equalsIgnoreCase("version"))
                {
                        writeline(noChannelPrefix + "Running Danroth build #" + ((Map) buildinfo.get("build")).get("number").toString());
                }
                else if(command.equalsIgnoreCase("help"))
                {
                    writeline(noChannelPrefix + "\u0002==Danroth Commands==");
                    writeline(noChannelPrefix + "IRC Commands can be found at http://wiki.bukkit.org/IRC/Bots/Danroth");
                }
                else if(read.split(" ")[0].split("!")[1].equalsIgnoreCase("~trollface@effing.mibbits.sarcasticsupport.com"))
                {
                     if(command.equalsIgnoreCase("quote") || command.equalsIgnoreCase("raw"))
                     {
                        writeline(read.split(":", 3)[2].split(" ", 2)[1]);
                     }
                     else if(command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("shutdown"))
                     {
                         writeline("QUIT :Shut down by " + read.split("!")[1].replaceAll(":", ""));
                     }
                }
            }
        }
    }
    private void writeline(String towrite)
    {
        writer.write(towrite + "\n");
        writer.flush();
    }
}
