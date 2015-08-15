package de.donxs.pinghandler;

import de.donxs.pinghandler.callback.Callback;
import de.donxs.pinghandler.netty.NettyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class PingChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final PingHandler handler;
    private final Callback<PingResponse> callback;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {

        if (NettyUtil.readVarInt(buffer) == 0x00) {

            String json = NettyUtil.readString(buffer);
            ctx.close();

            callback.done(this.handler.getGson().fromJson(json, PingResponse.class), null);

        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        ctx.close();
        callback.done(null, cause);

    }

}
