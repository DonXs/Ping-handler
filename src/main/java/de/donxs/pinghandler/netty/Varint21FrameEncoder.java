package de.donxs.pinghandler.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


@ChannelHandler.Sharable
public class Varint21FrameEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {

        int bodyLen = msg.readableBytes();
        int headerLen = 5;

        if ((bodyLen & 0xFFFFFF80) == 0) {
            headerLen = 1;
        }
        if ((bodyLen & 0xFFFFC000) == 0) {
            headerLen = 2;
        }
        if ((bodyLen & 0xFFE00000) == 0) {
            headerLen = 3;
        }
        if ((bodyLen & 0xF0000000) == 0) {
            headerLen = 4;
        }

        out.ensureWritable(headerLen + bodyLen);

        NettyUtil.writeVarInt(bodyLen, out);
        out.writeBytes(msg);
    }

}
