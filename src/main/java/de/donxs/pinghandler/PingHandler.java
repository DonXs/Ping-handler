package de.donxs.pinghandler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.donxs.pinghandler.callback.Callback;
import de.donxs.pinghandler.netty.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 *
 * @author Lukas
 */
@Getter
@RequiredArgsConstructor
public class PingHandler {

    private static final Class<? extends SocketChannel> SOCKET_CHANNEL = Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    private static final Class<? extends DatagramChannel> DATAGRAM_CHANNEL = Epoll.isAvailable() ? EpollDatagramChannel.class : NioDatagramChannel.class;
    private final EventLoopGroup eventLoop = Epoll.isAvailable() ? new EpollEventLoopGroup(1, new ThreadFactoryBuilder().setNameFormat("PingHandler-Thread-#").build()) : new NioEventLoopGroup(1, new ThreadFactoryBuilder().setNameFormat("PingHandler-Thread-#").build());
    private final Gson gson = new GsonBuilder().create();

    private Channel channel;

    public PingHandler fetch(final String address, final int port, final Callback<PingResponse> callback) {

        new Bootstrap()
                .channel(SOCKET_CHANNEL)
                .group(eventLoop)
                .handler(new ChannelInitializer() {

                    @Override
                    protected void initChannel(Channel channel) throws Exception {

                        PingHandler.this.channel = channel;

                        channel.config().setOption(ChannelOption.IP_TOS, 0x18);
                        channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);

                        channel.pipeline().addLast("timeout", new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS));
                        channel.pipeline().addLast("frame-decoder", new Varint21FrameDecoder());
                        channel.pipeline().addLast("frame-encoder", new Varint21FrameEncoder());
                        channel.pipeline().addLast("ping-handler", new PingChannelHandler(PingHandler.this, callback));

                    }

                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .connect(address, port)
                .addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture f) throws Exception {

                        ByteBuf handshake = channel.alloc().buffer();

                        NettyUtil.writeVarInt(0x00, handshake);
                        NettyUtil.writeVarInt(47, handshake);
                        NettyUtil.writeString(address, handshake);
                        handshake.writeShort(port);
                        NettyUtil.writeVarInt(1, handshake);

                        channel.writeAndFlush(handshake).sync();

                        ByteBuf legazyPing = channel.alloc().buffer();

                        NettyUtil.writeVarInt(0x00, legazyPing);

                        channel.writeAndFlush(legazyPing);

                    }

                });

        return this;

    }

    public void shutdown() {

        this.eventLoop.shutdown();

    }

}
