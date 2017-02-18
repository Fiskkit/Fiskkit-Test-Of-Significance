package com.fiskkit.tos.darkbot.network.handlers;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fiskkit.tos.darkbot.models.TagParam;
import com.fiskkit.tos.darkbot.network.MYSQLAccess;
import com.fiskkit.tos.darkbot.util.TimeTracker;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by Fabled on 9/9/2014.
 */
@ChannelHandler.Sharable
public class SignificanceServerHandler extends SimpleChannelInboundHandler<String> {
    private TimeTracker timeTracker;
    boolean pFlag = false;

    public SignificanceServerHandler() {
    }

    public SignificanceServerHandler(TimeTracker timeTracker) {
        this.timeTracker = timeTracker;

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.write("Welcome to the Test of Significance Management Interface @ " + InetAddress.getLocalHost().getHostName() + "!\r\n");
        ctx.write("Server time is currently: " + new Date() + "\r\n\r\n");
        ctx.write(">");
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) {
        long timeElapsed;
        long currentTime = System.nanoTime();
        Map<Integer, TagParam> TagParamsList;
        TagParam tempTagParams;


        // Generate and write a response.
        String response;
        boolean close = false;

        if (request.trim().equalsIgnoreCase("change param") || pFlag) {
            if (pFlag) {

                String[] args = request.split("\\s+");
                int tagId = Integer.parseInt(args[0]);
                String parameter = args[1];
                double new_value = Double.parseDouble(args[2]);
                String test = String.format("Changes..." + "\r\n" + "[Tag Number: %d] [Parameter: %s] [New Value: %.2f] \n", tagId, parameter, new_value );
                MYSQLAccess.changeTag(tagId, parameter, new_value);
                ChannelFuture future = ctx.write(test);

                pFlag = false;
                return;
            } else {
                ChannelFuture future = ctx.write("Please specify your changes..." + "\r\n" + "[Tag Number] [Parameter] [New Value]: ");
                pFlag = true;
                return;
            }

        }

        switch (request.toLowerCase().trim()) {
            case "":
                response = "Please enter a command...\r\n";
                break;

            case "eta":
                timeElapsed = currentTime - timeTracker.getTime();
                timeElapsed = TimeUnit.SECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS);


                response = "Test of Significance time elapsed: " + timeElapsed + " /300" + "\r\n";
                break;

            case "view params":
                StringBuffer sb = new StringBuffer();
                TagParamsList = MYSQLAccess.getTagParameters();
                sb.append("[ToS Parameters]\r\n\r\n");
                for (Map.Entry<Integer, TagParam> entry : TagParamsList.entrySet()) {
                    System.out.println("Tag ID: " + entry.getKey().toString());

                    System.out.println("--MU: " + entry.getValue().mu);
                    System.out.println("--NU: " + entry.getValue().nu);
                    System.out.println("--P: " + entry.getValue().p);
                    System.out.println("--Z: " + entry.getValue().z);


                    sb.append("Tag ID: ").append(entry.getKey().toString()).append("\n");
                    sb.append("--MU: ").append(entry.getValue().mu).append("\n");
                    sb.append("--NU: ").append(entry.getValue().nu).append("\n");
                    sb.append("--P: ").append(entry.getValue().p).append("\n");
                    sb.append("--Z: ").append(entry.getValue().z).append("\n");
                }
                response = sb.toString();
                break;

            case "quit":

                response = "Have a good day!\r\n";
                close = true;
                break;

            default:
                response = "Nah, we can't to that. Try again.\r\n"; // do these if expr != any above

        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.write(response);

        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
