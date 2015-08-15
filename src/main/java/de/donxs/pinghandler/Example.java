package de.donxs.pinghandler;

import de.donxs.pinghandler.callback.Callback;
import java.net.InetSocketAddress;


public class Example {

    public static void main(String[] args) throws Exception {

        final PingHandler handler = new PingHandler();
        handler.fetch(new InetSocketAddress("kukielka.tv", 25565), new Callback<PingResponse>() {

            @Override
            public void done(PingResponse value, Throwable throwable) {

                if (value != null) {

                    System.out.println(value.getVersion().getName());
                    System.out.println(value.getDescription());

                }

                handler.shutdown();

            }

        });

    }

}
